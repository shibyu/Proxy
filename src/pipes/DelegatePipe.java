package pipes;

import static base.LogManager.*;
import static base.Config.*;
import static base.Constant.*;

import formatters.*;
import processors.*;
import rules.RuleFactory;
import tasks.Task;
import util.Host;

import java.io.*;
import java.net.*;

import base.Config;
import base.Constant;
import base.SocketManager;
import contents.Content;
import contents.DataConverter;
import exceptions.ImplementationException;
import datagram.*;

// input を外部サービス (Fiddler を想定) に転送し、結果を別 Task から push してもらうことを想定したタスク;
// Queue を経由させようとすると、スレッドを新規に立てないといけないので、既にあるスレッド (Task) から実行する感じで;

public class DelegatePipe extends Pipe implements PushablePipe {
	
	private Socket proxyConnection;

	private Processor processor;
	private HttpRequestFormatter formatter;
	
	private BinaryFormatter binaryFormatter;
	
	private int type;
	private DataConverter converter;
	
	private Pipe subPipe;
	
	// TCP のときは owner となる Task がいる;
	// UDP のときはそれに相当する UdpListener がいる;
	private UdpListener listener;;
	
	// request を proxy に転送して、proxy が他の Task を実行する;
	// 他の Task から push される;
	public DelegatePipe(Task owner, InputStream input, OutputStream output, int bufferSize, String category, boolean isTCP, int type) {
		super(owner, input, output, bufferSize);
		this.type = type;
		Config config = Config.getConfig(isTCP);
		processor = new BinaryProcessor(bufferSize, RuleFactory.getRule(category, type, isTCP));
		formatter = new HttpRequestFormatter( new Host(config.getHost(), config.getSlavePort(category, type)) );
		formatter.setupSlaveOption(owner.getTaskId(), type);
		// TODO: データ変換は request と response で分けないといけない...;
		switch( type ) {
		case TYPE_REQUEST:
			converter = config.getRequestDataConverter(category);
			break;
		case TYPE_RESPONSE:
			converter = config.getResponseDataConverter(category);
			break;
		default:
			throw new ImplementationException("unknown type: " + type);
		}
		binaryFormatter = new BinaryFormatter(RuleFactory.getRule(category, type, true));
	}
	
	// request を proxy に転送するが UDP では connection がないので content を直接操作する;
	// なので厳密には Pipe ではないかも;
	public DelegatePipe(Host host, UdpListener listener, DataConverter converter) {
		// input が null なので Thread として走り始めてすぐに terminate するはず;
		// TODO: 実装があまりよろしくないので、proxy 接続部分を外に出す必要がありそう;
		// バッファサイズを拾ってくる適切な場所がなかったので、UDP の共通設定から拾っておく (ちょっと大きめな気がする);
		super(null, null, null, udpConfig.getBufferSize(CATEGORY_BASE));
		// type は変わらないが、taskId が変わる可能性があるので、ここでは設定しない;
		formatter = new HttpRequestFormatter(host);
		this.listener = listener;
		this.converter = converter;
	}
	
	// slave からリクエストを転送する;
	// slave が複数存在する可能性があるので、synchronized をつけておく (slave が unique であればいらないはず); 
	// TODO: ここで問題が起きても直接どうこうすることはできないので、間接的に Task に終了を促す必要がある;
	@Override
	synchronized public boolean push(Content content) {
		try {
			binaryFormatter.setContent(content);
			binaryFormatter.outputBytes(output);
			output.flush();
			return true;
		}
		catch( IOException e ) {
			debug(e);
			// 親がいれば終了を要請する (MasterTask が必ず親になるはず);
			if( owner != null ) {
				owner.terminate();
			}
			// TODO: 原因と影響を明確にするために、サーバに送ったデータの履歴を拾ってくることにする;
			return false;
		}
	}
	
	private void proxyConnect() throws IOException {
		if( tcpConfig.isKeepAlive(CATEGORY_PROXY) ) {
			// proxy が keep alive に対応している場合は、最初だけ接続する;
			if( proxyConnection == null ) {
				proxyConnection = SocketManager.tcpConnect( new Host(CATEGORY_PROXY) );
				// proxy (Fiddler) からの response (input) を吸い出さないと、バッファの関係で処理が止まってしまう;
				// response はいらないという設定ができるかは不明だが、取り敢えず捨ててしまって良いので、空の pipe に流し込んでおく;
				// TODO: この Pipe を閉じることができない...;
				subPipe = new VacantPipe(owner, proxyConnection.getInputStream(), bufferSize);
				subPipe.start();
			}
		}
		else {
			// proxy が keep alive に対応していないので、毎回接続する必要がある;
			proxyConnection = SocketManager.tcpConnect( new Host(CATEGORY_PROXY) );
		}
		if( proxyConnection == null ) {
			throw new ImplementationException("proxy connection is null");
		}
	}
	
	private void proxyDisconnect() throws IOException {
		// proxy が keep alive に対応している場合は、接続を閉じない;
		if( tcpConfig.isKeepAlive(CATEGORY_PROXY) ) { return; }
		// すぐに閉じてしまうので proxy からの response を吸い出さなくても良さそうだが、そうすると Fiddler には怒られる...;
		Pipe pipe = new InputDiscardPipe(owner, proxyConnection, new HttpProcessor(bufferSize), bufferSize);
		// 開始しておけば、request を 1個処理したところで勝手に終わってくれるはず;
		pipe.start();
		proxyConnection = null;
	}

	@Override
	public void transfer(byte[] buffer, int length) throws IOException {
		output("read " + Constant.getTypeString(type) + " " + length + " bytes @ " + owner, -1);
		processor.process(buffer, length);
		while( processor.hasMoreContent() ) {
			processContent( processor.pullContent(), false );
		}
	}
	
	// proxy に転送したか否かを返却することにする;
	synchronized public boolean processContent(Content content, String client, int type, boolean ignoreRUDP) throws IOException {
		this.type = type;
		//　元が TCP ではないので TaskId を指定せずに別処理に持っていくことにする;
		formatter.setupSlaveOption(-listener.getListenerId(), type);
		formatter.addHeader("OriginalClient", client);
		formatter.addHeader("UdpListener", Integer.toString(listener.getListenerId()));
		return processContent(content, ignoreRUDP);
	}
	
	private boolean processContent(Content content, boolean ignoreRUDP) throws IOException {
		output("original: " + util.Util.toHexString(content.getBytes()), LOG_RAW_DATA);
		if( content.isForceDirect() ) {
			// 特殊な用途で proxy を経由したくない場合には、直接送ってしまう;
			push(content);
			return false;
		}
		++contentId;
		if( converter != null ) {
			content = converter.convert(content);
			// RUDP 関連の制御パケットのみであれば、proxy には転送しない;
			if( ignoreRUDP && content.isRUDPOnly() ) {
				return false;
			}
		}
		formatter.setContent( content );
		formatter.addHeader( "ContentId", String.valueOf(contentId) );
		proxyConnect();
		OutputStream proxyOutput = proxyConnection.getOutputStream();
		formatter.outputBytes(proxyOutput);
		proxyOutput.flush();
		proxyDisconnect();
		output("push " + Constant.getTypeString(type) + "#" + contentId + " content to proxy @ " + owner, -1);
		return true;
	}
	
	@Override
	public void terminate() {
		// keep alive のときには VacantPipe がいるはずなので、そちらを先に閉じておく;
		if( subPipe != null ) { subPipe.terminate(); }
		super.terminate();
	}

}

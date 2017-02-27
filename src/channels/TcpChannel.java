package channels;

import static base.Config.*;
import static base.LogManager.*;

import base.*;
import tasks.Task;
import util.Host;
import util.Parser;
import util.Util;

import java.io.*;
import java.net.*;
import java.nio.channels.*;

abstract public class TcpChannel {
	
	// TODO: wrap したというのに内部オブジェクトを外から触らせてしまっているので、本当は継承にした方が良さそうな気がする;
	// とはいえインスタンス生成が static メソッドを呼び出す感じなので、継承にするとその辺りが良く分からなく...;
	protected ServerSocketChannel listenChannel;
	protected String category;

	protected TcpChannel(String category) {
		this.category = category;
	}
	
	public String getCategory() {
		return category;
	}
	
	protected Config getConfig() {
		return tcpConfig;
	}

	public int getPort() {
		return getConfig().getPort( getCategory() );
	} 
	
	public final boolean register(Selector selector) {
		if( getConfig().isEnable( getCategory() ) == false ) { return false; }
		try {
			listenChannel = ServerSocketChannel.open();
			listenChannel.configureBlocking(false);
			int port = getPort();
			listenChannel.socket().bind(new InetSocketAddress(port));
			output("listening tcp port: " + port, LOG_LISTEN);
			listenChannel.register(selector, SelectionKey.OP_ACCEPT);
			return true;
		}
		catch( IOException e ) {
			// ClosedChannelException もまとめて catch してしまう;
			// Channel が既に閉じている場合らしいが、普通の IOException と原因の比較もできないわけで...;
			trace(e);
			return false;
		}
	}
	
	abstract public Task createTask(SocketChannel clientConnection);
	
	private boolean isAcceptableRemoteHost(String host) {
		// trace(host);
		Host target = Parser.parseHost(host);
		for( Object deny : tcpConfig.getDenyHosts() ) {
			if( Util.hostMatches(target.getHost(), deny.toString()) ) { return false; }
		}
		return true;
	}
	
	public void accept() {

		try {
			// この Socket は使用が終わったところで閉じる必要がある;
			// Task は別スレッドになっているので、そちらの終了時に必ず閉じるようにする;
			SocketChannel clientConnection = listenChannel.accept();
			String remote = clientConnection.getRemoteAddress().toString();
			// なぜか / で始まるらしいので削っておく...;
			if( remote.startsWith("/") ) { remote = remote.substring("/".length()); }
			if( isAcceptableRemoteHost(remote) == false ) {
				output("connection refused: " + remote, LOG_CONNECT);
				// 担当するタスクがいないので、ここで閉じる;
				// close するためのダミータスクを作っても良いが、さすがに健全な実装ではない気がするのでこのままにしておく;
				SocketManager.close(clientConnection);
				return;
			}
			output("connected: " + clientConnection, LOG_CONNECT);
			Task task = createTask(clientConnection);
			if( TaskPool.getInstance().addTask(task) == false ) {
				// TODO: 503 service temporary unavailable;
				error("Service Temporary Unavailable: too many connections");
				// 開始できなかったので、タスクを終了させる;
				task.finish();
				return;
			}
			task.start();
		}
		catch( IOException e ) {
			trace(e);
		}
			
	}
	
}

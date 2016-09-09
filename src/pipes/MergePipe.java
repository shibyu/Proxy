package pipes;

import static base.LogManager.*;

import java.io.*;

import base.ListenerPool;
import base.TaskPool;
import channels.SlaveChannel;
import contents.Content;
import contents.DataConverter;
import exceptions.ImplementationException;
import formatters.*;
import processors.*;
import rules.CustomRule;
import tasks.*;
import datagram.*;

public class MergePipe extends Pipe {

	private int type;

	private HttpProcessor processor;
	private Formatter formatter;
	private TaskPool pool;
	
	private DataConverter converter;
	
	public MergePipe(Task owner, InputStream input, OutputStream output, int bufferSize, int type, CustomRule rule, TaskPool pool, DataConverter converter) {
		super(owner, input, output, bufferSize);
		processor = new HttpProcessor(bufferSize);
		formatter = new BinaryFormatter(rule);
		this.type = type;
		this.pool = pool;
		this.converter = converter;
	}

	@Override
	public void transfer(byte[] buffer, int length) throws IOException {
		output("read " + length + " bytes @ " + owner, -1);
		processor.process(buffer, length);
		while( processor.hasMoreContent() ) {
			++contentId;
			Content content = processor.getContent();
			if( converter != null ) { content = converter.convert(content); }
			formatter.setContent( content );
			int masterTaskId = processor.getTaskId();
			boolean result;
			// UDP の場合は listener id が負数で入ってくる;
			if( masterTaskId < 0 ) {
				result = processListener(content);
			}
			else {
				result = processMaster(masterTaskId);
			}
			String message = result ? "successfully pushed this request" : "failed to push this request";
			HttpResponseFormatter response = new HttpResponseFormatter();
			response.setContent(message.getBytes());
			response.outputBytes(output);
			output.flush();
			// ここで output を close してしまうと input も close されておかしなことになる模様;
			// close しないと Fiddler が keep alive してくれるようなので、結果的には良さそうに見える;
			// output.close();
			output("back #" + contentId + " content to proxy @ " + owner, -1);
		}
	}
	
	private boolean processListener(Content content) throws IOException {
		UdpListener listener = ListenerPool.getInstance().getUdpListener(processor.getListenerId());
		if( listener instanceof HttpWrapListener ) {
			HttpWrapListener httpWrapper = (HttpWrapListener)(listener);
			switch( type ) {
			case SlaveChannel.TYPE_REQUEST_SLAVE:
				httpWrapper.pushRequest(processor.getClient(), content);
				break;
			case SlaveChannel.TYPE_RESPONSE_SLAVE:
				httpWrapper.pushResponse(processor.getClient(), content);
				break;
			default:
				throw new ImplementationException("unknown slave type: " + type);
			}
		}
		else {
			// なぜか全然関係ない UdpListener に返ってきた...;
			// プロキシで listener id を書き換えている可能性が濃厚だが、実装エラーの可能性もないとはいえない;
			return false;
		}
		return true;
	}
	
	private boolean processMaster(int masterTaskId) throws IOException {
		recordMaster(masterTaskId);
		MasterTask master = getMasterTask( masterTaskId );
		return push(master);
	}
	
	private void recordMaster(int masterTaskId) {
		// owner が SlaveTask でない状況でこの Pipe が使用されているのは問題のような気もするが...;
		if( (owner instanceof SlaveTask) == false ) { return; }
		((SlaveTask)(owner)).recordMaster(masterTaskId);
	}
	
	private boolean push(MasterTask master) {
		if( master == null ) { return false; }
		switch( type ) {
		case SlaveChannel.TYPE_REQUEST_SLAVE:
			return master.getRequestPipe().push(formatter);
		case SlaveChannel.TYPE_RESPONSE_SLAVE:
			return master.getResponsePipe().push(formatter);
		default:
			throw new ImplementationException("unknown slave type: " + type);
		}
	}
	
	private MasterTask getMasterTask(int taskId) {
		Task task = pool.getTask(taskId);
		if( task == null || (task instanceof MasterTask) == false ) { return null; }
		return (MasterTask)(task);
	}

}

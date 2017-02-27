package tasks;

import static base.LogManager.*;

import java.io.*;
import java.nio.channels.SocketChannel;

import pipes.*;

public class HttpTask extends SlaveAttachableTask {
	
	public HttpTask(SocketChannel clientConnection, String category) throws IOException {
		super(clientConnection, category);
	}
	
	@Override
	public void execute() {

		// 基本的には HTTP Request を受け取って、HTTP Response を返す作りにしたい;
		// WebSocket に対応するためには、upgrade された後は普通に非同期通信にしないといけない;
		// そうなると MasterTask と同じ仕事をする感じになりそう;

		// HTTP なので、まずは request を受け取って、接続先を決定させる必要がある;
		// 接続先に接続すれば、後はそれを適宜 Pipe に食わせてやれば良さそう;
		// TODO: keep alive で別の接続先に行くことはないと思うが、対応できていてもいいのかも知れない;
		
		try {
			// どこにリクエストを転送するのかはこの時点では不明なので、null を取り敢えず渡しておく;
			// 後から変更してしまって問題ないはずだが...;
			HttpRequestPipe requestPipe = new HttpRequestPipe(this, clientConnection.socket(), null, bufferSize);
			setRequestPipe(requestPipe);
			requestPipe.start();
			// タスクが終了すると接続が切られてしまうので、join する必要がある;
			requestPipe.join();
			// start 直後には response はないため、request を join した後に改めて join する;
			// ここで response がなければ知らない;
			joinAll();
		}
		catch( IOException e ) {
			trace(e);
		}
		catch( InterruptedException e ) {
			trace(e);
		}
		
	}
	
	public void registerResponsePipe(Pipe responsePipe) {
		setResponsePipe(responsePipe);
		responsePipe.start();
	}
	
	@Override
	public String getTaskName() {
		return "Http";
	}

}

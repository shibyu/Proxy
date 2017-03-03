package tasks;

import static base.LogManager.*;
import static base.Config.*;
import static base.Constant.*;

import base.SocketManager;
import pipes.*;
import util.Host;
import util.DebugInputStream;

import java.net.*;
import java.io.*;
import java.nio.channels.SocketChannel;

public class PortMapTask extends Task {

	private Host target;
	private boolean isRequestDebug;
	private boolean isResponseDebug;
	
	private Pipe requestPipe;
	private Pipe responsePipe;

	public PortMapTask(SocketChannel clientConnection, String category, int fromPort, Host target) throws IOException {
		super(clientConnection, category);
		this.target = target;
		isRequestDebug = tcpConfig.isRequestDebug(category, fromPort);
		isResponseDebug = tcpConfig.isResponseDebug(category, fromPort);
	}

	@Override
	public void execute() {

		// 中身を見るか、何も考えずに 2スレッドで動かすか;
		// HTTP の場合、中身を厳密に見るべきだが、本来の目的が HTTP Proxy を作ることではないので、簡易にしたい;
		// C in => S out に改変可能に流すのと S in => C out に改変可能に流すのを作るのが良さそう;
		// 解決: これは port の mapping を行うだけなので、中身は見ないで転送するだけ;

		Socket socket = null;

		try {
			
			// DONE: remote host への port mapping を実現;
			// とはいえ、HTTP みたいな Host 情報の乗っているプロトコルでは、挙動がおかしくなるっぽい;
			// クライアント側で map されていることを認識していないとダメっぽい;
			// それだとどうなるのやら...だが;
			// TODO: remote host の場合は別クラスにした方が良いのかも？;
			socket = SocketManager.tcpConnect(target);
			if( socket == null ) { return; }
			
			InputStream requestInput = clientInput;
			if( isRequestDebug ) {
				requestInput = new DebugInputStream(requestInput, TYPE_REQUEST);
			}
			requestPipe = new TruePipe(this, requestInput, socket.getOutputStream(), bufferSize);
			
			InputStream responseInput = socket.getInputStream();
			if( isResponseDebug ) {
				responseInput = new DebugInputStream(responseInput, TYPE_RESPONSE);
			}
			responsePipe = new TruePipe(this, responseInput, clientOutput, bufferSize);

			requestPipe.start();
			responsePipe.start();

			requestPipe.join();
			responsePipe.join();

		}

		catch (IOException e) {
			trace(e);
		}

		catch (InterruptedException e) {
			trace(e);
		}

		finally {
			// 自分のもらった Socket は基底クラスで閉じてもらえるので、自分で作ったものだけ閉じておく;
			SocketManager.close(socket);
		}

	}
	
	@Override
	public String getTaskName() {
		return "PortMap";
	}
	
	@Override
	public void terminate() {
		terminate(requestPipe);
		terminate(responsePipe);
	}

}

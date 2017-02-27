package tasks;

import static base.LogManager.*;

import base.SocketManager;
import pipes.*;
import util.Host;

import java.net.*;
import java.io.*;
import java.nio.channels.SocketChannel;

public class PortMapTask extends Task {

	private Host target;
	
	private Pipe requestPipe;
	private Pipe responsePipe;

	public PortMapTask(SocketChannel clientConnection, String category, Host target) throws IOException {
		super(clientConnection, category);
		this.target = target;
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
			
			requestPipe = new TruePipe(this, clientInput, socket.getOutputStream(), bufferSize);
			responsePipe = new TruePipe(this, socket.getInputStream(), clientOutput, bufferSize);

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

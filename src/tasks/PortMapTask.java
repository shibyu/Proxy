package tasks;

import static base.LogManager.*;

import base.SocketManager;
import pipes.*;

import java.net.*;
import java.io.*;
import java.nio.channels.SocketChannel;

public class PortMapTask extends Task {

	private int toPort;
	
	private Pipe requestPipe;
	private Pipe responsePipe;

	public PortMapTask(SocketChannel clientConnection, String category, int toPort) throws IOException {
		super(clientConnection, category);
		this.toPort = toPort;
	}

	@Override
	public void execute() {

		// 中身を見るか、何も考えずに 2スレッドで動かすか;
		// HTTP の場合、中身を厳密に見るべきだが、本来の目的が HTTP Proxy を作ることではないので、簡易にしたい;
		// C in => S out に改変可能に流すのと S in => C out に改変可能に流すのを作るのが良さそう;
		// 解決: これは port の mapping を行うだけなので、中身は見ないで転送するだけ;

		Socket socket = null;

		try {
			
			// TODO: remote host への port mapping を実現してもいいかも;
			socket = SocketManager.tcpConnect("127.0.0.1", toPort);
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

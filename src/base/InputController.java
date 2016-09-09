package base;

import static base.Config.*;
import static base.LogManager.trace;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

public class InputController {
	
	private InputStream input;
	private boolean isActive;
	
	public InputController(InputStream input) {
		this.input = input;
		isActive = input != null;
	}
	
	public int read(byte buffer[]) throws IOException {
		return read(buffer, 0, buffer.length);
	}
	
	synchronized public void disable() {
		isActive = false;
	}
	
	synchronized private boolean isActive() {
		return isActive;
	}
	
	public int read(byte buffer[], int offset, int length) throws IOException {
		while( isActive() ) {
			try {
				return input.read(buffer, offset, length);
			}
			catch( SocketTimeoutException e ) {
				try {
					// busy loop にしてしまうと、他の stream の読み書きに影響がある模様 (thread 数に依存？);
					// TODO: time out がセットされていないとまずいことになるので、それを検知できるようにしたい;
					Thread.sleep(tcpConfig.getTimeout());
				}
				catch( InterruptedException e2 ) {
					trace(e2);
					return -1;
				}
				continue;
			}
		}
		// 外的要因で停止した場合ではあるが、向こうから接続を切られたように見せたいので -1 を返しておく (負数なら何でも良いかも？);
		return -1;
	}
	
}

package base;

import static base.LogManager.*;
import static base.Config.*;

import java.io.IOException;

import clients.*;

// 設計方針メモ;
// port と Task を mapping して管理する感じにしたい;
// 一つのサービスで複数の port を listen する;
// port ごとに thread を立てることにした;

public class Proxy {

	public static void main(String args[]) {
		// ダミークライアントを起動するための仮実装;
		for( String arg : args ) {
			if( arg.equalsIgnoreCase("admin") ) {
				TcpClient text = new TextClient(CATEGORY_ADMIN);
				text.execute();
				return;
			}
			if( arg.equalsIgnoreCase("echo") ) {
				TcpClient text = new TextClient(CATEGORY_ECHO);
				text.execute();
				return;
			}
			if( arg.equalsIgnoreCase("http") ) {
				TcpClient http = new HttpClient(CATEGORY_HTTP);
				http.execute();
				return;
			}
			if( arg.equalsIgnoreCase("udp") ) {
				try {
					UdpClient udp = new UdpClient(CATEGORY_CLIENT);
					udp.execute();
				}
				catch( IOException e ) {
					trace(e);
				}
				return;
			}
			// その他の option の場合は binary として扱うことにする;
			// TODO: 暫定実装なので整理する必要がある;
			TcpClient binary = new BinaryClient(arg);
			binary.execute();
			return;
		}
		// TODO: 設定ファイルを指定する感じのコマンドラインオプションでも作る;
		Proxy proxy = new Proxy();
		proxy.init(null);
		proxy.start();
	}
	
	private void init(String configName) {
		ListenerPool.getInstance().setup();
		TaskPool.getInstance().setup();
	}
	
	private void start() {
		ListenerPool.getInstance().listen();
	}
	
}

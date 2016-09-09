package clients;

import static base.Config.*;
import static base.LogManager.*;

import java.io.*;
import java.net.*;

// client 側では DatagramChannel ではなく DatagramSocket を使うことにする;
// listen しないので bind は不要;
public class UdpClient extends Client {
	
	private SocketAddress host;

	public UdpClient(String category) throws IOException {
		int port = udpConfig.getPort(category); 
		host = new InetSocketAddress("127.0.0.1", port);
	}
	
	@Override
	public void execute() {
		
		try( BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)) ) {
			// socket 自体は使い回せそうな気がする;
			try( DatagramSocket socket = new DatagramSocket() ) {
				while( true ) {
					String line = reader.readLine();
					System.out.print("input> ");
					if( clientExits(line) ) { break; }
					if( line.isEmpty() || line.equalsIgnoreCase("exit") ) { break; }
					DatagramPacket packet = new DatagramPacket(line.getBytes(), 0, line.length(), host);
					socket.send(packet);
				}
			}
		}
		catch( IOException e ) {
			trace(e);
		}
		
	}
	
}

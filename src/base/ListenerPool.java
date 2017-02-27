package base;

import static base.Config.*;
import static base.LogManager.*;

import java.nio.channels.*;
import java.util.*;
import java.util.Map.Entry;

import channels.*;
import datagram.*;
import exceptions.ImplementationException;

import java.io.*;

import util.Parser;
import util.Host;

// tcp については connection の概念があるので、Channel を使って管理する;
// udp についても Channel を使用すること自体は可能だが、connection の概念がなく、それを実装するようにしないといけない;
// 結果的に Task クラスと同じレイヤーの作業が必要になりそうなので、Channel を使用するメリットがなくなりそう;
// 適切に sleep を入れながら (non-blocking になるように) 実装すれば問題ないはずなので、スレッドだらけになるけれども、そちらで進めてみる;

public class ListenerPool {

	private static final ListenerPool self = new ListenerPool();
	public static ListenerPool getInstance() { return self; }

	private ListenerPool() { }

	private Map<Integer, TcpChannel> tcpChannels;
	private Map<Integer, UdpListener> udpListeners;
	private int udpListenerCount;
	private Selector selector;

	// port ごとに Listener を作成して mapping する;
	public void setup() {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			trace(e);
			System.exit(0);
		}
		setupTcpChannels();
		setupUdpListeners();
	}
	
	private boolean tcpRegister(TcpChannel tcpChannel) {
		if( tcpChannel.register(selector) == false ) { return false; }
		tcpChannels.put(tcpChannel.getPort(), tcpChannel);
		return true;
	}

	// ここでは tcp と統一しているが、内部的には全然別物だし、以降の処理でもインタフェースが同じになることはなさそう;
	private boolean udpRegister(UdpListener udpListener) {
		if( udpListener.register() == false ) { return false; }
		int id = ++udpListenerCount;
		udpListeners.put(id, udpListener);
		udpListener.setListenerId(id);
		UdpListenerManager.getInstance().register(udpListener);
		udpListener.start();
		return true;
	}
	
	public UdpListener getUdpListener(int listenerId) {
		return udpListeners.get(listenerId);
	}
	
	private void setupTcpChannels() {
		tcpChannels = new HashMap<Integer, TcpChannel>();
		if( tcpRegister( AdminChannel.getInstance() ) == false ) {
			// 管理用 Channel が登録できないとかは異常なので、強制終了させてしまおう;
			fatal("failed to setup AdminListener");
			System.exit(0);
		}
		tcpRegister( EchoChannel.getInstance() );
		// WebSocket 用に slave が必要になる可能性があるので、ここで register しておく;
		if( tcpRegister( HttpChannel.getInstance() ) ) {
			setupRequestSlave(CATEGORY_HTTP, true);
			setupResponseSlave(CATEGORY_HTTP, true);
		}
		tcpRegister( HttpEchoChannel.getInstance() );
		setupTcpPortMap();
		// Master Slave Channel を config に従って量産する;
		setupMasterSlave();
		// CustomChannel を config に従って量産する;
		setupCustom();
	}
	
	private void setupUdpListeners() {
		udpListeners = new HashMap<Integer, UdpListener>();
		udpListenerCount = 0;
		setupUdpPortMap();
		setupHttpWrapper();
	}

	// port map といいつつトンネルにも使える...かも;
	private void setupTcpPortMap() {
		Map<?, ?> mapping = tcpConfig.getMapProperty(CATEGORY_PORTMAP, "portmap");
		for (Entry<?, ?> pair : mapping.entrySet()) {
			int fromPort = Parser.parseInt(pair.getKey().toString());
			Host target = Parser.parsePort(pair.getValue().toString());
			trace("map tcp port: " + fromPort + " => " + target);
			tcpRegister( new PortMapChannel(fromPort, target) );
		}
	}
	
	private void setupUdpPortMap() {
		Map<?, ?> mapping = udpConfig.getMapProperty(CATEGORY_PORTMAP, "portmap");
		for( Entry<?, ?> pair : mapping.entrySet() ) {
			int fromPort = Parser.parseInt(pair.getKey().toString());
			Host target = Parser.parsePort(pair.getValue().toString());
			trace("map udp port: " + fromPort + " => " + target);
			udpRegister( new PortMapListener(fromPort, target) );
		}
	}
	
	private void setupMasterSlave() {
		List<String> categories = tcpConfig.getMasterCategories();
		for( String category : categories ) {
			output("master listener: " + category, LOG_LISTEN);
			if( tcpRegister( new MasterChannel(category) ) ) {
				setupRequestSlave(category, true);
				setupResponseSlave(category, true);
			}
		}
	}
	
	private void setupRequestSlave(String category, boolean isTCP) {
		if( Config.getConfig(isTCP).isEnableRequestSlaveCategory(category) == false ) { return; }
		tcpRegister( SlaveChannel.createRequestSlaveChannel(category, isTCP) );
	}
	
	private void setupResponseSlave(String category, boolean isTCP) {
		if( Config.getConfig(isTCP).isEnableResponseSlaveCategory(category) == false ) { return; }
		tcpRegister( SlaveChannel.createResponseSlaveChannel(category, isTCP) );
	}

	// UDP を HTTP に wrap するというのは TCP でいうところの Master Slave と同じような実装になる;
	private void setupHttpWrapper() {
		List<String> categories = udpConfig.getMasterCategories();
		for( String category : categories ) {
			output("http wrapper: " + category, LOG_LISTEN);
			if( udpRegister( new HttpWrapListener(category) ) ) {
				// slave は HTTP を受信するので TCP になる;
				setupRequestSlave(category, false);
				setupResponseSlave(category, false);
			}
		}
	}
	
	private void setupCustom() {
		List<String> categories = tcpConfig.getCustomCategories();
		for (String category : categories) {
			trace("custom listener: " + category);
			tcpRegister( new CustomChannel(category) );
		}
	}

	public void listen() {

		try {

			output("start listening...", LOG_LISTEN);

			while (selector.select() > 0) {
				Iterator<SelectionKey> selected = selector.selectedKeys().iterator();
				while (selected.hasNext()) {
					SelectionKey key = selected.next();
					// 作業したので削除することにより、作業済みであることを示す (そうしないといつまでも作業してくれと言われて無限ループする);
					selected.remove();
					if (key.isAcceptable()) {
						SelectableChannel channel = key.channel();
						if( channel instanceof ServerSocketChannel ) {
							int port = ((ServerSocketChannel)(channel)).socket().getLocalPort();
							tcpChannels.get(port).accept();
						}
						else {
							// ここにはこないはずだが...;
							throw new ImplementationException("unknown acceptable channel type: " + channel);
						}
					}
					else {
						// こないはず...;
						throw new ImplementationException("unknown operatable selection key: " + key);
					}
				}
			}

		}

		catch (IOException e) {
			trace(e);
		}

	}

}

package rules;

import static base.LogManager.*;

import contents.IntermediateObject;
import exceptions.DataFormatException;
import exceptions.ImplementationException;
import util.DataIO;
import util.Parser;
import util.Util;

public class PhotonUdpRule extends CustomRule {

	// user idx (int16) # packets (int16) request id (int32) session id (int32);
	
	// each packets ... (repeats # packets);
	// unknown (int32) packet whole size (int32) ...;
	
	public static final String KEY_USER_ID = "UserId";
	private static final int POS_USER_ID = 0;
	
	// パケット数は明示的に記載する必要もなさそうなので、key は作らないでおく;
	private static final int POS_PACKET_COUNT = 2;
	
	public static final String KEY_PACKET_TOKEN = "PacketToken";
	private static final int POS_PACKET_TOKEN = 4;
	
	public static final String KEY_SESSION_ID = "SessionId";
	private static final int POS_SESSION_ID = 8;
	
	public static final String KEY_PACKETS = "Packets";
	private static final int POS_PACKETS = 12;
	
	public static final String KEY_PACKET_HEADER = "PacketHeader";
	private static final int POS_PACKET_HEADER = 0;
	private static final int PACKET_HEADER_SIZE = 4;
	
	// 外には出さないが、内部実装の都合上使用する...;
	private static final String KEY_PACKET_SIZE = "PacketSize";
	private static final int POS_PACKET_SIZE = 4;
	
	private static final String KEY_PACKET_INDEX = "PacketIndex";
	private static final int POS_PACKET_INDEX = 8;
	
	public static final String KEY_PACKET_PAYLOAD = "PacketPayload";
	private static final int POS_PACKET_PAYLOAD = 12;
	
	// 個別の定義 (変換に使用するというよりは debug 用途);
	private static final String KEY_PACKET_TYPE = "PacketType";
	private static final String PACKET_TYPE_ACK = "AckPacket";
	private static final String PACKET_TYPE_CONTROL = "ControlPacket";
	private static final String PACKET_TYPE_PHOTON = "PhotonPacket";
	private static final String PACKET_TYPE_PHOTON_PLUS = "PhotonPacketPlus";

	// ACK;
	private static final String KEY_ACK_PACKET_INDEX = "AckPacketIndex";
	private static final int POS_ACK_PACKET_INDEX = 12;
	
	private static final String KEY_ACK_PACKET_TOKEN = "AckPacketToken";
	private static final int POS_ACK_PACKET_TOKEN = 16;
	
	// PHOTON;
	private static final String KEY_PHOTON_PLUS = "PhotonAdditional";
	private static final int POS_PHOTON_PLUS = 12;
	
	PhotonUdpRule() {
		// 一応親クラスに渡してはいるが、これは現実的に work しているのだろうか？;
		super(12, 2, "int16", false, 0, 0, "");
	}
	
	public int getUserId(byte buffer[]) {
		return DataIO.readInt16(buffer, POS_USER_ID);
	}
	
	public void setUserId(byte buffer[], int userId) {
		DataIO.writeInt16(buffer, POS_USER_ID, userId);
	}
	
	public int getPacketToken(byte buffer[]) {
		return DataIO.readInt32(buffer, POS_PACKET_TOKEN);
	}
	
	public void setPacketToken(byte buffer[], int packetToken) {
		DataIO.writeInt32(buffer, POS_PACKET_TOKEN, packetToken);
	}
	
	public int getSessionId(byte buffer[]) {
		return DataIO.readInt32(buffer, POS_SESSION_ID);
	}
	
	public void setSessionId(byte buffer[], int sessionId) {
		DataIO.writeInt32(buffer, POS_SESSION_ID, sessionId);
	}
	
	private int getPacketCount(byte buffer[]) {
		return DataIO.readInt16(buffer, POS_PACKET_COUNT);
	}
	
	private void setPacketCount(byte buffer[], int packetCount) {
		DataIO.writeInt16(buffer, POS_PACKET_COUNT, packetCount);
	}
	
	public IntermediateObject[] getPackets(byte buffer[]) {
		int packetCount = getPacketCount(buffer);
		int offset = POS_PACKETS;
		IntermediateObject packets[] = new IntermediateObject[packetCount];
		for( int i = 0; i < packetCount; ++i ) {
			IntermediateObject packet = getPacket(buffer, offset);
			Object size = packet.remove(KEY_PACKET_SIZE);
			if( size == null || (size instanceof Integer) == false ) {
				throw new ImplementationException("packet size not found");
			}
			packets[i] = packet;
			offset += (Integer)(size);
		}
		return packets;
	}
	
	private IntermediateObject getPacket(byte buffer[], int offset) {
		IntermediateObject packet = new IntermediateObject();
		packet.put(KEY_PACKET_HEADER, Util.toHexString(buffer, offset + POS_PACKET_HEADER, PACKET_HEADER_SIZE));
		packet.put(KEY_PACKET_SIZE, DataIO.readInt32(buffer, offset + POS_PACKET_SIZE));
		packet.put(KEY_PACKET_INDEX, DataIO.readInt32(buffer, offset + POS_PACKET_INDEX));
		getCustomizedPacket(packet, buffer, offset);
		return packet;
	}

	// Packet を返却する実装でも良いけれども、immutable に作っていないので、副作用で処理しておく感じで;
	private void getCustomizedPacket(IntermediateObject packet, byte buffer[], int offset) {
		int size = packet.getInt(KEY_PACKET_SIZE);
		int payloadPosition = POS_PACKET_PAYLOAD;
		switch( buffer[offset] ) {
		case 0x01:
			// maybe ACK Packet;
			{
				packet.put(KEY_PACKET_TYPE, PACKET_TYPE_ACK);
				packet.put(KEY_ACK_PACKET_INDEX, DataIO.readInt32(buffer, offset + POS_ACK_PACKET_INDEX));
				packet.put(KEY_ACK_PACKET_TOKEN, DataIO.readInt32(buffer, offset + POS_ACK_PACKET_TOKEN));
				payloadPosition += 8;
			}
			break;
		case 0x06:
			// maybe Photon Packet;
			{
				packet.put(KEY_PACKET_TYPE, PACKET_TYPE_PHOTON);
				getPhotonCore(packet, buffer, offset + payloadPosition);
				packet.setRUDPOnly(false);
			}
			return;
		case 0x07:
			// maybe Photon Packet with preceding 1 byte;
			// memo: 連番になっているようにも見えたが、あるときとないときがあるので良く分からない;
			{
				packet.put(KEY_PACKET_TYPE, PACKET_TYPE_PHOTON_PLUS);
				packet.put(KEY_PHOTON_PLUS, DataIO.readInt32(buffer, offset + POS_PHOTON_PLUS));
				payloadPosition += 4;
				getPhotonCore(packet, buffer, offset + payloadPosition);
				packet.setRUDPOnly(false);
			}
			return;
			// 取り敢えずこの二つは存在を認識しているので message を出さないようにしておく;
		case 0x02: // SYN ?;
		case 0x03: // SYN ACK ?;
			break;
			// maybe Control Packet;
			// 0x04 と 0x0c は別のものかも知れないが...;
		case 0x04:
			// maybe connection close;
		case 0x0c:
		case 0x05:
			packet.put(KEY_PACKET_TYPE, PACKET_TYPE_CONTROL);
			break;
		default:
			error("to be implemented: " + buffer[offset]);
		}
		// 残りは payload として出力してしまう;
		packet.put(KEY_PACKET_PAYLOAD, Util.toHexString(buffer, offset + payloadPosition, size - payloadPosition));
		// Photon 関連の Packet は適切に処理して return してしまっているので、ここに到達するのは RUDP 関連の Packet のみと信じることにする;
		// TODO: Photon 系が紛れ込んでいないことを確認する...;
		packet.setRUDPOnly(true);
	}
	
	private void getPhotonCore(IntermediateObject packet, byte buffer[], int offset) {
		PhotonRule rule = RuleFactory.getPhotonRule();
		rule.getPhotonCore(packet, buffer, offset);
	}
	
	public int setPackets(byte buffer[], Object packets[]) {
		// 先に packet 数を更新しておく;
		setPacketCount(buffer, packets.length);
		int offset = POS_PACKETS;
		for( Object packet : packets ) {
			if( (packet instanceof IntermediateObject) == false ) {
				throw new ImplementationException("unknown data type: " + packet);
			}
			offset += setPacket(buffer, offset, (IntermediateObject)(packet));
		}
		return offset;
	}
	
	// 処理した packet のサイズを返却する;
	// 次にどこから buffer を使って良いかを通知する;
	private int setPacket(byte buffer[], int offset, IntermediateObject packet) {
		Util.writeByteArray(buffer, offset + POS_PACKET_HEADER, toBinary(packet.get(KEY_PACKET_HEADER).toString()), 0, PACKET_HEADER_SIZE);
		DataIO.writeInt32(buffer, offset + POS_PACKET_INDEX, packet.getInt(KEY_PACKET_INDEX));
		int length = setCustomizedPacket(buffer, offset, packet);
		DataIO.writeInt32(buffer, offset + POS_PACKET_SIZE, length);
		return length;
	}
	
	private byte[] toBinary(String input) {
		byte buffer[] = null;
		try {
			buffer = Parser.parseByteArray(input.length() / 2, input);
		}
		catch (DataFormatException e) {
			throw new ImplementationException("invalid payload: " + input);
		}
		return buffer;
	}
	
	// packet のサイズを返却する;
	private int setCustomizedPacket(byte buffer[], int offset, IntermediateObject packet) {
		int payloadPosition = POS_PACKET_PAYLOAD;
		switch( buffer[offset] ) {
		case 0x01:
			// ACK;
			DataIO.writeInt32(buffer, offset + POS_ACK_PACKET_INDEX, packet.getInt(KEY_ACK_PACKET_INDEX));
			DataIO.writeInt32(buffer, offset + POS_ACK_PACKET_TOKEN, packet.getInt(KEY_ACK_PACKET_TOKEN));
			payloadPosition += 8;
			break;
		case 0x06:
			// Photon;
			payloadPosition += setPhotonCore(packet, buffer, offset + payloadPosition);
			return payloadPosition;
		case 0x07:
			// Photon Plus;
			DataIO.writeInt32(buffer, offset + POS_PHOTON_PLUS, packet.getInt(KEY_PHOTON_PLUS));
			payloadPosition += 4;
			payloadPosition += setPhotonCore(packet, buffer, offset + payloadPosition);
			return payloadPosition;
		default:
			break;
		}
		String payload = packet.get(KEY_PACKET_PAYLOAD).toString();
		int length = payload.length() / 2;
		Util.writeByteArray(buffer, offset + payloadPosition, toBinary(payload), 0, length);
		return payloadPosition + length;
	}
	
	// Photon の core 部分の長さのみを返す;
	// 0 を返せば全体が hex として処理される;
	private int setPhotonCore(IntermediateObject packet, byte buffer[], int offset) {
		PhotonRule rule = RuleFactory.getPhotonRule();
		// データの出力が終わったところが返ってくるので、開始位置からの差分を返せば良さそう;
		return rule.setPhotonCore(packet, buffer, offset) - offset;
	}
	
}

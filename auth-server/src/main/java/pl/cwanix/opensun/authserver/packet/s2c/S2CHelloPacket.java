package pl.cwanix.opensun.authserver.packet.s2c;

import io.netty.channel.ChannelHandlerContext;
import pl.cwanix.opensun.authserver.server.session.AuthServerSession;
import pl.cwanix.opensun.commonserver.packets.ServerPacket;
import pl.cwanix.opensun.commonserver.server.SUNServerChannelHandler;
import pl.cwanix.opensun.utils.bytes.BytesUtils;
import pl.cwanix.opensun.utils.packets.FixedLengthField;
import pl.cwanix.opensun.utils.packets.PacketHeader;

public class S2CHelloPacket extends ServerPacket {

	private static final int INFO_MAX_LEN = 64;
	
	public static final PacketHeader PACKET_ID = new PacketHeader((byte) 0x33, (byte) 0x00);
	
	private FixedLengthField serverInfo;
	private FixedLengthField encKey;
	
	public S2CHelloPacket() {
		this.serverInfo = new FixedLengthField(INFO_MAX_LEN);
		this.encKey = new FixedLengthField(FixedLengthField.DWORD);
	}
	
	@Override
	public void process(ChannelHandlerContext ctx) {
		AuthServerSession session = (AuthServerSession) ctx.channel().attr(SUNServerChannelHandler.SESSION_ATTRIBUTE).get();
		encKey.setValue(session.getEncKey());
	}

	@Override
	public byte[] toByteArray() {
		return BytesUtils.mergeArrays(PACKET_ID.getValue(), serverInfo.getValue(), encKey.getValue());
	}
}

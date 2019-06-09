package pl.cwanix.opensun.authserver.packet.c2s;

import java.util.Arrays;

import org.springframework.web.client.RestTemplate;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import pl.cwanix.opensun.authserver.entities.UserEntity;
import pl.cwanix.opensun.authserver.packet.s2c.S2CAnsAuthPacket;
import pl.cwanix.opensun.authserver.properties.AuthServerProperties;
import pl.cwanix.opensun.authserver.server.AuthServerChannelHandler;
import pl.cwanix.opensun.authserver.server.session.AuthServerSession;
import pl.cwanix.opensun.commonserver.packets.ClientPacket;
import pl.cwanix.opensun.utils.encryption.TEA;
import pl.cwanix.opensun.utils.packets.FixedLengthField;
import pl.cwanix.opensun.utils.packets.PacketHeader;

@Getter
public class C2SAskAuthPacket extends ClientPacket {
	
	public static final PacketHeader PACKET_ID = new PacketHeader((byte) 0x33, (byte) 0x03);
	
	private FixedLengthField unknown1;
	private FixedLengthField name;
	private FixedLengthField unknown2;
	private FixedLengthField password;
	private FixedLengthField unknown3;
	
	public C2SAskAuthPacket(byte[] value) {
		this.unknown1 = new FixedLengthField(4, Arrays.copyOfRange(value, 0, 4));
		this.name = new FixedLengthField(50, Arrays.copyOfRange(value, 4, 54));
		this.unknown2 = new FixedLengthField(FixedLengthField.BYTE, value[54]);
		this.password = new FixedLengthField(16, Arrays.copyOfRange(value, 55, 71));
		this.unknown3 = new FixedLengthField(8, Arrays.copyOfRange(value, 71, value.length));
	}
	
	public void process(ChannelHandlerContext ctx) {
		AuthServerSession session = ctx.channel().attr(AuthServerChannelHandler.SESSION_ATTRIBUTE).get();
		RestTemplate restTemplate = ctx.channel().attr(AuthServerChannelHandler.REST_TEMPLATE_ATTRIBUTE).get();
		AuthServerProperties properties = ctx.channel().attr(AuthServerChannelHandler.PROPERIES_ATTRIBUTE).get();
		
		String decodedPass = new String(TEA.passwordDecode(password.getValue(), session.getEncKey()));
		UserEntity userEntity = restTemplate.getForObject("http://" + properties.getDb().getIp() + ":" + properties.getDb().getPort() + "/user/findByName?name=" + name.toString(), UserEntity.class);
		S2CAnsAuthPacket ansAuthPacket = new S2CAnsAuthPacket();
		
		if (userEntity == null) {
			ansAuthPacket.setResult((byte) 1);
		} else if (!decodedPass.equals(userEntity.getPassword())) {
			ansAuthPacket.setResult((byte) 2);
		} else if (startAgentServerSession(restTemplate, properties, userEntity.getId()) > 0) {
			ansAuthPacket.setResult((byte) 3);
		} else {
			session.setUser(userEntity);
			ansAuthPacket.setResult((byte) 0);
		}
		
		ctx.writeAndFlush(ansAuthPacket);
	}
	
	private int startAgentServerSession(RestTemplate restTemplate, AuthServerProperties properties, long userId) {
		return restTemplate.postForObject("http://" + properties.getAgent().getIp() + ":" + properties.getAgent().getPort() + "/session/new?userId=" + userId, null, Integer.class);
	}
}

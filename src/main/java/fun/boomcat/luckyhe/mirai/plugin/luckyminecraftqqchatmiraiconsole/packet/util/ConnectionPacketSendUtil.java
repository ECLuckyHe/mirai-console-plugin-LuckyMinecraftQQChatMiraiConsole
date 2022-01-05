package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarInt;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo.Packet;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ConnectionPacketSendUtil {
    public static Packet getErrorPacket(String errorMessage, Charset charset) {
        VarInt packetId = new VarInt(0x01);
        VarIntString errorString = new VarIntString(errorMessage, charset);
        return new Packet(
                new VarInt(packetId.getBytesLength() + errorString.getBytesLength(charset)),
                packetId,
                ByteUtil.byteMergeAll(errorString.getBytes(charset))
        );
    }

    public static Packet getErrorPacket(String errorMessage) {
        return getErrorPacket(errorMessage, StandardCharsets.UTF_8);
    }

    public static Packet getCorrectResponsePacket(String sessionName, String address, Charset charset) {
        VarInt packetId = new VarInt(0x00);
        VarIntString sessionNameString = new VarIntString(sessionName, charset);
        VarIntString addressString = new VarIntString(address, charset);
        return new Packet(
                new VarInt(packetId.getBytesLength() + sessionNameString.getBytesLength(charset) + addressString.getBytesLength(charset)),
                packetId,
                ByteUtil.byteMergeAll(sessionNameString.getBytes(charset), addressString.getBytes(charset))
        );
    }

    public static Packet getCorrectResponsePacket(String sessionName, String address) {
        return getCorrectResponsePacket(sessionName, address, StandardCharsets.UTF_8);
    }
}

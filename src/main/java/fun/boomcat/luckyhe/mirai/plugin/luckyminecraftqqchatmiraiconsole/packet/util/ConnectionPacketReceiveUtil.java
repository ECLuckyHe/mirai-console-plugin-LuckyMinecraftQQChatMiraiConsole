package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarInt;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarLong;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.exception.PacketLengthNotMatchException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.exception.VarIntStringLengthNotMatchException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.exception.VarIntTooBigException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.exception.VarLongTooBigException;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo.MinecraftData;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ConnectionPacketReceiveUtil {
    public static Packet getPacket(InputStream inputStream) throws IOException, VarIntTooBigException, PacketLengthNotMatchException {
        VarInt packetLen = new VarInt(inputStream);
        VarInt packetId = new VarInt(inputStream);

        byte[] data = new byte[packetLen.getValue() - packetId.getBytesLength()];
        int readLen = inputStream.read(data);
        if (readLen != packetLen.getValue() - packetId.getBytesLength()) {
            throw new PacketLengthNotMatchException();
        }

        return new Packet(packetLen, packetId, data);
    }

    public static MinecraftData getMinecraftData(Packet packet, Charset charset) throws VarLongTooBigException, IOException, VarIntStringLengthNotMatchException, VarIntTooBigException, PacketLengthNotMatchException {
        VarInt length = packet.getLength();
        VarInt id = packet.getId();
        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getData().length);

        int index = 0;
        VarLong sessionId = new VarLong(Arrays.copyOfRange(data, index, data.length));
        index += sessionId.getBytesLength();
        VarIntString serverName = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += serverName.getBytesLength(charset);
        VarIntString joinFormatString = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += joinFormatString.getBytesLength(charset);
        VarIntString quitFormatString = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += quitFormatString.getBytesLength(charset);
        VarIntString msgFormatString = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += msgFormatString.getBytesLength(charset);
        VarIntString deathFormatString = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += deathFormatString.getBytesLength(charset);
        VarIntString kickFormatString = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += kickFormatString.getBytesLength(charset);

        if (index != data.length) {
            throw new PacketLengthNotMatchException();
        }

        return new MinecraftData(
                sessionId,
                serverName,
                joinFormatString,
                quitFormatString,
                msgFormatString,
                deathFormatString,
                kickFormatString
        );
    }

    public static MinecraftData getMinecraftData(Packet packet) throws VarLongTooBigException, IOException, VarIntStringLengthNotMatchException, VarIntTooBigException, PacketLengthNotMatchException {
        return getMinecraftData(packet, StandardCharsets.UTF_8);
    }

}

package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.util;

public class ByteUtil {
    public static byte[] byteMergeAll(byte[] ...values) {
        int length = 0;
        for (byte[] value : values) {
            length += value.length;
        }

        byte[] allBytes = new byte[length];
        int count = 0;
        for (byte[] value : values) {
            System.arraycopy(value, 0, allBytes, count, value.length);
            count += value.length;
        }

        return allBytes;
    }
}

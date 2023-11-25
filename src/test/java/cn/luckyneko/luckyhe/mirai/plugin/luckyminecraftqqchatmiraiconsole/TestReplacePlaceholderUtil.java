package cn.luckyneko.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.ReplacePlaceholderUtil;
import org.junit.Test;

/**
 * @Author Lucky_He
 * @Description
 * @Date 2023/11/25 23:27
 */

public class TestReplacePlaceholderUtil {
    @Test
    public void testReplace() {
        System.out.println(ReplacePlaceholderUtil.replacePlaceholderWithString(
                "%message%",
                "%message%",
                "\\\\"
        ));
    }
}

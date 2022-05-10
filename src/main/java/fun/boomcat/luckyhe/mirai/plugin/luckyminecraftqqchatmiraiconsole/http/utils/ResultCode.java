package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

public enum ResultCode {
    SUCCESS(0, "操作成功"),
    VERIFY_NEEDED(1, "需要验证"),
    VERIFY_NOT_PASSED(2, "验证失败"),
    ERROR(100, "其它错误");

    private final int code;
    private final String msg;
    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

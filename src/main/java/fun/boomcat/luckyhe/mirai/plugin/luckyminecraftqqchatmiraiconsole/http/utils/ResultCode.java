package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

public enum ResultCode {
    SUCCESS(0, "操作成功"),
    SESSION_NOT_EXISTED(1, "会话不存在"),
    SESSION_EXISTED(2, "会话已存在"),
    VERIFY_NOT_PASSED(403, "验证密码错误"),
    INNER_ERROR(500, "内部错误");

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

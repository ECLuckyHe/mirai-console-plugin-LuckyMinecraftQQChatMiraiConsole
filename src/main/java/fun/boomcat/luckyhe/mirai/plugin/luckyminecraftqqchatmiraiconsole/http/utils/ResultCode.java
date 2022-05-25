package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

public enum ResultCode {
    SUCCESS(0, "操作成功"),
    SESSION_NOT_EXISTED(1, "会话不存在"),
    SESSION_EXISTED(2, "会话已存在"),
    WRONG_REQUEST_DATA(401, "请求数据错误"),
    VERIFY_NOT_PASSED(403, "验证密码错误"),
    INNER_ERROR(500, "内部错误"),
    WRONG_REQUEST_FORMAT(400, "请求格式错误，应为 application/json 格式"),
    ;

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

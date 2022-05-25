package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

public enum ResultCode {
    SUCCESS(0, "操作成功"),
    SESSION_NOT_EXISTED(1, "会话不存在"),
    SESSION_EXISTED(2, "会话已存在"),
    SESSION_ID_NOT_PROVIDED(3, "未提供会话号"),
    SESSION_ID_TYPE_NOT_CORRECT(4, "会话号类型不正确，应为 long 类型"),
    VERIFY_PASSWORD_TYPE_NOT_CORRECT(5, "验证密码类型不正确，应为 String 类型"),
    VERIFY_PASSWORD_NOT_PROVIDED(6, "未提供验证密码"),
    PAGE_SIZE_TYPE_NOT_CORRECT(7, "pageSize 类型不正确，应为 Integer 类型"),
    PAGE_NO_TYPE_NOT_CORRECT(8, "pageNo 类型不正确，应为 Integer 类型"),
    PAGE_SIZE_NOT_PROVIDED(9, "未提供 pageSize"),
    PAGE_NO_NOT_PROVIDED(10, "未提供 pageNo"),
    PAGE_SIZE_VALUE_NOT_CORRECT(11, "pageSize 值不正确，取值范围应大于0"),
    PAGE_NO_VALUE_NOT_CORRECT(12, "pageNo 值不正确，取值范围应大于0"),
    VERIFY_NOT_PASSED(403, "验证密码错误"),
    INNER_ERROR(500, "内部错误"),
    WRONG_REQUEST_DATA(400, "请求格式错误，应为 application/json 格式"),
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

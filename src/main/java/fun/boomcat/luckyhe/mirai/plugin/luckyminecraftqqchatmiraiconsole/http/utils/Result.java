package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils;

public class Result<T> {
    private final int code;
    private final T data;
    private final String msg;

    public Result(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.data = data;
    }
}

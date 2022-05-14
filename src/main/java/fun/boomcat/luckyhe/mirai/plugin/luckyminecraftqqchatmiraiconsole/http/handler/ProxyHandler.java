package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.handler;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.HttpUtil;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.Result;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.http.utils.ResultCode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class ProxyHandler implements InvocationHandler {

    private final ResponseResult target;

    public ProxyHandler (ResponseResult target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method mapGet = Map.class.getMethod("get", Object.class);
        String verifyPassword = (String) mapGet.invoke(args[0], "verifyPassword");

        if (!HttpUtil.checkVerifyPassword(verifyPassword)) {
            return Result.error(ResultCode.VERIFY_NOT_PASSED);
        }

        return method.invoke(target, args);
    }
}

package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class ConfigOperation {
    private static File configPath;
    private static final String configFilename = "config.yml";
    private static final Yaml yaml = new Yaml();
    private volatile static Map<String, Object> configMap;

    public static void initConfigPath(File path, String configContent) throws IOException {
        configPath = path;
//
//        File[] files = configPath.listFiles();
//        boolean hasConfig = false;
//        for (File file : files) {
//            if (file.getName().equals(configFilename)) {
//                hasConfig = true;
//                break;
//            }
//        }

//        if (!hasConfig) {
        if (!new File(configPath.getPath() + "/" + configFilename).exists()) {
            copyConfigPathFromResource(configContent);
        }
    }

    private static void copyConfigPathFromResource(String configContent) throws IOException {
        FileOutputStream fos = new FileOutputStream(configPath.getPath() + "/" + configFilename);

        byte[] buf = configContent.getBytes(StandardCharsets.UTF_8);
        fos.write(buf);
        fos.close();
    }

    private static Map<String, Object> getConfigMap() throws IOException {
        if (configMap == null) {
            synchronized (ConfigOperation.class) {
                if (configMap == null) {
                    configMap = yaml.load(Files.newInputStream(Paths.get(configPath.getPath() + "/" + configFilename)));
                }
            }
        }

        return configMap;
    }

    public static int getPort() throws IOException {
        return ((int)getConfigMap().get("port"));
    }

    public static int getHeartbeat() throws IOException {
        return (int) getConfigMap().get("heartbeat");
    }

    public static String getIp() throws IOException {
        return (String) getConfigMap().get("ip");
    }

    public static Boolean getHttpManageEnabled() throws IOException {
        return (Boolean) ((Map<String, Object>) getConfigMap().get("httpManage")).get("enabled");
    }

    public static String getHttpManagePassword() throws IOException {
        return (String) ((Map<String, Object>) getConfigMap().get("httpManage")).get("password");
    }

    public static Integer getHttpManagePort() throws IOException {
        return (Integer) ((Map<String, Object>) getConfigMap().get("httpManage")).get("port");
    }
}

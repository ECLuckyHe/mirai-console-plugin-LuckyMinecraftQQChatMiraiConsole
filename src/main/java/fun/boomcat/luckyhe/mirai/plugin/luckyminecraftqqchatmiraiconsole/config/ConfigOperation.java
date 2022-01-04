package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.config;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ConfigOperation {
    private static File configPath;
    private static String configFilename = "config.yml";
    private static Yaml yaml = new Yaml();
    private static Map<String, Object> configMap;

    public static void initConfigPath(File path, String configContent) throws IOException {
        configPath = path;

        File[] files = configPath.listFiles();
        boolean hasConfig = false;
        for (File file : files) {
            if (file.getName().equals(configFilename)) {
                hasConfig = true;
                break;
            }
        }

        if (!hasConfig) {
            copyConfigPathFromResource(configContent);
        }
    }

    private static void copyConfigPathFromResource(String configContent) throws IOException {
        FileOutputStream fos = new FileOutputStream(configPath.getPath() + "/" + configFilename);

        byte[] buf = configContent.getBytes(StandardCharsets.UTF_8);
        fos.write(buf);
        fos.close();
    }

    private static Map<String, Object> getConfigMap() throws FileNotFoundException {
        if (configMap == null) {
            configMap = yaml.load(new FileInputStream(configPath.getPath() + "/" + configFilename));
        }

        return configMap;
    }

    public static short getPort() throws FileNotFoundException {
        return ((short) getConfigMap().get("port"));
    }
}

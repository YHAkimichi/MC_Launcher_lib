package be.Akimichi;

import java.util.HashMap;
import java.util.Map;

public class LibConfig {
    public static final String LibVersion = "1.1-SNAPSHOT";
    public static final String[] SupporteVersion = {
            "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21",
            "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20",
            "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19",
            "1.18.2", "1.18.1", "1.18",
            "1.17.1", "1.17",
            "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1", "1.16",
            "1.15.2", "1.15.1", "1.15",
            "1.14.4", "1.14.3", "1.14.2", "1.14.1", "1.14",
            "1.13.2", "1.13.1", "1.13",
            "1.12.2", "1.12.1", "1.12",
            "1.11.2", "1.11.1", "1.11",
            "1.10.2", "1.10.1", "1.10",
            "1.9.4", "1.9.3", "1.9.2", "1.9",
            "1.8.9","1.8.8",
            "1.7.10","1.7.9"
    };
    public static final String[] ForgeSupporteVersion = {
            "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1",
            "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19",
            "1.18.2", "1.18.1", "1.18",
            "1.17.1",
            "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1",
            "1.15.2", "1.15.1", "1.15",
            "1.14.4", "1.14.3", "1.14.2",
            "1.13.2",
            "1.12.2", "1.12.1", "1.12",
            "1.11.2", "1.11",
            "1.10.2", "1.10",
            "1.9.4", "1.9"
    };

    public static boolean Is_SupportVersion(String Version) {
        for (String s : SupporteVersion) {
            if (Version.equals(s)) {
                return true;
            }
        }
        return false;
    }
    public static boolean Is_ForgeSupporteVersion(String Version) {
        for (String s : ForgeSupporteVersion) {
            if (Version.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Object> GetConfigFileData(String LauncherName, String Version, String appFolder) {
        Map<String, Object> config = new HashMap<>();
        config.put("MinecraftVersion", Version);
        config.put("MinRam", "2");
        config.put("MaxRam", "2");
        config.put("LauncherName", LauncherName);
        config.put("CurrentVersion", "");
        config.put("LibVersion", LibConfig.LibVersion);
        config.put("pathToDirectory", appFolder + "\\"+ LauncherName);
        config.put("IsForge", "false");
        config.put("ForgeVersion", "");
        config.put("Debug", "true");
        config.put("JavaPath", "");
        return config;
    }
}

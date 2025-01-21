package be.Akimichi.Utils;

import be.Akimichi.Create_Init_Config_File;

import java.util.Objects;

public class LibrerieFonction {

    public static class Logger {
        public static class log {
            private static final String PREFIX = "[MC_Launcher_lib] ";
            private static final boolean LOGGING_ENABLED = Boolean.parseBoolean(Objects.requireNonNull(Create_Init_Config_File.readConfigFile()).get("Debug").toString());

            // Méthode de log
            public static void debug(String message) {
                if (LOGGING_ENABLED) {
                    System.out.println(PREFIX + "[DEBUG] " + message);
                }
            }

            public static void warn(String message) {
                System.err.println(PREFIX + "[WARN] " + message);
            }

            public static void info(String message) {
                System.out.println(PREFIX + "[INFO] " + message);
            }

            public static void error(String message) {
                System.err.println(PREFIX + "[ERROR] " + message);
                System.err.println(PREFIX + "[ERROR] " + "This might result to a crash.");
            }
        }
    }
}

package be.Akimichi.Utils;

import be.Akimichi.Create_Init_Config_File;

import java.util.Objects;

public class LibrerieFonction {

    public static class Logger {

        private static final String PREFIX = "[MC_Launcher_lib] ";
        private static final boolean LOGGING_ENABLED = Boolean.parseBoolean(Objects.requireNonNull(Create_Init_Config_File.readConfigFile()).get("Debug").toString());

        // Méthode de log
        public static void log(String message) {
            if (LOGGING_ENABLED) {
                System.out.println(PREFIX + message);
            }
        }
    }
}

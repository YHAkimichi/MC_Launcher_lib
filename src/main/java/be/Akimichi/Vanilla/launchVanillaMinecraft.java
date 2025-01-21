package be.Akimichi.Vanilla;

import be.Akimichi.Create_Init_Config_File;
import be.Akimichi.Utils.LibrerieFonction;


import java.io.*;
import java.util.*;

import static be.Akimichi.Utils.MCFonction.*;

public class launchVanillaMinecraft {
    private static String MINECRAFT_VERSION = "1.20.1"; // Défini dynamiquement
    private static String gameFolderName = "MinecraftLauncher"; // Nom par défaut
    private static final Map config = Create_Init_Config_File.readConfigFile();
    @SuppressWarnings("unused")
    public static void launchGame() {
        try {

                assert config != null;
                MINECRAFT_VERSION = config.get("MinecraftVersion").toString();
                gameFolderName = config.get("LauncherName").toString();


            // Lancer le jeu
            launchMinecraft("user", MINECRAFT_VERSION, getGameDirectory(), config.get("MinRam").toString(), config.get("MaxRam").toString());
        } catch (Exception e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void launchMinecraft(String username, String minecraftVersion, String gameDir, String RamMin, String RamMax) {
        try {
            String clientJarPath = new File(gameDir + "/client.jar").getAbsolutePath();
            // Collecter les bibliothèques récursivement
            List<String> classPath = new ArrayList<>();
            classPath.add(clientJarPath);
            File librariesDir = new File(gameDir + "/libraries");
            collectLibraries(librariesDir, classPath);

            // Obtenir le chemin des natives


            // Générer l'UUID déterministe
            String uuid = generateOfflineUUID(username);

            // Construire la commande de lancement
            List<String> command = new ArrayList<>();
            assert config != null;
            command.add(config.get("JavaPath").toString());
            command.add("-Xmx"+RamMax+"G");
            command.add("-Xms"+RamMin+"G");
            if (getVersionAsNumber(MINECRAFT_VERSION) < 1130) {
                String nativesDir = getNativesDirectory(librariesDir);
                command.add("-Djava.library.path=" + nativesDir);  // Ajouter java.library.path
            }
            command.add("-cp");
            command.add(String.join(File.pathSeparator, classPath));  // Windows : pathSeparator pour séparer les chemins
            command.add("net.minecraft.client.main.Main");
            getMinecraftArgs(minecraftVersion, gameDir, command);



            LibrerieFonction.Logger.log.debug("Commande : " + String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Exception e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
        }
    }







    public static void collectLibraries(File directory, List<String> classPath) {
        if (directory.isDirectory()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isDirectory()) {
                    collectLibraries(file, classPath);  // Appel récursif pour les sous-dossiers
                } else if (file.getName().endsWith(".jar")) {
                    classPath.add(file.getAbsolutePath());
                }
            }
        }
    }

    private static String generateOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes()).toString();
    }


    private static String getGameDirectory() {
        String appData = System.getenv("APPDATA"); // Obtenir le chemin AppData
        return appData + File.separator + gameFolderName;
    }
}

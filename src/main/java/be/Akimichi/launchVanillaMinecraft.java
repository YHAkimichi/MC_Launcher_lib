package be.Akimichi;

import java.io.*;
import java.util.*;

public class launchVanillaMinecraft {
    private static String MINECRAFT_VERSION = "1.20.1"; // Défini dynamiquement
    private static String gameFolderName = "MinecraftLauncher"; // Nom par défaut

    public static void launchGame() {
        try {

            Map<String, Object> config;


            if (Create_Init_Config_File.exist()){
                config = Create_Init_Config_File.readConfigFile();
                MINECRAFT_VERSION = config.get("MinecraftVersion").toString();
                gameFolderName = config.get("LauncherName").toString();
            }else {
                throw new RuntimeException("[MC_Launcher_lib] Library not initialized");
            }

            // Lancer le jeu
            launchMinecraft("user", MINECRAFT_VERSION, getGameDirectory(), config.get("MinRam").toString(), config.get("MaxRam").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            command.add("java");
            command.add("-Xmx"+RamMax+"G");
            command.add("-Xms"+RamMin+"G");
            if (getVersionAsNumber(MINECRAFT_VERSION) < 1130) {
                String nativesDir = getNativesDirectory(librariesDir);
                command.add("-Djava.library.path=" + nativesDir);  // Ajouter java.library.path
            }
            command.add("-cp");
            command.add(String.join(File.pathSeparator, classPath));  // Windows : pathSeparator pour séparer les chemins
            command.add("net.minecraft.client.main.Main");
            command.add("--username");
            command.add(username);
            command.add("--version");
            command.add(minecraftVersion);
            command.add("--gameDir");
            command.add(new File(gameDir).getAbsolutePath());
            command.add("--assetsDir");
            command.add(new File(gameDir + "/assets").getAbsolutePath());
            command.add("--assetIndex");
            command.add("index");
            command.add("--accessToken");
            command.add("fake_access_token");  // Token factice
            command.add("--uuid");
            command.add(uuid);  // Utiliser l'UUID généré
            command.add("--userType");
            command.add("mojang");
            command.add("--userProperties");
            command.add("{}");// Propriétés utilisateur vides


            System.out.println("[MC_Launcher_lib] Commande : " + String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getNativesDirectory(File librariesDir) throws IOException {
        File lwjglNativesJar = new File(librariesDir,
                "org\\lwjgl\\lwjgl\\lwjgl-platform\\2.9.2-nightly-20140822\\lwjgl-platform-2.9.2-nightly-20140822-natives-windows.jar");

        File lwjglNativesJar2 = new File(librariesDir,
                "org\\lwjgl\\lwjgl\\lwjgl-platform\\2.9.1\\lwjgl-platform-2.9.1-natives-windows.jar");

        File lwjglNativesJar3 = new File(librariesDir,
                "org\\lwjgl\\lwjgl\\lwjgl-platform\\2.9.0\\lwjgl-platform-2.9.0-natives-windows.jar");

        File lwjglNativesJar4 = new File(librariesDir,
                "org\\lwjgl\\lwjgl\\3.2.2\\lwjgl-3.2.2-natives-windows.jar");



        if (!lwjglNativesJar.exists() && !lwjglNativesJar2.exists() && !lwjglNativesJar3.exists() && !lwjglNativesJar4.exists()) {
            throw new RuntimeException("[MC_Launcher_lib] Fichier JAR des natives non trouvé : " + lwjglNativesJar.getAbsolutePath() + " " + lwjglNativesJar2.getAbsolutePath() + " " + lwjglNativesJar3.getAbsolutePath() + " " + lwjglNativesJar4.getAbsolutePath());
        }

        // Définir un répertoire dans AppData\Roaming\Launcher\Natives
        File nativesDir = new File(System.getenv("APPDATA") + "/Launcher/natives");
        nativesDir.mkdirs(); // Crée le dossier si nécessaire
        if (lwjglNativesJar.exists()) {
            extractJar(lwjglNativesJar, nativesDir);
        }else if (lwjglNativesJar2.exists()) {
            extractJar(lwjglNativesJar2, nativesDir);
        }else if (lwjglNativesJar3.exists()) {
            extractJar(lwjglNativesJar3, nativesDir);
        }else if (lwjglNativesJar4.exists()) {
            extractJar(lwjglNativesJar4, nativesDir);
        }
        return nativesDir.getAbsolutePath(); // Retourner le chemin des natives
    }


    private static void extractJar(File jarFile, File destination) throws IOException {
        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();

                File entryDestination = new File(destination, new File(entry.getName()).getName());
                if (!entry.isDirectory()) {
                    entryDestination.getParentFile().mkdirs();
                    try (InputStream in = jar.getInputStream(entry);
                         OutputStream out = new FileOutputStream(entryDestination)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }

            }
        }
    }


    private static void collectLibraries(File directory, List<String> classPath) {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
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

    private static int getVersionAsNumber(String version) {
        String[] parties = version.split("\\."); // Séparer par le point
        int majeur = Integer.parseInt(parties[0]); // Partie majeure
        int mineur = parties.length > 1 ? Integer.parseInt(parties[1]) : 0; // Partie mineure
        int correctif = parties.length > 2 ? Integer.parseInt(parties[2]) : 0; // Partie correctif

        // Normalisation pour obtenir des valeurs sur deux chiffres par niveau
        return majeur * 1000 + mineur * 10 + correctif;
    }

    private static String getGameDirectory() {
        String appData = System.getenv("APPDATA"); // Obtenir le chemin AppData
        return appData + File.separator + gameFolderName;
    }
}

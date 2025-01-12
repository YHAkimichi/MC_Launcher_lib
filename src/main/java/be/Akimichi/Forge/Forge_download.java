package be.Akimichi.Forge;

import be.Akimichi.Config;
import be.Akimichi.Create_Init_Config_File;
import be.Akimichi.Utils.Create_launcher_profil;
import be.Akimichi.Vanilla.Vanilla_download;
import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import static be.Akimichi.LibConfig.ForgeSupporteVersion;
import static be.Akimichi.LibConfig.Is_ForgeSupporteVersion;
import static be.Akimichi.Utils.MCFonction.getVersionAsNumber;
import static be.Akimichi.Utils.LibrerieFonction.Logger;

public class Forge_download {

    // Base URL pour les fichiers Maven
    private static final String BASE_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/";


    // URL du fichier JSON des versions Forge
    private static final String VERSION_MANIFEST_URL = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json";

    @SuppressWarnings("rawtypes")
    private static final Map config = Create_Init_Config_File.readConfigFile();

    // Répertoire de téléchargement par défaut
    private static final String DOWNLOAD_DIRECTORY;

    static {
        assert config != null;
        DOWNLOAD_DIRECTORY = config.get("pathToDirectory").toString() + "/forge_installer/";
    }

    public static void download() {


        String minecraftVersion = Objects.requireNonNull(Create_Init_Config_File.readConfigFile()).get("MinecraftVersion").toString();
        if (!Is_ForgeSupporteVersion(minecraftVersion)) {
            throw new RuntimeException("[MC_Launcher_lib] "+minecraftVersion+" is not a supported Minecraft version. \nSupports versions are \n" + Arrays.toString(ForgeSupporteVersion));
        }
        Vanilla_download.download();
        Create_launcher_profil.Create();




        assert config != null;
        String minecraftDir = config.get("pathToDirectory").toString();

        // Si le chemin est vide, utiliser un chemin par défaut

        String forgeVersion = getForgeVersion(minecraftVersion);

        if (forgeVersion == null) {
            System.err.println("[MC_Launcher_lib] Aucune version Forge trouvée pour Minecraft " + minecraftVersion);
            return;
        }


        List<String> urls = getForgeUrlsForVersion(forgeVersion, minecraftVersion);
        Logger.log("Téléchargement des fichiers : " + urls);
        createDownloadDirectory();
        downloadFiles(urls);

        Logger.log(forgeVersion);

        //if (Objects.equals(config.get("ForgeVersion").toString(), "") || !Objects.equals(config.get("ForgeVersion").toString(), forgeVersion)) {
            String installerPath;
            if (getVersionAsNumber(minecraftVersion) > 1095) {
                installerPath = DOWNLOAD_DIRECTORY + "forge-" + minecraftVersion + "-" + forgeVersion + "-installer.jar";
            }else {
                installerPath = DOWNLOAD_DIRECTORY + "forge-" + minecraftVersion + "-" + forgeVersion + "-" + minecraftVersion + "-installer.jar";
            }
            Config.setToForge(forgeVersion);
            Logger.log("path "+ installerPath);
            if (new File(installerPath).exists()) {
                Logger.log("Fichier Forge trouvé, lancement de l'installation...");
                if (getVersionAsNumber(minecraftVersion) < 1122) {
                  ForgeInstaller.Install();
                }else executeForgeInstallerSilently(installerPath, minecraftDir);
            } else {
                System.err.println("[MC_Launcher_lib] Le fichier d'installation Forge est introuvable. Assurez-vous qu'il a été correctement téléchargé.");
            }
        //}else {

        //}
    }

    private static String getForgeVersion(String minecraftVersion) {
        try {
            URL url = new URL(VERSION_MANIFEST_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.err.println("[MC_Launcher_lib] Impossible de récupérer la version Forge.");
                return null;
            }

            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(inputStream)) {

                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject promos = json.getAsJsonObject("promos");

                String recommendedKey = minecraftVersion + "-recommended";
                if (promos.has(recommendedKey)) {
                    return promos.get(recommendedKey).getAsString();
                } else {
                    Logger.log("Version 'recommended' non trouvée, essayant 'latest'.");
                    String latestKey = minecraftVersion + "-latest";
                    if (promos.has(latestKey)) {
                        return promos.get(latestKey).getAsString();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération des données: " + e.getMessage());
        }

        return null;
    }

    private static List<String> getForgeUrlsForVersion(String forgeVersion, String minecraftVersion) {
        String version = minecraftVersion + "-" + forgeVersion;
        String version1090 = minecraftVersion + "-" + forgeVersion + "-" + minecraftVersion;
        List<String> files;
        if (getVersionAsNumber(minecraftVersion) > 1095) {
            files = Collections.singletonList(
                    "forge-" + version + "-installer.jar"
            );
            return files.stream()
                    .map(file -> BASE_URL + version + "/" + file)
                    .toList();
        }else {
            files = Collections.singletonList(
                    "forge-" + version1090 + "-installer.jar"
            );
            return files.stream()
                    .map(file -> BASE_URL + version1090 + "/" + file)
                    .toList();
        }

    }
    private static void createDownloadDirectory() {
        Path path = Paths.get(DOWNLOAD_DIRECTORY);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                Logger.log("Répertoire créé: " + DOWNLOAD_DIRECTORY);
            } catch (IOException e) {
                System.err.println("[MC_Launcher_lib] Erreur lors de la création du répertoire: " + e.getMessage());
            }
        }
    }

    private static void downloadFiles(List<String> fileUrls) {
        for (String fileUrl : fileUrls) {
            try {
                downloadFile(fileUrl);
            } catch (IOException e) {
                System.err.println("Erreur lors du téléchargement de " + fileUrl + ": " + e.getMessage());
            }
        }
    }

    private static void downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = getFileNameFromUrl(fileUrl);
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(DOWNLOAD_DIRECTORY + fileName)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                Logger.log("Fichier téléchargé : " + fileName);
            }
        } else {
            System.err.println("[MC_Launcher_lib] Échec du téléchargement, code réponse: " + responseCode);
        }
        connection.disconnect();
    }



    private static void executeForgeInstallerSilently(String installerPath, String minecraftDir) {
        try {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(installerPath);
            command.add("--installClient");
            command.add(minecraftDir);
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Logger.log("Commande d'installation : " + String.join(" ", command));
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();

            if (process.exitValue() == 0) {
                Logger.log("Installation de Forge terminée en mode silencieux.");
            } else {
                System.err.println("[MC_Launcher_lib] Erreur lors de l'installation de Forge. Code d'erreur : " + process.exitValue());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("[MC_Launcher_lib] Erreur lors de l'exécution silencieuse de l'installateur Forge: " + e.getMessage());
        }
    }
    private static String getFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}

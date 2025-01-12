package be.Akimichi.Vanilla;

import be.Akimichi.Config;
import be.Akimichi.Create_Init_Config_File;
import be.Akimichi.Utils.DownloadJava;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import be.Akimichi.LibConfig;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Vanilla_download {


    private static long totalBytes = 0; // Taille totale des fichiers à télécharger
    private static final AtomicLong downloadedBytes = new AtomicLong(0); // Bytes téléchargés
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String BASE_ASSET_URL = "https://resources.download.minecraft.net/";
    private static String MINECRAFT_VERSION = "1.20.1"; // Défini dynamiquement
    private static String gameFolderName = "MinecraftDownloader"; // Nom par défaut
    private static final ExecutorService executor = Executors.newFixedThreadPool(5); // Thread pool pour téléchargements parallèles
    private static String SetupVersion;

    private static final String[] IGNORED_FILES = {
            "org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1.jar",
            "org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1.jar",
            "org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1.jar",
            "org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1.jar",
            "org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1.jar",
            "org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1.jar",
            "org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1.jar"
    };


    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(double percentage);
    }


    @SuppressWarnings("unused")
    public static boolean download(ProgressCallback progressCallback) {
        @SuppressWarnings("rawtypes") Map config;


            config = Create_Init_Config_File.readConfigFile();
            assert config != null;
            MINECRAFT_VERSION = config.get("version").toString();
            gameFolderName = config.get("LauncherName").toString();
            SetupVersion = config.get("CurrentVersion").toString();

        if (!is_official_version(MINECRAFT_VERSION)) {
            throw new RuntimeException("[MC_Launcher_lib] "+MINECRAFT_VERSION+" is not a Supported Minecraft version.");
        }
        try {


            double progress = (double) downloadedBytes.get() / totalBytes * 100;
            if (progressCallback != null) {
                progressCallback.onProgress(progress);
            }

            JsonObject manifest = getManifest();
            if (manifest == null) {
                System.out.println("[MC_Launcher_lib] Impossible de récupérer la liste des versions.");
                return false;
            }

            if (!Objects.equals(MINECRAFT_VERSION, SetupVersion)) {
                System.out.println("[MC_Launcher_lib] La version a changé. Suppression des anciens fichiers...");
                cleanMinecraftDirectory();
            } else {
                System.out.println("[MC_Launcher_lib] La version est déjà à jour. Aucun fichier ne sera supprimé.");
            }

            String versionUrl = getVersionUrl(manifest, MINECRAFT_VERSION);
            if (versionUrl == null) {
                System.out.println("[MC_Launcher_lib] URL introuvable pour la version sélectionnée.");
                return false;
            }

            // Télécharger les fichiers nécessaires
            downloadVersionFiles(versionUrl, progressCallback);
            System.out.println("[MC_Launcher_lib] Téléchargement terminé.");

            // Sauvegarder la version après un téléchargement réussi
            saveVersion();
            return true;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    @SuppressWarnings("ConstantValue")
    public static void download() {
        @SuppressWarnings("rawtypes") Map config;

            config = Create_Init_Config_File.readConfigFile();
            assert config != null;
            MINECRAFT_VERSION = config.get("MinecraftVersion").toString();
            gameFolderName = config.get("LauncherName").toString();
            SetupVersion = config.get("CurrentVersion").toString();

        if (!is_official_version(MINECRAFT_VERSION)) {
            throw new RuntimeException("[MC_Launcher_lib] "+MINECRAFT_VERSION+" is not a supported Minecraft version. \nSupports versions are \n" + Arrays.toString(LibConfig.SupporteVersion));
        }
        try {

            JsonObject manifest = getManifest();
            if (manifest == null) {
                System.out.println("[MC_Launcher_lib] Impossible de récupérer la liste des versions.");
                return;
            }

            if (!is_official_version(MINECRAFT_VERSION)) {
                return;
            }

            if (!Objects.equals(MINECRAFT_VERSION, SetupVersion)) {
                System.out.println("[MC_Launcher_lib] La version a changé. Suppression des anciens fichiers...");
                cleanMinecraftDirectory();
            } else {
                System.out.println("[MC_Launcher_lib] La version est déjà à jour. Aucun fichier ne sera supprimé.");
            }

            String versionUrl = getVersionUrl(manifest, MINECRAFT_VERSION);
            if (versionUrl == null) {
                System.out.println("[MC_Launcher_lib] URL introuvable pour la version sélectionnée.");
                return;
            }

            // Télécharger les fichiers nécessaires
            ProgressCallback progressCallback = null;
            downloadVersionFiles(versionUrl, progressCallback);
            downloadFileWithProgress(versionUrl ,config.get("pathToDirectory").toString() + "/"+ MINECRAFT_VERSION + ".json",progressCallback);
            System.out.println("[MC_Launcher_lib] Téléchargement terminé.");

            // Sauvegarder la version après un téléchargement réussi
            saveVersion();
            DownloadJava.Download();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private static JsonObject getManifest() throws IOException {
        String json = downloadText(VERSION_MANIFEST_URL);
        return JsonParser.parseString(json).getAsJsonObject();
    }


    private static boolean is_official_version(String version) {
        return LibConfig.Is_SupportVersion(version);
    }


    private static String getGameDirectory() {
        String appData = System.getenv("APPDATA"); // Obtenir le chemin AppData
        return appData + File.separator + gameFolderName;
    }


    private static void cleanMinecraftDirectory() {
        File minecraftDir = new File(getGameDirectory());
        if (minecraftDir.exists()) {
            deleteDirectory(minecraftDir);
            System.out.println("[MC_Launcher_lib] Dossier " + minecraftDir.getAbsolutePath() + " supprimé.");
        }
    }


    private static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (!file.getName().equals("LauncherLibConfig.json") ) {
                    deleteDirectory(file);
                }
            }
        }
         if (!directory.delete()){
             System.out.println("[MC_Launcher_lib] Impossible de supprimer le dossier.");
         }
    }


    private static String getVersionUrl(JsonObject manifest, String versionId) {
        JsonArray versions = manifest.getAsJsonArray("versions");

        for (JsonElement versionElement : versions) {
            JsonObject versionObj = versionElement.getAsJsonObject();
            if (versionObj.get("id").getAsString().equals(versionId)) {
                return versionObj.get("url").getAsString();
            }
        }
        return null;
    }


    private static void downloadVersionFiles(String versionUrl, ProgressCallback progressCallback) throws IOException {
        String json = downloadText(versionUrl);
        JsonObject versionData = JsonParser.parseString(json).getAsJsonObject();

        calculateTotalSizeFromMetadata(versionData);

        JsonObject client = versionData.getAsJsonObject("downloads").getAsJsonObject("client");
        downloadFileWithProgressAsync(client.get("url").getAsString(), getGameDirectory() + "/client.jar", progressCallback);

        JsonArray libraries = versionData.getAsJsonArray("libraries");
        for (JsonElement libElement : libraries) {
            JsonObject libObject = libElement.getAsJsonObject();
            JsonObject downloads = libObject.getAsJsonObject("downloads");

            if (downloads.has("artifact")) {
                JsonObject artifact = downloads.getAsJsonObject("artifact");
                String libPath = artifact.get("path").getAsString();

                if (shouldSkipFile(libPath)) {
                    downloadFileWithProgressAsync(artifact.get("url").getAsString(), getGameDirectory() + "/libraries/" + libPath, progressCallback);
                }
            }

            if (downloads.has("classifiers")) {
                JsonObject classifiers = downloads.getAsJsonObject("classifiers");
                JsonObject nativeArtifact = classifiers.getAsJsonObject(getOSClassifier());
                if (nativeArtifact != null) {
                    String libPath = nativeArtifact.get("path").getAsString();

                    if (shouldSkipFile(libPath)) {
                        downloadFileWithProgressAsync(nativeArtifact.get("url").getAsString(), getGameDirectory() + "/libraries/" + libPath, progressCallback);
                    }
                }
            }
        }

        JsonObject assetIndex = versionData.getAsJsonObject("assetIndex");
        downloadFileWithProgressAsync(assetIndex.get("url").getAsString(), getGameDirectory() + "/assets/indexes/index.json", progressCallback);

        downloadAssetsAsync(assetIndex.get("url").getAsString(), progressCallback);

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.HOURS)){
                System.out.println("[MC_Launcher_lib] Impossible de terminer l'executor");
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }


    private static boolean shouldSkipFile(String filePath) {
        for (String ignored : Vanilla_download.IGNORED_FILES) {
            if (filePath.contains(ignored)) {
                System.out.println("[MC_Launcher_lib] Ignoré : " + filePath);
                return false;
            }
        }
        return true;
    }


    private static void calculateTotalSizeFromMetadata(JsonObject versionData) throws IOException {
        totalBytes = 0; // Réinitialisation pour éviter les accumulations d'anciens appels

        JsonObject client = versionData.getAsJsonObject("downloads").getAsJsonObject("client");
        totalBytes += client.get("size").getAsLong();

        JsonArray libraries = versionData.getAsJsonArray("libraries");
        for (JsonElement libElement : libraries) {
            JsonObject libObject = libElement.getAsJsonObject();
            JsonObject downloads = libObject.getAsJsonObject("downloads");

            if (downloads.has("artifact")) {
                JsonObject artifact = downloads.getAsJsonObject("artifact");
                totalBytes += artifact.get("size").getAsLong();
            }

            if (downloads.has("classifiers")) {
                JsonObject classifiers = downloads.getAsJsonObject("classifiers");
                JsonObject nativeArtifact = classifiers.getAsJsonObject(getOSClassifier());
                if (nativeArtifact != null) {
                    totalBytes += nativeArtifact.get("size").getAsLong();
                }
            }
        }

        // Assets
        JsonObject assetIndex = versionData.getAsJsonObject("assetIndex");
        String assetIndexJson = downloadText(assetIndex.get("url").getAsString());
        JsonObject assets = JsonParser.parseString(assetIndexJson).getAsJsonObject().getAsJsonObject("objects");

        for (Map.Entry<String, JsonElement> assetElement : assets.entrySet()) {
            JsonObject assetObj = assetElement.getValue().getAsJsonObject();
            totalBytes += assetObj.get("size").getAsLong();
        }
    }


    private static void downloadFileWithProgressAsync(String urlString, String destination, ProgressCallback progressCallback) {
        executor.submit(() -> {
            try {
                downloadFileWithProgress(urlString, destination, progressCallback);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
    }


    private static void downloadFileWithProgress(String urlString, String destination, ProgressCallback progressCallback) throws IOException {
        File destFile = new File(destination);
        if (destFile.exists()) {
            downloadedBytes.addAndGet(destFile.length());
            System.out.println("[MC_Launcher_lib] Fichier déjà téléchargé : " + destination);
            return;
        }
        System.out.printf("[MC_Launcher_lib] Taille totale calculée : %d octets%n", totalBytes);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");

        //noinspection ResultOfMethodCallIgnored
        destFile.getParentFile().mkdirs();
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                downloadedBytes.addAndGet(bytesRead);
                double progress = (double) downloadedBytes.get() / totalBytes * 100;
                //System.out.println("progress = " + progress);

                if (progressCallback != null) {
                    progressCallback.onProgress(progress);  // Appeler le callback
                }
            }
        }
        System.out.println("[MC_Launcher_lib] Téléchargé : " + destination);
    }


    private static String getOSClassifier() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "natives-windows";
        } else if (os.contains("max")) {
            return "natives-osx";
        } else if (os.contains("linux")) {
            return "natives-linux";
        }
        throw new UnsupportedOperationException("Système d'exploitation non supporté : " + os);
    }


    private static void downloadAssetsAsync(String assetIndexUrl, ProgressCallback progressCallback) throws IOException {
        String assetIndexJson = downloadText(assetIndexUrl);
        JsonObject assets = JsonParser.parseString(assetIndexJson)
                .getAsJsonObject()
                .getAsJsonObject("objects");

        for (Map.Entry<String, JsonElement> entry : assets.entrySet()) {
            String hash = entry.getValue().getAsJsonObject().get("hash").getAsString();
            String subPath = hash.substring(0, 2) + "/" + hash;
            String destination = getGameDirectory() + "/assets/objects/" + subPath;

            downloadFileWithProgressAsync(BASE_ASSET_URL + subPath, destination, progressCallback);
        }
    }


    private static void saveVersion() {
        if (!Config.ChangeCurrentVersion(MINECRAFT_VERSION)) {
            throw new RuntimeException("[MC_Launcher_lib] Erreur lors de l'enregistrement de la version.");
        }
    }

    static String downloadText(String urlString) throws IOException {
        try (Scanner scanner = new Scanner(new URL(urlString).openStream(), StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").next();
        }
    }
}

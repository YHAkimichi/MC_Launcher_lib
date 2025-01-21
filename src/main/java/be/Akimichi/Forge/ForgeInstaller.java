package be.Akimichi.Forge;

import be.Akimichi.Create_Init_Config_File;
import be.Akimichi.Utils.LibrerieFonction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import static be.Akimichi.Utils.MCFonction.getVersionAsNumber;


public class ForgeInstaller {
    private static final String MINECRAFT_DIR = System.getenv("APPDATA") + "/Launcher/";
    private static final Map config = Create_Init_Config_File.readConfigFile();

    public static void Install() {

        assert config != null;
        String version = config.get("MinecraftVersion").toString()+"-"+config.get("ForgeVersion").toString();
        String MINECRAFTVERSION = config.get("MinecraftVersion").toString();
        String forgeVersion = config.get("ForgeVersion").toString();
        try {
            downloadAndInstallForge(version, MINECRAFTVERSION, forgeVersion);
            System.out.println("Forge " + version + " installed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downloadAndInstallForge(String version,String MCV, String FV) throws IOException, InterruptedException {
        Path installerPath ;
        String installerUrl = constructInstallerUrl(MCV,FV);

            installerPath = Paths.get(MINECRAFT_DIR +"forge_installer/"+ "forge-" + version + "-installer.jar");

        System.out.println(installerPath);


        // Téléchargement de l'installateur Forge
        downloadFile(installerUrl, installerPath, version);

        // Extraction des fichiers nécessaires
        executeForgeInstallerSilently(installerPath.toString());
        Path extractedDir = Paths.get(MINECRAFT_DIR +"forge_installer/" + "forge-" + version + "-extracted");
        Path path;
        if (getVersionAsNumber(MCV) > 1095){
            path = Path.of("forge-" + version + "-universal.jar");
        }else {
            path = Path.of("forge-" + MCV + "-" + FV + "-" + MCV + "-universal.jar");
        }
        extractJar(path, extractedDir);

        // Analyse du fichier version.json
        Path versionJsonPath = Paths.get(extractedDir + "/version.json");
        assert config != null;
        new File(MINECRAFT_DIR + "/versions/" + config.get("MinecraftVersion").toString() + "-forge-" + config.get("ForgeVersion").toString() + "/").mkdirs();
        Files.copy(versionJsonPath, Path.of(MINECRAFT_DIR + "/versions/" + config.get("MinecraftVersion").toString() + "-forge-" + config.get("ForgeVersion").toString() +"/" + config.get("MinecraftVersion").toString() + "-forge-" + config.get("ForgeVersion").toString() + ".json"), StandardCopyOption.REPLACE_EXISTING);

        List<Library> libraries = parseLibraries(versionJsonPath);

        // Téléchargement des bibliothèques
        for (Library library : libraries) {
            downloadLibrary(library);
        }

        // Installation du fichier universal.jar dans le répertoire mods
        //Path universalJar = Paths.get("forge-" + version + "-universal.jar");
        Path modsDir = Paths.get(MINECRAFT_DIR, "\\libraries\\net\\minecraftforge\\forge\\");
        Files.createDirectories(modsDir);

// Construire le nouveau nom de fichier
        Path universalJar;
        if (getVersionAsNumber(MCV) > 1095){
            universalJar = Paths.get("forge-" + version + "-universal.jar");
        }else {
            universalJar = Paths.get("forge-" + MCV + "-" + FV + "-" + MCV + "-universal.jar");
        }


        Path renamedFile;
        if (getVersionAsNumber(MCV) > 1095){
            renamedFile = modsDir.resolve(version + "\\" + "forge-" + version + ".jar");
        }else {
            renamedFile = modsDir.resolve(version + "-" + MCV + "\\" + "forge-" + MCV + "-" + FV + "-" + MCV + ".jar");
        }


        //Path renamedFile = modsDir.resolve("forge-" + version + ".jar");

// Copier et renommer le fichier
        Files.copy(universalJar, renamedFile, StandardCopyOption.REPLACE_EXISTING);

        LibrerieFonction.Logger.log.debug("Fichier copié et renommé en : " + renamedFile);
    }

    private static String constructInstallerUrl(String minecraftVersion, String forgeVersion) {
        String BASE_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/";
        String version = minecraftVersion + "-" + forgeVersion;
        String version1090 = minecraftVersion + "-" + forgeVersion + "-" + minecraftVersion;
        List<String> files;
        if (getVersionAsNumber(minecraftVersion) > 1095) {
            files = Collections.singletonList(
                    "forge-" + version + "-installer.jar"
            );
            return files.stream()
                    .map(file -> BASE_URL + version + "/" + file)
                    .toList().toString();
        }else {
            files = Collections.singletonList(
                    "forge-" + version1090 + "-installer.jar"
            );
            return files.stream()
                    .map(file -> BASE_URL + version1090 + "/" + file)
                    .toList().toString();
        }
        // Construction dynamique de l'URL de l'installateur Forge
    }

    private static void downloadFile(String urlString, Path targetPath,String version) throws IOException {
        // URL à ignorer
        String ignoredUrl = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + ".jar";
        //System.out.println("Downloading " + urlString + " to " + targetPath);
        // Vérifier si l'URL correspond à l'URL ignorée
        if (urlString.equals(ignoredUrl)) {
            LibrerieFonction.Logger.log.debug("Téléchargement ignoré pour l'URL : " + urlString);
            return;
        }

        File destFile = targetPath.toFile();

        // Si le fichier existe déjà et n'est pas vide, on considère qu'il est téléchargé
        if (destFile.exists() && destFile.length() > 0) {
            LibrerieFonction.Logger.log.debug("Fichier déjà téléchargé : " + targetPath.toString());
            return;
        }

        // Variables pour gérer la redirection
        String currentUrl = urlString.replace("[", "").replace("]", "");
        boolean redirect;
        int maxRedirects = 5; // Limite le nombre de redirections
        int redirectCount = 0;

        do {
            HttpURLConnection conn = (HttpURLConnection) new URL(currentUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false); // Désactive le suivi automatique pour le gérer manuellement

            int responseCode = conn.getResponseCode();
            redirect = (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP);

            if (redirect) {
                currentUrl = conn.getHeaderField("Location");
                LibrerieFonction.Logger.log.debug("Redirection vers : " + currentUrl);
                redirectCount++;

                if (redirectCount > maxRedirects) {
                    LibrerieFonction.Logger.log.error("Trop de redirections (limite de " + maxRedirects + ")");
                }
            } else if (responseCode != HttpURLConnection.HTTP_OK) {
                LibrerieFonction.Logger.log.error("Erreur HTTP : " + responseCode + " pour l'URL : " + currentUrl);
            } else {
                // Téléchargement du fichier après avoir suivi toutes les redirections
                destFile.getParentFile().mkdirs();
                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(destFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long downloadedSize = 0;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        downloadedSize += bytesRead;
                    }
                }
                LibrerieFonction.Logger.log.debug("Téléchargé avec succès : " + targetPath.toString());
            }
        } while (redirect);
    }

    private static void executeForgeInstallerSilently(String installerPath) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(installerPath);
        command.add("-e");
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        LibrerieFonction.Logger.log.debug("Commande d'extraction : " + String.join(" ", command));
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        process.waitFor();

        if (process.exitValue() == 0) {
            LibrerieFonction.Logger.log.info("Extraction de Forge terminée");
        } else {
            LibrerieFonction.Logger.log.error("Erreur lors de l'extraction de Forge. Code d'erreur : " + process.exitValue());
        }
    }

    private static void extractJar(Path jarPath, Path targetDir) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            jar.stream().forEach(entry -> {
                try (InputStream is = jar.getInputStream(entry)) {
                    Path outPath = targetDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(outPath);
                    } else {
                        Files.createDirectories(outPath.getParent());
                        Files.copy(is, outPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static List<Library> parseLibraries(Path versionJsonPath) throws IOException {
        List<Library> libraries = new ArrayList<>();
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(versionJsonPath)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray librariesArray = jsonObject.getAsJsonArray("libraries");
            for (JsonElement libElement : librariesArray) {
                JsonObject libObject = libElement.getAsJsonObject();
                String name = libObject.get("name").getAsString();
                String url = libObject.has("url") ? libObject.get("url").getAsString() : "https://libraries.minecraft.net/";
                libraries.add(new Library(name, url));
            }
        }
        return libraries;
    }

    private static void downloadLibrary(Library library) throws IOException {
        String[] parts = library.name.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid library name: " + library.name);
        }
        String group = parts[0].replace('.', '/');
        String artifact = parts[1];
        String version = parts[2];
        String jarName = artifact + "-" + version + ".jar";
        String path = group + "/" + artifact + "/" + version + "/" + jarName;
        String url = library.url + path;
        Path targetPath = Paths.get(MINECRAFT_DIR, "libraries", path);
        Files.createDirectories(targetPath.getParent());
        downloadFile(url, targetPath, version);
    }

    private static class Library {
        String name;
        String url;

        Library(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}

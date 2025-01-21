package be.Akimichi.Utils;

import be.Akimichi.Config;
import be.Akimichi.Create_Init_Config_File;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static be.Akimichi.Utils.LibrerieFonction.Logger;
import static be.Akimichi.Utils.MCFonction.getVersionAsNumber;

public class DownloadJava {

    public static void Download() {
        var config = Create_Init_Config_File.readConfigFile();
        assert config != null;

        String javaVersionUrl = getVersionJava(config.get("MinecraftVersion").toString());
        String downloadPath = config.get("pathToDirectory").toString();
        String fileZip = downloadPath + "\\java\\Java.zip";
        Path destDir = Paths.get(downloadPath, "java");

        if (!Objects.equals(config.get("JavaPath").toString(), "")) {
            return;
        }

        try {
            // Crée le répertoire pour le téléchargement si nécessaire
            if (!Files.exists(destDir)) {
                Logger.log.debug("Creating directory: " + destDir.toAbsolutePath());
                Files.createDirectories(destDir);
            }

            // Téléchargement du fichier
            Logger.log.info("Téléchargement de Java...");
            downloadFile(javaVersionUrl, fileZip);

            // Extraction du fichier ZIP
            extractZip(Paths.get(fileZip), destDir);

            // Supprime le fichier ZIP
            Files.deleteIfExists(Paths.get(fileZip));
            Logger.log.debug("Deleted the ZIP file: " + fileZip);

            // Configure le chemin Java
            String[] FolderIn = new File(downloadPath + "\\java\\").list();
            assert FolderIn != null;
            String JavaPath = FolderIn[0];
            Config.ChangeJavaPath(downloadPath + "\\java\\"+JavaPath+"\\bin\\java" );

        } catch (IOException e) {
            Logger.log.error("Erreur : " + e.getMessage());
        }
    }

    private static String getVersionJava(String version) {
        if (getVersionAsNumber(version) > 1165) {
            return "https://builds.openlogic.com/downloadJDK/openlogic-openjdk/22.0.2+9/openlogic-openjdk-22.0.2+9-windows-x64.zip";
        } else {
            return "https://builds.openlogic.com/downloadJDK/openlogic-openjdk-jre/8u432-b06/openlogic-openjdk-jre-8u432-b06-windows-x64.zip";
        }
    }

    private static void downloadFile(String urlString, String destination) throws IOException {
        Path destPath = Paths.get(destination);
        if (Files.exists(destPath)) {
            Logger.log.debug("[MC_Launcher_lib] Fichier déjà téléchargé : " + destination);
            return;
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");

        Files.createDirectories(destPath.getParent());

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(destination)) {

            byte[] buffer = new byte[16384]; // Tampon plus grand pour un meilleur débit
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        Logger.log.info("Téléchargement terminé : " + destination);
    }

    private static void extractZip(Path zipPath, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (OutputStream os = Files.newOutputStream(outPath)) {
                        byte[] buffer = new byte[16384]; // Tampon plus grand
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
        Logger.log.info("Extraction terminée : " + zipPath);
    }
}

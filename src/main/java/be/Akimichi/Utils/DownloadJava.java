package be.Akimichi.Utils;


import be.Akimichi.Config;
import be.Akimichi.Create_Init_Config_File;

import java.io.*;

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
        if (!Objects.equals(config.get("JavaPath").toString(), "")) {
            return;
        }

        try {
            File downloadDir = new File(downloadPath + "\\java");
            if (!downloadDir.exists()) {
                Logger.log("Creating directory: " + downloadDir.getAbsolutePath());
                boolean created = downloadDir.mkdirs();
                if (!created) {
                    throw new IOException("Failed to create directory: " + downloadDir.getAbsolutePath());
                }
            }
            Logger.log("Téléchargement de Java...");
            executeCommand("curl " + javaVersionUrl +" --output " + downloadPath + "\\java\\Java.jar");

            String fileZip = downloadPath + "\\java\\Java.jar";
            File destDir = new File(downloadPath + "\\java");

            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
            File Jar = new File( downloadPath + "\\java\\Java.jar");
            if (Jar.delete()) {
                Logger.log("Deleted the file: " + Jar.getName());
            } else {
                Logger.log("Failed to delete the file.");
            }

            File Dir = new File(downloadPath + "\\java");
            File[] files = Dir.listFiles();
            assert files != null;
            Config.ChangeJavaPath(files[0].getAbsolutePath() + "\\bin\\java");


        } catch (IOException | InterruptedException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
    private static String getVersionJava(String version) {
        if (getVersionAsNumber(version) > 1165){
            return "https://builds.openlogic.com/downloadJDK/openlogic-openjdk/22.0.2+9/openlogic-openjdk-22.0.2+9-windows-x64.zip";
        }else {
            return "https://builds.openlogic.com/downloadJDK/openlogic-openjdk-jre/8u432-b06/openlogic-openjdk-jre-8u432-b06-windows-x64.zip";
        }

    }

    private static void executeCommand(String command) throws IOException, InterruptedException {
        Logger.log("Exécution de la commande : " + command);
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();

        // Lecture des sorties (standard et erreurs)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                Logger.log(line);
            }
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }
        }
    }
}

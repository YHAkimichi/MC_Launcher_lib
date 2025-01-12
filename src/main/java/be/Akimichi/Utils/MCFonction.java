package be.Akimichi.Utils;

import be.Akimichi.Create_Init_Config_File;
import com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static be.Akimichi.Config.getNativeForVersion;

public class MCFonction {

    private static final Map config = Create_Init_Config_File.readConfigFile();

    public static int getVersionAsNumber(String version) {
        String[] parties = version.split("\\."); // Séparer par le point
        int majeur = Integer.parseInt(parties[0]); // Partie majeure
        int mineur = parties.length > 1 ? Integer.parseInt(parties[1]) : 0; // Partie mineure
        int correctif = parties.length > 2 ? Integer.parseInt(parties[2]) : 0; // Partie correctif

        // Normalisation pour obtenir des valeurs sur deux chiffres par niveau
        return majeur * 1000 + mineur * 10 + correctif;
    }
    public static String getNativesDirectory(File LauncherDir) throws IOException {

        File nativesDir = new File(LauncherDir + "/natives");
        List<String> List = getNativeForVersion();

        if (!nativesDir.mkdirs() && !nativesDir.exists()){
            System.out.println("[MC_Launcher_lib] Le fichier native n'a pas pu etre creer");
        }

        for (String element: List) {
            File pathToDir = new File(LauncherDir + "/libraries/" + element);
            if (pathToDir.exists()) {
                extractJar(pathToDir, nativesDir);
            }else {
                LibrerieFonction.Logger.log("Can't find file : " + pathToDir.getAbsolutePath());
            }
        }

        LibrerieFonction.Logger.log(nativesDir.getAbsolutePath());
        return nativesDir.getAbsolutePath();
    }


    public static void extractJar(File jarFile, File destination) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                File entryDestination = new File(destination, new File(entry.getName()).getName());
                if (!entry.isDirectory()) {
                    if (!entryDestination.getParentFile().mkdirs() && !entryDestination.getParentFile().exists()) {
                        System.err.println("[MC_Launcher_lib] Une erreur c'est produite lors de la création du fichier : " + entryDestination.getAbsolutePath());
                    }
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

    public static String generateOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes()).toString();
    }


    public static void getMinecraftArgs(String forgeVersion, String gameDir, List<String> command){
        String uuid = generateOfflineUUID("username");

        command.add("--username");
        command.add("username");


        command.add("--version");
        command.add(forgeVersion);


        command.add("--gameDir");
        command.add(new File(gameDir).getAbsolutePath());


        command.add("--assetsDir");
        command.add(new File(gameDir + "/assets").getAbsolutePath());


        command.add("--assetIndex");
        command.add("index");


        command.add("--uuid");
        command.add(uuid);


        command.add("--accessToken");
        command.add("fake");


        command.add("--userType");
        command.add("msa");


        command.add("--versionType");
        command.add("release");

    }
    public static List<String> getVanillaClassPath(){

        try {
            assert config != null;
            String jsonFilePath = config.get("pathToDirectory").toString() +"/"+ config.get("MinecraftVersion").toString()+".json";
            // Lire le contenu du fichier JSON
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            JsonObject versionJson = JsonParser.parseString(jsonContent).getAsJsonObject();

            // Liste des chemins à ajouter au classpath
            List<String> classpathEntries = new ArrayList<>();

            // Liste des fichiers LWJGL 3.2.1 à exclure
            Set<String> excludedPaths = Set.of(
                    "org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1.jar",
                    "org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1.jar",
                    "org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1.jar",
                    "org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1.jar",
                    "org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1.jar",
                    "org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1.jar",
                    "org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1.jar",
                    "ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0.jar"
            );

            // Récupérer les bibliothèques
            JsonArray libraries = versionJson.getAsJsonArray("libraries");
            for (JsonElement libraryElement : libraries) {
                JsonObject library = libraryElement.getAsJsonObject();
                JsonObject downloads = library.getAsJsonObject("downloads");

                if (downloads != null && downloads.has("artifact")) {
                    JsonObject artifact = downloads.getAsJsonObject("artifact");
                    String path = artifact.get("path").getAsString();

                    // Exclure les chemins spécifiques
                    if (excludedPaths.contains(path)) {
                        continue; // Ignorer cette bibliothèque
                    }
                        if (!classpathEntries.contains(path)) {
                            classpathEntries.add(path);
                        }
                }
            }

            // Afficher les chemins

            List<String> lib = new ArrayList<>(List.of());
            for (String path : classpathEntries) {
                lib.add(config.get("pathToDirectory").toString()+"/libraries/"+path);
                LibrerieFonction.Logger.log(path);
            }
            LibrerieFonction.Logger.log("VanillaClassPath : " + lib);
            return lib;
        } catch (IOException e) {
            LibrerieFonction.Logger.log("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
            return null;
        } catch (JsonSyntaxException e) {
            LibrerieFonction.Logger.log("Erreur de syntaxe JSON : " + e.getMessage());
            return null;
        }
    }

}

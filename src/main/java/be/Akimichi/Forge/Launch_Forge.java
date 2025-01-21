package be.Akimichi.Forge;

import be.Akimichi.Create_Init_Config_File;
import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static be.Akimichi.Utils.MCFonction.*;
import static be.Akimichi.Vanilla.launchVanillaMinecraft.collectLibraries;
import static be.Akimichi.Utils.LibrerieFonction.Logger;

public class Launch_Forge {

    private static String MINECRAFT_DIR = System.getenv("APPDATA") + "/Launcher"; // Répertoire du launcher
    private static String MINECRAFT_VERSION = "1.20.1"; // Défini dynamiquement
    private static String gameFolderName = "MinecraftLauncher"; // Nom par défaut
    @SuppressWarnings("rawtypes")
    private static final Map config = Create_Init_Config_File.readConfigFile();

    public static void LaunchGame() throws IOException {

        @SuppressWarnings("rawtypes") Map config;
        String FullforgeVersion;
        String forgeVersion;

        config = Create_Init_Config_File.readConfigFile();
        assert config != null;
        MINECRAFT_VERSION = config.get("MinecraftVersion").toString();
        gameFolderName = config.get("LauncherName").toString();
        MINECRAFT_DIR = config.get("pathToDirectory").toString();
        forgeVersion = config.get("ForgeVersion").toString();
        FullforgeVersion = MINECRAFT_VERSION + "-forge-" + forgeVersion;





        // Vérifier si le fichier JSON Forge existe

        Path versionJsonPath;
        if (getVersionAsNumber(MINECRAFT_VERSION) > 1095) {
            String versionDir = MINECRAFT_DIR + "/versions/" + FullforgeVersion;
            versionJsonPath = Path.of(versionDir + "/" + MINECRAFT_VERSION + "-forge-" + forgeVersion + ".json");
        }else {
            String versionDir = MINECRAFT_DIR + "/versions/" + MINECRAFT_VERSION + "-forge" + MINECRAFT_VERSION + "-" + forgeVersion +"-"+ MINECRAFT_VERSION;
            versionJsonPath = Path.of(versionDir + "/"+ MINECRAFT_VERSION +  "-forge" + MINECRAFT_VERSION + "-" + forgeVersion + "-" + MINECRAFT_VERSION + ".json");
        }
        //Path versionJsonPath = Paths.get(versionDir + "/" + FullforgeVersion + ".json");
        if (!Files.exists(versionJsonPath)) {
            Logger.log.debug("Le fichier de configuration de Forge est introuvable : " + versionJsonPath);
            return;
        }

        // Lire les informations nécessaires à partir du fichier JSON Forge
        JsonObject forgeConfig = readForgeConfig(versionJsonPath);
        if (forgeConfig == null) {
            Logger.log.debug("Erreur lors de la lecture du fichier Forge JSON.");
            return;
        }

        // Construire la commande de lancement
        List<String> command = buildLaunchCommand(forgeConfig, forgeVersion, config.get("MinRam").toString(), config.get("MaxRam").toString());

        // Lancer Minecraft avec Forge
        launchMinecraft(command);
    }

    private static JsonObject readForgeConfig(Path versionJsonPath) {
        try (FileReader reader = new FileReader(versionJsonPath.toFile())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            Logger.log.debug("Erreur lors de la lecture du fichier Forge JSON: " + e.getMessage());
            return null;
        }
    }

    private static List<String> buildLaunchCommand(JsonObject forgeConfig, String forgeVersion, String RamMin, String RamMax) throws IOException {
        String gameDir = getGameDirectory();
        String clientJarPath = new File(gameDir + "/client.jar").getAbsolutePath();
        List<String> classPath = new ArrayList<>();
        classPath.add(clientJarPath);
        File librariesDir = new File(gameDir + "/libraries");
        collectLibraries(librariesDir, classPath);
        List<String> command = new ArrayList<>();
        String libraryDirectory = MINECRAFT_DIR + "/libraries";
        String FullforgeVersion = MINECRAFT_VERSION + "-forge-" + forgeVersion;

        Path VersionJsonPath;
        if (getVersionAsNumber(MINECRAFT_VERSION) > 1095) {
            String versionDir = MINECRAFT_DIR + "/versions/" + FullforgeVersion;
            VersionJsonPath = Path.of(versionDir + "/forge-" + MINECRAFT_VERSION + "-" + forgeVersion + ".json");
        }else {
            String versionDir = MINECRAFT_DIR + "/versions/" + MINECRAFT_VERSION + "-forge" + MINECRAFT_VERSION + "-" + forgeVersion +"-"+ MINECRAFT_VERSION;
            VersionJsonPath = Path.of(versionDir + "/"+ MINECRAFT_VERSION +  "-forge" + MINECRAFT_VERSION + "-" + forgeVersion + "-" + MINECRAFT_VERSION + ".json");
        }

        assert config != null;
        command.add("\""+config.get("JavaPath").toString()+"\"");
        command.add("-Xmx"+RamMax+"G");
        command.add("-Xms"+RamMin+"G");
        command.add("-Djava.library.path=" + getNativesDirectory(new File(gameDir)));  // Ajouter java.library.path

        if (forgeConfig.get("arguments") != null) {
            JsonArray jvmArguments;
            if (forgeConfig.get("arguments").getAsJsonObject().get("jvm") != null) {
                jvmArguments = forgeConfig.get("arguments").getAsJsonObject().get("jvm").getAsJsonArray();
            }else {
                jvmArguments = null;
            }
            if (jvmArguments != null) {
                for (JsonElement arg : jvmArguments) {
                    command.add(arg.getAsString().replace("${library_directory}", libraryDirectory)
                            .replace("${version_name}", "client")
                            .replace("${classpath_separator}", File.pathSeparator));
                    Logger.log.debug("Argument JVM ajouté : " + arg.getAsString());
                }
            } else {
                Logger.log.debug("Aucun argument JVM supplémentaire dans le fichier JSON.");
            }
        }

        List<String> ListClassPath = addJarsToClasspath(forgeConfig, command, MINECRAFT_DIR, VersionJsonPath);
        List<String> VanillaClassPath = getVanillaClassPath();

        assert VanillaClassPath != null;

        for (String classPathElement : VanillaClassPath) {
            if (!ListClassPath.contains(classPathElement)) {
                ListClassPath.addAll(VanillaClassPath);
            }
        }
        // Inclure le fichier principal de Forge

        String mainForgeJar = MINECRAFT_DIR + "/client.jar" ; // Chemin vers le fichier principal de Forge
        File mainForgeFile = new File(mainForgeJar);
        if (mainForgeFile.exists()) {
            ListClassPath.add(mainForgeFile.getAbsolutePath());
            Logger.log.debug("Fichier principal Forge ajouté au classpath : " + mainForgeFile.getAbsolutePath());
        } else {
            Logger.log.debug("Fichier principal Forge introuvable : " + mainForgeJar);
        }


        // Ajoute les fichiers nécessaires au classpath
        command.add("-cp");
        command.add(ListClassPath.toString().replace(" ", ";").replace(",", "").replace("[", "").replace("]", "").replace("/", "\\"));




        // Ajouter la classe principale
        JsonElement mainClassElement = forgeConfig.get("mainClass");
        if (mainClassElement != null && !mainClassElement.isJsonNull()) {
            command.add(mainClassElement.getAsString());
        } else {
            throw new IllegalStateException("La classe principale (mainClass) est introuvable dans le fichier Forge JSON.");
        }


        //Ajoute les arguments Minecraft s'ils n'ont pas été donnés
        if (getVersionAsNumber(MINECRAFT_VERSION) > 1130) {

            getMinecraftArgs(forgeVersion, gameDir, command);
            Logger.log.debug("Commande complète : " + command);

        }

        // Ajouter les arguments Minecraft (MinecraftArguments) du fichier JSON
        if (forgeConfig.get("arguments") != null) {
            JsonArray minecraftArguments = forgeConfig.get("arguments").getAsJsonObject().get("game").getAsJsonArray();
            if (minecraftArguments != null) {
                for (JsonElement arg : minecraftArguments) {
                    command.add(arg.getAsString().replace("${gameDir}", MINECRAFT_DIR)
                            .replace("${assetsDir}", MINECRAFT_DIR + "/assets")
                            .replace("${versionName}", forgeVersion)
                            .replace("${classpath_separator}", File.pathSeparator));
                    Logger.log.debug("Argument Minecraft ajouté : " + arg.getAsString());
                }
            } else {
                Logger.log.debug("Aucun argument Minecraft supplémentaire dans le fichier JSON.");
            }
        }else {
            String minecraftArguments = forgeConfig.get("minecraftArguments").getAsString();
            String[] minecraftArgumentsList = minecraftArguments.split(" ");
            for (String arg : minecraftArgumentsList) {
                command.add(arg.replace("${gameDir}", MINECRAFT_DIR)
                        .replace("${assetsDir}", MINECRAFT_DIR + "/assets")
                        .replace("${versionName}", forgeVersion)
                        .replace("${classpath_separator}", File.pathSeparator)
                        .replace("$${auth_player_name}", "username")
                        .replace("${version_name}", MINECRAFT_VERSION)
                        .replace("${game_directory}", new File(gameDir).getAbsolutePath())
                        .replace("${assets_root}", new File(gameDir + "/assets").getAbsolutePath())
                        .replace("${assets_index_name}", "index")
                        .replace("${auth_uuid}", "UUID")
                        .replace("${auth_access_token}", "fake_access_token")
                        .replace("${user_type}", "mojang"));
                Logger.log.debug("Argument Minecraft ajouté : " + arg);
            }
        }

        return command;
    }


    private static List<String> addJarsToClasspath(JsonObject forgeConfig, List<String> command, String MINECRAFT_DIR, Path VersionJsonPath) throws IOException {
        JsonArray Libraries = forgeConfig.getAsJsonArray("libraries");
        List<String> ListClassPath = new ArrayList<>(List.of());
        for (JsonElement libraryElement : Libraries) {
            JsonObject library = libraryElement.getAsJsonObject();
            JsonObject downloads = library.getAsJsonObject("downloads");
            if (downloads == null) {
                List<String> libs = new ArrayList<>(List.of());
                List<Library> libraries = parseLibraries(VersionJsonPath);
                for (Library librarys : libraries) {

                    libs.add(getLibrary(librarys));

                }
                return libs;
            }
            if (downloads.has("artifact")) {
                JsonObject artifact = downloads.getAsJsonObject("artifact");
                if (artifact.has("path")) {
                    String path = artifact.get("path").getAsString();
                    Logger.log.debug("JAR ajouter : "+MINECRAFT_DIR+"/libraries/"+ path);
                    File file = new File(MINECRAFT_DIR+"/libraries/"+ path);
                    ListClassPath.add(file.getAbsolutePath());
                }
            }
        }
        return ListClassPath;
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
                libraries.add(new Library(name));
            }
        }
        return libraries;
    }

    private static String getLibrary(Library library) throws IOException {

        String[] parts = library.name.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid library name: " + library.name);
        }
        String group = parts[0].replace('.', '/');
        String artifact = parts[1];
        String version = parts[2];
        String jarName = artifact + "-" + version + ".jar";
        String path = group + "/" + artifact + "/" + version + "/" + jarName;
        Path targetPath = Paths.get(MINECRAFT_DIR, "libraries", path);
        System.out.println(targetPath);
        return targetPath.toString();
    }

    private static class Library {
        String name;
        //String url;

        Library(String name) {
            this.name = name;
            //this.url = url;
        }
    }

    private static void launchMinecraft(List<String> command) {
        try {
            Logger.log.debug("Commande JVM : " + String.join(" ", command));
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO(); // Rediriger la sortie pour voir les logs dans la console
            Process process = processBuilder.start();
            process.waitFor(); // Attendre la fin du processus
        } catch (IOException | InterruptedException e) {
            Logger.log.debug("Erreur lors du lancement du processus Minecraft : " + e.getMessage());
        }
    }

    // Classe Logger pour gérer les logs
    private static String getGameDirectory() {
        String appData = System.getenv("APPDATA"); // Obtenir le chemin AppData
        return appData + File.separator + gameFolderName;
    }

}

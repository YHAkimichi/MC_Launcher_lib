package be.Akimichi;

import be.Akimichi.Utils.LibrerieFonction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import static be.Akimichi.Utils.MCFonction.getVersionAsNumber;

public class Config {
    private static final Map config = Create_Init_Config_File.readConfigFile();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void ChangeRam(String MinRam, String MaxRam) {
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(Objects.requireNonNull(readLauncherConfigFile()));
            Map config = gson.fromJson(reader, Map.class);
            reader.close();


            // Étape 2 : Modifier les données
            config.put("MinRam", MinRam);
            config.put("MaxRam", MaxRam);  // Changer la version telecharger


            // Étape 3 : Réécrire le fichier avec les nouvelles données
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Objects.requireNonNull(readLauncherConfigFile()));
            gsonPretty.toJson(config, writer);
            writer.close();


        } catch (IOException e) {
            LibrerieFonction.Logger.log.debug(e.getMessage());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void ChangeJavaPath(String Path) {
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(Objects.requireNonNull(readLauncherConfigFile()));
            Map config = gson.fromJson(reader, Map.class);
            reader.close();


            // Étape 2 : Modifier les données

            config.put("JavaPath", Path);


            // Étape 3 : Réécrire le fichier avec les nouvelles données
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Objects.requireNonNull(readLauncherConfigFile()));
            gsonPretty.toJson(config, writer);
            writer.close();


        } catch (IOException e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
        }
    }


    public static boolean ChangeCurrentVersion(String Version) {
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(Objects.requireNonNull(readLauncherConfigFile()));
            @SuppressWarnings("rawtypes") Map config = gson.fromJson(reader, Map.class);
            reader.close();

            // Étape 2 : Modifier les données
            //noinspection unchecked
            config.put("CurrentVersion", Version);  // Changer la version telecharger


            // Étape 3 : Réécrire le fichier avec les nouvelles données
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Objects.requireNonNull(readLauncherConfigFile()));
            gsonPretty.toJson(config, writer);
            writer.close();
            return true;


        } catch (IOException e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
            return false;
        }
    }

    public static void addMod(String modId, String Jar) {
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(Objects.requireNonNull(readLauncherConfigFile()));
            @SuppressWarnings("rawtypes") Map config = gson.fromJson(reader, Map.class);
            reader.close();
            List<Map<String, String>> modsList = (List<Map<String, String>>) config.get("Mods");


            for (Map<String, String> mod : modsList) {
                if (mod.get("Id").equals(modId) && mod.get("Jar").equals(Jar)) {
                    return;
                }
            }
            Map<String, String> newMod = new HashMap<>();
            newMod.put("Id", modId);
            newMod.put("Jar", Jar);
            modsList.add(newMod);

            // Étape 2 : Modifier les données
            //noinspection unchecked
            config.put("Mods", modsList);  // Changer la version telecharger


            // Étape 3 : Réécrire le fichier avec les nouvelles données
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Objects.requireNonNull(readLauncherConfigFile()));
            gsonPretty.toJson(config, writer);
            writer.close();


        } catch (IOException e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
        }
    }

    public static String getModName(String modId) {
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(Objects.requireNonNull(readLauncherConfigFile()));
            @SuppressWarnings("rawtypes") Map config = gson.fromJson(reader, Map.class);
            reader.close();
            List<Map<String, String>> modsList = (List<Map<String, String>>) config.get("Mods");


            for (Map<String, String> mod : modsList) {
                if (mod.get("Id").equals(modId)) {
                    return mod.get("Jar");
                }
            }


        } catch (IOException e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
        }
        return modId;
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setToForge(String ForgeVersion) {
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(Objects.requireNonNull(readLauncherConfigFile()));
            Map config = gson.fromJson(reader, Map.class);
            reader.close();

            // Étape 2 : Modifier les données
            config.put("ForgeVersion", ForgeVersion);
            config.put("IsForge", "true");


            // Étape 3 : Réécrire le fichier avec les nouvelles données
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Objects.requireNonNull(readLauncherConfigFile()));
            gsonPretty.toJson(config, writer);
            writer.close();


        } catch (IOException e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
        }
    }

    public static void Set_Debug_Mode(Boolean Debug) {
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(Objects.requireNonNull(readLauncherConfigFile()));
            Map config = gson.fromJson(reader, Map.class);
            reader.close();

            // Étape 2 : Modifier les données
            config.put("Debug", Debug.toString());

            // Étape 3 : Réécrire le fichier avec les nouvelles données
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Objects.requireNonNull(readLauncherConfigFile()));
            gsonPretty.toJson(config, writer);
            writer.close();


        } catch (IOException e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
        }
    }

    public static List<String> getNativeForVersion() {
        assert config != null;
        String version = config.get("MinecraftVersion").toString();
        if (getVersionAsNumber(version) > 1140) {
            return List.of(new String[]{
                    "org\\lwjgl\\lwjgl\\3.2.2\\lwjgl-3.2.2-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-glfw\\3.2.2\\lwjgl-glfw-3.2.2-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-jemalloc\\lwjgl-jemalloc-3.2.2-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-openal\\3.2.2\\lwjgl-openal-3.2.2-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-opengl\\3.2.2\\lwjgl-opengl-3.2.2-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-stb\\3.2.2\\lwjgl-stb-3.2.2-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-tinyfd\\3.2.2\\lwjgl-tinyfd-3.2.2-natives-windows.jar",
                    "com\\mojang\\text2speech\\1.11.3\\text2speech-1.11.3-natives-windows.jar"
            });
        } else if (getVersionAsNumber(version) > 1130) {
            return List.of(new String[]{
                    "org\\lwjgl\\lwjgl\\3.1.6\\lwjgl-3.1.6-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-glfw\\3.1.6\\lwjgl-glfw-3.1.6-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-jemalloc\\3.1.6\\lwjgl-jemalloc-3.1.6-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-openal\\3.1.6\\lwjgl-openal-3.1.6-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-opengl\\3.1.6\\lwjgl-opengl-3.1.6-natives-windows.jar",
                    "org\\lwjgl\\lwjgl-stb\\3.1.6\\lwjgl-stb-3.1.6-natives-windows.jar",
                    "com\\mojang\\text2speech\\1.10.3\\text2speech-1.10.3-natives-windows.jar",
            });
        } else if (getVersionAsNumber(version) > 1095) {
            return List.of(new String[]{
                    "org\\lwjgl\\lwjgl\\lwjgl\\2.9.4-nightly-20150209\\lwjgl-2.9.4-nightly-20150209.jar",
                    "org\\lwjgl\\lwjgl\\lwjgl_util\\2.9.4-nightly-20150209\\lwjgl-2.9.4-nightly-20150209.jar",
                    "org\\lwjgl\\lwjgl\\lwjgl-platform\\2.9.4-nightly-20150209\\lwjgl-platform-2.9.4-nightly-20150209-natives-windows.jar",
                    "com\\mojang\\text2speech\\1.10.3\\text2speech-1.10.3-natives-windows.jar",
            });
        } else if (getVersionAsNumber(version) > 1000) {
            return List.of(new String[]{
                    "org\\lwjgl\\lwjgl\\lwjgl-platform\\2.9.4-nightly-20150209\\lwjgl-platform-2.9.4-nightly-20150209-natives-windows.jar",
            });
        }
        return List.of();
    }


    public static String readLauncherConfigFile() {
        String configFile = "LauncherConfigFile.json";

        Gson gson = new Gson();

        try (FileReader reader = new FileReader(configFile)) {
            // Lire le fichier JSON en tant que Map

            return gson.fromJson(reader, Map.class).get("FilePath").toString();
        } catch (IOException e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
        }

        return null;
    }
}

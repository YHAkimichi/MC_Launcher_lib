package be.Akimichi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Create_Init_Config_File {
    public static File PathToConfigFile = FindConfigFile();

    public static boolean ConfigCreateFile(String LauncherName, String Version) {

        String appFolder = System.getenv("APPDATA");

        File configFile = new File(appFolder, "LauncherLibConfig.json");

        if (!configFile.exists()) {
            Map<String, Object> config = new HashMap<>();
            config.put("MinecraftVersion", Version);
            config.put("MinRam", "2");
            config.put("MaxRam", "2");
            config.put("LauncherName", LauncherName);
            config.put("CurrentVersion", "");
            config.put("LibVersion", LibConfig.LibVersion);


            // Convertir la Map en JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(config);

            // Écrire le JSON dans un fichier
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(jsonString);
                System.out.println("[MC_Launcher_lib] Config file created.");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    public static Map<String, Object> readConfigFile()  {
        File configFile = PathToConfigFile;

        Gson gson = new Gson();

        try (FileReader reader = new FileReader(configFile)) {
            // Lire le fichier JSON en tant que Map
            Map<String, Object> config = gson.fromJson(reader, Map.class);

            return config;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static boolean exist() {
        System.out.println(PathToConfigFile);
        if (PathToConfigFile != null){
            System.out.println("oui");
            return true;
        }else {
            System.out.println("non");
            return false;
        }
    }


    public static File FindConfigFile(){
        String startDirectory = System.getenv("APPDATA");  // Démarrer dans le répertoire courant
        File configFile = FindConfigFileMethod(new File(startDirectory), "LauncherLibConfig.json");
            return configFile;
    }


    public static File FindConfigFileMethod(File directory, String fileName) {
        if (directory == null || !directory.isDirectory()) {
            return null;
        }

        File[] files = directory.listFiles();
        if (files == null) return null;

        for (File file : files) {
            if (file.isDirectory()) {
                File found = FindConfigFileMethod(file, fileName);
                if (found != null) {
                    return found;
                }
            } else if (fileName.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }
    public static boolean ChangeCurrentVersion(String Version){
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(PathToConfigFile);
            Map<String, Object> config = gson.fromJson(reader, Map.class);
            reader.close();
            System.out.println(config);

            // Étape 2 : Modifier les données
            config.put("CurrentVersion", Version);  // Changer la version telecharger


            // Étape 3 : Réécrire le fichier avec les nouvelles données
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(PathToConfigFile);
            gsonPretty.toJson(config, writer);
            writer.close();
            return true;


        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean ChangeRam(String MinRam, String MaxRam){
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(PathToConfigFile);
            Map<String, Object> config = gson.fromJson(reader, Map.class);
            reader.close();
            System.out.println(config);

            // Étape 2 : Modifier les données
            config.put("MinRam", MinRam);
            config.put("MaxRam", MaxRam);  // Changer la version telecharger


            // Étape 3 : Réécrire le fichier avec les nouvelles données
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(PathToConfigFile);
            gsonPretty.toJson(config, writer);
            writer.close();
            return true;


        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

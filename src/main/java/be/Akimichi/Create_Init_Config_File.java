package be.Akimichi;

import be.Akimichi.Utils.LibrerieFonction.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.Map;

import static be.Akimichi.Config.readLauncherConfigFile;
import static be.Akimichi.LibConfig.GetConfigFileData;

public class Create_Init_Config_File {


    public static void ConfigCreateFile(String LauncherName, String Version) {

        String appFolder = System.getenv("APPDATA");

        File configFile = new File(appFolder, "\\"+LauncherName+"\\LauncherLibConfig.json");
        File LauncherFolder = new File(appFolder, "\\" + LauncherName);

        if (!LauncherFolder.exists()) {
            if (LauncherFolder.mkdir()){
                Logger.log("Folder created");
            }
        }

        if (!configFile.exists()) {
            Map<String, Object> config = GetConfigFileData(LauncherName, Version, appFolder);
            // Convertir la Map en JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(config);

            // Écrire le JSON dans un fichier
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(jsonString);
                System.out.println("[MC_Launcher_lib] Config file created.");
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }


    @SuppressWarnings("rawtypes")
    public static Map readConfigFile()  {
        File configFile = new File(readLauncherConfigFile());

        Gson gson = new Gson();

        try (FileReader reader = new FileReader(configFile)) {
            // Lire le fichier JSON en tant que Map

            return gson.fromJson(reader, Map.class);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }


    public static boolean exist(String LauncherName) {
        File configFile = new File(System.getenv("APPDATA")+LauncherName);
        return configFile.exists();

    }
}

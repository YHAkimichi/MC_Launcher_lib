package be.Akimichi;


import be.Akimichi.Utils.LibrerieFonction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static be.Akimichi.Config.readLauncherConfigFile;


public class InitLibrary {

    public static void init(String LauncherName, String Version) {
        Create_Launcher_Config_File(LauncherName);
        if (!Create_Init_Config_File.exist(LauncherName)) {
            Create_Init_Config_File.ConfigCreateFile(LauncherName, Version);
        }


        Map config = Create_Init_Config_File.readConfigFile();
        assert config != null;
        if (!config.get("MinecraftVersion").toString().equals(Version)) {
            //SetMinecraftVersion(Version);
        }
    }


    private static void Create_Launcher_Config_File(String LauncherName)  {
        // Créer des données pour le JSON
        Map<String, Object> data = new HashMap<>();
        data.put("FilePath",System.getenv("APPDATA") +"\\"+ LauncherName + "\\" + "LauncherLibConfig.json");


        // Sérialiser les données en JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data);

        // Écrire le JSON dans un fichier
        String filePath = "LauncherConfigFile.json";

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json);
            System.out.println("[MC_Launcher_lib] Fichier JSON créé avec succès : " + filePath);
        } catch (IOException e) {
            System.err.println("[MC_Launcher_lib] Erreur lors de l'écriture du fichier JSON : " + e.getMessage());
        }
    }
    private static void SetMinecraftVersion(String Version) {
        Gson gson = new Gson();

        try {
            // Lire le fichier JSON existant
            FileReader reader = new FileReader(Objects.requireNonNull(readLauncherConfigFile()));
            Map config = gson.fromJson(reader, Map.class);
            reader.close();

            // Étape 2 : Modifier les données
            config.put("MinecraftVersion", Version);

            // Étape 3 : Réécrire le fichier avec les nouvelles données
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Objects.requireNonNull(readLauncherConfigFile()));
            gsonPretty.toJson(config, writer);
            writer.close();


        } catch (IOException e) {
            LibrerieFonction.Logger.log.error(e.getMessage());
        }
    }
}

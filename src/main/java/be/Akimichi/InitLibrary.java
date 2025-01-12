package be.Akimichi;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class InitLibrary {

    public static void init(String LauncherName, String Version) {
        Create_Launcher_Config_File(LauncherName);
        if (!Create_Init_Config_File.exist(LauncherName)) {
            Create_Init_Config_File.ConfigCreateFile(LauncherName, Version);
        }
    }
    public static void Create_Launcher_Config_File(String LauncherName)  {
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
            System.out.println("Fichier JSON créé avec succès : " + filePath);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier JSON : " + e.getMessage());
        }
    }
}

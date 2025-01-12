package be.Akimichi.Utils;

import be.Akimichi.Create_Init_Config_File;
import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;

public class Create_launcher_profil {


    public static void Create() {
        String startDirectory = System.getenv("APPDATA");
        String dir = Objects.requireNonNull(Create_Init_Config_File.readConfigFile()).get("LauncherName").toString();
        // Répertoire par défaut du fichier launcher_profiles.json
        // Changez-le si nécessaire
        String MINECRAFT_DIR = (startDirectory + "/" + dir);
        // Définir le chemin du fichier launcher_profiles.json
        Path launcherProfilesPath = Paths.get(MINECRAFT_DIR, "launcher_profiles.json");

        // Créer le fichier si nécessaire
        createLauncherProfilesJson(launcherProfilesPath);
    }

    private static void createLauncherProfilesJson(Path path) {
        // Vérifier si le fichier existe déjà
        if (Files.exists(path)) {
            LibrerieFonction.Logger.log("Le fichier launcher_profiles.json existe déjà.");
            return;
        }
        String version = Objects.requireNonNull(Create_Init_Config_File.readConfigFile()).get("MinecraftVersion").toString();
        // Créer la structure du profil Minecraft
        JsonObject launcherProfiles = new JsonObject();
        JsonObject profiles = new JsonObject();

        // Créer un profil de jeu standard (sans Forge)
        JsonObject profile = new JsonObject();
        profile.addProperty("name", "Minecraft");
        profile.addProperty("lastVersionId", version); // Version de Minecraft sans Forge, ajustez si nécessaire

        JsonObject lastUsed = new JsonObject();
        lastUsed.addProperty("time", System.currentTimeMillis()); // Heure actuelle pour simuler un lancement

        profile.add("lastUsed", lastUsed);

        // Définir les arguments JVM (vous pouvez les ajuster selon vos préférences)
        JsonObject jvmArguments = new JsonObject();
        jvmArguments.addProperty("maxHeapSize", "2048M");
        profile.add("jvmArguments", jvmArguments);

        // Ajouter le profil créé à la liste des profils
        profiles.add("Minecraft", profile);

        // Ajouter les profils à la structure principale
        launcherProfiles.add("profiles", profiles);

        // Ajouter la version par défaut (si nécessaire)
        JsonObject versionManifest = new JsonObject();
        versionManifest.addProperty("name", version); // Version Minecraft
        versionManifest.addProperty("id", version);  // Version Minecraft
        launcherProfiles.add("versionManifest", versionManifest);

        // Enregistrer dans le fichier JSON
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(launcherProfiles.toString());
            LibrerieFonction.Logger.log("Le fichier launcher_profiles.json a été créé avec succès à : " + path);
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du fichier : " + e.getMessage());
        }
    }
}


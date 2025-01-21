package be.Akimichi.Forge;

import be.Akimichi.Utils.LibrerieFonction;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ModDownloader {
    // Cette clé ne peut être utilisée que pour MC_Launcher_lib
    private static final String API_KEY = "$2a$10$5qb3qPfX2hnDm1sKqakIg.gL.xR3MU/h9pIWFmM5oaEatZDbO0gjC";
    private static final String API_BASE_URL = "https://api.curseforge.com/v1";

    // Méthode pour effectuer une requête GET
    private static String sendGetRequest(String endpoint) throws IOException {
        URL url = new URL(API_BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("x-api-key", API_KEY);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                return response.toString();
            }
        } else {
            throw new IOException("Échec de la requête : Code " + responseCode);
        }
    }

    // Méthode pour télécharger un fichier
    private static void downloadFile(String fileURL, String savePath) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream out = new FileOutputStream(savePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        LibrerieFonction.Logger.log.info("Fichier téléchargé dans : " + savePath);
    }

    // Méthode principale pour rechercher et télécharger un mod compatible
    public static String downloadMod(int projectId, String minecraftVersion, String forgeVersion, String saveDir) {

        try {
            //  Construire l'URL avec les filtres Minecraft et Forge
            String filesEndpoint = "/mods/" + projectId + "/files";
            String response = sendGetRequest(filesEndpoint);

            // Filtrer les fichiers compatibles dans la réponse JSON
            JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
            JsonArray files = responseJson.getAsJsonArray("data");

            String downloadUrl = null;
            String fileName = null;

            for (JsonElement fileElement : files) {
                JsonObject fileObject = fileElement.getAsJsonObject();

                // Vérifie la compatibilité avec Minecraft et Forge
                JsonArray gameVersions = fileObject.getAsJsonArray("gameVersions");
                boolean isCompatible = false;

                for (JsonElement versionElement : gameVersions) {
                    if (versionElement.getAsString().equals(minecraftVersion)
                            || versionElement.getAsString().contains("Forge " + forgeVersion)) {
                        isCompatible = true;
                        break;
                    }
                }

                if (isCompatible) {
                    downloadUrl = fileObject.get("downloadUrl").getAsString();
                    fileName = fileObject.get("fileName").getAsString();
                    break;
                }
            }

            //Télécharger le fichier compatible
            if (downloadUrl != null && fileName != null) {
                LibrerieFonction.Logger.log.info("Téléchargement du fichier : " + fileName);
                downloadFile(downloadUrl, saveDir + "/" + fileName);
                return fileName;
            } else {
                LibrerieFonction.Logger.log.warn("Aucun fichier compatible trouvé pour Minecraft " + minecraftVersion + " et Forge " + forgeVersion);
            }
        } catch (IOException e) {
            LibrerieFonction.Logger.log.error("Erreur : " + e.getMessage());
        }

        return null;
    }
}

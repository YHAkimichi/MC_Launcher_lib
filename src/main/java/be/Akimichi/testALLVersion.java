package be.Akimichi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static be.Akimichi.Vanilla_download.download;
import static be.Akimichi.Vanilla_download.downloadText;

public class testALLVersion {


    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
     static String version = "1.7.9";


     public static void test() throws IOException {
         List<String> VersionList = filterStringsByPattern(ALLversion(getManifest()));
         VersionList.forEach(element ->{
             version = element;
             if (!version.equals("1.21") || !version.equals("1.20")) {
                 System.out.println("verification de la version : " + element);
                 try {
                     if (download(progress ->{

                     })) {
                         launchVanillaMinecraft.launchGame();
                     }
                     System.out.println(element + " ok");
                 } catch (Exception e) {
                     System.out.println(element + " not ok");
                     e.printStackTrace();
                 }
             }
         });
    }


    public static JsonObject getManifest() throws IOException {
        String json = downloadText(VERSION_MANIFEST_URL);
        return JsonParser.parseString(json).getAsJsonObject();
    }



    public static List<String> ALLversion(JsonObject manifest) {
        JsonArray versions = manifest.getAsJsonArray("versions");
        List<String> versionList = new ArrayList<>();

        for (JsonElement versionElement : versions) {
            JsonObject versionObj = versionElement.getAsJsonObject();
            if (versionObj.get("type").getAsString().equals("release")){
              versionList.add(versionObj.get("id").getAsString());
                System.out.println(versionObj.get("id").getAsString());
            }
        }
        return versionList;
    }
    public static List<String> filterStringsByPattern(List<String> strings) {
        // Définir le motif regex
        Pattern pattern = Pattern.compile("^1\\.\\d+$");

        // Filtrer les chaînes qui correspondent au motif
        List<String> filteredList = new ArrayList<>();
        for (String str : strings) {
            if (pattern.matcher(str).matches()) {
                filteredList.add(str);
            }
        }

        return filteredList;
    }

}

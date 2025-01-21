package be.Akimichi.Minecraft;

import be.Akimichi.Config;
import be.Akimichi.Create_Init_Config_File;
import be.Akimichi.Forge.Forge_download;
import be.Akimichi.Forge.Launch_Forge;
import be.Akimichi.Forge.ModDownloader;
import be.Akimichi.Utils.LibrerieFonction;
import be.Akimichi.Vanilla.Vanilla_download;
import be.Akimichi.Vanilla.launchVanillaMinecraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Game {
    public static class Forge {
        public static void Launch() throws IOException {
            LibrerieFonction.Logger.log.info("Launching Minecraft Forge");
            Launch_Forge.LaunchGame();
        }

        public static void Update() {
            LibrerieFonction.Logger.log.info("Updating Minecraft Forge");
            Forge_download.download();
            LibrerieFonction.Logger.log.info("Downloading Mods");
            DownloadMods();
            LibrerieFonction.Logger.log.info("Download complete");
        }

        private static void DownloadMods() {
            Map config = Create_Init_Config_File.readConfigFile();
            List<Integer> mods = Mods.Mods;
            assert config != null;
            File modDir = new File(config.get("pathToDirectory") + "/mods");
            if (!modDir.exists()) {
                if (!modDir.mkdir()) {
                    LibrerieFonction.Logger.log.error("Could not create mods directory");
                }
            }
            for (Integer mod : mods) {
                if (!new File(config.get("pathToDirectory") + "/mods/è!" + Config.getModName(mod.toString())).exists()){
                    String Name = ModDownloader.downloadMod(mod, config.get("MinecraftVersion").toString(), config.get("ForgeVersion").toString(), config.get("pathToDirectory").toString() + "/mods/");
                    Config.addMod(mod.toString(), Name);
                }
            }
        }

        public static void Launch_Update() throws IOException {
            Update();
            Launch();
        }
    }
    public static class Vanilla {
        public static void Launch() throws IOException {
            LibrerieFonction.Logger.log.info("Launching Minecraft");
            launchVanillaMinecraft.launchGame();
        }

        public static void Update() {
            LibrerieFonction.Logger.log.info("Updating Minecraft");
            Vanilla_download.download();
            LibrerieFonction.Logger.log.info("Download complete");
        }

        public static void Launch_Update() throws IOException {
            Update();
            Launch();
        }
    }


    public static class Mods {
        public static List<Integer> Mods = new ArrayList<>();
        public static void Add(List<Integer> mods) {
            Mods.addAll(mods);
        }
    }


}

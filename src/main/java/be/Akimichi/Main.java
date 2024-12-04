package be.Akimichi;


import java.io.IOException;

import static be.Akimichi.Create_Init_Config_File.ChangeRam;



public class Main {

    public static void main(String[] args) throws IOException {
        InitLibrary.init("Launcher","1.7.9");
        ChangeRam("2","14");
        Vanilla_download.download();
        launchVanillaMinecraft.launchGame();
    }
}
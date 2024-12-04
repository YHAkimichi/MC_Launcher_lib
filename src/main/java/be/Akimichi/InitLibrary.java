package be.Akimichi;

public class InitLibrary {
    public static void init(String LauncherName, String Version){
        if (Create_Init_Config_File.exist()){
            return;
        }else {
            while (Create_Init_Config_File.ConfigCreateFile(LauncherName, Version));

        }
    }
}

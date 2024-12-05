
# MC_Launcher_lib

**MC_Launcher_lib** is a powerful library designed to help developers create custom Minecraft launchers. With easy-to-use functions, you can streamline the process of downloading game files, configuring settings, and launching Minecraft.

## Features

- Initialize the library with custom launcher settings.
- Download the necessary Minecraft game files.
- Monitor download progress with a percentage callback.
- Configure RAM allocation for optimized gameplay.
- Launch the game with ease.

## Getting Started

Follow these steps to integrate and use **MC_Launcher_lib** in your project.

### 1. Initialize the Library

Use the `InitLibrary.init` method to set up the library with your custom launcher name and version.

```java
InitLibrary.init("LauncherName", "version");
```
### 2. Download Minecraft Files

To download the game files, you can use the `Vanilla_download.download `method.
#### Basic Download

```java
Vanilla_download.download();
```
#### Download with Progress Callback

To track download progress, use a callback function:
```java
Vanilla_download.download(percentage -> {
    System.out.println("Download Progress: " + percentage + "%");
});
```
### 3. Launch the Game

Once the game files are downloaded, you can launch Minecraft using the `launchVanillaMinecraft`.launchGame method.

```java
launchVanillaMinecraft.launchGame();
```
### Optional: Change RAM Allocation

To adjust the minimum and maximum RAM allocation for Minecraft, use the `Create_Init_Config_File.ChangeRam` method:
```java
Create_Init_Config_File.ChangeRam("MinRam", "MaxRam");
```
Replace `"MinRam"` and `"MaxRam"` with your desired values (e.g., `"2"` for 2GB).

### Example Usage
```java
InitLibrary.init("MyCustomLauncher", "1.12.2");

Vanilla_download.download(percentage -> {
    System.out.println("Download Progress: " + percentage + "%");
});

Create_Init_Config_File.ChangeRam("2", "4");

launchVanillaMinecraft.launchGame();

```
### Note
This version includes a mention that the library currently works only with cracked vanilla Minecraft.

### List of support version
#### Vanilla
##### "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21",
##### "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1","1.20",
##### "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19",
##### "1.18.2", "1.18.1", "1.18",
##### "1.17.1", "1.17",
##### "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1", "1.16",
##### "1.15.2", "1.15.1", "1.15",
##### "1.14.4", "1.14.3", "1.14.2", "1.14.1", "1.14",
##### "1.13.2", "1.13.1", "1.13",
##### "1.12.2", "1.12.1", "1.12",
##### "1.11.2", "1.11.1", "1.11",
##### "1.10.2", "1.10.1", "1.10",
##### "1.9.4", "1.9.3", "1.9.2", "1.9",
##### "1.8.9",
##### "1.7.9"

### Requirements

- Java 8 or higher

### Support

If you encounter any issues or have questions, please contact us or open an issue in the repository.
# Spotify display
### what is it?
It's a small project that displays the currently playing song from spotify.

## How to use
1. Download this repository to your computer
2. You need a Spotify application this can be acquired at [Dashboard | Spotify for Developers](https://developer.spotify.com/dashboard)
3. Now we need to configure the application to work correctly whit SpotifyDisplay
    - Open the settings panel for the appilcation and make sure you are allowed to edit the settings
    - add `http://localhost:8080/` to the `Redirect URIs`
    - If you whant your friends to be abel to use this too then go to the `User Managment` tab and add your friends
4. Save the `Client ID` and `Client secret` to a file, those are available in the settings panel
5. Now we need to save those values to the correct file for the appilcation
    ```json
    {
        "ClientID": "Your ID hear!",
        "ClientSecret": "Your Secret hear!"
    }
    ```
   This is a JSON example of the file. replace the values of `ClientID` with your applications `Client ID` and the value of `ClientSecret` whit your `Client secret`
6. This file is then saved to `src/main/resources/credentials.json`
7. now open a terminal(if your using Windows 10 or older open a command prompt) and run
    - terminal: `./gradlew shadowJar`
    - command prompt: `gradlew.bat shadowJar`
8. now you can open `build/libs` and run `spotifyDisplay-1.0-SNAPSHOT-all.jar`
Optional steps
    - You can export the appilcation as an executable instead of a jar file by running
        - Mac OS: `java -jar ./packr-all-4.0.0.jar config-mac.json`. NOTE: the macOS JRE is not included in this repository, download a JRE and then change in the config file so the jdk tag points to the JRE
        - Windows: `java -jar ./packr-all-4.0.0.jar config-win.json`
        - linux: I don't have a linux config file pre created but the windows one should work just change the platform and jdk to linux friendly options according too [packr github](https://github.com/libgdx/packr#usage)

## Requiremtns
- Using a pre compiled version
   - Java JRE version 17 or newer
   - A Spotify account
- Compileing your own version
   - Java JDK 17 or newer
   - Spotify API application
   - A Spotify account
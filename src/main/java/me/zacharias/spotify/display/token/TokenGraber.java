package me.zacharias.spotify.display.token;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.awt.*;
import java.io.IOException;

public class TokenGraber {
    Info info;
    SpotifyApi spotifyApi;
    public TokenGraber(SpotifyApi spotifyApi){
        this.spotifyApi = spotifyApi;
    }

    public String getCode() {
        info = new Info("Opening webpage for new token", "Missing token");

        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .state("x4xkmn9pu3j6ukrs8n")
                .scope("user-read-currently-playing")
                //.show_dialog(true)
                .build();

        WebServer server = new WebServer(8080);

        try {
            Desktop.getDesktop().browse(authorizationCodeUriRequest.execute());
            info.setMessage("Waiting for webpage to return token");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String code;

        while((code = server.getCode()) == null){
            synchronized (this){
                try {
                    this.wait(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        info.setMessage("Token recived, This window will close in 3 seconds");

        synchronized (this){
            try {
                this.wait(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        info.frame.dispose();

        try {
            server.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return code;
    }
}

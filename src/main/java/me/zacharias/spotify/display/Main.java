package me.zacharias.spotify.display;

import me.zacharias.spotify.display.token.TokenGraber;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONObject;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static me.zacharias.spotify.display.PaintUtil.*;

public class Main extends JPanel implements MouseListener, KeyListener {

    private final int[][] SettingsIcon = LoadImage.getGrayScaleImageFromResources("settings.bin");
    private final int[][] next = LoadImage.getGrayScaleImageFromResources("next.bin");
    private final int[][] back = LoadImage.getGrayScaleImageFromResources("back.bin");
    private final int[][] selected = LoadImage.getGrayScaleImageFromResources("selected.bin");
    private final int[][] empty = LoadImage.getGrayScaleImageFromResources("empty.bin");
    private final int[][] reloadIcon = LoadImage.getGrayScaleImageFromResources("reload.bin");
    private final int baseSettingsY = 55;
    private int selectedView;
    private int point;
    private int fadeStart;
    private int fadeComplete;
    private long endTime;
    public SpotifyApi spotifyApi = null;
    private AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = null;
    private Rectangle minimise;
    private Rectangle settings;
    private Rectangle showIconSwitch;
    private Rectangle darkModeSwitch;
    private Rectangle nextViweMode;
    private Rectangle backViweMode;
    private Rectangle close;
    private Rectangle reload;
    private Rectangle alwaysOnTopSwitch;
    private Rectangle cacheIconsSwitch;
    private Rectangle fadeIconSwitch;
    private Button cacheClearButton;
    private TextBox fadeStartTime;
    private TextBox fadeCompleteTime;
    private TextBox selectedTextBox;
    private String oldSong;
    private String token;
    private static boolean online = true;
    private boolean darkMode = false;
    private boolean showIcon = true;
    private boolean frameMove = false;
    private boolean settingMenu = false;
    private boolean is_playing = false;
    private boolean inizilised = false;
    private boolean puase = false;
    private boolean alwaysOnTop = false;
    private boolean cacheIcons = false;
    private boolean fadeIcon = false;
    private GetUsersCurrentlyPlayingTrackRequest item = null;
    private final JFrame frame = new JFrame();

    private long last = 0L;
    private final ActionListener task = (a) -> {
            try {
                AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
                // Set access and refresh token for further "spotifyApi" object usage
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                endTime = System.currentTimeMillis();
                Time time = new Time(System.currentTimeMillis() - last);
                System.out.println("Time since last "+ (time.getTimeInMinutes()==0?time.getTimeInMilliseconds()+"ms":time.getTimeInMinutes()+"m"));
                last = System.currentTimeMillis();
                //valid = authorizationCodeCredentials.getExpiresIn();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
    };
    Timer display = new javax.swing.Timer((1000/35), (a) -> this.repaint());
    Timer moveTimer = new javax.swing.Timer(1, (a) -> this.moveFrame());
    Timer timer = new Timer(58*60*1000, task);
    private JSONObject currentPlayingJson;
    Runnable spotifyListening = () -> {
        while(true){
            if(puase){
                synchronized (selected){
                    try {
                        selected.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if(online) {
                try {
                    synchronized (selected) {
                        if(currentPlayingJson != null) {
                            is_playing = currentPlayingJson.getBoolean("is_playing");
                            if (is_playing) {
                                selected.wait(200);
                            } else {
                                item = spotifyApi.getUsersCurrentlyPlayingTrack().build();
                                JSONObject cp = new JSONObject(item.getJson());
                                if (cp != null) {
                                    is_playing = cp.getBoolean("is_playing");
                                } else {
                                    return;
                                }
                            }
                        }else{
                            selected.wait(200);
                            item = spotifyApi.getUsersCurrentlyPlayingTrack().build();
                        }
                    }
                    if (System.currentTimeMillis() > 0) {
                        currentPlayingJson = new JSONObject(item.getJson());
                        if (!oldSong.equals(currentPlayingJson.getJSONObject("item").getString("uri"))) {
                            oldSong = currentPlayingJson.getJSONObject("item").getString("uri");
                            newSong();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    synchronized (selected) {
                        try {
                            while (true) {
                                selected.wait(1000);

                                puase = true;

                                this.timer.stop();
                                this.timer = new Timer(58 * 60 * 1000, task);
                                this.timer.start();

                                task.actionPerformed(null);

                                puase = false;

                                item = spotifyApi.getUsersCurrentlyPlayingTrack().build();
                                if (item.getJson() != null) {
                                    currentPlayingJson = new JSONObject(item.getJson());
                                    break;
                                }
                            }
                        } catch (InterruptedException | IOException | ParseException | SpotifyWebApiException ex) {
                            //throw new RuntimeException(ex);
                            ex.printStackTrace();
                        }
                    }
                }
                if (currentPlayingJson == null && is_playing) {
                    try {
                        AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
                        // Set access and refresh token for further "spotifyApi" object usage
                        spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                        endTime = System.currentTimeMillis() + 0;

                        puase = true;

                        this.timer.stop();
                        this.timer = new Timer(58*60*1000, task);
                        this.timer.start();

                        task.actionPerformed(null);

                        puase = false;

                        //valid = authorizationCodeCredentials.getExpiresIn();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage()+"\nrow: 135");
                    }
                }
            }else{
                currentPlayingJson = new JSONObject(Loader.loadFile("./example.json"));
            }
        }
    };
    private Thread t = new Thread(spotifyListening);
    private static BufferedImage offlineIcon = null;
    private final BufferedImage onSetting = LoadImage.getImageFromResources("on.png");
    private final BufferedImage offSetting = LoadImage.getImageFromResources("off.png");
    private BufferedImage[] icons = new BufferedImage[3];
    private BufferedImage icon = null;
    private BufferedImage drawFrame;
    private final File iconCatch;
    private final File configFile;
    private final JSONObject config;
    private SizeMode sizeMode = SizeMode.NORMAL;
    private Font size20Font;
    private Clock clock = new Clock();

    public static void main(String[] args) throws IOException {
        if(args.length > 0) {
            if (args[0].equalsIgnoreCase("offline")) {
                online = false;
                offlineIcon = ImageIO.read(new File("./example.jpg"));
            }
        }
        try {
            new Main();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Color invert(Color color) {
        int r = 255 - color.getRed();
        int g = 255 - color.getGreen();
        int b = 255 - color.getBlue();

        return new Color(r, g, b);  // Inverse of the original colour is returned.
    }
    public static Color average(BufferedImage img, int x, int y, int width, int height){
        int x1 = x;
        int y1 = y;
        int x2 = x+width;
        int y2 = y+height;

        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;

        // iterate through the pixels in the specified region
        for (int y3 = y1; y3 <= y2; y3++) {
            for (int x3 = x1; x3 <= x2; x3++) {
                // get the RGB value of the current pixel
                int rgb = img.getRGB(x, y);

                // extract the red, green, and blue values
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // add the red, green, and blue values to the total values
                totalRed += red;
                totalGreen += green;
                totalBlue += blue;
            }
        }

// calculate the average red, green, and blue values
        int i = (x2 - x1 + 1) * (y2 - y1 + 1);
        int avgRed = totalRed / i;
        int avgGreen = totalGreen / i;
        int avgBlue = totalBlue / i;

// create a new Color object using the average red, green, and blue values
        Color avgColor = new Color(avgRed, avgGreen, avgBlue);


        return avgColor;
    }
    public Main() throws Exception {

        JSONObject credentials = new JSONObject(Loader.loadFile(this.getClass().getResource("/credentials.json").getFile()));

        spotifyApi = SpotifyApi.builder()
                .setClientId(credentials.getString("ClientID"))
                .setClientSecret(credentials.getString("ClientSecret"))
                //.setAccessToken(object.getString("token"))
                .setRedirectUri(new URI("http://localhost:8080/"))
                .build();

        clock.clockIn(0);

        File dataFolder = new File("./spotifyDisplayData");
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        System.out.println("OS name: "+osName);
        if (osName.contains("windows")){
            dataFolder = new File(System.getenv("APPDATA")+"/spotifyDisplay");
        }

        System.out.println("dataFolder: "+dataFolder.getAbsolutePath());

        if(!dataFolder.exists()){
            dataFolder.mkdir();
        }

        iconCatch = new File(dataFolder,"catch");
        configFile = new File(dataFolder,"config.json");

        // Inizelise display window
        frame.setTitle("Spotify Display");
        frame.setSize(600,300);
        frame.setUndecorated(true);
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addMouseListener(this);
        frame.addKeyListener(this);
        frame.setBackground(new Color(0,0,0, 0));
        frame.setUndecorated(true);
        setBackground(new Color(0,0,0,0));

        // Loading config if config file exist
        if(configFile.exists()){
            // Reading config file into JSONObject
            config = new JSONObject(Loader.loadFile(configFile.getPath()));

            // Setting location to last location
            frame.setLocation(config.getInt("x"), config.getInt("y"));

            // Reading general config
            darkMode = config.getBoolean("darkMode");
            showIcon = config.getBoolean("showIcon");
            alwaysOnTop = config.getBoolean("alwaysOnTop");
            sizeMode = config.getEnum(SizeMode.class, "sizeMode");
            selectedView = sizeMode.getSize();
            cacheIcons = config.getBoolean("cacheIcons");
            fadeIcon = config.getBoolean("fadeIcon");
            fadeStart = config.getInt("fadeStart");
            fadeComplete = config.getInt("fadeComplete");

            // Setting spotify token OPS this token can only be used to read the current playing this have no control over what's playing
            if(config.has("token")) {
                // A saved token was found
                spotifyApi.setRefreshToken(config.getString("token"));
            }else {
                // A saved token was not found, starting a token grabber
                GrabSpotifyToken();
            }

            // Chaging the frame size to the config settings
            frame.setSize(sizeMode.getDim());

            // Making the window always be on top if configured too
            frame.setAlwaysOnTop(alwaysOnTop);
        }else{
            config = new JSONObject();
            // A saved token was not found, starting a token grabber
            GrabSpotifyToken();
        }

        // Making the windows visible
        frame.setVisible(true);

        // Creating a Spotify token refresh object
        authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                    .build();

        Time time = new Time();

        // Checking if the system is set to online mode, if not then this is run for development testing
        if(online) {
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();


            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());


            time.setMin(3600);
            endTime = System.currentTimeMillis() + time.getTimeInMilliseconds();
            last = System.currentTimeMillis();

            // Starting a token refresh thread
            // TODO fix error if task failed
            this.timer.start();


            long timer = System.currentTimeMillis();
            // Getting the current playing song
            while(item == null) {
                try {
                    item = spotifyApi.getUsersCurrentlyPlayingTrack().build();
                    currentPlayingJson = new JSONObject(item.getJson());
                } catch (Exception e) {
                    System.out.println("not playing anything");
                    synchronized (this){
                        this.wait(500);
                    }
                }
            }
            System.out.println("took: " + (System.currentTimeMillis() - timer) + " ms to get current playing");
        }

        // Checking if the directory for spotify album cover exist, else creating it and the size subdirectories
        // This is used to seed up icon loading on slower networks but may take up more space
        if(!iconCatch.exists()){
            iconCatch.mkdir();
            new File(iconCatch,"small").mkdir();
            new File(iconCatch,"medium").mkdir();
            new File(iconCatch,"large").mkdir();
        }

        // Creating a Shutdown hook for when the program is exited to save all config and the spotify refresh token to config file
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                config.put("x", frame.getX());
                config.put("y", frame.getY());
                config.put("darkMode", darkMode);
                config.put("showIcon", showIcon);
                config.put("sizeMode", sizeMode);
                config.put("alwaysOnTop", alwaysOnTop);
                config.put("cacheIcons", cacheIcons);
                config.put("token", spotifyApi.getRefreshToken());
                config.put("fadeIcon", fadeIcon);
                config.put("fadeComplete", fadeComplete);
                config.put("fadeStart", fadeStart);
                if (!configFile.exists()) {
                    configFile.createNewFile();
                }
                BufferedWriter out = new BufferedWriter(new FileWriter(configFile));
                out.write(config.toString(4));
                out.flush();
                out.close();
            }catch (Exception e){

            }
        }));

        // Starting the window update timer and the window move timer
        // The window move timer is used since the standard title bar is disabled
        display.start();
        moveTimer.start();

        // Inizelising a veriable used to ditermen if a new song have started to play or not. This is used to fetch the new album cover, so that it's only loaded once and not multible times during the songs playtime
        oldSong = "";

        frame.setResizable(false);

        // Making sure that the window is etleas 300px heigh
        // TODO adding a mini mode where a 128x128 image is used requiring to remove this if statement
        if(this.getHeight() < 300){
            frame.setSize(600,300+(300-this.getHeight()));
        }

        // Starting the timer to read current playing to get the new album cover and loading the response json into the item object for later handling
        t.start();

        setOpaque(false);
    }

    /**
     * This method grabes the spotify token whit the use of {@link TokenGraber} class
     * @throws IOException
     * @throws SpotifyWebApiException
     * @throws ParseException
     */
    private void GrabSpotifyToken() throws IOException, SpotifyWebApiException, ParseException {
        if(online) {
            // Starting a token grabber
            TokenGraber tokenGraber = new TokenGraber(spotifyApi);
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(tokenGraber.getCode())
                    .build();

            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            // A token have now been grabbed and is put in the SpotifyAPI object

            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
        }
    }

    /**
     * This method is used to load in the new album cover from cache or internet
     * @deprecated use {@link #getIcon()}
     */
    @Deprecated
    private void newSong() {
        if(currentPlayingJson != null){
            try{
                icon = getIcon();
                //frame.setIconImage(new ImageIcon(new URL(currentPlayingJson.getJSONObject("item").getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url"))).getImage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * New method for getting the album cover
     * @return Album cover
     * @throws IOException
     */
    public BufferedImage getIcon() throws IOException {
        String name = currentPlayingJson.getJSONObject("item").getJSONObject("album").getString("uri");
        File img = new File(iconCatch, sizeMode.getIconSizeName() + "/" + name.replaceAll(":", "_")+".AC");
        if(!online)
            return offlineIcon;
        if (img.exists()) {
            return sizeMode.changeIcon(ImageIO.read(img));
        } else if(cacheIcons) {
            BufferedImage icon = ImageIO.read(new URL(currentPlayingJson.getJSONObject("item").getJSONObject("album").getJSONArray("images").getJSONObject(sizeMode.getIconSize()).getString("url")));
            ImageIO.write(icon, "PNG", new FileOutputStream(img));
            return sizeMode.changeIcon(icon);
        }
        else {
            BufferedImage[] icons = new BufferedImage[3];
            icons[0] = ImageIO.read(new URL(currentPlayingJson.getJSONObject("item").getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url")));
            icons[1] = ImageIO.read(new URL(currentPlayingJson.getJSONObject("item").getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url")));
            icons[2] = ImageIO.read(new URL(currentPlayingJson.getJSONObject("item").getJSONObject("album").getJSONArray("images").getJSONObject(2).getString("url")));
            return sizeMode.changeIcon(icons[sizeMode.getIconSize()]);
        }
    }

    /**
     * Moves the frame to snap to the edges of the screen when dragged to those edges
     */
    private void moveFrame() {
        if (frameMove) {
            Point e = frame.getMousePosition();
            Point p = MouseInfo.getPointerInfo().getLocation();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            Rectangle screenBounds = gc.getBounds();

            if (isWithinMargin(p, 10, 0, 0, new Point(0, 0))) {
                // Snap to top left
                frame.setLocation(0, 0);
            } else if (isWithinMargin(p, 10, 0, 0, new Point(0, screenSize.height))) {
                // Snap to bottom left
                frame.setLocation(0, screenSize.height - frame.getHeight());
            } else if (isWithinMargin(p, 10, 0, 0, new Point(screenBounds.width, screenSize.height))) {
                // Snap to bottom right
                frame.setLocation(screenBounds.width - frame.getWidth(), screenSize.height - frame.getHeight());
            } else if(isWithinMargin(p, 10, 0, 0, new Point(screenBounds.width, 0))){
                // Snap to bottom left
                frame.setLocation(screenBounds.width-frame.getWidth(), 0);
            }else {
                p.y -= 10;
                p.x -= frame.getWidth() / 2;
                frame.setLocation(p);
            }
        }
    }

    private static boolean isWithinMargin(Point point, int margin, int xOffset, int yOffset, Point position) {
        int dx = point.x - position.x - xOffset;
        int dy = point.y - position.y - yOffset;
        return dx * dx + dy * dy <= margin * margin;
    }


    /**
     * Adds two points and returns the result.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the sum of the two points
     */
    public static Point addPoints(Point p1, Point p2) {
        int x = p1.x + p2.x;
        int y = p1.y;
        return new Point(x, y);
    }

    /**
     * Paints the window
     * @param g
     */
    private void draw(Graphics2D g) {

        int settingsY = baseSettingsY;
        int maxSizeModeWidth = g.getFontMetrics().stringWidth(SizeMode.EXTRA_LARGE.getName());

        if(!inizilised){

            // Initialising values after change of display size

            close = new Rectangle(getWidth()-22,2,20,20);
            minimise = new Rectangle(close.x-27,close.y,close.width,close.height);
            settings = new Rectangle(minimise.x-27,minimise.y,minimise.width,minimise.height);
            reload = new Rectangle(settings.x-27, settings.y, settings.width, settings.height);

            Font f = g.getFont();
            int localSettingsY = settingsY;
            showIconSwitch = new Rectangle(getWidth()-42,localSettingsY-20,40,20);
            localSettingsY += 22;
            darkModeSwitch = new Rectangle(getWidth()-42,localSettingsY-20,40,20);
            localSettingsY += 22;
            size20Font = new Font(g.getFont().getName(), Font.PLAIN, 20);
            g.setFont(size20Font);
            nextViweMode = new Rectangle(getWidth()-11,localSettingsY-17,10,20);
            backViweMode = new Rectangle(getWidth()-g.getFontMetrics().stringWidth(SizeMode.EXTRA_LARGE.getName())-22,localSettingsY-17,10,20);
            g.setFont(f);
            localSettingsY += 22;
            alwaysOnTopSwitch = new Rectangle(getWidth()-42,localSettingsY-20,40,20);
            localSettingsY += 22;
            cacheIconsSwitch = new Rectangle(getWidth()-42,localSettingsY-20,40,20);
            localSettingsY += 22;
            cacheClearButton = new Button(() -> {
                File[] folders = iconCatch.listFiles();
                if(folders == null) return;
                for(File folder : folders){
                    File[] icons = folder.listFiles();
                    if(icons == null) continue;
                    for(File icon : icons){
                        if(icon.exists()){
                            if(!icon.delete()){
                                System.out.println("Failed to delete the icon: "+icon.getPath());
                            }
                        }
                    }
                }
            }, getWidth()-42,localSettingsY-20,40,21, "clearCache");
            localSettingsY += 22;
            fadeIconSwitch = new Rectangle(getWidth()-42,localSettingsY-20,40,20);
            localSettingsY += 22;
            fadeStartTime = new TextBox(getWidth()-52,localSettingsY-20,50,20,"^[0-9]{0,4}$");
            fadeStartTime.setValue(fadeStart+"");
            localSettingsY += 22;
            fadeCompleteTime = new TextBox(getWidth()-52,localSettingsY-20,50,20,"^[0-9]{0,4}$");
            fadeCompleteTime.setValue(fadeComplete+"");

            inizilised = true;
        }

        BufferedImage icon = this.icon;

        JSONObject item = null;

        if(currentPlayingJson != null) {
            if(currentPlayingJson.has("item")){
                if(currentPlayingJson.get("item") != null){
                    if(currentPlayingJson.get("item") instanceof JSONObject obj){
                        item = obj;
                    }
                }
            }


            if (!online) {
                icon = offlineIcon;
            }
        }

        Color color = new Color(0,0,0);
        Color invert = Color.WHITE;

        if(icon != null && !darkMode){
            int x = switch (sizeMode){
                //case MINI -> 59;
                case LARGE, EXTRA_LARGE -> 315;
                default -> 145;
            };
            int y = switch (sizeMode){
                //case MINI -> 59;
                case LARGE, EXTRA_LARGE -> 315;
                default -> 145;
            };
            int width = switch (sizeMode){
                default -> 10;
            };
            color = average(icon,x,y,width,width);
            //invert = /*createReadableColor(color, Color.WHITE);*/invert(color);
            invert = color;
        }

        for(int i = 0; i < 0; i++){
            invert = invert.brighter();
        }

        if(icon != null && !settingMenu) {

            if(clock.time(0) > fadeStart && fadeIcon){
                int transparencyPercent = mapValue(clock.time(0), fadeStart, fadeComplete, 0, 100);
                icon = createTransparentImage(icon, transparencyPercent);
                Color darker = color.darker().darker();
            }else{
                g.setColor(color.darker().darker());
                if(sizeMode.isFillBackground()) {
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                g.setColor(invert);
            }

            if(sizeMode == SizeMode.NORMAL) {
                printNameAndPlaying(g, color, invert, getWidth() - (300 + (currentPlayingJson.getBoolean("is_playing") ? Icons.pause : Icons.play).length + 5),true);

                int totalTime = item.getInt("duration_ms");
                int v = (int) (((currentPlayingJson.getInt("progress_ms") + 0.0) / totalTime) * 200);
                Time t = new Time(currentPlayingJson.getInt("progress_ms"));
                Time total = new Time(item.getInt("duration_ms"));

                String sec = (t.getSec() > 9 ? t.getSec() + "" : "0" + t.getSec());
                int min = t.getMin();
                String totalSec = (total.getSec() > 9 ? total.getSec() + "" : "0" + total.getSec());
                int totalMin = total.getMin();
                //g2d.drawRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, 100, 10);
                //g2d.fillRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, v, 10);

                Stroke stroke = g.getStroke();

                g.setColor(new ColorUIResource(84, 84, 84));
                g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
                int width = g.getFontMetrics().stringWidth(min + ":" + sec);
                g.drawLine(width + 6, 29, width + 6 + 200, 29);

                g.setColor(invert);
                g.drawLine(width + 6, 29, width + 6 + v, 29);

                g.drawString(min + ":" + sec, 0, 33);
                g.drawString(totalMin + ":" + totalSec, g.getFontMetrics().stringWidth(min + ":" + sec) + 212, 33);


                ArrayList<String> artists = new ArrayList<>();

                for (Object o : currentPlayingJson.getJSONObject("item").getJSONArray("artists")) {
                    if (o instanceof JSONObject jo) {
                        artists.add(jo.getString("name"));
                    }
                }

                g.setFont(new Font(g.getFont().getName(), Font.PLAIN, 14));

                for (int i = 0; i < artists.size(); i++) {
                    g.drawString(artists.get(i), 2, 50 + (i * 14) + (i * 3));
                }

                if (icon != null && showIcon) {
                    g.drawImage(icon, getWidth() - 300, 0, null);
                }
            }
            else if(sizeMode == SizeMode.SMALL){

                if (icon != null && showIcon){
                    g.drawImage(icon, getWidth() - 300, 0, null);
                    g.setColor(new Color(0,0,0, 178));
                    g.fillRect(0,0,getWidth(),35);
                    g.setColor(invert);
                }


                printNameAndPlaying(g, color, invert, getWidth() - (((currentPlayingJson.getBoolean("is_playing") ? Icons.pause : Icons.play).length + 5)+100),false);

                int totalTime = item.getInt("duration_ms");
                int v = (int) (((currentPlayingJson.getInt("progress_ms") + 0.0) / totalTime) * 200);
                Time t = new Time(currentPlayingJson.getInt("progress_ms"));
                Time total = new Time(item.getInt("duration_ms"));

                String sec = (t.getSec() > 9 ? t.getSec() + "" : "0" + t.getSec());
                int min = t.getMin();
                String totalSec = (total.getSec() > 9 ? total.getSec() + "" : "0" + total.getSec());
                int totalMin = total.getMin();
                //g2d.drawRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, 100, 10);
                //g2d.fillRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, v, 10);

                Stroke stroke = g.getStroke();

                g.setColor(new ColorUIResource(84, 84, 84));
                g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
                int width = g.getFontMetrics().stringWidth(min + ":" + sec);
                g.drawLine(width + 6, 29, width + 6 + 200, 29);

                g.setColor(invert);
                g.drawLine(width + 6, 29, width + 6 + v, 29);

                g.drawString(min + ":" + sec, 0, 33);
                g.drawString(totalMin + ":" + totalSec, g.getFontMetrics().stringWidth(min + ":" + sec) + 212, 33);
            }
            else if(sizeMode == SizeMode.LARGE){
                if (icon != null && showIcon){
                    g.drawImage(icon, getWidth() - 640, 0, null);
                    g.setColor(new Color(0,0,0, 178));
                    g.fillRect(0,0,getWidth(),35);
                    g.setColor(invert);
                }

                printNameAndPlaying(g, color, invert, getWidth() - ((currentPlayingJson.getBoolean("is_playing") ? Icons.pause : Icons.play).length + 5)-76,false);

                int totalTime = item.getInt("duration_ms");
                int v = (int) (((currentPlayingJson.getInt("progress_ms") + 0.0) / totalTime) * 200);
                Time t = new Time(currentPlayingJson.getInt("progress_ms"));
                Time total = new Time(item.getInt("duration_ms"));

                String sec = (t.getSec() > 9 ? t.getSec() + "" : "0" + t.getSec());
                int min = t.getMin();
                String totalSec = (total.getSec() > 9 ? total.getSec() + "" : "0" + total.getSec());
                int totalMin = total.getMin();
                //g2d.drawRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, 100, 10);
                //g2d.fillRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, v, 10);

                Stroke stroke = g.getStroke();

                g.setColor(new ColorUIResource(84, 84, 84));
                g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
                int width = g.getFontMetrics().stringWidth(min + ":" + sec);
                g.drawLine(width + 6, 29, width + 6 + 200, 29);

                g.setColor(invert);
                g.drawLine(width + 6, 29, width + 6 + v, 29);

                g.drawString(min + ":" + sec, 0, 33);
                g.drawString(totalMin + ":" + totalSec, g.getFontMetrics().stringWidth(min + ":" + sec) + 212, 33);
            }
            else if(sizeMode == SizeMode.EXTRA_LARGE){
                printNameAndPlaying(g, color, invert, getWidth() - (300 + (currentPlayingJson.getBoolean("is_playing") ? Icons.pause : Icons.play).length + 5),true);

                int totalTime = item.getInt("duration_ms");
                int v = (int) (((currentPlayingJson.getInt("progress_ms") + 0.0) / totalTime) * 200);
                Time t = new Time(currentPlayingJson.getInt("progress_ms"));
                Time total = new Time(item.getInt("duration_ms"));

                String sec = (t.getSec() > 9 ? t.getSec() + "" : "0" + t.getSec());
                int min = t.getMin();
                String totalSec = (total.getSec() > 9 ? total.getSec() + "" : "0" + total.getSec());
                int totalMin = total.getMin();
                //g2d.drawRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, 100, 10);
                //g2d.fillRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, v, 10);

                Stroke stroke = g.getStroke();

                g.setColor(new ColorUIResource(84, 84, 84));
                g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
                int width = g.getFontMetrics().stringWidth(min + ":" + sec);
                g.drawLine(width + 6, 29, width + 6 + 200, 29);

                g.setColor(invert);
                g.drawLine(width + 6, 29, width + 6 + v, 29);

                g.drawString(min + ":" + sec, 0, 33);
                g.drawString(totalMin + ":" + totalSec, g.getFontMetrics().stringWidth(min + ":" + sec) + 212, 33);


                ArrayList<String> artists = new ArrayList<>();

                for (Object o : currentPlayingJson.getJSONObject("item").getJSONArray("artists")) {
                    if (o instanceof JSONObject jo) {
                        artists.add(jo.getString("name"));
                    }
                }

                g.setFont(new Font(g.getFont().getName(), Font.PLAIN, 14));

                for (int i = 0; i < artists.size(); i++) {
                    g.drawString(artists.get(i), 2, 50 + (i * 14) + (i * 3));
                }

                if (icon != null && showIcon){
                    g.drawImage(icon, getWidth() - 640, 0, null);
                }
            }
            /*else if(sizeMode == SizeMode.MINI){
                if (icon != null && showIcon){
                    g.drawImage(icon, getWidth() - 300, 0, null);
                    g.setColor(new Color(0,0,0, 178));
                    g.fillRect(0,0,getWidth(),35);
                    g.setColor(invert);
                }


                printNameAndPlaying(g, color, invert, getWidth() - ((currentPlayingJson.getBoolean("is_playing") ? Icons.pause : Icons.play).length + 5)-76,false);

                int totalTime = item.getInt("duration_ms");
                int v = (int) (((currentPlayingJson.getInt("progress_ms") + 0.0) / totalTime) * 200);
                Time t = new Time(currentPlayingJson.getInt("progress_ms"));
                Time total = new Time(item.getInt("duration_ms"));

                String sec = (t.getSec() > 9 ? t.getSec() + "" : "0" + t.getSec());
                int min = t.getMin();
                String totalSec = (total.getSec() > 9 ? total.getSec() + "" : "0" + total.getSec());
                int totalMin = total.getMin();
                //g2d.drawRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, 100, 10);
                //g2d.fillRect(g2d.getFontMetrics().stringWidth(min + ":" + sec) + 3, 24, v, 10);

                Stroke stroke = g.getStroke();

                g.setColor(new ColorUIResource(84, 84, 84));
                g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
                int width = g.getFontMetrics().stringWidth(min + ":" + sec);
                g.drawLine(width + 6, 29, width + 6 + 200, 29);

                g.setColor(invert);
                g.drawLine(width + 6, 29, width + 6 + v, 29);

                g.drawString(min + ":" + sec, 0, 33);
                g.drawString(totalMin + ":" + totalSec, g.getFontMetrics().stringWidth(min + ":" + sec) + 212, 33);
            }*/
        }
        else if(settingMenu){

            g.setColor(color.darker().darker());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(invert);

            if(currentPlayingJson != null){
                printNameAndPlaying(g, color, invert, getWidth(),true);
            }

            g.setFont(new Font(g.getFont().getName(), Font.PLAIN, 20));

            g.drawString("Show icon", 0,settingsY);
            g.drawImage(showIcon?onSetting:offSetting,null, showIconSwitch.x, showIconSwitch.y);

            settingsY+=22;

            g.drawString("Dark mode", 0,settingsY);
            g.drawImage(darkMode?onSetting:offSetting,null,darkModeSwitch.x, darkModeSwitch.y);

            settingsY+=22;

            g.drawString("Size mode",0,settingsY);

            drawScaleBitMap(g, next,getWidth()-21,settingsY-17,invert);

            int width = g.getFontMetrics().stringWidth(sizeMode.getName())/2;

//getWidth()-maxSizeModeWidth/2-width/2
            g.drawString(sizeMode.getName(), getWidth()-width-(maxSizeModeWidth/2)-28, settingsY);


            drawScaleBitMap(g, back, backViweMode.x, settingsY-17, invert);

            g.draw(nextViweMode);
            g.draw(backViweMode);

            settingsY+=22;

            g.drawString("Always on top",0,settingsY);
            g.drawImage(alwaysOnTop?onSetting:offSetting,null,alwaysOnTopSwitch.x, alwaysOnTopSwitch.y);

            settingsY+=22;

            g.drawString("Cache song icons",0,settingsY);
            g.drawImage(cacheIcons?onSetting:offSetting,null,cacheIconsSwitch.x, cacheIconsSwitch.y);

            settingsY+=22;

            g.drawString("Clear Cache",0,settingsY);
            cacheClearButton.draw(g, invert);

            settingsY += 22;

            g.drawString("Fade icon", 0, settingsY);
            g.drawImage(fadeIcon?onSetting:offSetting,null, fadeIconSwitch.x,fadeIconSwitch.y);

            settingsY += 22;

            g.drawString("Fade start time",0,settingsY);
            fadeStartTime.draw(g);

            settingsY += 22;

            g.drawString("Fade complete time",0,settingsY);
            fadeCompleteTime.draw(g);

        }
        else{
            g.setColor(Color.black);
            g.fillRect(0,0,getWidth(),90);
            g.setColor(Color.white);
            g.setFont(new Font(g.getFont().getName(),Font.PLAIN,20));
            g.drawString("Ether no song is currently playing", 0,50);
            g.drawString("or we are having trouble getting",0,70);
            g.drawString("the current song.",0,90);

        }

        Color c = invert;

        if(!showIcon){
            c = Color.WHITE;
        }


        g.setColor(c);

        //g2d.setColor(Color.white);
        g.setStroke(new BasicStroke(3));

        if(close == null) return;

        g.drawLine(close.x, close.y, (int) (close.x + close.getWidth()), (int) (close.y + close.getHeight()));
        g.drawLine((int) (close.x + close.getWidth()), close.y, close.x, (int) (close.y + close.getHeight()));

        //if(sizeMode == SizeMode.MINI)
        g.drawLine(minimise.x,minimise.y+minimise.height/2,minimise.width+minimise.x,minimise.y+minimise.height/2);
        drawScaleBitMap(g, SettingsIcon, settings.x, settings.y, c);

        if(online) drawScaleBitMap(g, reloadIcon, reload.x, reload.y, c);
    }

    public static BufferedImage createTransparentImage(BufferedImage image, int transparencyPercent) {
        if (transparencyPercent < 0 || transparencyPercent > 100) {
            throw new IllegalArgumentException("Transparency value must be between 0 and 100.");
        }

        int transparency = 255 * (100 - transparencyPercent) / 100; // convert percent to alpha value

        /*BufferedImage transparentImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = transparentImage.createGraphics();

        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < image.getHeight(); y++){
                Color c = new Color(image.getRGB(x,y));
                Color c1 = new Color(c.getRed(), c.getGreen(), c.getBlue(), transparency);
                g2d.setColor(c1);
                g2d.fillRect(x,y,1,1);
            }
        }*/

        BufferedImage transparentImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = transparentImage.createGraphics();
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) transparency / 255);
        g2d.setComposite(alphaComposite);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return transparentImage;
    }

    public static int mapValue(double value, double minInput, double maxInput, int minOutput, int maxOutput) {
        if (value < minInput) {
            value = minInput;
        }
        if(value > maxInput){
            value = maxInput;
        }

        double percentage = (value - minInput) / (maxInput - minInput);
        int mappedValue = (int) (percentage * (maxOutput - minOutput) + minOutput);

        return mappedValue;
    }

    private void printNameAndPlaying(Graphics2D g2d, Color color, Color invert, int maxWidth, boolean fillUnderIcon) {
        g2d.setFont(new Font(g2d.getFont().getName(), Font.PLAIN, 20));

        String name = currentPlayingJson.getJSONObject("item").getString("name").toUpperCase(Locale.ROOT);
        String spacing = " ".repeat(3);
        boolean[][] is_playings = currentPlayingJson.getBoolean("is_playing") ? Icons.pause : Icons.play;
        if (g2d.getFontMetrics().stringWidth(name) > maxWidth) {

            smartString(name + spacing, is_playings.length + 5 - point, 0,getWidth() - 105,is_playings.length + 5, g2d);
            //g2d.drawString(name + spacing, is_playings.length + 5 - point, 20);
            //System.out.println((g2d.getFontMetrics().stringWidth(name + spacing) + is_playings.length + 5) + "<" + (getWidth() - 80) + " = " + (g2d.getFontMetrics().stringWidth(name + spacing) + is_playings.length + 5 < getWidth() - 80));
            if (g2d.getFontMetrics().stringWidth(name + spacing) + is_playings.length + 5 > getWidth() - 105) {
                smartString(name + spacing, g2d.getFontMetrics().stringWidth(name.toUpperCase(Locale.ROOT) + spacing) - point + is_playings.length + 5, 0,getWidth() - 105,is_playings.length + 5, g2d);
                //System.out.println((g2d.getFontMetrics().stringWidth(name + spacing) - point + is_playings.length + 5));
                //g2d.drawString(name + spacing, g2d.getFontMetrics().stringWidth(name + spacing) - point + is_playings.length + 5, 20);
                //System.out.println(g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi?10:60))).stringWidth(name)-point);
            }

            point++;
            if (point > g2d.getFontMetrics().stringWidth(name + spacing)) {
                point -= g2d.getFontMetrics().stringWidth(name + spacing);
            }
        } else {
            smartString(name, is_playings.length + 5, 0,getWidth() - 105,is_playings.length + 5, g2d);
            //g2d.drawString(name, is_playings.length + 5, 20);
        }

        drawBitmap(g2d, is_playings, 3, 3);

        g2d.setFont(new Font(g2d.getFont().getName(), Font.PLAIN, 10));
    }
    private void newSize() {
        frame.setSize(sizeMode.getDim());
        //System.out.println(sizeMode.getName());
        inizilised = false;
    }
    public void smartString(String str, int x, int y, int maxWidth, int minX, Graphics2D g2d){
        int width = g2d.getFontMetrics().stringWidth(str);
        int height = g2d.getFont().getSize();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = img.createGraphics();

        g.setFont(g2d.getFont());

        g.setColor(Color.WHITE);

        //StringBuilder unicode = new StringBuilder();

        //for(int c : str.toCharArray()){
        //    unicode.append("\\u").append(String.format("%04x", c));
        //}

        g.drawString(/*unicode.toString()*/str, 0, height);

        Color mask = new Color(0, 0, 0,178);
        Color original = g2d.getColor();

        for(int x1 = x; x1 < width+x; x1++){
            for(int y1 = y; y1 < height+y; y1++){
                if(new Color(img.getRGB(x1-x, y1-y)).equals(Color.WHITE)) {
                    if(x1 > maxWidth) return;
                    if(x1 < minX) continue;
                    try {
                        if(showIcon) {
                            Color colorX = new Color(icon.getRGB(x1, y1));
                            float alpha = mask.getAlpha() / 255f;
                            int red = (int) (colorX.getRed() * alpha + mask.getRed() * (1 - alpha));
                            int green = (int) (colorX.getGreen() * alpha + mask.getGreen() * (1 - alpha));
                            int blue = (int) (colorX.getBlue() * alpha + mask.getBlue() * (1 - alpha));
                            Color result = new Color(red, green, blue);
                            if(isSimilar(result,original,0.03f)) {
                                g2d.setColor(result);
                                //System.out.println("Color: "+invert((colorX)));
                            }else{
                                g2d.setColor(invert(result));
                            }
                        }else{
                            //g2d.setColor(Color.WHITE);
                        }
                        g2d.fillRect(x1,y1,1,1);
                    }catch (ArrayIndexOutOfBoundsException ignored){}
                }
            }
        }
    }

    private static boolean isSimilar(Color colorX, Color colorY, float similarityThreshold) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
        float[] xyzX = cs.fromRGB(colorX.getColorComponents(null));
        float[] xyzY = cs.fromRGB(colorY.getColorComponents(null));
        float[] labX = cs.fromCIEXYZ(xyzX);
        float[] labY = cs.fromCIEXYZ(xyzY);

        float deltaE = calculateDeltaE(labX, labY);
        return deltaE <= similarityThreshold;
    }



    private static float calculateDeltaE(float[] labX, float[] labY) {
        float deltaL = labX[0] - labY[0];
        float deltaA = labX[1] - labY[1];
        float deltaB = labX[2] - labY[2];
        float c1 = (float) Math.sqrt(labX[1] * labX[1] + labX[2] * labX[2]);
        float c2 = (float) Math.sqrt(labY[1] * labY[1] + labY[2] * labY[2]);
        float deltaC = c1 - c2;
        float deltaH = deltaA * deltaA + deltaB * deltaB - deltaC * deltaC;
        deltaH = deltaH < 0 ? 0 : (float) Math.sqrt(deltaH);
        float sl = 1f;
        float kc = 1f;
        float kh = 1f;
        float f1 = deltaL / sl;
        float f2 = deltaC / (kc * c1);
        float f3 = deltaH / (kh * c1);
        float deltaE = (float) Math.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
        return deltaE;
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawFrame = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = drawFrame.createGraphics();

        //g2d.setColor(Color.black);
        //g2d.fillRect(0,0,getWidth(),getHeight());

        draw(g2d);

        g.drawImage(drawFrame,0,0,null);
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        clock.clockIn(0);
        if(close.contains(e.getPoint())){
            System.exit(0);
        }else if(minimise.contains(e.getPoint())){
            frame.setState(JFrame.ICONIFIED);
        }else if(settings.contains(e.getPoint())){
            settingMenu = !settingMenu;
            newSize();
        }else if(settingMenu) {
            if (showIconSwitch.contains(e.getPoint())) {
                showIcon = !showIcon;
            } else if (darkModeSwitch.contains(e.getPoint())) {
                darkMode = !darkMode;
            } else if (nextViweMode.contains(e.getPoint())) {
                selectedView++;
                if (selectedView > SizeMode.EXTRA_LARGE.getSize()) {
                    selectedView = SizeMode.SMALL.getSize();
                }
                sizeMode = SizeMode.getSizeMode(selectedView);

                try{
                    icon = getIcon();
                }catch (Exception e1){
                    throw new RuntimeException(e1);
                }
            } else if (backViweMode.contains(e.getPoint())) {
                selectedView = sizeMode.getSize() - 1;
                if (selectedView < SizeMode.SMALL.getSize()) {
                    selectedView = SizeMode.EXTRA_LARGE.getSize();
                }
                sizeMode = SizeMode.getSizeMode(selectedView);

                try{
                    icon = getIcon();
                }catch (Exception e1){
                    throw new RuntimeException(e1);
                }
            } else if (alwaysOnTopSwitch.contains(e.getPoint())){
                alwaysOnTop = !alwaysOnTop;
                frame.setAlwaysOnTop(alwaysOnTop);
            } else if (cacheIconsSwitch.contains(e.getPoint())){
                cacheIcons = !cacheIcons;
            } else if(fadeIconSwitch.contains(e.getPoint())){
                fadeIcon = !fadeIcon;
            } else if (fadeStartTime.boundBox.contains(e.getPoint())) {
                fadeStartTime.selected = true;
            } else if (fadeCompleteTime.boundBox.contains(e.getPoint())) {
                fadeCompleteTime.selected = true;
            }

            if (!fadeStartTime.boundBox.contains(e.getPoint())) {
                fadeStartTime.selected = false;
                fadeStart = Integer.parseInt(fadeStartTime.getValue());
            }
            if (!fadeCompleteTime.boundBox.contains(e.getPoint())) {
                fadeCompleteTime.selected = false;
                fadeComplete = Integer.parseInt(fadeCompleteTime.getValue());
            }

            cacheClearButton.press(e.getPoint());
        }else if(reload.contains(e.getPoint()) && online){
            puase = true;
            Time time = new Time();
            try {
                AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                        .build();
                AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
                // Set access and refresh token for further "spotifyApi" object usage
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                endTime = System.currentTimeMillis() + time.getTimeInMilliseconds();
                //valid = authorizationCodeCredentials.getExpiresIn();
                currentPlayingJson = new JSONObject(item.getJson());
                if (!oldSong.equals(currentPlayingJson.getJSONObject("item").getString("uri"))) {
                    oldSong = currentPlayingJson.getJSONObject("item").getString("uri");
                    newSong();
                }
                puase = false;
                synchronized (selected) {
                    selected.notifyAll();
                }
            } catch (Exception e1) {
                System.out.println("Error: " + e1.getMessage()+"\nrow: 862");
            }
        }
    }
    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getPoint().y < 20 && !close.contains(e.getPoint()) && !minimise.contains(e.getPoint()) && !settings.contains(e.getPoint()) && !(reload.contains(e.getPoint()) && online)){
            if(mapValue(clock.time(0), fadeStart, fadeComplete, 0, 100) != 100) {
                frameMove = true;
            }
        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if(frameMove){
            frameMove = false;
        }
        cacheClearButton.release(e.getPoint());
    }
    @Override
    public void mouseEntered(MouseEvent e) {

    }
    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        fadeStartTime.type(e.getKeyCode());
        fadeCompleteTime.type(e.getKeyCode());
    }
}
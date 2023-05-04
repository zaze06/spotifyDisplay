package me.zacharias.spotify.display;

import java.awt.*;
import java.awt.image.BufferedImage;

public enum SizeMode {
    SMALL       (0,"Small",300,300, 1, false),
    NORMAL      (1,"Normal",600,300, 1, true),
    LARGE       (2,"Large",640,640, 0, false),
    EXTRA_LARGE (3,"Extra large",940,640, 0, true),
    CUSTOM      (4,"Custom",0,0, 0, false);

    private final int size;
    private final String name;
    private final Dimension dim;
    private final int iconSize;
    private final String iconSizeName;
    private final boolean fillBackground;
    SizeMode(int i, String name, int width, int height, int iconSize) {
        this(i, name, width, height, iconSize, false);
    }

    SizeMode(int i, String name, int width, int height, int iconSize, boolean fillBackground){
        size = i;
        this.name = name;
        this.dim = new Dimension(width, height);
        this.iconSize = iconSize;
        iconSizeName = switch (iconSize){
            case 0 -> "large";
            case 1 -> "medium";
            case 2 -> "small";
            default -> null;
        };
        this.fillBackground = fillBackground;
    }




    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public static SizeMode getSizeMode(int i){
        return switch (i){
            case 0 -> SMALL;
            case 2 -> LARGE;
            case 3 -> EXTRA_LARGE;
            case 4 -> CUSTOM;
            default -> NORMAL;
        };
    }

    public Dimension getDim() {
        return dim;
    }

    public int getIconSize() {
        return iconSize;
    }

    public String getIconSizeName() {
        return iconSizeName;
    }

    public boolean isFillBackground() {
        return fillBackground;
    }

    public BufferedImage changeIcon(BufferedImage read) {
        return read;
    }
}

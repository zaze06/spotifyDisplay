package me.zacharias.spotify.display;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;

public class LoadImage {
    public static boolean[][] getBitmapFromResources(String image){
        try {
            DataInputStream in = new DataInputStream(LoadImage.class.getResourceAsStream("/" + image));
            int width = in.read();
            int height = in.read();
            boolean[][] img = new boolean[width][height];
            int x = 0;
            int y = 0;
            for (int i = 0; i < width * height; i++) {
                img[x][y] = in.read() == 1;
                x++;
                if(x > width-1){
                    y++;
                    x=0;
                }
            }
            return img;
        }catch (Exception e){
            return new boolean[][]{
                    {true, false, true},
                    {false, true, false},
                    {true, false, true},
            };
        }
    }

    public static int[][] getGrayScaleImageFromResources(String image){
        try{
            DataInputStream in = new DataInputStream(LoadImage.class.getResourceAsStream("/"+image));
            int width = in.read();
            int height = in.read();
            int[][] img = new int[width][height];
            int x = 0, y = 0;
            for(int i = 0; i < width*height; i++){
                img[x][y] = in.read();
                x++;
                if(x > width-1){
                    x = 0;
                    y++;
                }
            }
            return img;
        }catch (Exception e){
            return new int[][]{
                    {255, 0, 255},
                    {0, 255, 0},
                    {255, 0, 255},
            };
        }
    }

    public static BufferedImage getImageFromResources(String image){
        try{
            return ImageIO.read(LoadImage.class.getResourceAsStream("/"+image));
        }catch (Exception e){
            BufferedImage bufferedImage = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);

            bufferedImage.setRGB(0,0, Color.BLACK.getRGB());
            bufferedImage.setRGB(0,0, Color.WHITE.getRGB());
            bufferedImage.setRGB(0,0, Color.BLACK.getRGB());

            bufferedImage.setRGB(0,0, Color.WHITE.getRGB());
            bufferedImage.setRGB(0,0, Color.BLACK.getRGB());
            bufferedImage.setRGB(0,0, Color.WHITE.getRGB());

            bufferedImage.setRGB(0,0, Color.BLACK.getRGB());
            bufferedImage.setRGB(0,0, Color.WHITE.getRGB());
            bufferedImage.setRGB(0,0, Color.BLACK.getRGB());

            return bufferedImage;
        }
    }
}

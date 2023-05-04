package me.zacharias.spotify.display;

import java.awt.*;

public class PaintUtil {
    public static void drawScaleBitMap(Graphics2D g2d, int[][] scaleMap, int offsetX, int offsetY, Color saturatedColor){
        Color c = g2d.getColor();
        for(int x = 0; x < scaleMap.length; x++){
            for(int y = 0; y < scaleMap[x].length; y++){
                try {
                    if (scaleMap[x][y] != 0) {
                        g2d.setColor(getGrayscaleColor(scaleMap[x][y], saturatedColor));
                        g2d.fillRect(x + offsetX, y + offsetY, 1, 1);
                    }
                }
                catch(Exception e)
                {
                    System.out.println("scaleMap.length: "+scaleMap.length+" scalMap[y].length: "+scaleMap[y].length);
                    throw e;
                }
            }
        }
        g2d.setColor(c);
    }

    public static void drawBitmap(Graphics2D g2d, boolean[][] bitmap, int offsetX, int offsetY){
        for(int y = 0; y < bitmap.length; y++){
            for(int x = 0; x < bitmap[y].length; x++){
                if(bitmap[x][y]) {
                    g2d.fillRect(x + offsetX, y+offsetY, 1, 1);
                }
            }
        }
    }

    public static Color createReadableColor(Color backgroundColor, Color textColor) {
        // Adjust the brightness of the text color to be different from the background color
        float[] hsb = Color.RGBtoHSB(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), null);

        // Adjust the saturation of the text color to be different from the background color
        float saturation = hsb[1];
        float backgroundSaturation = Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), null)[1];

        // If the background is very vibrant, make the text color more muted.
        if (backgroundSaturation > 0.8) {
            saturation -= 0.3;
        }
        // If the background is very muted, make the text color more vibrant.
        else if (backgroundSaturation < 0.2) {
            saturation += 0.3;
        }
        // Make sure the saturation is still within the valid range (0.0-1.0)
        saturation = Math.min(saturation, 1.0f);
        saturation = Math.max(saturation, 0.0f);

        // Adjust the saturation of the text color
        textColor = Color.getHSBColor(hsb[0], saturation, hsb[2]);

        // Return the modified text color
        return textColor;
    }

    public static Color getGrayscaleColor(int alphaValue, Color color) {
        // Calculate the grayscale color between transparent and the input color
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                alphaValue
        );
    }
}

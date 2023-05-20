package me.zacharias.spotify.display;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Button {
    Rectangle buttonBox;
    PressAction pressAction;
    boolean pressed = false;
    int[][] pressedIcon;
    int[][] depressedIcon;

    public Button(PressAction pressAction, int x, int y, int width, int height) {
        this.pressAction = pressAction;
        buttonBox = new Rectangle(x,y,width,height);
    }

    public Button(PressAction pressAction, int x, int y, int width, int height, String iconName){
        this(pressAction,x,y,width,height);
        pressedIcon = LoadImage.getGrayScaleImageFromResources(iconName+"On.bin");
        depressedIcon = LoadImage.getGrayScaleImageFromResources(iconName+"Off.bin");
    }

    public void addHoverEventHandler(){

    }

    public void press(Point p){
        if(buttonBox.contains(p)){
            pressed = true;
            pressAction.onPress();
        }
    }

    public void release(Point p){
        if(buttonBox.contains(p)){
            pressed = true;
            pressAction.onRelease();
        }
    }

    public void draw(Graphics2D g, Color c){
        if(pressed){
            if(pressedIcon != null){
                PaintUtil.drawScaleBitMap(g,pressedIcon, buttonBox.x, buttonBox.y, c);
            }
        }else if(depressedIcon != null){
            PaintUtil.drawScaleBitMap(g,depressedIcon, buttonBox.x, buttonBox.y, c);
        }
    }


}

package me.zacharias.spotify.display;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class TextBox {
    Rectangle boundBox;
    String value = "";
    String regex;
    int pointer;
    boolean selected = false;
    public TextBox(int x, int y, int width, int height, String regex){
        boundBox = new Rectangle(x, y, width, height);
        this.regex = regex;
    }

    public TextBox(int x, int y, int width, int height){
        this(x, y, width, height, "");
    }

    public void type(int keyCode){
        if(!selected) return;
        if(keyCode == KeyEvent.VK_BACK_SPACE){
            if(pointer == 0) return;
            if(value.length()==0) return;
            ArrayList<Character> characters = new ArrayList<>();
            char[] chars = value.toCharArray();
            for(char c : chars){
                characters.add(c);
            }

            characters.remove(pointer-1);

            StringBuilder builder = new StringBuilder();
            for(char c : characters){
                builder.append(c);
            }

            value = builder.toString();
            pointer--;
        }else if(keyCode == KeyEvent.VK_ENTER){

        }else if(keyCode == KeyEvent.VK_LEFT){
            if(pointer-1 < 0) return;
            pointer--;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            if(pointer+1 > value.length()) return;
            pointer++;
        } else if (keyCode == KeyEvent.VK_UP) {
            pointer = 0;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            pointer = value.length();
        }else{
            ArrayList<Character> characters = new ArrayList<>();
            char[] chars = value.toCharArray();
            for(char c : chars){
                characters.add(c);
            }

            characters.add(pointer, (char) keyCode);

            StringBuilder builder = new StringBuilder();
            for(char c : characters){
                builder.append(c);
            }

            if(builder.toString().matches(regex)){
                value = builder.toString();
                pointer++;
            }
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void draw(Graphics2D g) {
        Color org = g.getColor();
        //g.setColor(org.darker().darker());
        g.fill3DRect(boundBox.x, boundBox.y, boundBox.width, boundBox.height, false);

        ArrayList<Character> characters = new ArrayList<>();
        char[] chars = value.toCharArray();
        for(char c : chars){
            characters.add(c);
        }

        if(selected){
            characters.add(pointer,'|');
        }

        StringBuilder builder = new StringBuilder();
        for(char c : characters){
            builder.append(c);
        }

        g.drawString(builder.toString(), boundBox.x,boundBox.y+boundBox.height-2);
    }
}

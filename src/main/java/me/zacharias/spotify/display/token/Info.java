package me.zacharias.spotify.display.token;

import javax.swing.*;
import java.awt.*;

public class Info extends JPanel {
    String message;
    String title;
    JFrame frame;

    public Info(String message, String title){
        this.message = message;
        this.title = title;

        frame = new JFrame(title);
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400,200);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawString(message, 0,10);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

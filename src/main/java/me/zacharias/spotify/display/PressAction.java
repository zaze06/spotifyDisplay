package me.zacharias.spotify.display;

public interface PressAction {
    void onPress();
    default void onRelease(){}
}

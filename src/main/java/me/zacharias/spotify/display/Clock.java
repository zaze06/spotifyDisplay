package me.zacharias.spotify.display;

import java.util.HashMap;
import java.util.Map;

public class Clock {
    Map<Integer, Long> map = new HashMap<>();

    public void clockIn(int id){
        map.put(id, System.currentTimeMillis());
    }

    public double time(int id){
        return new Time(System.currentTimeMillis()-map.get(id)).getTimeInSeconds();
    }
}

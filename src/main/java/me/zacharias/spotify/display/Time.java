package me.zacharias.spotify.display;

public class Time {
    long ms;
    int sec, min, hr;
    public Time(long ms){
        while(ms >= 1000){
            sec++;
            ms-=1000;
        }
        this.ms = ms;

        while (sec >= 60){
            min++;
            sec-=60;
        }

        while (min >= 60){
            hr++;
            min-=60;
        }
    }

    public Time(){
        ms = 0;
        sec = 0;
        min = 0;
        hr = 0;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMs(long ms) {
        this.ms = ms;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public double getTimeInSeconds() {
        return (double)(ms)/1000 + sec + (min*60) + (hr*60*60);
    }

    public long getTimeInMilliseconds() {
        return ms + (sec * 1000L) + ((long) min * 60 * 1000) + ((long) hr * 60 * 60 * 1000);
    }

    public int getTimeInMinutes() {
        return (int)Math.floor(getTimeInSeconds() / 60);
    }

    public int getTimeInHours() {
        return (int)Math.floor(getTimeInSeconds() / (60 * 60));
    }

    public int getHr() {
        return hr;
    }

    public int getMin() {
        return min;
    }

    public int getSec() {
        return sec;
    }

    public long getMs() {
        return ms;
    }
}

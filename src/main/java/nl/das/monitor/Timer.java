package nl.das.monitor;

  public class Timer {
    private String device;
    private int hour_off;
    private int hour_on;
    private int index;
    private int minute_off;
    private int minute_on;
    private int period;
    private int repeat;

    public Timer() { }
    
    public String getDevice() {
      return device;
    }
    public void setDevice(String device) {
      this.device = device;
    }
    public int getHour_off() {
      return hour_off;
    }
    public void setHour_off(int hour_off) {
      this.hour_off = hour_off;
    }
    public int getHour_on() {
      return hour_on;
    }
    public void setHour_on(int hour_on) {
      this.hour_on = hour_on;
    }
    public int getIndex() {
      return index;
    }
    public void setIndex(int index) {
      this.index = index;
    }
    public int getMinute_off() {
      return minute_off;
    }
    public void setMinute_off(int minute_off) {
      this.minute_off = minute_off;
    }
    public int getMinute_on() {
      return minute_on;
    }
    public void setMinute_on(int minute_on) {
      this.minute_on = minute_on;
    }
    public int getPeriod() {
      return period;
    }
    public void setPeriod(int period) {
      this.period = period;
    }
    public int getRepeat() {
      return repeat;
    }
    public void setRepeat(int repeat) {
      this.repeat = repeat;
    }

    
  }

package bwg;


import java.util.ArrayList;

public class Call {
    private int alltime = 0;
    private Number callerID;
    private ArrayList<Number> destination = new ArrayList<>();


    public Call(Number callerID){
        this.callerID = callerID;
    }



    public void addDestination(Number num) {
        this.alltime += parseTime(num.getTime());
        this.destination.add(num);
    }

    private int parseTime(String time){
        String[] util = time.split(":");
        int t = Integer.parseInt( util[1] );
        t += Integer.parseInt( util[0] ) * 60;
        return t;
    }

    public Number getCallerID() {
        return callerID;
    }

    public int getAlltimeInteger(){
        return alltime;
    }

    public String getAlltime() {

        return String.valueOf(this.alltime/60) + ":" + String.valueOf(this.alltime % 60);
    }

    public Number getDestination(int index) {
        return destination.get(index);
    }

    public ArrayList<Number> getDestinationList() {
        return destination;
    }

}

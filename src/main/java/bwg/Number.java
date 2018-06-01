package bwg;

public class Number {
    private String time;

    private String number;
    private String callDate;

    public Number(String num, String data, String time){
        this.number = num;
        this.time = time;
        this.callDate = data;
    }

    public String getCallDate() {
        return callDate;
    }

    @Override
    public String toString() {

        return number;
    }

    public String getTime() {
        return time;
    }

    public void setCallDate(String callDate) {
        this.callDate = callDate;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}

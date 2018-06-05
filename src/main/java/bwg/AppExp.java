package bwg;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppExp {
    private static String FILE_NAME_IN = ".\\data2.xlsx";
    //private static final String FILE_NAME_OUT = ".\\file-out.xlsx";
    private static boolean DETAILS = true;
    private static String CALLER_MASC_SCAN = "";
    private static String DESTINATION_MASC_SCAN = "";
    private static int ACTION = 0;
    private static String SORT = "time";

    private boolean DESTROY = false;
    private Thread pauseThread = null;

    private ArrayList<Call> calls = new ArrayList<>();
    private String periodT1 = "", periodT2 = "";

    private TextArea printOut = null;
    private javafx.scene.control.Button btscan = null;

    public AppExp(int action,
                  TextArea textArea,
                  javafx.scene.control.Button bt,
                  String inputFile,
                  boolean detail,
                  String callermask,
                  String destonationMask,
                  String sort){
        this.printOut = textArea;
        this.btscan = bt;

        calls = new ArrayList<>();

        ACTION = action;
        SORT = sort;
        DETAILS = detail;
        CALLER_MASC_SCAN = callermask;
        DESTINATION_MASC_SCAN = destonationMask;
        FILE_NAME_IN = inputFile;

        Core();
    }

    public static void main(String[] args) {
        new AppExp(
                0,
                null,
                null,
                FILE_NAME_IN,
                true,
                "",
                "",
                "time");
    }

    private void Core(){
        pauseThread = new Thread(() -> {
            print("scan [" + FILE_NAME_IN + "]\n");
            while (!DESTROY) {
                try {
                    print(".");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        pauseThread.start();

        File dataFile = new File(FILE_NAME_IN);
        FileInputStream fis;
        try {
            fis = new FileInputStream(dataFile);
        } catch (FileNotFoundException e) {
            print("\n File not found!");
            destroy();
            return;
        }

        XSSFWorkbook workBook;
        try {
            workBook = new XSSFWorkbook(fis);
        } catch (Exception e) {
            print("\n Numbers not found!");
            destroy();
            return;
        }
        XSSFSheet sheet = workBook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();

        //int iterator = 0;
        while (rowIterator.hasNext() && !DESTROY) {
            if (DESTROY){
                break;
            }

            Row row = rowIterator.next();

            Cell callCell = row.getCell(3);
            Cell destinationCell = row.getCell(7);
            Cell datacallCell = row.getCell(0);
            Cell calltimeCell = row.getCell(9);

            String callid = cellTypeToString(callCell);
            String destination = cellTypeToString(destinationCell);
            String datacall = cellTypeToString(datacallCell);
            String calltime = cellTypeToString(calltimeCell);

            if (Objects.equals(periodT1, "") || Objects.equals(periodT1, "Call Date"))
                periodT1 = datacall;
            periodT2 = datacall;
            // ACTIONs
            switch (ACTION){

                // search by mask
                case 1: {
                    if (!Objects.equals(CALLER_MASC_SCAN, "") && !maskNumberInline(CALLER_MASC_SCAN, callid))
                        continue;
                    if (!Objects.equals(DESTINATION_MASC_SCAN, "") && !maskNumberInline(DESTINATION_MASC_SCAN, destination))
                        continue;

                    scan(callid, destination, datacall, calltime);

                    break;

                }
                //time > 15 min
                case 2: {
                    try {
                        if (Integer.parseInt(calltime.split(":")[0]) < 15)
                            continue;

                        scan(callid, destination, datacall, calltime);
                    } catch (NumberFormatException ignored) {}
                    break;
                }
                //total time > 450 min
                case 3: {
                    scan(callid, destination, datacall, calltime);
                    break;
                }
                //default search
                default: {
                    if (!localNumberInline(callid))
                        continue;
                    if (!numberInline(destination))
                        continue;

                    scan(callid, destination, datacall, calltime);

                    break;
                }
            }
        }

        pauseThread.stop();

        sort(SORT);

        printAll(DETAILS);

        destroy();
    }

    /**
     * Out text;
     * @param text out string
     */
    private void print(String text){
        if(printOut != null)
            Platform.runLater(() -> printOut.appendText(text));

        System.out.print(text);
    }
    public void printAll(boolean details){
        print("\nPeriod: " + periodT1 + " - " + periodT2);

        print("\n");

        int i = 1;
        for(Call call : calls) {
            print("\n\n");
            int ii = 1;
            print(i + ") " + call.getCallerID().toString() +
                    "\n      calls: "  +  call.getDestinationList().size() +
                    "\n      time: "   +  call.getAlltime()
            );
            if (details)
                for (Number n : call.getDestinationList()){
                    print("\n      " + ii + ") " + n.toString() + " - " + n.getTime() + " - " + n.getCallDate());
                    ii++;
                }

            i++;
        }

        print("\nTotal: " + calls.size());
        print("\n");
    }

    public void sort(String param){
        if (Objects.equals(param, "time")){
            calls.sort((o1, o2) -> o2.getAlltimeInteger() - o1.getAlltimeInteger());
        } else if (Objects.equals(param, "counts")){
            calls.sort((o1, o2) -> o2.getDestinationList().size() - o1.getDestinationList().size());
        }
    }

    private void scan(String callid, String destination, String datacall, String calltime){
        boolean numberTrue = true;
        for (Call c : calls) {
            if (Objects.equals(c.getCallerID().toString(), callid)) {
                calls.get(calls.indexOf(c)).addDestination(new Number(destination, datacall, calltime));

                numberTrue  = false;
                break;
            }
        }
        if (numberTrue) {
            Call call = new Call(new Number(callid, null, "0:0"));
            call.addDestination(new Number(destination, datacall, calltime));
            calls.add(call);
        }
    }

    public void print(TextField tf, String text){
        tf.setText(text);
    }

    /**
     * Convert cell to string
     * @param cell String
     * @return cell format
     */
    private String cellTypeToString( Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }






    /** * Regex's */
    private boolean numberInline(String testString){
        Pattern p = Pattern.compile("^218[6-9][\\d][\\d]$");
        Matcher m = p.matcher(testString);
        return m.matches();
    }
    private boolean localNumberInline(String number){
        Pattern p = Pattern.compile("<[\\d][\\d][\\d]>");
        Matcher m = p.matcher(number);
        return m.find();
    }

    private boolean maskNumberInline(String mask, String number){
        Pattern p = Pattern.compile(mask);
        Matcher m = p.matcher(number);
        return m.find();
    }


    public void destroy(){
        DESTROY = true;
        pauseThread.stop();
        if(btscan != null)
            btscan.setDisable(false);
    }
}

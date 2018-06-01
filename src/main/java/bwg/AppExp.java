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
    private static String MASC_SCAN = "";
    private static String SORT = "time";


    private ArrayList<Call> calls = new ArrayList<>();
    private String periodT1 = "", periodT2 = "";

    private TextArea printOut = null;

    public AppExp(TextArea textArea, String inputFile, boolean detail, String mask, String sort){
        this.printOut = textArea;

        calls = new ArrayList<>();

        SORT = sort;
        DETAILS = detail;
        MASC_SCAN = mask;
        FILE_NAME_IN = inputFile;
        Core();
    }

    public static void main(String[] args) {
        new AppExp(null, FILE_NAME_IN, true, "", "time");
    }

    private void Core(){
        Thread pauseThread = new Thread(() -> {
            print("scan [" + FILE_NAME_IN + "]\n");
            while (true) {
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
            //e.printStackTrace();
            print("\n File not found!");
            pauseThread.stop();
            return;
        }

        XSSFWorkbook workBook;
        try {
            workBook = new XSSFWorkbook(fis);
        } catch (Exception e) {
            print("\n Numbers not found!");
            //e.printStackTrace();
            pauseThread.stop();
            return;
        }
        XSSFSheet sheet = workBook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();

        //int iterator = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Cell callCell = row.getCell(3);
            Cell destinationCell = row.getCell(7);
            Cell datacallCell = row.getCell(0);
            Cell calltimeCell = row.getCell(9);

            String callid = cellTypeToString(callCell);
            String destination = cellTypeToString(destinationCell);
            String datacall = cellTypeToString(datacallCell);
            String calltime = cellTypeToString(calltimeCell);

            if (Objects.equals(periodT1, ""))
                periodT1 = datacall;
            periodT2 = datacall;

            //if mack true
            if(Objects.equals(MASC_SCAN, "")) {
                if (!localNumberInline(callid)) {
                    continue;
                }
                boolean numberTrue = true;
                if (numberInline(destination)) {
                    for (Call c : calls) {
                        if (Objects.equals(c.getCallerID().toString(), callid)) {
                            calls.get(calls.indexOf(c)).addDestination(new Number(destination, datacall, calltime));

                            numberTrue = false;
                            break;
                        }
                    }
                    if (numberTrue) {
                        Call call = new Call(new Number(callid, null, "0:0"));
                        call.addDestination(new Number(destination, datacall, calltime));
                        calls.add(call);
                    }
                }
            } else {
                if (Objects.equals(periodT1, ""))
                    periodT1 = datacall;
                periodT2 = datacall;

                if (!maskNumberInline(MASC_SCAN, callid)) {
                    continue;
                }
                boolean numberTrue = true;
                for (Call c : calls) {
                    if (Objects.equals(c.getCallerID().toString(), callid)) {
                        calls.get(calls.indexOf(c)).addDestination(new Number(destination, datacall, calltime));

                        numberTrue = false;
                        break;
                    }
                }
                if (numberTrue) {
                    Call call = new Call(new Number(callid, null, "0:0"));
                    call.addDestination(new Number(destination, datacall, calltime));
                    calls.add(call);
                }
            }
        }

        pauseThread.stop();

        sort(SORT);

        printAll(DETAILS);


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

    public void print(TextField tf, String text){
        tf.setText(text);
    }

    /**
     * Convert cell to string
     * @param cell
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
        Pattern p = Pattern.compile("<"+mask+">");
        Matcher m = p.matcher(number);
        return m.find();
    }
}

package bwg.gui;

import bwg.AppExp;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

public class Controller {
    private String filepath = "";
    private AppExp core = null;
    private Thread main = null;
    private String actionName = "default";

    @FXML
    private Button onScanB;
    @FXML
    private Pane mask_pane;
    @FXML
    private TextField destination_mask;
    @FXML
    private ChoiceBox selectActionCB;
    @FXML
    private CheckBox detailCheckBox;
    @FXML
    private ChoiceBox sortSelectCB;
    @FXML
    private TextField callerID_mask;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    public TextField guiFilePath;
    @FXML
    public TextArea textArea;

    @FXML
    private void initialize(){
        sortSelectCB.setOnAction(event -> {
            if (core != null) {
                textArea.clear();
                core.sort(sortSelectCB.getValue().toString());
                core.printAll(detailCheckBox.isSelected());
            }
        });
        detailCheckBox.setOnAction(event -> {
            if (core != null) {
                textArea.clear();
                actionCode();
                core.printAll(detailCheckBox.isSelected());
            }
        });

        selectActionCB.setOnAction(event -> {
            mask_pane.setVisible(false);
            actionName = selectActionCB.getValue().toString();

        });
    }

    @FXML
    private void openDialog(){
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Resource File");
            filepath = fileChooser.showOpenDialog(anchorPane.getScene().getWindow()).getAbsolutePath();

            guiFilePath.setText(filepath);
        } catch (NullPointerException ignore){

        }
    }

    @FXML
    private void onScan(){
        onScanB.setDisable(true);

        try {
            textArea.clear();

            if(core != null)
                core.destroy();

            main = new Thread(() ->
                    core = new AppExp(
                    actionCode(),
                    textArea,
                    onScanB,
                    filepath,
                    detailCheckBox.isSelected(),
                    callerID_mask.getText(),
                    destination_mask.getText(),
                    sortSelectCB.getValue().toString() ));
            main.start();
        } catch (NullPointerException ignore){}
    }


    private int actionCode(){
        switch (actionName){
            case "default": {
                textArea.appendText("Action: \"default\"\n");
                return 0;
            }
            case "search by mask": {
                textArea.appendText("Action: \"search by mask\"\n");
                mask_pane.setVisible(true);
                return 1;
            }
            case "time > 15min": {
                textArea.appendText("Action: \"time > 15min\"\n");
                return 2;
            }
            case "total time > 450min": {
                return 3;
            }
            case "5min > 10 counts": {
                return 4;
            }
                /*
                <String fx:value="total time &gt; 450min" />
                <String fx:value="5min &gt; 10 counts" />
                */
            default:
                return 0;
        }
        //textArea.appendText(String.valueOf(action));
    }
}

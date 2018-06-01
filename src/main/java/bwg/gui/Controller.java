package bwg.gui;

import bwg.AppExp;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class Controller {
    private String filepath = "";
    private AppExp core;

    @FXML
    private CheckBox detailCheckBox;
    @FXML
    private ChoiceBox sortSelectCB;
    @FXML
    private TextField maskTextField;
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
            textArea.clear();
            core.printAll(detailCheckBox.isSelected());
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

        try {
            textArea.clear();
            new Thread(() ->
                    core = new AppExp(
                    textArea,
                    filepath,
                    detailCheckBox.isSelected(),
                    maskTextField.getText(),
                    sortSelectCB.getValue().toString() )).start();
        } catch (NullPointerException ignore){}
    }

}

package sample.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.formatUtils.bd3Utils;


import java.util.ArrayList;
import java.util.List;

public class bd3WindowController {

    @FXML
    private ChoiceBox<String> funcBox;

    @FXML
    private Button convertBtn;

    @FXML
    private Slider timeLine;

    @FXML
    private CheckBox allSubdomainBox;

    @FXML
    private ProgressIndicator progressBar;

    @FXML
    private ChoiceBox<Integer> subdomainChoiceBox;

    @FXML
    void initialize() {
        ObservableList<String> funcList = FXCollections.observableArrayList(bd3Utils.getFuncNames());
        funcBox.setItems(funcList);

        convertBtn.setOnAction(event -> convert());
        
        List<Integer> listSubdomain = new ArrayList<>();
        for (int i = 0; i < bd3Utils.getSubdomainCount(); i++) {
            listSubdomain.add(i);
        }
        progressBar.setVisible(false);


        subdomainChoiceBox.setItems(FXCollections.observableArrayList(listSubdomain));
        subdomainChoiceBox.setValue(0);

        timeLine.setSnapToTicks(true);
        timeLine.setMajorTickUnit(bd3Utils.getInterval());
        timeLine.setMinorTickCount(0);
        timeLine.setMax(bd3Utils.getInterval() * bd3Utils.getTimeCount());
    }

    private void convert() {

        int subdomainNumber = -1;
        if (!allSubdomainBox.isSelected()) {
            subdomainNumber = subdomainChoiceBox.getValue();
        }
        //bd3Utils.convert(subdomainNumber, funcBox.getValue(), (int)(timeLine.getValue() / bd3Utils.getInterval()));

        progressBar.setVisible(true);
        //bd3Utils.convert(subdomainNumber, funcBox.getValue(), (int)(timeLine.getValue() / bd3Utils.getInterval()));


        int finalSubdomainNumber = subdomainNumber;
        Service process = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        bd3Utils.convert(finalSubdomainNumber, funcBox.getValue(), (int)(timeLine.getValue() / bd3Utils.getInterval()));
                        return null;
                    }
                };
            }
        };

        process.setOnSucceeded( e -> {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Button btnOk = new Button("Ok.");
            btnOk.setOnAction(event -> {dialogStage.close();});

            VBox vbox = new VBox(new Text("Готово!"), btnOk);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(15));

            dialogStage.setScene(new Scene(vbox));
            dialogStage.show();
            progressBar.setVisible(false);

        });

        process.setOnFailed( e -> {
            progressBar.setVisible(false);
        });

        process.start();


    }

    class ThreadForConvert implements Runnable {

        int subdomainNumber;

        public ThreadForConvert(int subdomainNumber) {
            this.subdomainNumber = subdomainNumber;
        }

        @Override
        public void run(){
            bd3Utils.convert(subdomainNumber, funcBox.getValue(), (int)(timeLine.getValue() / bd3Utils.getInterval()));

        }
    }



}

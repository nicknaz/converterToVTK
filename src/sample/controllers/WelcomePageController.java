package sample.controllers;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.formatUtils.azhureUtils;
import sample.formatUtils.bd3Utils;

import java.io.File;

public class WelcomePageController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    private File setkaFile;
    private boolean possibleToContinue = false;

    @FXML
    private Button chooseFileBtn;

    @FXML
    private Button continueBtn;

    @FXML
    private Label descriptionText;

    @FXML
    private Label filePath;

    @FXML
    private ProgressIndicator progressBar;

    @FXML
    void initialize() {
        chooseFileBtn.setOnAction(event -> chooseFile());
        continueBtn.setOnAction(event -> nextPage(event));
        progressBar.setVisible(false);

    }

    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        setkaFile = fileChooser.showOpenDialog(new Stage());

        if (setkaFile != null) {
            filePath.setText(setkaFile.getAbsolutePath());
        }


        progressBar.setVisible(true);

        Service process = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        if (setkaFile != null) {
                            if(setkaFile.getName().contains(".k")){ // Ansys

                            }else if(setkaFile.getName().contains(".bd3")){ //ВК Динамика 3
                                possibleToContinue = bd3Utils.setSetkaFile(setkaFile);
                            } else { // ВК Ажурные схемы
                                possibleToContinue = azhureUtils.setSetkaFile(setkaFile);
                            }
                        }
                        return null;
                    }
                };
            }
        };

        process.setOnSucceeded( e -> {
            if (setkaFile != null) {
                if(setkaFile.getName().contains(".k")){ // Ansys

                }else if(setkaFile.getName().contains(".bd3")){ //ВК Динамика 3
                    descriptionText.setText(bd3Utils.getDescription());
                } else { // ВК Ажурные схемы
                    descriptionText.setText(azhureUtils.getDescription());
                }
            }
            progressBar.setVisible(false);

        });

        process.setOnFailed( e -> {
            progressBar.setVisible(false);
        });

        process.start();
    }

    private void nextPage(ActionEvent event) {
        try {
            if (possibleToContinue) {
                if(setkaFile.getName().contains(".k")){ // Ansys
                    root = FXMLLoader.load(getClass().getResource("sample.fxml"));
                }else if(setkaFile.getName().contains(".bd3")){ //ВК Динамика 3
                    root = FXMLLoader.load(getClass().getResource("bd3Window.fxml"));
                } else { // ВК Ажурные схемы
                    root = FXMLLoader.load(getClass().getResource("sample.fxml"));
                }
                stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            System.out.println("Error change scene: " + e.getMessage());
        }

    }

}

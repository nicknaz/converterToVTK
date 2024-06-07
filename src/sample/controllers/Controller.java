package sample.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.Converter;
import sample.CustomShape;
import sample.formatUtils.azhureUtils;
import sample.sharpImitation.BinaryReaderJ;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    @FXML
    private MenuItem closeMenuBtn;

    @FXML
    private Button convertBtn;

    @FXML
    private Menu helpBtn;

    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private Label label3;

    @FXML
    private Label label4;

    @FXML
    private Label label5;

    @FXML
    private Button openDataFileBtn;

    @FXML
    private MenuItem openMenuBtn;

    @FXML
    private Button btnView;

    @FXML
    private Button btnViewResults;

    @FXML
    private CheckBox isVoxels;


    private File setkaFile;
    private File dataFile;

    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    @FXML
    void initialize() {
        openDataFileBtn.setOnAction(event -> openDataFile());
        convertBtn.setOnAction(event -> convert());
        btnView.setOnAction(event -> view(azhureUtils.getDots(), azhureUtils.getVoxels()));
        btnViewResults.setOnAction(event -> viewResult());
        label1.setText(azhureUtils.getSetkaFile().getAbsolutePath());

    }

    public void convert() {

        try{
            azhureUtils.convert(isVoxels.isSelected());
            label3.setText("Результат: " + azhureUtils.getSetkaFile().getParentFile().getAbsolutePath()+"/"+azhureUtils.getSetkaFile().getName().split("\\.")[0]+".vtk");
            label4.setText("Количество узлов: " + azhureUtils.getDotCount());
            label5.setText("Количество элементов: " + azhureUtils.getVoxelCount());
        }catch (Exception e){
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);

            VBox vbox = new VBox(new Text("Формат входных данных неверен или программа не может их обработать!"), new Button("Ok."));
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(15));

            dialogStage.setScene(new Scene(vbox));
            dialogStage.show();
            //Pane.showMessageDialog(GUIManager.this, "Формат входных данных неверен или программа не может их обработать!", "Ошибка",JOptionPane.ERROR_MESSAGE);
        }


        //System.out.println(Converter.getDots());
    }

    public void viewResult(){
        List<List<Double>> newDots = new ArrayList<>();
        for (int i = 0; i < azhureUtils.getDotCount(); i++) {
            List<Double> tmp = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                tmp.add(azhureUtils.getDots().get(i).get(j) + azhureUtils.getDelta().get(i).get(j));
            }
            newDots.add(tmp);
        }
        view(newDots, azhureUtils.getVoxels());
    }

    public void view(List<List<Double>> dots, List<List<Integer>> voxels) {
        Stage primaryStage = new Stage();

        CustomShape sphere = new CustomShape(dots, voxels);

        Camera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(10000);
        camera.translateZProperty().set(-1000);

        Group world = new Group();
        world.getChildren().add(sphere);

        Group root = new Group();
        root.getChildren().add(world);
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(Color.rgb(230, 230, 230,0.7));

        PointLight point = new PointLight   ();
        point.setColor(Color.rgb(255, 255, 255,1));
        point.setLayoutX(400);
        point.setLayoutY(100);
        point.setTranslateZ(-1100);
        point.getScope().add(sphere);

        root.getChildren().addAll(point);
        //root.getChildren().add(prepareImageView());

        Scene scene = new Scene(root, 500, 500, true);
        scene.setFill(Color.LIGHTGREY);

        scene.setCamera(camera);

        initMouseControl(world, scene, primaryStage);

        sphere.translateXProperty().set(500 / 2);
        sphere.translateYProperty().set(500 / 2);
        sphere.translateZProperty().set(-400);

        angleX.set(90);
        angleY.set(0);


        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case W:
                    sphere.translateZProperty().set(sphere.getTranslateZ() + 100);
                    break;
                case S:
                    sphere.translateZProperty().set(sphere.getTranslateZ() - 100);
                    break;
                case D:
                    sphere.setRotationAxis(new Point3D(0,1,0));
                    sphere.setRotate(sphere.getRotate()+10);
                    break;
                case R:
                    sphere.setRotationAxis(new Point3D(1,0,0));
                    sphere.setRotate(sphere.getRotate()+10);
                    break;
                case F:
                    sphere.setRotationAxis(new Point3D(0,0,1));
                    sphere.setRotate(sphere.getRotate()+10);
                    break;
            }
        });

        primaryStage.setTitle("Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void openDataFile() {
        FileChooser fileChooser = new FileChooser();
        dataFile = fileChooser.showOpenDialog(new Stage());
        azhureUtils.setDataFile(dataFile);
        label2.setText(dataFile.getAbsolutePath());

    }

    private void initMouseControl(Group group, Scene scene, Stage stage) {
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });

        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            group.translateZProperty().set(group.getTranslateZ() + delta);
        });
    }
}

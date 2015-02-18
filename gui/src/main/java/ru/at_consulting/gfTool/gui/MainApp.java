package ru.at_consulting.gfTool.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
//        String fxmlFile = "C:\\share\\GIT_ATC\\GFTool\\src\\main\\resources\\fxml\\tool.fxml";
//        FXMLLoader loader = new FXMLLoader();
//        Parent root = loader.load(getClass().getResourceAsStream(fxmlFile));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tool.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("GFTool");

        primaryStage.setScene(new Scene(root, 1366, 768));
        primaryStage.setMinHeight(758);
        primaryStage.setMinWidth(1350);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}

package SOATestTool.gui;

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

        primaryStage.setScene(new Scene(root, 1350, 750));
        primaryStage.setMinHeight(740);
        primaryStage.setMinWidth(1330);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}

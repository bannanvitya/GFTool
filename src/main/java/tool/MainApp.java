package tool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class MainApp extends Application {

    private static Logger LOG = Logger.getLogger(String.valueOf(MainApp.class));

    @Override
    public void start(Stage primaryStage) throws Exception{
//        String fxmlFile = "C:\\share\\GIT_ATC\\GFTool\\src\\main\\resources\\fxml\\tool.fxml";
//        FXMLLoader loader = new FXMLLoader();
//        Parent root = loader.load(getClass().getResourceAsStream(fxmlFile));


        Parent root = FXMLLoader.load(getClass().getResource("/fxml/tool.fxml"));

        primaryStage.setTitle("GFTool");

        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.setMaxHeight(738);
        primaryStage.setMaxWidth(1016);
        primaryStage.setMinHeight(738);
        primaryStage.setMinWidth(1016);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

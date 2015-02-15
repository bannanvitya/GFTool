package ru.at_consulting.gfTool.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class WSDLNewProjectController implements Initializable {

    @FXML public Button WSDLBrowseButton;
    @FXML public Button WSDLOKButton;
    @FXML public TextField WSDLProjectNameField;
    @FXML public TextField WSDLBrowseField;


    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        WSDLBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File file = project.showOpenDialog(stage);

                if (file != null) {
                    WSDLProjectNameField.setText(file.getName());
                    WSDLBrowseField.setText(file.getAbsolutePath());
                }
            }
        });

        WSDLOKButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage stage = (Stage) WSDLOKButton.getScene().getWindow();
                                stage.close();
            }
        });
    }


}

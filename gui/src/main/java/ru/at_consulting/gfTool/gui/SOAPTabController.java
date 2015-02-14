package ru.at_consulting.gfTool.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class SOAPTabController implements Initializable {

    @FXML public Tab soapTab;

    @FXML public Label soapProjectStateLabel;
    @FXML public MenuItem soapProjectNew;
    @FXML public MenuItem soapProjectOpen;
    @FXML public MenuItem soapProjectSave;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        soapProjectStateLabel.setText("");

        soapProjectNew.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/fxml/wsdl.fxml"), resources);

                    Stage stage = new Stage();
                    stage.setTitle("Create New Project");

                    stage.setScene(new Scene(root, 331, 105));
                    stage.resizableProperty().setValue(false);
                    stage.show();


                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
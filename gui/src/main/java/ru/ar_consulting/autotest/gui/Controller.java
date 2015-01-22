package ru.ar_consulting.autotest.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import ru.at_consulting.autotest.soapclient.SoapClient_SimpleExample;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML public Button button1;
    @FXML public TextField hostField;


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        button1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                hostField.setText(SoapClient_SimpleExample.getMessage());
            }
        });

    }

}

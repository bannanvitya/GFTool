package ru.at_consulting.gfTool.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class SOAPTabController implements Initializable {
    private Node upperElement;


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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/wsdl.fxml"));
                    Parent wsdl = loader.load();

                    Stage stage = new Stage();
                    stage.setTitle("Create New Project");

                    stage.setScene(new Scene(wsdl, 331, 105));
                    stage.resizableProperty().setValue(false);
                    stage.setAlwaysOnTop(true);
                    stage.centerOnScreen();
                    stage.show();

                    Node root = upperElement;
                    upperElement.getScene().getRoot().setDisable(true);


                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }



    public void setSoapUpperElement(Node node){
        upperElement = node;
    }


}
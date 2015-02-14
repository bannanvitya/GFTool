package ru.at_consulting.gfTool.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;


import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class MainAppController implements Initializable {

    @FXML public TabPane MainTabPane;
    @FXML public Tab jmsTab;
    @FXML public Tab httpTab;
    @FXML public Tab soapTab;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        VBox jmsVbox = new VBox();
        VBox httpVbox = new VBox();
        VBox soapVbox = new VBox();

        try {
            jmsVbox = FXMLLoader.load(getClass().getResource("/fxml/JMS.fxml"));
            httpVbox = FXMLLoader.load(getClass().getResource("/fxml/HTTP.fxml"));
            soapVbox = FXMLLoader.load(getClass().getResource("/fxml/SOAP.fxml"));
        } catch (IOException e){
            e.printStackTrace();
        }

        jmsTab.setContent(jmsVbox);
        httpTab.setContent(httpVbox);
        soapTab.setContent(soapVbox);
    }




}

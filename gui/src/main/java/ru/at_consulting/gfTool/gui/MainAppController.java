package ru.at_consulting.gfTool.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.fxml.Initializable;


import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class MainAppController implements Initializable {

    @FXML public TabPane MainTabPane;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        Tab jmsTab = new Tab();
        Tab httpTab = new Tab();
        Tab soapTab = new Tab();

        try {
            jmsTab = FXMLLoader.load(getClass().getResource("/fxml/JMS.fxml"));
            httpTab = FXMLLoader.load(getClass().getResource("/fxml/HTTP.fxml"));
            soapTab = FXMLLoader.load(getClass().getResource("/fxml/SOAP.fxml"));
        } catch (IOException e){
            e.printStackTrace();
        }

        MainTabPane.getTabs().add(jmsTab);
        MainTabPane.getTabs().add(httpTab);
        MainTabPane.getTabs().add(soapTab);
    }




}

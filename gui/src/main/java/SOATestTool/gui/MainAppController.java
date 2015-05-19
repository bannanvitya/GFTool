package SOATestTool.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    @FXML public Tab smtpTab;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        VBox jmsVbox = new VBox();
        VBox httpVbox = new VBox();
        VBox soapVbox = new VBox();
        VBox smtpVbox = new VBox();

        FXMLLoader jmsLoader = new FXMLLoader();
        FXMLLoader httpLoader = new FXMLLoader();
        FXMLLoader soapLoader = new FXMLLoader();
        FXMLLoader smtpLoader = new FXMLLoader();

        try {
            jmsLoader = new FXMLLoader(getClass().getResource("/fxml/JMS.fxml"));
            httpLoader = new FXMLLoader(getClass().getResource("/fxml/HTTP.fxml"));
            soapLoader = new FXMLLoader(getClass().getResource("/fxml/SOAP.fxml"));
            smtpLoader = new FXMLLoader(getClass().getResource("/fxml/SMTP.fxml"));

            jmsVbox = jmsLoader.load();
            httpVbox = httpLoader.load();
            soapVbox = soapLoader.load();
            smtpVbox = smtpLoader.load();

        } catch (IOException e){
            e.printStackTrace();
        }

        JMSTabController jmsController = (JMSTabController)jmsLoader.getController();
        HTTPTabController httpController = (HTTPTabController)httpLoader.getController();
        SOAPTabController soapController = (SOAPTabController)soapLoader.getController();
        SMTPTabController smtpController = (SMTPTabController)smtpLoader.getController();

        jmsController.setJmsUpperElement(jmsVbox);
        httpController.setHttpUpperElement(httpVbox);
        soapController.setSoapUpperElement(soapVbox);
        smtpController.setSmtpUpperElement(smtpVbox);

        jmsTab.setContent(jmsVbox);
        httpTab.setContent(httpVbox);
        soapTab.setContent(soapVbox);
        smtpTab.setContent(smtpVbox);
    }




}

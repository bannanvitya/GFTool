package ru.at_consulting.gfTool.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.Initializable;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import ru.at_consulting.gfTool.IBMMqClient.IBMMqClient;
import ru.at_consulting.gfTool.IBMMqClient.IBMMqProfile;
import ru.at_consulting.gfTool.IBMMqClient.IBMMqRequest;
import ru.at_consulting.gfTool.api.ProfileNotFoundException;
import ru.at_consulting.gfTool.api.ProfileStructureException;
import ru.at_consulting.gfTool.api.SendRequestException;


import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML public Button button1;
    @FXML public TextField hostField;
    @FXML public TextField portField;
    @FXML public TextField queueManagerField;
    @FXML public TextField channelField;
    @FXML public TextField transportTypeField;
    @FXML public TextField queueNameField;
    @FXML public TextField userIdField;
    @FXML public TextField passField;
    @FXML public TextArea messageField;
    @FXML public Tab jmsTab;
    @FXML public Tab soapTab;


    @FXML public Button httpButton;
    @FXML public TextField contentTypeField;
    @FXML public RadioButton appJson;
    @FXML public RadioButton textXml;


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        if ((new File(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects")).length() != 0)
            try {
                ObjectInputStream ois =
                        new ObjectInputStream(new FileInputStream(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects"));

                hostField.setText(ois.readLine());
                portField.setText(ois.readLine());
                queueManagerField.setText(ois.readLine());
                channelField.setText(ois.readLine());
                transportTypeField.setText(ois.readLine());
                queueNameField.setText(ois.readLine());
                userIdField.setText(ois.readLine());
                passField.setText(ois.readLine());
                messageField.setText(ois.readLine());
            } catch (Exception ex) {
                ex.printStackTrace();
            }


        button1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {

                    FileOutputStream out = new FileOutputStream(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects");
                    ObjectOutputStream oout = new ObjectOutputStream(out);

                    oout.writeChars(hostField.getText() + "\n");
                    oout.writeChars(portField.getText() + "\n");
                    oout.writeChars(queueManagerField.getText() + "\n");
                    oout.writeChars(channelField.getText() + "\n");
                    oout.writeChars(transportTypeField.getText() + "\n");
                    oout.writeChars(queueNameField.getText() + "\n");
                    oout.writeChars(userIdField.getText() + "\n");
                    oout.writeChars(passField.getText() + "\n");
                    oout.writeChars(messageField.getText() + "\n");
                    oout.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                IBMMqProfile profile = new IBMMqProfile();

                Properties prop = new Properties();

                prop.setProperty("host", hostField.getText());
                prop.setProperty("port", portField.getText());
                prop.setProperty("queueManager", queueManagerField.getText());
                prop.setProperty("channel", channelField.getText());
                prop.setProperty("transportType", transportTypeField.getText());
                prop.setProperty("queueName", queueNameField.getText());
                prop.setProperty("userId", userIdField.getText());
                prop.setProperty("password", passField.getText());


                try {
                    profile.setId(jmsTab.getText(), prop);
                } catch (ProfileNotFoundException e) {
                    e.printStackTrace();
                } catch (ProfileStructureException e) {
                    e.printStackTrace();
                }

                IBMMqRequest request = new IBMMqRequest(messageField.getText());
                IBMMqClient client = new IBMMqClient();
                client.setProfile(profile);

                Date now = new Date();

                try {
                    client.prepareRequest("request." + now.toString());

                } catch (ProfileStructureException e) {
                    e.printStackTrace();
                }

                try {
                    client.sendRequest(request);
                } catch (SendRequestException e) {
                    e.printStackTrace();
                } catch (ProfileStructureException e) {
                    e.printStackTrace();
                }

            }
        });


        httpButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

            }
        });

        contentTypeField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                if (!contentTypeField.getText().equals("")) {
                    appJson.setTextFill(Color.GRAY);
                    textXml.setTextFill(Color.GRAY);
                }
                else {
                    appJson.setTextFill(Color.BLACK);
                    textXml.setTextFill(Color.BLACK);
                }
            }
        });


    }


}

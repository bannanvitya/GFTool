package ru.at_consulting.gfTool.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;

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
    @FXML public RadioButton getM;
    @FXML public RadioButton postM;
    @FXML public TableView<Params> paramTable;
    @FXML public TextField paramNameField;
    @FXML public TextField paramValueField;
    @FXML public Button addRow;






    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        if (!getM.isSelected()) {
            paramTable.setVisible(false);
            paramNameField.setVisible(false);
            paramValueField.setVisible(false);
            addRow.setVisible(false);
        }

        final ObservableList<Params> data = FXCollections.observableArrayList(); // For TableView in HTTP Tab
        paramTable.setItems(data);

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

        getM.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                paramTable.setVisible(true);
                paramNameField.setVisible(true);
                paramValueField.setVisible(true);
                addRow.setVisible(true);
            }
        });

        postM.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                paramTable.setVisible(false);
                paramNameField.setVisible(false);
                paramValueField.setVisible(false);
                addRow.setVisible(false);

            }
        });

        addRow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!paramNameField.getText().equals("") && !paramValueField.getText().equals("")){
                    data.add(new Params(paramNameField.getText(), paramValueField.getText()));
                    paramNameField.clear();
                    paramValueField.clear();
                }

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

    public static class Params {

        private final SimpleStringProperty paramName;
        private final SimpleStringProperty paramValue;

        private Params(String pName, String pValue) {
            this.paramName = new SimpleStringProperty(pName);
            this.paramValue = new SimpleStringProperty(pValue);
        }

        public String getFirstName() {
            return paramName.get();
        }

        public void setFirstName(String fName) {
            paramName.set(fName);
        }

        public String getLastName() {
            return paramValue.get();
        }

        public void setLastName(String fName) {
            paramValue.set(fName);
        }


    }


}

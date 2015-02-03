package ru.at_consulting.gfTool.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;

import ru.at_consulting.gfTool.HTTPClient.HTTPClient;
import ru.at_consulting.gfTool.HTTPClient.HTTPProfile;
import ru.at_consulting.gfTool.HTTPClient.HTTPRequest;
import ru.at_consulting.gfTool.HTTPClient.HTTPResponse;
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
    @FXML public Button jmsButton;
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
    @FXML public Tab httpTab;
    @FXML public Tab soapTab;


    @FXML public Button httpButton;
    @FXML public TextField contentTypeField;
    @FXML public RadioButton appJson;
    @FXML public RadioButton textXml;
    @FXML public RadioButton otherContentType;
    @FXML public RadioButton getM;
    @FXML public RadioButton postM;
    @FXML public TableView<Params> paramTable;
    @FXML public TextField paramNameField;
    @FXML public TextField paramValueField;
    @FXML public TextField urlField;
    @FXML public Button addRow;
    @FXML public TextArea httpMessageField;
    @FXML public TextArea httpResponseField;






    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        if (!getM.isSelected()) {
            paramTable.setVisible(false);
            paramNameField.setVisible(false);
            paramValueField.setVisible(false);
            addRow.setVisible(false);
        }

        //final ObservableList<Params> data = FXCollections.observableArrayList(); // For TableView in HTTP Tab
        //paramTable.setItems(data);

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
            } catch (Exception ex) {
                ex.printStackTrace();
            }


        if ((new File(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects")).length() != 0)
            try {
                ObjectInputStream ois =
                        new ObjectInputStream(new FileInputStream(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects"));

                urlField.setText(ois.readLine());
                paramNameField.setText(ois.readLine());
                paramValueField.setText(ois.readLine());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        if ((new File(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.request")).length() != 0)
            try {
                StringBuffer fileData = new StringBuffer();
                BufferedReader reader = new BufferedReader(
                        new FileReader(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.request"));
                char[] buf = new char[1024];
                int numRead=0;
                while((numRead=reader.read(buf)) != -1){
                    String readData = String.valueOf(buf, 0, numRead);
                    fileData.append(readData);
                }
                reader.close();
                messageField.setText(fileData.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        if ((new File(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.request")).length() != 0)
            try {
                StringBuffer fileData = new StringBuffer();
                BufferedReader reader = new BufferedReader(
                        new FileReader(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.request"));
                char[] buf = new char[1024];
                int numRead=0;
                while((numRead=reader.read(buf)) != -1){
                    String readData = String.valueOf(buf, 0, numRead);
                    fileData.append(readData);
                }
                reader.close();
                httpMessageField.setText(fileData.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }



        jmsButton.setOnAction(new EventHandler<ActionEvent>() {

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

                try {
                    File file = new File(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.request");
                    FileOutputStream fop = new FileOutputStream(file);


                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    // get the content in bytes
                    byte[] contentInBytes = messageField.getText().getBytes();

                    fop.write(contentInBytes);
                    fop.flush();
                    fop.close();
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

                try {
                    FileOutputStream out = new FileOutputStream(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects");
                    ObjectOutputStream oout = new ObjectOutputStream(out);

                    oout.writeChars(urlField.getText() + "\n");
                    oout.writeChars(paramNameField.getText() + "\n");
                    oout.writeChars(paramValueField.getText() + "\n");
                    oout.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    File file = new File(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.request");
                    FileOutputStream fop = new FileOutputStream(file);


                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    // get the content in bytes
                    byte[] contentInBytes = httpMessageField.getText().getBytes();

                    fop.write(contentInBytes);
                    fop.flush();
                    fop.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                HTTPProfile profile = new HTTPProfile();

                Properties prop = new Properties();
                Properties headers = new Properties();

                for (Params param : paramTable.getItems()) {
                    headers.setProperty(param.getParamName(), param.getParamValue());
                }

                prop.setProperty("url", urlField.getText());

                if (getM.isSelected())
                    prop.setProperty("methodType", "GET");
                else
                    prop.setProperty("methodType", "POST");


                if (appJson.isSelected())
                    prop.setProperty("contentType", "application/json");
                else if (textXml.isSelected())
                    prop.setProperty("contentType", "text/xml");
                else if (otherContentType.isSelected())
                    prop.setProperty("contentType", contentTypeField.getText());


                try {
                    if (prop.getProperty("methodType").equals("GET"))
                        profile.setId(httpTab.getText(), prop, headers);
                    else
                        profile.setId(httpTab.getText(), prop);

                } catch (ProfileNotFoundException e) {
                    e.printStackTrace();
                } catch (ProfileStructureException e) {
                    e.printStackTrace();
                }

                HTTPClient client = new HTTPClient();
                HTTPRequest req = new HTTPRequest(httpMessageField.getText());
                client.setProfile(profile);
                HTTPResponse resp = null;
                try {
                    resp = (HTTPResponse) client.sendRequest(req);
                }catch (ProfileStructureException e) {
                    e.printStackTrace();
                } catch (SendRequestException e) {
                    e.printStackTrace();
                }

                if (resp != null)
                httpResponseField.setText("Code: " + resp.getStatus() + "\n" + "Message: " + resp.getMessage());
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
                    paramTable.getItems().add(new Params(paramNameField.getText(), paramValueField.getText()));
                    paramNameField.clear();
                    paramValueField.clear();
                }

            }
        });





        contentTypeField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                if (!contentTypeField.getText().equals("")) {
                    otherContentType.setSelected(true);
                    appJson.setTextFill(Color.GRAY);
                    textXml.setTextFill(Color.GRAY);
                }
                else {
                    appJson.setTextFill(Color.BLACK);
                    textXml.setTextFill(Color.BLACK);
                }
            }
        });


        otherContentType.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                appJson.setTextFill(Color.GRAY);
                textXml.setTextFill(Color.GRAY);
            }
        });

        textXml.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                appJson.setTextFill(Color.BLACK);
                textXml.setTextFill(Color.BLACK);
            }
        });

        appJson.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                appJson.setTextFill(Color.BLACK);
                textXml.setTextFill(Color.BLACK);
            }
        });


    }

    public static class Params {

        private final StringProperty paramName = new SimpleStringProperty("");
        private final StringProperty paramValue = new SimpleStringProperty("");

        private Params(String pName, String pValue) {
            setParamName(pName);
            setParamValue(pValue);
        }

        public String getParamName() {
            return paramName.get();
        }

        public void setParamName(String name) {
            this.paramName.set(name);
        }

        public StringProperty firstParamName() {
            return paramName;
        }

        public String getParamValue() {
            return paramValue.get();
        }

        public void setParamValue(String name) {
            paramValue.set(name);
        }

        public StringProperty surnameParamValue() {
            return paramValue;
        }


    }


}

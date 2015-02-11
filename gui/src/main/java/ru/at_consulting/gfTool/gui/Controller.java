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

import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
    @FXML public Tab jmsTab;

    @FXML public Button jmsButton;
    @FXML public TextField jmsHostField;
    @FXML public TextField jmsPortField;
    @FXML public TextField jmsQueueManagerField;
    @FXML public TextField jmsChannelField;
    @FXML public TextField jmsTransportTypeField;
    @FXML public TextField jmsQueueNameField;
    @FXML public TextField jmsUserIdField;
    @FXML public TextField jmsPassField;
    @FXML public TextArea jmsRequestField;


    @FXML public Tab httpTab;

    @FXML public Button httpButton;
    @FXML public TextField httpContentTypeField;
    @FXML public RadioButton httpAppJson;
    @FXML public RadioButton httpTextXml;
    @FXML public RadioButton httpOtherContentType;
    @FXML public RadioButton httpGetMethod;
    @FXML public RadioButton httpPostMethod;
    @FXML public TableView<Params> httpParamTable;
    @FXML public TextField httpParamNameField;
    @FXML public TextField httpParamValueField;
    @FXML public TextField httpUrlField;
    @FXML public Button httpAddRow;
    @FXML public TextArea httpRequestField;
    @FXML public TextArea httpResponseField;
    @FXML public MenuItem jmsProjectOpen;
    @FXML public MenuItem jmsProjectSave;


    @FXML public Tab soapTab;



    private void SaveFile(String content, File file){
        try {
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
            } catch (IOException ex) {
                System.out.println("IOExcaption, bro");
            }
        }

    private void jmsProjectInitialGet(){
        if ((new File(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects")).length() != 0)
            try {
                ObjectInputStream ois =
                        new ObjectInputStream(new FileInputStream(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects"));

                jmsHostField.setText(ois.readLine());
                jmsPortField.setText(ois.readLine());
                jmsQueueManagerField.setText(ois.readLine());
                jmsChannelField.setText(ois.readLine());
                jmsTransportTypeField.setText(ois.readLine());
                jmsQueueNameField.setText(ois.readLine());
                jmsUserIdField.setText(ois.readLine());
                jmsPassField.setText(ois.readLine());
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
                jmsRequestField.setText(fileData.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    private void httpProjectInitialGet(){
        if ((new File(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects")).length() != 0)
            try {
                ObjectInputStream ois =
                        new ObjectInputStream(new FileInputStream(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects"));

                httpUrlField.setText(ois.readLine());
                httpParamNameField.setText(ois.readLine());
                httpParamValueField.setText(ois.readLine());
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
                httpRequestField.setText(fileData.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    private void jmsProjectSave(){
        try {

            FileOutputStream out = new FileOutputStream(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects");
            ObjectOutputStream oout = new ObjectOutputStream(out);

            oout.writeChars(jmsHostField.getText() + "\n");
            oout.writeChars(jmsPortField.getText() + "\n");
            oout.writeChars(jmsQueueManagerField.getText() + "\n");
            oout.writeChars(jmsChannelField.getText() + "\n");
            oout.writeChars(jmsTransportTypeField.getText() + "\n");
            oout.writeChars(jmsQueueNameField.getText() + "\n");
            oout.writeChars(jmsUserIdField.getText() + "\n");
            oout.writeChars(jmsPassField.getText() + "\n");
            oout.writeChars(jmsRequestField.getText() + "\n");
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
            byte[] contentInBytes = jmsRequestField.getText().getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void httpProjectSave(){
        try {
            FileOutputStream out = new FileOutputStream(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects");
            ObjectOutputStream oout = new ObjectOutputStream(out);

            oout.writeChars(httpUrlField.getText() + "\n");
            oout.writeChars(httpParamNameField.getText() + "\n");
            oout.writeChars(httpParamValueField.getText() + "\n");
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
            byte[] contentInBytes = httpRequestField.getText().getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        if (!httpGetMethod.isSelected()) {
            httpParamTable.setVisible(false);
            httpParamNameField.setVisible(false);
            httpParamValueField.setVisible(false);
            httpAddRow.setVisible(false);
        }

        jmsProjectInitialGet();
        httpProjectInitialGet();


        jmsButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                jmsProjectSave();

                IBMMqProfile profile = new IBMMqProfile();

                Properties prop = new Properties();

                prop.setProperty("host", jmsHostField.getText());
                prop.setProperty("port", jmsPortField.getText());
                prop.setProperty("queueManager", jmsQueueManagerField.getText());
                prop.setProperty("channel", jmsChannelField.getText());
                prop.setProperty("transportType", jmsTransportTypeField.getText());
                prop.setProperty("queueName", jmsQueueNameField.getText());
                prop.setProperty("userId", jmsUserIdField.getText());
                prop.setProperty("password", jmsPassField.getText());


                try {
                    profile.setId(jmsTab.getText(), prop);
                } catch (ProfileNotFoundException e) {
                    e.printStackTrace();
                } catch (ProfileStructureException e) {
                    e.printStackTrace();
                }

                IBMMqRequest request = new IBMMqRequest(jmsRequestField.getText());
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

                httpProjectSave();

                HTTPProfile profile = new HTTPProfile();

                Properties prop = new Properties();
                Properties headers = new Properties();

                for (Params param : httpParamTable.getItems()) {
                    headers.setProperty(param.getParamName(), param.getParamValue());
                }

                prop.setProperty("url", httpUrlField.getText());

                if (httpGetMethod.isSelected())
                    prop.setProperty("methodType", "GET");
                else
                    prop.setProperty("methodType", "POST");


                if (httpAppJson.isSelected())
                    prop.setProperty("contentType", "application/json");
                else if (httpTextXml.isSelected())
                    prop.setProperty("contentType", "text/xml");
                else if (httpOtherContentType.isSelected())
                    prop.setProperty("contentType", httpContentTypeField.getText());


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
                HTTPRequest req = new HTTPRequest(httpRequestField.getText());
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

        httpGetMethod.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                httpParamTable.setVisible(true);
                httpParamNameField.setVisible(true);
                httpParamValueField.setVisible(true);
                httpAddRow.setVisible(true);
            }
        });

        httpPostMethod.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                httpParamTable.setVisible(false);
                httpParamNameField.setVisible(false);
                httpParamValueField.setVisible(false);
                httpAddRow.setVisible(false);

            }
        });

        httpAddRow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!httpParamNameField.getText().equals("") && !httpParamValueField.getText().equals("")){
                    httpParamTable.getItems().add(new Params(httpParamNameField.getText(), httpParamValueField.getText()));
                    httpParamNameField.clear();
                    httpParamValueField.clear();
                }

            }
        });





        httpContentTypeField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                if (!httpContentTypeField.getText().equals("")) {
                    httpOtherContentType.setSelected(true);
                    httpAppJson.setTextFill(Color.GRAY);
                    httpTextXml.setTextFill(Color.GRAY);
                }
                else {
                    httpAppJson.setTextFill(Color.BLACK);
                    httpTextXml.setTextFill(Color.BLACK);
                }
            }
        });


        httpOtherContentType.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                httpAppJson.setTextFill(Color.GRAY);
                httpTextXml.setTextFill(Color.GRAY);
            }
        });

        httpTextXml.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                httpAppJson.setTextFill(Color.BLACK);
                httpTextXml.setTextFill(Color.BLACK);
            }
        });

        httpAppJson.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                httpAppJson.setTextFill(Color.BLACK);
                httpTextXml.setTextFill(Color.BLACK);
            }
        });



        jmsProjectOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                project.showOpenDialog(stage);
            }
        });


        jmsProjectSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File file = project.showSaveDialog(stage);

                if(file != null){
                    SaveFile("some", file);
                }
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

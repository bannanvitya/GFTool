package ru.at_consulting.gfTool.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codehaus.jackson.map.ObjectMapper;
import ru.at_consulting.gfTool.HTTPClient.HTTPClient;
import ru.at_consulting.gfTool.HTTPClient.HTTPProfile;
import ru.at_consulting.gfTool.HTTPClient.HTTPRequest;
import ru.at_consulting.gfTool.HTTPClient.HTTPResponse;
import ru.at_consulting.gfTool.api.ProfileNotFoundException;
import ru.at_consulting.gfTool.api.ProfileStructureException;
import ru.at_consulting.gfTool.api.SendRequestException;

import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class HTTPTabController implements Initializable {

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
    @FXML public MenuItem httpProjectOpen;
    @FXML public MenuItem httpProjectSave;
    @FXML public Label httpProjectStateLabel;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        httpProjectStateLabel.setText("");

        if (!httpGetMethod.isSelected()) {
            httpParamTable.setVisible(false);
            httpParamNameField.setVisible(false);
            httpParamValueField.setVisible(false);
            httpAddRow.setVisible(false);
        }

        httpProjectInitialGet(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects");


        httpButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                httpProjectSave(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects");

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
                        profile.setId("httpTab", prop, headers);
                    else
                        profile.setId("httpTab", prop);

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

        httpProjectSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File file = project.showSaveDialog(stage);


                if(file != null){
                    httpProjectSave(file.getPath());
                    httpProjectSave(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects");
                    httpProjectStateLabel.setText(file.getName() + "  ");
                }
            }
        });

        httpProjectOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File file = project.showOpenDialog(stage);

                if (file != null){
                    httpProjectInitialGet(file.getPath());
                    httpProjectSave(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects");
                    httpProjectStateLabel.setText(file.getName() + "  ");
                }
            }
        });
    }

    private void httpProjectInitialGet(String path){
        if ((new File(path)).length() != 0)
            try {
                ObjectMapper mapper = new ObjectMapper();
                Properties httpTabProp = mapper.readValue(new File(path), Properties.class);

                httpUrlField.setText(httpTabProp.getProperty(httpUrlField.getId()));
                httpParamNameField.setText(httpTabProp.getProperty(httpParamNameField.getId()));
                httpParamValueField.setText(httpTabProp.getProperty(httpParamValueField.getId()));
                httpRequestField.setText(httpTabProp.getProperty(httpRequestField.getId()));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    private void httpProjectSave(String path){
        try {
            Properties httpTabProp = new Properties();
            httpTabProp.setProperty(httpUrlField.getId(), httpUrlField.getText());
            httpTabProp.setProperty(httpParamNameField.getId(), httpParamNameField.getText());
            httpTabProp.setProperty(httpParamValueField.getId(), httpParamValueField.getText());
            httpTabProp.setProperty(httpRequestField.getId(), httpRequestField.getText());

            File resultFile = new File(path);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(resultFile, httpTabProp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

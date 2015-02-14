package ru.at_consulting.gfTool.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codehaus.jackson.map.ObjectMapper;
import ru.at_consulting.gfTool.IBMMqClient.IBMMqClient;
import ru.at_consulting.gfTool.IBMMqClient.IBMMqProfile;
import ru.at_consulting.gfTool.IBMMqClient.IBMMqRequest;
import ru.at_consulting.gfTool.api.ProfileNotFoundException;
import ru.at_consulting.gfTool.api.ProfileStructureException;
import ru.at_consulting.gfTool.api.SendRequestException;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class JMSTabController implements Initializable {

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
    @FXML public MenuItem jmsProjectOpen;
    @FXML public MenuItem jmsProjectSave;
    @FXML public Label jmsProjectStateLabel;



    private void jmsProjectInitialGet(String path){
        if ((new File(path)).length() != 0)
            try {
                ObjectMapper mapper = new ObjectMapper();
                Properties jmsTabProp = mapper.readValue(new File(path), Properties.class);

                jmsHostField.setText(jmsTabProp.getProperty(jmsHostField.getId()));
                jmsPortField.setText(jmsTabProp.getProperty(jmsPortField.getId()));
                jmsQueueManagerField.setText(jmsTabProp.getProperty(jmsQueueManagerField.getId()));
                jmsChannelField.setText(jmsTabProp.getProperty(jmsChannelField.getId()));
                jmsTransportTypeField.setText(jmsTabProp.getProperty(jmsTransportTypeField.getId()));
                jmsQueueNameField.setText(jmsTabProp.getProperty(jmsQueueNameField.getId()));
                jmsUserIdField.setText(jmsTabProp.getProperty(jmsUserIdField.getId()));
                jmsPassField.setText(jmsTabProp.getProperty(jmsPassField.getId()));
                jmsRequestField.setText(jmsTabProp.getProperty(jmsRequestField.getId()));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }
    private void jmsProjectSave(String path){
        try {
            Properties jmsTabProp = new Properties();
            jmsTabProp.setProperty(jmsHostField.getId(), jmsHostField.getText());
            jmsTabProp.setProperty(jmsPortField.getId(), jmsPortField.getText());
            jmsTabProp.setProperty(jmsQueueManagerField.getId(), jmsQueueManagerField.getText());
            jmsTabProp.setProperty(jmsChannelField.getId(), jmsChannelField.getText());
            jmsTabProp.setProperty(jmsTransportTypeField.getId(), jmsTransportTypeField.getText());
            jmsTabProp.setProperty(jmsQueueNameField.getId(), jmsQueueNameField.getText());
            jmsTabProp.setProperty(jmsUserIdField.getId(), jmsUserIdField.getText());
            jmsTabProp.setProperty(jmsPassField.getId(), jmsPassField.getText());
            jmsTabProp.setProperty(jmsRequestField.getId(), jmsRequestField.getText());



            File resultFile = new File(path);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(resultFile, jmsTabProp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        jmsProjectStateLabel.setText("");
        jmsProjectInitialGet(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects");


        jmsButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                jmsProjectSave(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects");

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

        jmsProjectOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File file = project.showOpenDialog(stage);

                if (file != null){
                    jmsProjectInitialGet(file.getPath());
                    jmsProjectSave(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects");
                    jmsProjectStateLabel.setText(file.getName() + "  ");
                }
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
                    jmsProjectSave(file.getPath());
                    jmsProjectSave(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects");
                    jmsProjectStateLabel.setText(file.getName() + "  ");
                }
            }
        });


    }
}
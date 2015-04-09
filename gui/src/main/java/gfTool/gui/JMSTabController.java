package gfTool.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codehaus.jackson.map.ObjectMapper;
import gfTool.IBMMqClient.IBMMqClient;
import gfTool.IBMMqClient.IBMMqProfile;
import gfTool.IBMMqClient.IBMMqRequest;
import gfTool.api.PostconditionsException;
import gfTool.api.ProfileNotFoundException;
import gfTool.api.ProfileStructureException;
import gfTool.api.SendRequestException;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class JMSTabController implements Initializable {
    private Node upperElement;


    TabPane jmsMainTabPane = new TabPane();
    Tab jmsAddButtonTab = new Tab();
    AnchorPane jmsInnerPane = new AnchorPane();


    @FXML public Button jmsButton;
    @FXML public MenuItem jmsProjectOpen;
    @FXML public MenuItem jmsProjectSave;
    @FXML public Label jmsProjectStateLabel;

    @FXML public VBox jmsVBox;

    private void jmsProjectInitialGet(String path){
        if ((new File(path)).length() != 0)
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, LinkedHashMap<String, String>> map = mapper.readValue(new File(path), Map.class);

                for (Map.Entry<String, LinkedHashMap<String, String>> e : map.entrySet()){
                    LinkedHashMap<String, String> jmsTabProp = e.getValue();
                    Tab t = addTab(e.getKey(), jmsMainTabPane);
                    t.setText(e.getKey());
                    SplitPane split = (SplitPane)t.getContent();
                    for (Node n : split.getItems()) {
                        AnchorPane ap = (AnchorPane) n;
                        for (Node tf : ap.getChildren()) {
                            try {
                                TextField f = (TextField) tf;
                                f.setText(jmsTabProp.get(f.getId()));
                            } catch (ClassCastException ex) {
                                try {
                                    TextArea ar = (TextArea) tf;
                                    ar.setText(jmsTabProp.get(ar.getId()));
                                } catch (ClassCastException er) {
                                    continue;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }
    private void jmsProjectSave(String path){
        try {
            Map<String, Properties> map = new HashMap<String, Properties>();
            for (Tab t : jmsMainTabPane.getTabs()) {
                SplitPane split = (SplitPane)t.getContent();
                Properties jmsTabProp = new Properties();
                if (split != null)
                for (Node ap : split.getItems()) {
                    AnchorPane pane = (AnchorPane) ap;
                    for (Node tf : pane.getChildren()) {
                        try {
                            TextField f = (TextField) tf;
                            jmsTabProp.put(f.getId(), f.getText());
                        } catch (ClassCastException ex) {
                            try {
                                TextArea ar = (TextArea) tf;
                                jmsTabProp.put(ar.getId(), ar.getText());
                            } catch (ClassCastException er) {
                                continue;
                            }
                        }
                    }
                }
                if (!jmsTabProp.isEmpty())
                    map.put(t.getId(), jmsTabProp);
            }
            File resultFile = new File(path);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(resultFile, map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        jmsMainTabPane.sideProperty().setValue(Side.LEFT);
        jmsMainTabPane.tabClosingPolicyProperty().setValue(TabPane.TabClosingPolicy.SELECTED_TAB);

        jmsInnerPane.getChildren().addAll(jmsMainTabPane);

        AnchorPane.setBottomAnchor(jmsMainTabPane, 0.0);
        AnchorPane.setLeftAnchor(jmsMainTabPane, 0.0);
        AnchorPane.setRightAnchor(jmsMainTabPane, 0.0);
        AnchorPane.setTopAnchor(jmsMainTabPane, 0.0);

        jmsAddButtonTab.setText("+");
        jmsAddButtonTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                                            public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
                                                                SingleSelectionModel<Tab> selectionModel = jmsMainTabPane.getSelectionModel();
                                                                if (new_val){
                                                                    Date now = new Date();
                                                                    Tab tab = addTab(now.toString(), jmsMainTabPane);
                                                                    selectionModel.select(tab);
                                                                }
                                                            }
                                                        }
        );



        jmsButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                jmsProjectSave(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects");

                IBMMqProfile profile = new IBMMqProfile();

                Properties prop = new Properties();

                SingleSelectionModel<Tab> selectionModel = jmsMainTabPane.getSelectionModel();
                SplitPane split = (SplitPane)jmsMainTabPane.getTabs().get(selectionModel.getSelectedIndex()).getContent();

                AnchorPane projectAnchor = (AnchorPane) split.getItems().get(1);
                TextField hostField = (TextField)projectAnchor.getChildren().get(8);
                TextField portField = (TextField)projectAnchor.getChildren().get(9);
                TextField queueField = (TextField)projectAnchor.getChildren().get(10);
                TextField channelField = (TextField)projectAnchor.getChildren().get(11);
                TextField transportTypeField = (TextField)projectAnchor.getChildren().get(12);
                TextField queueNameField = (TextField)projectAnchor.getChildren().get(13);
                TextField userIdField = (TextField)projectAnchor.getChildren().get(14);
                TextField passwordField = (TextField)projectAnchor.getChildren().get(15);

                prop.setProperty("host", hostField.getText());
                prop.setProperty("port", portField.getText());
                prop.setProperty("queueManager", queueField.getText());
                prop.setProperty("channel", channelField.getText());
                prop.setProperty("transportType", transportTypeField.getText());
                prop.setProperty("queueName", queueNameField.getText());
                prop.setProperty("userId", userIdField.getText());
                prop.setProperty("password", passwordField.getText());
                System.out.println(prop.toString());


                try {
                    profile.setId("jmsTab", prop);
                } catch (ProfileNotFoundException e) {
                    e.printStackTrace();
                } catch (ProfileStructureException e) {
                    e.printStackTrace();
                }

                AnchorPane requestAnchor = (AnchorPane) split.getItems().get(0);
                TextArea requestArea = (TextArea)requestAnchor.getChildren().get(1);

                IBMMqRequest request = new IBMMqRequest(requestArea.getText());
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
                try {
                    client.postconditions();
                } catch (PostconditionsException e){
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


        jmsVBox.getChildren().addAll(jmsInnerPane); // In this VBox 1) AnchorPane for button 2) AnchorPane named "jmsInnerPane" for all inner dynamic elements

        VBox.setVgrow(jmsInnerPane, Priority.ALWAYS);

        Date now = new Date();
        addTab(now.toString(), jmsMainTabPane);
        jmsMainTabPane.getTabs().add(jmsAddButtonTab);
        SingleSelectionModel<Tab> selectionModel = jmsMainTabPane.getSelectionModel();
        selectionModel.select(jmsMainTabPane.getTabs().indexOf(jmsAddButtonTab) - 1); // add tab to create new tabs


        jmsProjectInitialGet(System.getenv("GFTOOL_ROOT") + "/serz/jms.tab.objects");

        jmsProjectStateLabel.setText("");

    }

    public Tab addTab(String id, TabPane someTabPane){
        Tab tab = new Tab();
        tab.setId(id);

        SplitPane split = new SplitPane();
        split.setDividerPositions(0.6, 0.4);
        tab.setContent(split);

        AnchorPane requestAnchor = new AnchorPane();
        requestAnchor.setMinWidth(350.0);

        AnchorPane projectAnchor = new AnchorPane();
        projectAnchor.setMinWidth(350.0);



        Label requestLabel = new Label();
        requestLabel.setText("Request");

        TextArea requestArea = new TextArea();
        requestArea.setId("requestArea");
        requestArea.wrapTextProperty().setValue(true);

        requestAnchor.getChildren().addAll(requestLabel, requestArea); //requestAnchor elements

        AnchorPane.setLeftAnchor(requestLabel, 7.0);
        AnchorPane.setTopAnchor(requestLabel, 7.0);

        AnchorPane.setBottomAnchor(requestArea, 7.0);
        AnchorPane.setLeftAnchor(requestArea, 7.0);
        AnchorPane.setRightAnchor(requestArea, 1.0);
        AnchorPane.setTopAnchor(requestArea, 30.0);


        Label hostLabel = new Label();
        hostLabel.setPrefHeight(25.0);
        hostLabel.setText("Host");

        Label portLabel = new Label();
        portLabel.setPrefHeight(25.0);
        portLabel.setText("Port");

        Label queueManagerLabel = new Label();
        queueManagerLabel.setPrefHeight(25.0);
        queueManagerLabel.setText("Queue Manager");

        Label channelLabel = new Label();
        channelLabel.setPrefHeight(25.0);
        channelLabel.setText("Channel");

        Label transportTypeLabel = new Label();
        transportTypeLabel.setPrefHeight(25.0);
        transportTypeLabel.setText("Transport Type");

        Label queueNameLabel = new Label();
        queueNameLabel.setPrefHeight(25.0);
        queueNameLabel.setText("Queue Name");

        Label userIdLabel = new Label();
        userIdLabel.setPrefHeight(25.0);
        userIdLabel.setText("User ID");

        Label passwordLabel = new Label();
        passwordLabel.setPrefHeight(25.0);
        passwordLabel.setText("Password");

        TextField hostField = new TextField();
        hostField.setId("hostField");
        TextField portField = new TextField();
        portField.setId("portField");
        TextField queueManagerField = new TextField();
        queueManagerField.setId("queueManagerField");
        TextField channelField = new TextField();
        channelField.setId("channelField");
        TextField transportTypeField = new TextField();
        transportTypeField.setId("transportTypeField");
        TextField queueNameField = new TextField();
        queueNameField.setId("queueNameField");
        TextField userIdField = new TextField();
        userIdField.setId("userIdField");
        TextField passwordField = new TextField();
        passwordField.setId("passwordField");


        TextArea responseArea = new TextArea();
        responseArea.wrapTextProperty().setValue(true);
        projectAnchor.getChildren().addAll(hostLabel, portLabel, queueManagerLabel, channelLabel, transportTypeLabel, queueNameLabel, userIdLabel, passwordLabel,
                hostField, portField, queueManagerField, channelField, transportTypeField, queueNameField, userIdField, passwordField); //responseAnchor elements

        AnchorPane.setLeftAnchor(hostLabel, 1.0);
        AnchorPane.setTopAnchor(hostLabel, 7.0);

        AnchorPane.setLeftAnchor(portLabel, 1.0);
        AnchorPane.setTopAnchor(portLabel, 33.0);

        AnchorPane.setLeftAnchor(queueManagerLabel, 1.0);
        AnchorPane.setTopAnchor(queueManagerLabel, 59.0);

        AnchorPane.setLeftAnchor(channelLabel, 1.0);
        AnchorPane.setTopAnchor(channelLabel, 85.0);

        AnchorPane.setLeftAnchor(transportTypeLabel, 1.0);
        AnchorPane.setTopAnchor(transportTypeLabel, 111.0);

        AnchorPane.setLeftAnchor(queueNameLabel, 1.0);
        AnchorPane.setTopAnchor(queueNameLabel, 137.0);

        AnchorPane.setLeftAnchor(userIdLabel, 1.0);
        AnchorPane.setTopAnchor(userIdLabel, 177.0);

        AnchorPane.setLeftAnchor(passwordLabel, 1.0);
        AnchorPane.setTopAnchor(passwordLabel, 203.0);


        AnchorPane.setLeftAnchor(hostField, 87.0);
        AnchorPane.setTopAnchor(hostField, 7.0);
        AnchorPane.setRightAnchor(hostField, 7.0);

        AnchorPane.setLeftAnchor(portField, 87.0);
        AnchorPane.setTopAnchor(portField, 33.0);
        AnchorPane.setRightAnchor(portField, 7.0);

        AnchorPane.setLeftAnchor(queueManagerField, 87.0);
        AnchorPane.setTopAnchor(queueManagerField, 59.0);
        AnchorPane.setRightAnchor(queueManagerField, 7.0);

        AnchorPane.setLeftAnchor(channelField, 87.0);
        AnchorPane.setTopAnchor(channelField, 85.0);
        AnchorPane.setRightAnchor(channelField, 7.0);

        AnchorPane.setLeftAnchor(transportTypeField, 87.0);
        AnchorPane.setTopAnchor(transportTypeField, 111.0);
        AnchorPane.setRightAnchor(transportTypeField, 7.0);

        AnchorPane.setLeftAnchor(queueNameField, 87.0);
        AnchorPane.setTopAnchor(queueNameField, 137.0);
        AnchorPane.setRightAnchor(queueNameField, 7.0);

        AnchorPane.setLeftAnchor(userIdField, 87.0);
        AnchorPane.setTopAnchor(userIdField, 177.0);
        AnchorPane.setRightAnchor(userIdField, 7.0);

        AnchorPane.setLeftAnchor(passwordField, 87.0);
        AnchorPane.setTopAnchor(passwordField, 203.0);
        AnchorPane.setRightAnchor(passwordField, 7.0);


        split.getItems().addAll(requestAnchor, projectAnchor); // split elements


        if (someTabPane.getTabs().size()>1)
            someTabPane.getTabs().add(someTabPane.getTabs().size()-1, tab);
        else
            someTabPane.getTabs().add(tab);
        tab.setText("default");
        return tab;
    }

    public void setJmsUpperElement(Node node){
        upperElement = node;
    }
}
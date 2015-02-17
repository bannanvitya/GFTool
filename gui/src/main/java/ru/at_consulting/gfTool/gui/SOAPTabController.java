package ru.at_consulting.gfTool.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.at_consulting.gfTool.soapclient.SoapProfile;
import sun.plugin.javascript.navig.Anchor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class SOAPTabController implements Initializable {
    private Node upperElement;
    private Map<String, String> messagesMap;
    private File wsdl;
    private SoapProfile profile = new SoapProfile();



    TabPane projects = new TabPane();
    @FXML public Label soapProjectStateLabel;
    @FXML public MenuItem soapProjectNew;
    @FXML public MenuItem soapProjectOpen;
    @FXML public MenuItem soapProjectSave;
    @FXML public AnchorPane soapProjectAnchorPane;
    @FXML public TextArea soapMessageField;

    public Tab addTab(TabPane tp){
        Tab tab = new Tab();

        AnchorPane tabAnchor = new AnchorPane();
        tabAnchor.setMinWidth(200.0);
        tab.setContent(tabAnchor);

        AnchorPane projectRequestsAnchorPane = new AnchorPane();

        TextField projectNameField = new TextField();
        projectNameField.setPromptText("Project Name");
        projectNameField.setMinWidth(150.0);


        TextField wsdlField = new TextField();
        wsdlField.setPromptText("WSDL");
        wsdlField.setMinWidth(200.0);
        wsdlField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (wsdl != null) {
                    projectNameField.setText(wsdl.getName());
                    profile.setUrl(wsdl.getAbsolutePath());
                } else if (wsdlField.getText().contains("?wsdl")){
                    projectNameField.setText(wsdlField.getText().substring(0, wsdlField.getText().indexOf("?wsdl")));
                    profile.setUrl(wsdlField.getText());
                }
            }
        });


        Button getItButton = new Button();
        getItButton.setText("Get It!");
        getItButton.setMinWidth(60.0);
        getItButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!profile.getId().equals("Profile not init")) {
                    profile.processWsdl();
                    profile.processMessagesMap();
                    messagesMap = profile.getMessagesMap();

                    int i = 0;
                    for(Map.Entry<String, String> entry : messagesMap.entrySet()) {
                        i++;
                        String key = entry.getKey();
                        String value = entry.getValue();
                        Button some = new Button();
                        some.setText(key);
                        some.setPrefHeight(25.0);
                        some.setMaxHeight(25.0);
                        some.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                soapMessageField.setText(value);
                            }
                        });
                        projectRequestsAnchorPane.getChildren().add(some);
                        AnchorPane.setLeftAnchor(some, 7.0);
                        AnchorPane.setTopAnchor(some, (7.0+ i*25.0));
                    }
                }

            }
        });


        Button browseButton = new Button();
        browseButton.setText("Browse");
        browseButton.setMinWidth(60.0);
        browseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                wsdl = project.showOpenDialog(stage);
                if (wsdl != null) {
                    wsdlField.setText(wsdl.getAbsolutePath());
                }

            }
        });







        tabAnchor.getChildren().addAll(projectNameField, wsdlField, getItButton, projectRequestsAnchorPane, browseButton);

        AnchorPane.setRightAnchor(getItButton, 1.0);
        AnchorPane.setTopAnchor(getItButton, 70.0);

        AnchorPane.setLeftAnchor(browseButton, 7.0);
        AnchorPane.setTopAnchor(browseButton, 35.0);

        AnchorPane.setLeftAnchor(projectNameField, 7.0);
        AnchorPane.setRightAnchor(projectNameField, 0.0);
        AnchorPane.setTopAnchor(projectNameField, 7.0);

        AnchorPane.setLeftAnchor(wsdlField, 70.0);
        AnchorPane.setRightAnchor(wsdlField, 0.0);
        AnchorPane.setTopAnchor(wsdlField, 35.0);

        AnchorPane.setBottomAnchor(projectRequestsAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(projectRequestsAnchorPane, 0.0);
        AnchorPane.setRightAnchor(projectRequestsAnchorPane, 0.0);
        AnchorPane.setTopAnchor(projectRequestsAnchorPane, 88.0);
        tp.getTabs().add(tab);
        tab.setText("default");
        return tab;
    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {


        projects.sideProperty().setValue(Side.LEFT);
        projects.tabClosingPolicyProperty().setValue(TabPane.TabClosingPolicy.SELECTED_TAB);

        Tab addButtonTab = new Tab();
        addButtonTab.setText("+");
        addButtonTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
        public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
            if (new_val) {
                Tab tab = addTab(projects);
                SingleSelectionModel<Tab> selectionModel = projects.getSelectionModel();
                selectionModel.select(tab);
            }
        }
        }
        );

        projects.getTabs().add(addButtonTab);       // add tab to create new tabs
        soapProjectAnchorPane.getChildren().addAll(projects);
        AnchorPane.setTopAnchor(projects, 0.0);
        AnchorPane.setBottomAnchor(projects, 7.0);
        AnchorPane.setLeftAnchor(projects, 0.0);
        AnchorPane.setRightAnchor(projects, 1.0);



         soapProjectStateLabel.setText("");

        soapProjectNew.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {

                    FXMLLoader wsdlLoader = new FXMLLoader(getClass().getResource("/fxml/WSDL.fxml"));
                    AnchorPane wsdlPane = wsdlLoader.load();

                    WSDLNewProjectController wsdlController = (WSDLNewProjectController) wsdlLoader.getController();
                    wsdlController.setWsdlInitElement(upperElement);

                    Parent wsdl = wsdlPane;

                    Stage stage = new Stage();
                    stage.setTitle("Create New Project");


                    stage.setScene(new Scene(wsdl, 331, 105));
                    stage.resizableProperty().setValue(false);
                    stage.setAlwaysOnTop(true);
                    stage.centerOnScreen();
                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        public void handle(WindowEvent we) {
                            upperElement.getScene().getRoot().setDisable(false);
                        }
                    });
                    stage.show();

                    upperElement.getScene().getRoot().setDisable(true);
                } catch (IOException e) {
                e.printStackTrace();
            }
            }
        });

    }


    public void setSoapUpperElement(Node node){
        upperElement = node;
    }


}
package gfTool.gui;

import com.predic8.wsdl.Binding;
import gfTool.api.*;
import gfTool.soapclient.*;
import groovy.util.MapEntry;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codehaus.jackson.map.ObjectMapper;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class SOAPTabController implements Initializable, ClientTabControllerApi {
    private Node upperElement;
    private static Map<String, SoapClient> projectMap = new HashMap<String, SoapClient>();
    private static Map<String, File> wsdlsMap = new HashMap<String, File>();;

    public static Map<String, SoapClient> getProjectMap(){
        return projectMap;
    }

    public static Map<String, File> getWsdlsMap(){
        return wsdlsMap;
    }


    TabPane soapMainTabPane = new TabPane();
    Tab soapAddButtonTab = new Tab();
    AnchorPane soapInnerPane = new AnchorPane();

    @FXML public Label soapProjectStateLabel;
    @FXML public MenuItem soapProjectOpen;
    @FXML public MenuItem soapProjectSave;
    @FXML public Button soapButton;
    @FXML public VBox soapVBox;

    public Tab addTab(String id, TabPane someTabPane) {
        Tab tab = new Tab();
        tab.setId(id);

        SplitPane split = new SplitPane();
        split.setDividerPositions(0.3, 0.4, 0.3);
        tab.setContent(split);

        AnchorPane projectAnchor = new AnchorPane();
         projectAnchor.setMinWidth(300.0);

        AnchorPane requestAnchor = new AnchorPane();
        requestAnchor.setMinWidth(400.0);

        AnchorPane responseAnchor = new AnchorPane();
        responseAnchor.setMinWidth(200.0);

        TextField requestUrlField = new TextField();
        requestUrlField.setId("requestUrlField");
        requestUrlField.setPromptText("URL");

        Label requestLabel = new Label();
        requestLabel.setText("Request");

        TextArea requestArea = new TextArea();
        requestArea.setId("requestArea");
        requestArea.wrapTextProperty().setValue(true);

        requestAnchor.getChildren().addAll(requestUrlField, requestLabel, requestArea); //requestAnchor elements

        AnchorPane.setLeftAnchor(requestUrlField, 1.0);
        AnchorPane.setRightAnchor(requestUrlField, 1.0);
        AnchorPane.setTopAnchor(requestUrlField, 36.0);

        AnchorPane.setLeftAnchor(requestLabel, 1.0);
        AnchorPane.setTopAnchor(requestLabel, 83.0);

        AnchorPane.setBottomAnchor(requestArea, 7.0);
        AnchorPane.setLeftAnchor(requestArea, 1.0);
        AnchorPane.setRightAnchor(requestArea, 1.0);
        AnchorPane.setTopAnchor(requestArea, 110.0);


        Label responseLabel = new Label();
        responseLabel.setText("Response");

        TextArea responseArea = new TextArea();
        responseArea.setId("responseArea");
        responseArea.wrapTextProperty().setValue(true);
        responseAnchor.getChildren().addAll(responseLabel, responseArea); //responseAnchor elements

        AnchorPane.setLeftAnchor(responseLabel, 1.0);
        AnchorPane.setTopAnchor(responseLabel, 83.0);

        AnchorPane.setBottomAnchor(responseArea, 7.0);
        AnchorPane.setLeftAnchor(responseArea, 1.0);
        AnchorPane.setRightAnchor(responseArea, 1.0);
        AnchorPane.setTopAnchor(responseArea, 110.0);


        split.getItems().addAll(projectAnchor, requestAnchor, responseAnchor); // split elements


        TextField projectNameField = new TextField();
        projectNameField.setId("projectNameField");
        projectNameField.setPromptText("Project Name");
        projectNameField.setMinWidth(150.0);


        Button projectNameButton = new Button("OK");
        projectNameButton.setDisable(true);
        projectNameButton.setMinWidth(60.0);


        TextField wsdlField = new TextField();
        wsdlField.setDisable(true);
        wsdlField.setId("wsdlField");
        wsdlField.setPromptText("WSDL");
        wsdlField.setMinWidth(200.0);



        TreeView<String> treeView = new TreeView<String>(); // create empty treeView
        TreeItem<String> root = new TreeItem<String>(); // root treeItem
        treeView.setRoot(root);
        root.setExpanded(true);

        Button getItButton = new Button();
        getItButton.setDisable(true);
        getItButton.setText("Get It!");
        getItButton.setMinWidth(60.0);



        requestArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) { /*
                if (messagesMap != null) {
                    String opName = treeView.getSelectionModel().selectedItemProperty().getValue().getValue();
                    messagesMap.replace(opName, newValue);
                    profile.setMessagesMap(messagesMap);
                } */
            }
        });


        Button browseButton = new Button();
        browseButton.setDisable(true);
        browseButton.setText("Browse");
        browseButton.setMinWidth(60.0);
        browseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File wsdl = project.showOpenDialog(stage);
                if (wsdl != null) {
                getWsdlsMap().put(projectNameField.getText(), wsdl);
                wsdlField.setText(wsdl.getAbsolutePath());
                }
            }
        });



        projectNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (newValue.equals("")){
                    projectNameField.setPromptText("Project Name: You HAVE to fill it!");
                    projectNameButton.setDisable(true);
                    browseButton.setDisable(true);
                    wsdlField.setDisable(true);
                    getItButton.setDisable(true);
                } else
                {
                    projectNameButton.setDisable(false);
                }
            }
        });

        wsdlField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (newValue.equals("")){
                    projectNameField.setPromptText("You HAVE to fill it too!");
                    getItButton.setDisable(true);
                } else
                {
                    getItButton.setDisable(false);
                }
            }
        });

        projectNameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                browseButton.setDisable(false);
                wsdlField.setDisable(false);
                getProjectMap().put(projectNameField.getText(), new SoapClient());
            }
        });

        wsdlField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (!newValue.equals(""))
                    getItButton.setDisable(false);
                else
                    getItButton.setDisable(true);
            }
        });

        getItButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File wsdl = getWsdlsMap().get(projectNameField.getText());
                SoapClient soap = getProjectMap().get(projectNameField.getText());
                SoapProfile profile = (SoapProfile) soap.getProfile();
                if (wsdl != null) {
                    projectNameField.setText(wsdl.getName());
                    profile.setUrlToWsdl(wsdl.getAbsolutePath());
                } else if (wsdlField.getText().contains("?wsdl")){
                    projectNameField.setText(wsdlField.getText().substring(0, wsdlField.getText().indexOf("?wsdl")));
                    profile.setUrlToWsdl(wsdlField.getText());
                } else if (wsdlField.getText().contains("?WSDL")) {
                    projectNameField.setText(wsdlField.getText().substring(0, wsdlField.getText().indexOf("?WSDL")));
                    profile.setUrlToWsdl(wsdlField.getText());
                }
                if (!profile.getId().equals("Profile not init")) {
                    profile.processWsdl();
                    profile.processMessagesConfigMap();
                    profile.processMessagesMap();
                    profile.processBindings();

                    Map<String, SoapMsgConfig> messagesConfigMap = profile.getMessagesConfigMap();
                    Map<String, String> messagesMap = profile.getMessagesMap();
                    List<Binding> bindings = profile.getBindings();

                    root.setValue(wsdlField.getText());
                    tab.setText(projectNameField.getText());

                    treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                        @Override
                        public void changed(ObservableValue observable, Object oldValue,
                                            Object newValue) {

                            TreeItem<String> selectedItem = (TreeItem<String>) newValue;
                            requestArea.setText(messagesMap.get(selectedItem.getValue()));
                        }

                    });

                    for(Binding entry : bindings) {
                        TreeItem<String> bnd = new TreeItem<String>(entry.getName());
                        bnd.setExpanded(false);
                        for(Map.Entry<String, SoapMsgConfig> ent : messagesConfigMap.entrySet()){
                            SoapMsgConfig forOperation = ent.getValue();
                            if (entry.getName() == forOperation.getPort().getBinding().getName()){

                                TreeItem<String> op = new TreeItem<String>(forOperation.getBindOp().getName());
                                bnd.getChildren().add(op);
                            }
                        }
                    root.getChildren().add(bnd);
                    }
                }
            }
        });
        projectAnchor.getChildren().addAll(projectNameField, projectNameButton, wsdlField, getItButton, treeView, browseButton); //projectAnchor elements



        AnchorPane.setRightAnchor(getItButton, 1.0);
        AnchorPane.setTopAnchor(getItButton, 70.0);

        AnchorPane.setLeftAnchor(browseButton, 7.0);
        AnchorPane.setTopAnchor(browseButton, 35.0);

        AnchorPane.setLeftAnchor(projectNameField, 7.0);
        AnchorPane.setRightAnchor(projectNameField, 63.0);
        AnchorPane.setTopAnchor(projectNameField, 7.0);

        AnchorPane.setLeftAnchor(wsdlField, 70.0);
        AnchorPane.setRightAnchor(wsdlField, 0.0);
        AnchorPane.setTopAnchor(wsdlField, 35.0);

        AnchorPane.setBottomAnchor(treeView, 7.0);
        AnchorPane.setLeftAnchor(treeView, 1.0);
        AnchorPane.setRightAnchor(treeView, 0.0);
        AnchorPane.setTopAnchor(treeView, 110.0);

        AnchorPane.setRightAnchor(projectNameButton, 1.0);
        AnchorPane.setTopAnchor(projectNameButton, 7.0);






        if (someTabPane.getTabs().size()>1)
            someTabPane.getTabs().add(someTabPane.getTabs().size()-1, tab);
        else
            someTabPane.getTabs().add(tab);
        tab.setText("default");
        return tab;
    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        soapMainTabPane.sideProperty().setValue(Side.LEFT);
        soapMainTabPane.tabClosingPolicyProperty().setValue(TabPane.TabClosingPolicy.SELECTED_TAB);
        soapInnerPane.getChildren().addAll(soapMainTabPane);
        AnchorPane.setBottomAnchor(soapMainTabPane, 0.0);
        AnchorPane.setLeftAnchor(soapMainTabPane, 0.0);
        AnchorPane.setRightAnchor(soapMainTabPane, 0.0);
        AnchorPane.setTopAnchor(soapMainTabPane, 0.0);

        soapAddButtonTab.setText("+");
        soapAddButtonTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
        public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
            SingleSelectionModel<Tab> selectionModel = soapMainTabPane.getSelectionModel();
            if (new_val){
                Date now = new Date();
                Tab tab = addTab(now.toString(), soapMainTabPane);
                selectionModel.select(tab);
                }
            }
        }
        );

        soapButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                /* SingleSelectionModel<Tab> selectionModel = soapMainTabPane.getSelectionModel();
                SplitPane split = (SplitPane)soapMainTabPane.getTabs().get(selectionModel.getSelectedIndex()).getContent();
                AnchorPane requestAnchor = (AnchorPane) split.getItems().get(1);
                TextField urlField = (TextField) requestAnchor.getChildren().get(0);
                TextArea requestArea = (TextArea) requestAnchor.getChildren().get(2);

                AnchorPane responseAnchor = (AnchorPane) split.getItems().get(2);
                TextArea responseArea = (TextArea) responseAnchor.getChildren().get(1);

                try {
                    client.preconditions();

                    profile.setId(urlField.getText());
                    client.setProfile(profile);


                    SoapRequest req = new SoapRequest(requestArea.getText());
                    SoapResponse resp = (SoapResponse) client.sendRequest(req, urlField.getText());
                    responseArea.setText(resp.getMessage());

                    client.postconditions();
                }catch(ProfileStructureException | ProfileNotFoundException | SendRequestException | PostconditionsException e){
                    e.printStackTrace();
                } */

            }
        });

        soapProjectSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                /* FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File file = project.showSaveDialog(stage);



                    File resultFile = new File(System.getenv("GFTOOL_ROOT") + "/serz/soap.tab.objects");
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        mapper.writeValue(resultFile, profile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    SaveAndOpen.projectGlobalSave(file.getPath(), soapMainTabPane, SOAPTabController.this);
                    soapProjectStateLabel.setText(file.getName() + "  ");
                } */
            }
        });


        soapProjectOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File file = project.showOpenDialog(stage);

                if (file != null){
                    SaveAndOpen.projectGlobalOpen(file.getPath(), soapMainTabPane, SOAPTabController.this);
                    SaveAndOpen.projectGlobalSave(System.getenv("GFTOOL_ROOT") + "/serz/soap.tab.objects", soapMainTabPane, SOAPTabController.this);
                    soapProjectStateLabel.setText(file.getName() + "  ");
                }
            }
        });


        soapVBox.getChildren().addAll(soapInnerPane);
        VBox.setVgrow(soapInnerPane, Priority.ALWAYS);
        SaveAndOpen.projectGlobalOpen(System.getenv("GFTOOL_ROOT") + "/serz/soap.tab.objects", soapMainTabPane, SOAPTabController.this);


        if (soapMainTabPane.getTabs().size() == 0) {
            Date now = new Date();
            Tab tab = addTab(now.toString(), soapMainTabPane);
        }
        soapMainTabPane.getTabs().add(soapAddButtonTab);


        SingleSelectionModel<Tab> selectionModel = soapMainTabPane.getSelectionModel();
        selectionModel.select(soapMainTabPane.getTabs().indexOf(soapAddButtonTab) - 1); // add tab to create new tabs

        soapProjectStateLabel.setText("");

    }


    public void setSoapUpperElement(Node node){
        upperElement = node;
    }





}
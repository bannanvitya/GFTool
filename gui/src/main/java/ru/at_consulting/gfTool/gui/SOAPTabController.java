package ru.at_consulting.gfTool.gui;

import com.predic8.wsdl.Binding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import ru.at_consulting.gfTool.api.PostconditionsException;
import ru.at_consulting.gfTool.api.ProfileNotFoundException;
import ru.at_consulting.gfTool.api.ProfileStructureException;
import ru.at_consulting.gfTool.api.SendRequestException;
import ru.at_consulting.gfTool.soapclient.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class SOAPTabController implements Initializable {
    private Node upperElement;
    private Map<String, String> messagesMap;
    private Map<String, SoapMsgConfig> messagesConfigMap;
    private List<Binding> bindings;
    private File wsdl;
    private SoapProfile profile = new SoapProfile();
    private SoapClient client = new SoapClient();



    TabPane soapMainTabPane = new TabPane();
    Tab addButtonTab = new Tab();
    AnchorPane innerPane = new AnchorPane();
    @FXML public Label soapProjectStateLabel;
    @FXML public MenuItem soapProjectOpen;
    @FXML public MenuItem soapProjectSave;
    @FXML public Button soapButton;
    @FXML public VBox soapVBox;

    public Tab addTab(){
        Tab tab = new Tab();



        SplitPane split = new SplitPane();
        split.setDividerPositions(0.3, 0.4, 0.3);
        innerPane.getChildren().addAll(split);
        AnchorPane.setBottomAnchor(split, 0.0);
        AnchorPane.setLeftAnchor(split, 0.0);
        AnchorPane.setRightAnchor(split, 0.0);
        AnchorPane.setTopAnchor(split, 0.0);

        tab.setContent(split);

        AnchorPane projectAnchor = new AnchorPane();
         projectAnchor.setMinWidth(300.0);

        AnchorPane requestAnchor = new AnchorPane();
        requestAnchor.setMinWidth(400.0);

        AnchorPane responseAnchor = new AnchorPane();
        responseAnchor.setMinWidth(200.0);

        TextField requestUrlField = new TextField();
        requestUrlField.setPromptText("URL");

        Label requestLabel = new Label();
        requestLabel.setText("Request");

        TextArea requestArea = new TextArea();

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

        responseAnchor.getChildren().addAll(responseLabel, responseArea); //responseAnchor elements

        AnchorPane.setLeftAnchor(responseLabel, 1.0);
        AnchorPane.setTopAnchor(responseLabel, 83.0);

        AnchorPane.setBottomAnchor(responseArea, 7.0);
        AnchorPane.setLeftAnchor(responseArea, 1.0);
        AnchorPane.setRightAnchor(responseArea, 1.0);
        AnchorPane.setTopAnchor(responseArea, 110.0);


        split.getItems().addAll(projectAnchor, requestAnchor, responseAnchor); // split elements


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
                    profile.setUrlToWsdl(wsdl.getAbsolutePath());
                } else if (wsdlField.getText().contains("?wsdl")){
                    projectNameField.setText(wsdlField.getText().substring(0, wsdlField.getText().indexOf("?wsdl")));
                    profile.setUrlToWsdl(wsdlField.getText());
                }
            }
        });



        TreeView<String> treeView = new TreeView<String>(); // create empty treeView
        TreeItem<String> root = new TreeItem<String>(); // root treeItem
        treeView.setRoot(root);
        root.setExpanded(true);

        Button getItButton = new Button();
        getItButton.setText("Get It!");
        getItButton.setMinWidth(60.0);
        getItButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!profile.getId().equals("Profile not init")) {
                    profile.processWsdl();
                    profile.processMessagesConfigMap();
                    profile.processMessagesMap();
                    profile.processBindings();

                    messagesConfigMap = profile.getMessagesConfigMap();
                    messagesMap = profile.getMessagesMap();
                    bindings = profile.getBindings();

                    root.setValue(projectNameField.getText());
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



        projectAnchor.getChildren().addAll(projectNameField, wsdlField, getItButton, treeView, browseButton); //projectAnchor elements

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

        AnchorPane.setBottomAnchor(treeView, 7.0);
        AnchorPane.setLeftAnchor(treeView, 10.0);
        AnchorPane.setRightAnchor(treeView, 0.0);
        AnchorPane.setTopAnchor(treeView, 110.0);






        if (soapMainTabPane.getTabs().size()>1)
        soapMainTabPane.getTabs().add(soapMainTabPane.getTabs().size()-1, tab);
        else
        soapMainTabPane.getTabs().add(tab);
        tab.setText("default");
        return tab;
    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {


        soapMainTabPane.sideProperty().setValue(Side.LEFT);
        soapMainTabPane.tabClosingPolicyProperty().setValue(TabPane.TabClosingPolicy.SELECTED_TAB);

        addButtonTab.setText("+");
        addButtonTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
        public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
            SingleSelectionModel<Tab> selectionModel = soapMainTabPane.getSelectionModel();
            if (new_val){
                Tab tab = addTab();
                selectionModel.select(tab);
                }
            }
        }
        );

        soapButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SingleSelectionModel<Tab> selectionModel = soapMainTabPane.getSelectionModel();
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
                }

            }
        });


        /*soapUrlField.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue ov, String old_val, String new_val) {
                SingleSelectionModel<Tab> selectionModel = projects.getSelectionModel();
                if (!new_val.equals("")){
                    // handling url changes here
                }
                }
            }
        );
        */


        soapVBox.getChildren().addAll(innerPane);
        VBox.setVgrow(innerPane, Priority.ALWAYS);
        addTab();
        soapMainTabPane.getTabs().add(addButtonTab);
        SingleSelectionModel<Tab> selectionModel = soapMainTabPane.getSelectionModel();
        selectionModel.select(soapMainTabPane.getTabs().indexOf(addButtonTab) - 1); // add tab to create new tabs
        AnchorPane.setTopAnchor(soapMainTabPane, 0.0);
        AnchorPane.setBottomAnchor(soapMainTabPane, 7.0);
        AnchorPane.setLeftAnchor(soapMainTabPane, 0.0);
        AnchorPane.setRightAnchor(soapMainTabPane, 1.0);

        soapProjectStateLabel.setText("");

    }


    public void setSoapUpperElement(Node node){
        upperElement = node;
    }


}
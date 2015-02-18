package ru.at_consulting.gfTool.gui;

import com.predic8.wsdl.Binding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.at_consulting.gfTool.soapclient.SoapMsgConfig;
import ru.at_consulting.gfTool.soapclient.SoapProfile;
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



    TabPane projects = new TabPane();
    @FXML public Label soapProjectStateLabel;
    @FXML public MenuItem soapProjectOpen;
    @FXML public MenuItem soapProjectSave;
    @FXML public AnchorPane soapProjectAnchorPane;
    @FXML public TextArea soapMessageField;

    public Tab addTab(TabPane tp){
        Tab tab = new Tab();

        AnchorPane tabAnchor = new AnchorPane();
        tabAnchor.setMinWidth(200.0);
        tab.setContent(tabAnchor);

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



        TreeView<String> treeView = new TreeView<String>();
        TreeItem<String> root = new TreeItem<String>();
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

                    root.setValue(wsdlField.getText().substring(wsdlField.getText().lastIndexOf("/") + 1, wsdlField.getText().indexOf("?wsdl")));
                    tab.setText(wsdlField.getText().substring(wsdlField.getText().lastIndexOf("/") + 1, wsdlField.getText().indexOf("?wsdl")));

                    treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                        @Override
                        public void changed(ObservableValue observable, Object oldValue,
                                            Object newValue) {

                            TreeItem<String> selectedItem = (TreeItem<String>) newValue;
                            soapMessageField.setText(messagesMap.get(selectedItem.getValue()));
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



        tabAnchor.getChildren().addAll(projectNameField, wsdlField, getItButton, treeView, browseButton);

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

        AnchorPane.setBottomAnchor(treeView, 0.0);
        AnchorPane.setLeftAnchor(treeView, 10.0);
        AnchorPane.setRightAnchor(treeView, 0.0);
        AnchorPane.setTopAnchor(treeView, 110.0);
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

    }


    public void setSoapUpperElement(Node node){
        upperElement = node;
    }


}
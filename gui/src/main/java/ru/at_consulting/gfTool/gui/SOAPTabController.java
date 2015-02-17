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

        VBox box = new VBox();

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


                    box.fillWidthProperty().setValue(true);
                    AnchorPane.setTopAnchor(box, 88.0);
                    for(Binding entry : bindings) {

                        AnchorPane pane = new AnchorPane();
                        box.getChildren().add(pane);

                        Label binding = new Label();
                        binding.setText(entry.getName());
                        binding.setPrefHeight(12.0);
                        binding.setMaxHeight(12.0);

                        AnchorPane operationsPane = new AnchorPane();


                        Button wrap = new Button();
                        wrap.setText("+");
                        wrap.setMaxWidth(12.0);
                        wrap.setMaxHeight(12.0);
                        wrap.setPrefWidth(12.0);
                        wrap.setPrefHeight(12.0);
                        wrap.setPadding(Insets.EMPTY);
                        wrap.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                if (wrap.getText().equals("+")) {
                                    int bind = 0;
                                    for(Map.Entry<String, SoapMsgConfig> ent : messagesConfigMap.entrySet()){
                                        SoapMsgConfig forOperation = ent.getValue();
                                        if (entry.getName() == forOperation.getPort().getBinding().getName()){
                                            Button op = new Button();
                                            op.setPrefHeight(10.0);
                                            op.setMaxHeight(10.0);
                                            op.setPadding(Insets.EMPTY);
                                            op.setText(forOperation.getBindOp().getName());
                                            op.setOnAction(new EventHandler<ActionEvent>() {
                                                @Override
                                                public void handle(ActionEvent event) {
                                                   soapMessageField.setText(messagesMap.get(forOperation.getBindOp().getName()));
                                                }
                                            });
                                            operationsPane.getChildren().add(op);
                                            AnchorPane.setLeftAnchor(op, 10.0);
                                            AnchorPane.setTopAnchor(op, 2.0 + bind*18.0);
                                            bind++;
                                        }
                                    }
                                    wrap.setText("-");
                                }
                                else{
                                    while (!operationsPane.getChildren().isEmpty()) {
                                        operationsPane.getChildren().remove(operationsPane.getChildren().get(0));
                                    }
                                    wrap.setText("+");
                                }

                            }
                        });



                        pane.getChildren().addAll(binding, wrap, operationsPane);

                        AnchorPane.setLeftAnchor(binding, 14.0);
                        AnchorPane.setTopAnchor(binding, 2.0);
                        AnchorPane.setLeftAnchor(wrap, 2.0);
                        AnchorPane.setTopAnchor(wrap, 2.0);
                        AnchorPane.setLeftAnchor(operationsPane, 7.0);
                        AnchorPane.setTopAnchor(operationsPane, 16.0);
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







        tabAnchor.getChildren().addAll(projectNameField, wsdlField, getItButton, box, browseButton);

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

        AnchorPane.setBottomAnchor(box, 0.0);
        AnchorPane.setLeftAnchor(box, 0.0);
        AnchorPane.setRightAnchor(box, 0.0);
        AnchorPane.setTopAnchor(box, 88.0);
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
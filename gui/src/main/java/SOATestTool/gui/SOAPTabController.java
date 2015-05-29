package SOATestTool.gui;

import SOATestTool.api.PostconditionsException;
import SOATestTool.api.ProfileNotFoundException;
import SOATestTool.api.ProfileStructureException;
import SOATestTool.api.SendRequestException;
import SOATestTool.soapclient.*;
import com.predic8.wsdl.Binding;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codehaus.jackson.map.ObjectMapper;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class SOAPTabController implements Initializable, ClientTabControllerApi {
    private Node upperElement;
    private static Map<String, String> projectMap = new HashMap<String, String>();
    private static Map<String, SoapProfile> profilesMap = new HashMap<String, SoapProfile>();

    public static Map<String, SoapProfile> getProfilesMap(){
        return profilesMap;
    }
    public static Map<String, String> getProjectMap(){
        return projectMap;
    }



    TabPane soapMainTabPane = new TabPane();
    Tab soapAddButtonTab = new Tab();
    AnchorPane soapInnerPane = new AnchorPane();

    @FXML public Button soapButton;
    @FXML public Button saveButton;
    @FXML public VBox soapVBox;


    // elements for load
    @FXML public Button soapLoadStartButton;
    @FXML public Button soapLoadStopButton;
    @FXML public TextField soapLoadNeededTpsField;
    @FXML public RadioButton soapLoadByCountRadioButton;
    @FXML public RadioButton soapLoadByDateTimeRadioButton;
    @FXML public TextField soapLoadWhenToStopField;
    @FXML public TextField soapLoadThreadsField;
    @FXML public TextField soapLoadCurrentTpsField;
    @FXML public TextField soapLoadCurrentCountField;
    @FXML public volatile ProgressIndicator soapLoadProgressIndicator;


    // non volatile elements of load
    private boolean soapLoadKeyToStop = false;
    private static Object lockObject = new Object();


    // volatile elements of load
    private volatile DoubleProperty globalTps = new SimpleDoubleProperty(0.0);
    private volatile LongProperty globalCount = new SimpleLongProperty(0);
    private volatile LongProperty localCount = new SimpleLongProperty(0);
    private volatile int numberOfThreads = 0;
    private volatile long duration;
    private volatile long globalDuration = 0;
    private volatile double neededTps;
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private volatile Date globalEnd;
    private volatile Date globalBegin;
    private volatile Date now;
    private volatile long neededCount;
    private volatile Date localBegin;





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

        soapLoadStopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                soapLoadKeyToStop = true;
            }
        });


        soapLoadByCountRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                soapLoadWhenToStopField.setPromptText("count");
            }
        });

        soapLoadByDateTimeRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                soapLoadWhenToStopField.setPromptText("date time");
            }
        });
        
        soapButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sendSoapRequest(false);
            }
        });

        soapLoadStartButton.setOnAction(new EventHandler<ActionEvent>() {


            @Override
            public void handle(ActionEvent event) {

                SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/soap.tab.objects", soapMainTabPane, SOAPTabController.this);
                soapLoadCurrentCountField.setText("0.0");
                soapLoadCurrentTpsField.setText("0.0");

                globalCount.setValue(0);
                globalTps.setValue(0.0);
                numberOfThreads = Integer.parseInt(soapLoadThreadsField.getText());
                for (int j = 0; j< numberOfThreads; j++) {
                    Thread loadThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            soapLoad(soapLoadProgressIndicator, soapLoadCurrentTpsField, soapLoadCurrentCountField);
                        }
                    });
                    loadThread.setDaemon(true);
                    loadThread.start();
                }
                soapLoadKeyToStop = false;
                localCount.setValue(0);
            }
        });

        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                    SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/soap.tab.objects", soapMainTabPane, SOAPTabController.this);
                    File resultFile = new File(System.getenv("SOATOOL_ROOT") + "/serz/soap.profiles.objects");
                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        mapper.writeValue(resultFile, projectMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        });


        soapVBox.getChildren().addAll(soapInnerPane);
        VBox.setVgrow(soapInnerPane, Priority.ALWAYS);

        globalOpen(System.getenv("SOATOOL_ROOT") + "/serz/soap.profiles.objects");


        if (soapMainTabPane.getTabs().size() == 0) {
            Date now = new Date();
            Tab tab = addTab(now.toString(), soapMainTabPane);
        }


        soapMainTabPane.getTabs().add(soapAddButtonTab);

        SingleSelectionModel<Tab> selectionModel = soapMainTabPane.getSelectionModel();
        selectionModel.select(soapMainTabPane.getTabs().indexOf(soapAddButtonTab) - 1); // add tab to create new tabs
    }

    public void globalOpen(String path){
        SaveAndOpen.projectGlobalOpen(System.getenv("SOATOOL_ROOT") + "/serz/soap.tab.objects", soapMainTabPane, SOAPTabController.this);

        File resultFile = new File(path);
        ObjectMapper mapper = new ObjectMapper();
        try {
            projectMap = (Map<String, String>) mapper.readValue(resultFile, Map.class);
            for (Tab currentTab : soapMainTabPane.getTabs()){
                SplitPane split = (SplitPane)currentTab.getContent();

                AnchorPane requestAnchor = (AnchorPane) split.getItems().get(1);
                TextArea requestArea = (TextArea) requestAnchor.getChildren().get(2);

                AnchorPane projectAnchor = (AnchorPane) split.getItems().get(0);
                String projectName = ((TextField) projectAnchor.getChildren().get(0)).getText();
                TreeView<String> treeView = (TreeView<String>) projectAnchor.getChildren().get(4);

                setTreeView(projectName, treeView, currentTab, requestArea);
                System.out.println("Project Name: " + projectName + " Wsdl: " + projectMap.get(projectName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    
    private void soapLoad(ProgressIndicator progressIndicator, TextField tpsField, TextField countField){
        System.out.println(Thread.currentThread().getName() + "  -- Start.");

        try {
            neededTps = Double.parseDouble(soapLoadNeededTpsField.getText());
        } catch (Exception e){
            System.out.println("Fill neededTps Field correctly!");
        }
        now = new Date();
        globalBegin = now;
        System.out.println(Thread.currentThread().getName() + now);

        if (soapLoadByDateTimeRadioButton.isSelected()) {
            try {
                globalEnd = df.parse(soapLoadWhenToStopField.getText());
                globalDuration = Math.abs(now.getTime() - globalEnd.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            localBegin = new Date();
            while (now.getTime() < globalEnd.getTime() && !soapLoadKeyToStop) {
                now = new Date();
                if (localCount.getValue() == 0) {
                    localBegin = now;
                    duration = Math.abs(now.getTime() - localBegin.getTime());
                } else {
                    duration = Math.abs(now.getTime() - localBegin.getTime());
                    if (duration / 1000 > 60.0) {
                        localBegin = now;
                        duration = Math.abs(now.getTime() - localBegin.getTime());
                        synchronized (lockObject) {
                            localCount.setValue(0);
                        }
                    } else {
                        synchronized (lockObject) {
                            globalTps.setValue((localCount.getValue() / (duration / 1000)));
                        }
                    }
                }

                synchronized (lockObject) {
                    localCount.setValue(localCount.getValue() + 1);
                    globalCount.setValue(globalCount.getValue() + 1);


                    Platform.runLater(() -> {
                        progressIndicator.setProgress((double)Math.abs(globalBegin.getTime() - now.getTime())/(double)globalDuration);

                        tpsField.setText(Integer.toString(globalTps.getValue().intValue()));
                        countField.setText(Long.toString(globalCount.getValue()));
                    });
                }
                double temp = 0.0;
                if (globalTps.getValue() > neededTps) {
                    temp = (localCount.getValue() / neededTps - duration / 1000) * 1000;
                    synchronized (lockObject) {
                        globalTps.setValue(neededTps);
                    }
                    long s = (long) temp;
                    try {
                        Thread.sleep(s);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(Thread.currentThread().getName() + "  -- globalCount: " + globalCount.getValue() + " globalTps: " + globalTps.getValue() + " localCount: " + localCount.getValue() + " Sleep for: " + temp);
                try {
                        sendSoapRequest(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {

            neededCount = Long.parseLong(soapLoadWhenToStopField.getText());

            while (globalCount.getValue() < neededCount && !soapLoadKeyToStop) {
                now = new Date();
                if (localCount.getValue() == 0) {
                    localBegin = now;
                    duration = Math.abs(now.getTime() - localBegin.getTime());
                } else {
                    duration = Math.abs(now.getTime() - localBegin.getTime());
                    if (duration / 1000 > 10.0) {
                        localBegin = now;
                        duration = Math.abs(now.getTime() - localBegin.getTime());
                        synchronized (lockObject) {
                            localCount.setValue(0);
                        }
                    } else {
                        synchronized (lockObject) {
                            globalTps.setValue (localCount.getValue() / (((double) duration) / 1000));
                        }
                    }
                    //System.out.println(tps);
                }

                synchronized (lockObject) {
                    localCount.setValue(localCount.getValue() + 1);
                    globalCount.setValue(globalCount.getValue() + 1);
                    progressIndicator.setProgress((double)globalCount.getValue()/(double)neededCount);


                    Platform.runLater(() -> {
                        tpsField.setText(Integer.toString(globalTps.getValue().intValue()));
                        countField.setText(Long.toString(globalCount.getValue()));
                    });
                }


                double tempSleep = 0.0;
                //System.out.println(Thread.currentThread().getName() + "  --  " + globalIterations);
                if (globalTps.getValue() > neededTps) {
                    tempSleep = (localCount.getValue() / neededTps - duration / 1000) * 1000;
                    synchronized (lockObject) {
                        globalTps.setValue(neededTps);
                    }
                    long s = (long) tempSleep;
                    try {
                        Thread.sleep(s);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(Thread.currentThread().getName() + "  -- globalCount: " + globalCount.getValue() + " globalTps: " + globalTps.getValue() + " localCount: " + localCount.getValue() + " Sleep for: " + tempSleep);
                try {
                        sendSoapRequest(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(Thread.currentThread().getName() + "  -- Done.");
    }
    
    
    private void sendSoapRequest(boolean isLoad){
        SingleSelectionModel<Tab> selectionModel = soapMainTabPane.getSelectionModel();
        SplitPane split = (SplitPane)soapMainTabPane.getTabs().get(selectionModel.getSelectedIndex()).getContent();
        AnchorPane requestAnchor = (AnchorPane) split.getItems().get(1);
        TextField urlField = (TextField) requestAnchor.getChildren().get(0);
        TextArea requestArea = (TextArea) requestAnchor.getChildren().get(2);

        AnchorPane responseAnchor = (AnchorPane) split.getItems().get(2);
        TextArea responseArea = (TextArea) responseAnchor.getChildren().get(1);

        AnchorPane projectAnchor = (AnchorPane) split.getItems().get(0);
        TextField projectNameField = (TextField) projectAnchor.getChildren().get(0);


        SoapProfile profile = getProfilesMap().get(projectNameField.getText());
        SoapClient client = new SoapClient();
        client.setProfile(profile);

        try {
            synchronized (lockObject) {
                client.preconditions();

                profile.setId(urlField.getText());
                client.setProfile(profile);


                SoapRequest req = new SoapRequest(requestArea.getText());
                SoapResponse resp = (SoapResponse) client.sendRequest(req, urlField.getText());

                if (!isLoad)
                    responseArea.setText(resp.getMessage());
                client.postconditions();
            }
        }catch(ProfileStructureException | ProfileNotFoundException | SendRequestException | PostconditionsException e){
            e.printStackTrace();
        }

    }

    public Tab addTab(String id, TabPane someTabPane) {
        Tab tab = new Tab();
        tab.setId(id);

        SplitPane split = new SplitPane();
        split.setDividerPositions(0.25f, 0.7333f);
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
                    getProjectMap().replace(projectNameField.getText(), wsdl.getAbsolutePath());
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

        projectNameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                browseButton.setDisable(false);
                wsdlField.setDisable(false);
                getProjectMap().put(projectNameField.getText(), "");
                projectNameField.setDisable(true);
            }
        });

        wsdlField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (!newValue.equals("")) {
                    getItButton.setDisable(false);
                    if (newValue.contains("?wsdl") || newValue.contains("?WSDL"))
                        getProjectMap().replace(projectNameField.getText(), newValue);
                }
                else {
                    getItButton.setDisable(true);
                    wsdlField.setPromptText("You HAVE to fill it too!");
                }
            }
        });

        getItButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setTreeView(projectNameField.getText(), treeView, tab, requestArea);
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


    public void setTreeView(String projectName,TreeView<String> treeView, Tab tab, TextArea requestArea) {
        String wsdlUrl = getProjectMap().get(projectName);

        SoapProfile profile = new SoapProfile();
        profile.setUrlToWsdl(wsdlUrl);
        try {
            profile.setId(wsdlUrl);
        } catch (ProfileNotFoundException e) {
            e.printStackTrace();
        } catch (ProfileStructureException e) {
            e.printStackTrace();
        }


        if (!profile.getId().equals("Profile not init")) {
            profile.processWsdl();
            profile.processMessagesConfigMap();
            profile.processMessagesMap();
            profile.processBindings();

            getProfilesMap().put(projectName, profile);

            Map<String, SoapMsgConfig> messagesConfigMap = profile.getMessagesConfigMap();
            Map<String, String> messagesMap = profile.getMessagesMap();
            List<Binding> bindings = profile.getBindings();

            treeView.getRoot().setValue(projectName);
            tab.setText(projectName);

            treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                @Override
                public void changed(ObservableValue observable, Object oldValue,
                                    Object newValue) {

                    TreeItem<String> selectedItem = (TreeItem<String>) newValue;
                    requestArea.setText(messagesMap.get(selectedItem.getValue()));
                }
            });

            for (Binding entry : bindings) {
                TreeItem<String> bnd = new TreeItem<String>(entry.getName());
                bnd.setExpanded(false);
                for (Map.Entry<String, SoapMsgConfig> ent : messagesConfigMap.entrySet()) {
                    SoapMsgConfig forOperation = ent.getValue();
                    if (entry.getName() == forOperation.getPort().getBinding().getName()) {

                        TreeItem<String> op = new TreeItem<String>(forOperation.getBindOp().getName());
                        bnd.getChildren().add(op);
                    }
                }
                treeView.getRoot().getChildren().add(bnd);
            }
        }
    }

    public void setSoapUpperElement(Node node){
        upperElement = node;
    }

}
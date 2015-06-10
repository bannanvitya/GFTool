package SOATestTool.gui;

import SOATestTool.Common.LinearRandomInt;
import SOATestTool.Common.LinearRandomString;
import SOATestTool.Common.NormalDistribution;
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
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.codehaus.jackson.map.ObjectMapper;
import SOATestTool.IBMMqClient.IBMMqClient;
import SOATestTool.IBMMqClient.IBMMqProfile;
import SOATestTool.IBMMqClient.IBMMqRequest;
import SOATestTool.api.PostconditionsException;
import SOATestTool.api.ProfileNotFoundException;
import SOATestTool.api.ProfileStructureException;
import SOATestTool.api.SendRequestException;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class JMSTabController implements Initializable, ClientTabControllerApi {
    private Node upperElement;


    TabPane jmsMainTabPane = new TabPane();
    Tab jmsAddButtonTab = new Tab();
    AnchorPane jmsInnerPane = new AnchorPane();


    @FXML public Button jmsButton;
    @FXML public MenuItem jmsProjectOpen;
    @FXML public MenuItem jmsProjectSave;
    @FXML public Label jmsProjectStateLabel;

    // elements for load
    @FXML public Button jmsLoadStartButton;
    @FXML public Button jmsLoadStopButton;
    @FXML public TextField jmsLoadNeededTpsField;
    @FXML public RadioButton jmsLoadByCountRadioButton;
    @FXML public RadioButton jmsLoadByDateTimeRadioButton;
    @FXML public TextField jmsLoadWhenToStopField;
    @FXML public TextField jmsLoadThreadsField;
    @FXML public TextField jmsLoadCurrentTpsField;
    @FXML public TextField jmsLoadCurrentCountField;
    @FXML public CheckBox jmsLoadThinkTimeCkeckBox;
    @FXML public TextField jmsLoadThinkTimeField;
    @FXML public TextField jmsLoadDeviationField;
    @FXML public CheckBox jmsLoadNormalDistributionCkeckBox;
    @FXML public volatile ProgressIndicator jmsLoadProgressIndicator;


    // non volatile elements of load
    private boolean jmsLoadKeyToStop = false;
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


    @FXML public VBox jmsVBox;


    private void jmsProjectActualSave(String path){
        try {
            Properties map = new Properties();
            String tabName = "";
            for (Tab t : jmsMainTabPane.getTabs()) {
                if (t.isSelected()) {
                    tabName = t.getText();
                    SplitPane split = (SplitPane) t.getContent();
                    if (split != null)
                        for (Node ap : split.getItems()) {
                            AnchorPane pane = (AnchorPane) ap;
                            for (Node tf : pane.getChildren()) {
                                try {
                                    TextField f = (TextField) tf;
                                    map.put(f.getId(), f.getText());
                                } catch (ClassCastException ex) {
                                    try {
                                        TextArea ar = (TextArea) tf;
                                        map.put(ar.getId(), ar.getText());
                                    } catch (ClassCastException er) {
                                        continue;
                                    }
                                }
                            }
                        }
                    if (!map.isEmpty())
                        map.put(t.getId(), map);
                }
            }
            File resultFile = new File(path);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(resultFile, map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void jmsLoad(XYChart.Series series, ProgressIndicator progressIndicator, TextField tpsField, TextField countField){
        System.out.println(Thread.currentThread().getName() + "  -- Start.");

        NormalDistribution a = new NormalDistribution();

        Long thinkTime = Long.valueOf(0);
        if (jmsLoadThinkTimeCkeckBox.isSelected())
            thinkTime  = Long.parseLong(jmsLoadThinkTimeField.getText());

        try {
            neededTps = Double.parseDouble(jmsLoadNeededTpsField.getText());
        } catch (Exception e){
            System.out.println("Fill neededTps Field correctly!");
        }
        now = new Date();
        globalBegin = now;
        System.out.println(Thread.currentThread().getName() + now);

        if (jmsLoadByDateTimeRadioButton.isSelected()) {
            try {
                globalEnd = df.parse(jmsLoadWhenToStopField.getText());
                globalDuration = Math.abs(now.getTime() - globalEnd.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            localBegin = new Date();
            while (now.getTime() < globalEnd.getTime() && !jmsLoadKeyToStop) {

                if (jmsLoadThinkTimeCkeckBox.isSelected())
                    try {
                        if (jmsLoadNormalDistributionCkeckBox.isSelected()) {
                            Double tmpSleep = a.getGaussian(thinkTime, Long.parseLong(jmsLoadDeviationField.getText()));
                            Long sleepTime = Long.parseLong(tmpSleep.toString().substring(0, tmpSleep.toString().indexOf('.')));
                            System.out.println(sleepTime.toString());
                            Thread.sleep(Math.abs(sleepTime));
                        }
                        else
                            Thread.sleep(thinkTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

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
                    progressIndicator.setProgress((double) Math.abs(globalBegin.getTime() - now.getTime()) / (double) globalDuration);

                    Platform.runLater(() -> {
                        tpsField.setText(Integer.toString(globalTps.getValue().intValue()));
                        countField.setText(Long.toString(globalCount.getValue()));

                        series.getData().add(new XYChart.Data(globalCount.getValue(), globalTps.getValue().intValue()));
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
                //System.out.println(Thread.currentThread().getName() + "  -- globalCount: " + globalCount.getValue() + " globalTps: " + globalTps.getValue() + " localCount: " + localCount.getValue() + " Sleep for: " + temp);
                try {
                    sendJmsRequest();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            neededCount = Long.parseLong(jmsLoadWhenToStopField.getText());

            while (globalCount.getValue() < neededCount && !jmsLoadKeyToStop) {

                if (jmsLoadThinkTimeCkeckBox.isSelected())
                    try {
                        if (jmsLoadNormalDistributionCkeckBox.isSelected()) {
                            Double tmpSleep = a.getGaussian(thinkTime, Long.parseLong(jmsLoadDeviationField.getText()));
                            Long sleepTime = Long.parseLong(tmpSleep.toString().substring(0, tmpSleep.toString().indexOf('.')));
                            System.out.println(sleepTime.toString());
                            Thread.sleep(Math.abs(sleepTime));
                        }
                        else
                            Thread.sleep(thinkTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

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

                        series.getData().add(new XYChart.Data(globalCount.getValue(), globalTps.getValue().intValue()));
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
                //System.out.println(Thread.currentThread().getName() + "  -- globalCount: " + globalCount.getValue() + " globalTps: " + globalTps.getValue() + " localCount: " + localCount.getValue() + " Sleep for: " + tempSleep);
                try {
                    sendJmsRequest();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(Thread.currentThread().getName() + "  -- Done.");
    }

    private void sendJmsRequest(){
        SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/jms.tab.objects", jmsMainTabPane, JMSTabController.this);

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
        PasswordField passwordField = (PasswordField)projectAnchor.getChildren().get(15);

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

        String request_text = requestArea.getText();
        LinearRandomInt rInt = new LinearRandomInt( BigInteger.valueOf(System.currentTimeMillis()));
        LinearRandomString rStr = new LinearRandomString(7);

        while (request_text.contains("${rnd_int}")){
            rInt.next();
            request_text = request_text.replaceFirst("(?:\\$\\{rnd_int})+", rInt.getState().toString());
        }

        while (request_text.contains("${rnd_str}")){
            request_text = request_text.replaceFirst("(?:\\$\\{rnd_str})+", rStr.nextString());
        }

        IBMMqRequest request = new IBMMqRequest(request_text);
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

        jmsLoadStopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                jmsLoadKeyToStop = true;
            }
        });

        jmsLoadByCountRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                jmsLoadWhenToStopField.setPromptText("count");
            }
        });

        jmsLoadByDateTimeRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                jmsLoadWhenToStopField.setPromptText("date time");
            }
        });

        jmsLoadStartButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/jms.tab.objects", jmsMainTabPane, JMSTabController.this);

                final Stage stage = new Stage(StageStyle.UNIFIED);
                stage.setTitle("Transactions per second");

                final NumberAxis xAxis;
                if (jmsLoadByCountRadioButton.isSelected()){
                    xAxis = new NumberAxis(0, Long.parseLong(jmsLoadWhenToStopField.getText()), 10);
                    Long tempAxisWidth = Long.parseLong(jmsLoadWhenToStopField.getText());
                    Double axisWidth = tempAxisWidth.doubleValue();
                    xAxis.setMinWidth(2000);

                }
                else
                {
                    xAxis = new NumberAxis();
                }

                xAxis.setAutoRanging(false);
                xAxis.setLabel("Count");

                Long yAxisRange = Long.parseLong(jmsLoadNeededTpsField.getText());
                yAxisRange = yAxisRange + (Long) yAxisRange/10;

                final NumberAxis yAxis = new NumberAxis(0, yAxisRange, 10);
                yAxis.setAutoRanging(false);

                final LineChart<Number,Number> lineChart =
                        new LineChart<Number,Number>(xAxis,yAxis);
                lineChart.setTitle("Tps");
                lineChart.setAnimated(false);
                lineChart.setCreateSymbols(false);

                Scene scene  = new Scene(lineChart,800,600);
                stage.setScene(scene);
                stage.show();
                
                jmsLoadCurrentCountField.setText("0.0");
                jmsLoadCurrentTpsField.setText("0.0");

                globalCount.setValue(0);
                globalTps.setValue(0.0);
                numberOfThreads = Integer.parseInt(jmsLoadThreadsField.getText());
                for (int j = 0; j< numberOfThreads; j++) {
                    Thread loadThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            XYChart.Series seriesForThread = new XYChart.Series();
                            seriesForThread.setName(Thread.currentThread().getName());
                            seriesForThread.getData().add(new XYChart.Data(0, 0));

                            Rectangle rect = new Rectangle(0, 0);
                            rect.setVisible(false);
                            seriesForThread.setNode(rect);

                            Platform.runLater(() -> {
                                lineChart.getData().add(seriesForThread);
                            });

                            jmsLoad(seriesForThread, jmsLoadProgressIndicator, jmsLoadCurrentTpsField, jmsLoadCurrentCountField);
                        }
                    });
                    loadThread.setDaemon(true);
                    loadThread.start();
                }
                jmsLoadKeyToStop = false;
                localCount.setValue(0);
            }
        });
        
        jmsAddButtonTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                                            public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
                                                                SingleSelectionModel<Tab> selectionModel = jmsMainTabPane.getSelectionModel();
                                                                if (new_val){
                                                                    Date now = new Date();
                                                                    Tab tab = addTab(now.toString(), jmsMainTabPane);
                                                                    selectionModel.select(tab);
                                                                }
                                                            }
                                                        });

        jmsButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendJmsRequest();
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
                    SaveAndOpen.projectGlobalOpen(file.getPath(), jmsMainTabPane, JMSTabController.this);
                    SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/jms.tab.objects", jmsMainTabPane, JMSTabController.this);
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
                    SaveAndOpen.projectGlobalSave(file.getPath(), jmsMainTabPane, JMSTabController.this);
                    SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/jms.tab.objects", jmsMainTabPane, JMSTabController.this);
                    jmsProjectStateLabel.setText(file.getName() + "  ");
                }
            }
        });

        jmsVBox.getChildren().addAll(jmsInnerPane); // In this VBox 1) AnchorPane for button 2) AnchorPane named "jmsInnerPane" for all inner dynamic elements

        VBox.setVgrow(jmsInnerPane, Priority.ALWAYS);

        SaveAndOpen.projectGlobalOpen(System.getenv("SOATOOL_ROOT") + "/serz/jms.tab.objects", jmsMainTabPane, JMSTabController.this);
        if (jmsMainTabPane.getTabs().size() == 0) {
            Date now = new Date();
            addTab(now.toString(), jmsMainTabPane);
        }

        jmsMainTabPane.getTabs().add(jmsAddButtonTab);
        SingleSelectionModel<Tab> selectionModel = jmsMainTabPane.getSelectionModel();
        selectionModel.select(jmsMainTabPane.getTabs().indexOf(jmsAddButtonTab) - 1); // add tab to create new tabs
        jmsProjectStateLabel.setText("");
    }

    @Override
    public Tab addTab(String id, TabPane someTabPane){
        Tab tab = new Tab();
        tab.setId(id);

        SplitPane split = new SplitPane();
        split.setDividerPositions(0.75f);
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
        PasswordField passwordField = new PasswordField();
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
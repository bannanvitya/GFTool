package SOATestTool.gui;

import SOATestTool.Common.LinearRandomInt;
import SOATestTool.Common.LinearRandomString;
import SOATestTool.Common.NormalDistribution;
import javafx.application.Platform;
import javafx.beans.property.*;
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
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import SOATestTool.HTTPClient.HTTPClient;
import SOATestTool.HTTPClient.HTTPProfile;
import SOATestTool.HTTPClient.HTTPRequest;
import SOATestTool.HTTPClient.HTTPResponse;
import SOATestTool.api.ProfileNotFoundException;
import SOATestTool.api.ProfileStructureException;
import SOATestTool.api.SendRequestException;
import javafx.stage.StageStyle;

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
public class HTTPTabController implements Initializable, ClientTabControllerApi {
    private Node upperElement;


    // elements for sequence requests
    @FXML public Button httpButton;
    @FXML public MenuItem httpProjectOpen;
    @FXML public MenuItem httpProjectSave;
    @FXML public Label httpProjectStateLabel;
    @FXML public VBox httpVBox;

    // elements for load
    @FXML public Button httpLoadStartButton;
    @FXML public Button httpLoadStopButton;
    @FXML public TextField httpLoadNeededTpsField;
    @FXML public RadioButton httpLoadByCountRadioButton;
    @FXML public RadioButton httpLoadByDateTimeRadioButton;
    @FXML public TextField httpLoadWhenToStopField;
    @FXML public TextField httpLoadThreadsField;
    @FXML public CheckBox httpLoadThinkTimeCkeckBox;
    @FXML public TextField httpLoadThinkTimeField;
    @FXML public TextField httpLoadDeviationField;
    @FXML public CheckBox httpLoadNormalDistributionCkeckBox;
    @FXML public volatile TextField httpLoadCurrentTpsField;
    @FXML public volatile TextField httpLoadCurrentCountField;
    @FXML public volatile ProgressIndicator httpLoadProgressIndicator;


    // main elements of tabs
    TabPane httpMainTabPane = new TabPane();
    Tab httpAddButtonTab = new Tab();
    AnchorPane httpInnerPane = new AnchorPane();


    // non volatile elements of load
    private boolean httpLoadKeyToStop = false;
    private static Object lockObject = new Object();


    // volatile elements of load
    private volatile DoubleProperty globalTps = new SimpleDoubleProperty(0.0);
    private volatile LongProperty globalCount = new SimpleLongProperty(0);
    private volatile LongProperty localCount = new SimpleLongProperty(0);
    private volatile int numberOfThreads = 0;
    private volatile long duration;
    private volatile double neededTps;
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private volatile Date globalEnd;
    private volatile Date globalBegin;
    private volatile long neededCount;
    private volatile Date localBegin;


    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        httpMainTabPane.sideProperty().setValue(Side.LEFT);
        httpMainTabPane.tabClosingPolicyProperty().setValue(TabPane.TabClosingPolicy.SELECTED_TAB);

        httpInnerPane.getChildren().addAll(httpMainTabPane);

        AnchorPane.setBottomAnchor(httpMainTabPane, 0.0);
        AnchorPane.setLeftAnchor(httpMainTabPane, 0.0);
        AnchorPane.setRightAnchor(httpMainTabPane, 0.0);
        AnchorPane.setTopAnchor(httpMainTabPane, 0.0);

        httpAddButtonTab.setText("+");
        httpAddButtonTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                                           public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
                                                               SingleSelectionModel<Tab> selectionModel = httpMainTabPane.getSelectionModel();
                                                               if (new_val){
                                                                   Date now = new Date();
                                                                   Tab tab = addTab(now.toString(), httpMainTabPane);
                                                                   selectionModel.select(tab);
                                                               }
                                                           }
                                                       }
        );

        
        
        httpLoadStopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                httpLoadKeyToStop = true;
            }
        });


        httpLoadByCountRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                httpLoadWhenToStopField.setPromptText("count");
            }
        });

        httpLoadByDateTimeRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                httpLoadWhenToStopField.setPromptText("date time");
            }
        });

        httpButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);
                sendHttpRequest(false);
            }
        });


        httpLoadStartButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);

                final Stage stage = new Stage(StageStyle.UNIFIED);
                stage.setTitle("Transactions per second");

                final NumberAxis xAxis;
                if (httpLoadByCountRadioButton.isSelected()){
                    xAxis = new NumberAxis(0, Long.parseLong(httpLoadWhenToStopField.getText()), 10);
                    Long tempAxisWidth = Long.parseLong(httpLoadWhenToStopField.getText());
                    Double axisWidth = tempAxisWidth.doubleValue();
                    xAxis.setMinWidth(2000);

                }
                else
                {
                    xAxis = new NumberAxis();
                }

                xAxis.setAutoRanging(false);
                xAxis.setLabel("Count");

                Long yAxisRange = Long.parseLong(httpLoadNeededTpsField.getText());
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
                
                globalCount.setValue(0);
                globalTps.setValue(0.0);
                numberOfThreads = Integer.parseInt(httpLoadThreadsField.getText());
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

                           httpLoad(seriesForThread, httpLoadProgressIndicator, httpLoadCurrentTpsField, httpLoadCurrentCountField);
                        }
                    });
                    loadThread.setDaemon(true);
                    loadThread.start();
                }
                httpLoadKeyToStop = false;
                localCount.setValue(0);
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
                    SaveAndOpen.projectGlobalSave(file.getPath(), httpMainTabPane, HTTPTabController.this);
                    SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);
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
                    SaveAndOpen.projectGlobalOpen(file.getPath(), httpMainTabPane, HTTPTabController.this);
                    SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);
                    httpProjectStateLabel.setText(file.getName() + "  ");
                }
            }
        });



        httpVBox.getChildren().addAll(httpInnerPane); // In this VBox 1) AnchorPane for button 2) AnchorPane named "jmsInnerPane" for all inner dynamic elements

        VBox.setVgrow(httpInnerPane, Priority.ALWAYS);


        SaveAndOpen.projectGlobalOpen(System.getenv("SOATOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);
        if (httpMainTabPane.getTabs().size() == 0) {
            Date now = new Date();
            addTab(now.toString(), httpMainTabPane);
        }

        httpMainTabPane.getTabs().add(httpAddButtonTab);
        SingleSelectionModel<Tab> selectionModel = httpMainTabPane.getSelectionModel();
        selectionModel.select(httpMainTabPane.getTabs().indexOf(httpAddButtonTab) - 1); // add tab to create new tabs



        httpProjectStateLabel.setText("");
    }

    @Override
    public Tab addTab(String id, TabPane someTabPane){
        Tab tab = new Tab();
        tab.setId(id);

        SplitPane split = new SplitPane();
        split.setDividerPositions(0.75f);
        tab.setContent(split);

        AnchorPane requestAnchor = new AnchorPane(); //requestAnchor
        requestAnchor.setMinWidth(350.0);

        AnchorPane projectAnchor = new AnchorPane(); //projectAnchor
        projectAnchor.setMinWidth(350.0);



        Label requestLabel = new Label(); //requestAnchor elements
        requestLabel.setText("Request");

        TextArea requestArea = new TextArea();
        requestArea.setId("requestArea");
        requestArea.wrapTextProperty().setValue(true);

        requestAnchor.getChildren().addAll(requestLabel, requestArea);

        AnchorPane.setLeftAnchor(requestLabel, 7.0);
        AnchorPane.setTopAnchor(requestLabel, 7.0);

        AnchorPane.setBottomAnchor(requestArea, 7.0);
        AnchorPane.setLeftAnchor(requestArea, 7.0);
        AnchorPane.setRightAnchor(requestArea, 1.0);
        AnchorPane.setTopAnchor(requestArea, 30.0);


        TableView<Params> tableView = new TableView<Params>(); //responseAnchor elements
        tableView.setEditable(true);

        TableColumn firstNameCol = new TableColumn("Header Name");
        TableColumn lastNameCol = new TableColumn("Header Value");




        tableView.getColumns().addAll(firstNameCol, lastNameCol);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<Params,String>("ParamName")
        );
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<Params,String>("ParamValue")
        );

        tableView.setPrefWidth(268.0);
        tableView.setPrefHeight(168.0);
        tableView.widthProperty().addListener((ov, old_val, new_val) -> {
            Double a = (Double) new_val;
            lastNameCol.setPrefWidth(a - 131.0);
        }
        );

        firstNameCol.setPrefWidth(131.0);
        lastNameCol.setPrefWidth(tableView.widthProperty().getValue()-firstNameCol.getPrefWidth());

        TextField headerNameField = new TextField();
        headerNameField.setPromptText("Header Name");
        headerNameField.setId("headerNameField");
        headerNameField.setPrefWidth(131.0);

        TextField headerValueField = new TextField();
        headerValueField.setId("headerValueField");
        headerValueField.setPromptText("Header Value");

        Button addRow = new Button("Add");
        addRow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!headerNameField.getText().equals("") && !headerValueField.getText().equals("")){
                    tableView.getItems().add(new Params(headerNameField.getText(), headerValueField.getText()));
                    headerNameField.clear();
                    headerValueField.clear();
                }

            }
        });

        Label responseLabel = new Label();
        responseLabel.setText("Response");

        TextArea responseTextArea = new TextArea();
        responseTextArea.setId("responseTextArea");
        responseTextArea.wrapTextProperty().setValue(true);

        Label methodLabel = new Label();
        methodLabel.setText("Method:");

        Label contentTypeLabel = new Label();
        contentTypeLabel.setText("ContentType:");

        final ToggleGroup methodGroup = new ToggleGroup();

        RadioButton getRadioButton = new RadioButton("GET");
        getRadioButton.setToggleGroup(methodGroup);
        getRadioButton.setId("getRadioButton");
        getRadioButton.selectedProperty().setValue(true);

        RadioButton postRadioButton = new RadioButton("POST");
        postRadioButton.setId("postRadioButton");
        postRadioButton.setToggleGroup(methodGroup);


        final ToggleGroup contentTypeGroup = new ToggleGroup();

        RadioButton appRadioButton = new RadioButton("application/json");
        appRadioButton.setToggleGroup(contentTypeGroup);
        appRadioButton.setId("appRadioButton");
        appRadioButton.selectedProperty().setValue(true);

        RadioButton textXmlRadioButton = new RadioButton("text/xml");
        textXmlRadioButton.setId("textXmlRadioButton");
        textXmlRadioButton.setToggleGroup(contentTypeGroup);

        RadioButton otherTypeRadioButton = new RadioButton("or other:");
        otherTypeRadioButton.setId("otherTypeRadioButton");
        otherTypeRadioButton.setToggleGroup(contentTypeGroup);


        TextField otherTypeField = new TextField();
        otherTypeField.setId("otherTypeField");
        otherTypeField.setPromptText("...");

        TextField urlField = new TextField();
        urlField.setId("urlField");
        urlField.setPromptText("URL");

        getRadioButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                tableView.setVisible(true);
                headerNameField.setVisible(true);
                headerValueField.setVisible(true);
                addRow.setVisible(true);
            }
        });

        postRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tableView.setVisible(false);
                headerNameField.setVisible(false);
                headerValueField.setVisible(false);
                addRow.setVisible(false);
            }
        });

        otherTypeField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                if (!otherTypeField.getText().equals("")) {
                    otherTypeRadioButton.setSelected(true);
                    appRadioButton.setTextFill(Color.GRAY);
                    textXmlRadioButton.setTextFill(Color.GRAY);
                }
                else {
                    appRadioButton.setTextFill(Color.BLACK);
                    textXmlRadioButton.setTextFill(Color.BLACK);
                }
            }
        });

        otherTypeRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                appRadioButton.setTextFill(Color.GRAY);
                textXmlRadioButton.setTextFill(Color.GRAY);
            }
        });

        textXmlRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                appRadioButton.setTextFill(Color.BLACK);
                textXmlRadioButton.setTextFill(Color.BLACK);
            }
        });

        appRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                appRadioButton.setTextFill(Color.BLACK);
                textXmlRadioButton.setTextFill(Color.BLACK);
            }
        });


        if (!getRadioButton.isSelected()) {
            tableView.setVisible(false);
            headerNameField.setVisible(false);
            headerValueField.setVisible(false);
            addRow.setVisible(false);
        }


        projectAnchor.getChildren().addAll(methodLabel, getRadioButton, postRadioButton, urlField, contentTypeLabel, appRadioButton,
                textXmlRadioButton, otherTypeRadioButton, otherTypeField, tableView, headerNameField, headerValueField, addRow, responseLabel, responseTextArea);

        AnchorPane.setTopAnchor(tableView, 254.0);
        AnchorPane.setLeftAnchor(tableView, 1.0);
        AnchorPane.setRightAnchor(tableView, 7.0);
        //AnchorPane.setBottomAnchor(tableView, 40.0);

        AnchorPane.setTopAnchor(headerNameField, 425.0);
        AnchorPane.setLeftAnchor(headerNameField, 1.0);

        AnchorPane.setTopAnchor(headerValueField, 425.0);
        AnchorPane.setLeftAnchor(headerValueField, 135.0);
        AnchorPane.setRightAnchor(headerValueField, 7.0);

        AnchorPane.setTopAnchor(addRow, 453.0);
        AnchorPane.setRightAnchor(addRow, 7.0);

        AnchorPane.setTopAnchor(responseLabel, 516.0);
        AnchorPane.setLeftAnchor(responseLabel, 2.0);

        AnchorPane.setTopAnchor(responseTextArea, 539.0);
        AnchorPane.setLeftAnchor(responseTextArea, 1.0);
        AnchorPane.setRightAnchor(responseTextArea, 7.0);
        AnchorPane.setBottomAnchor(responseTextArea, 7.0);

        AnchorPane.setTopAnchor(methodLabel, 0.0);
        AnchorPane.setLeftAnchor(methodLabel, 1.0);

        AnchorPane.setTopAnchor(contentTypeLabel, 104.0);
        AnchorPane.setLeftAnchor(contentTypeLabel, 1.0);

        AnchorPane.setTopAnchor(getRadioButton, 18.0);
        AnchorPane.setLeftAnchor(getRadioButton, 1.0);

        AnchorPane.setTopAnchor(postRadioButton, 36.0);
        AnchorPane.setLeftAnchor(postRadioButton, 1.0);

        AnchorPane.setTopAnchor(appRadioButton, 130.0);
        AnchorPane.setLeftAnchor(appRadioButton, 1.0);

        AnchorPane.setTopAnchor(textXmlRadioButton, 148.0);
        AnchorPane.setLeftAnchor(textXmlRadioButton, 1.0);

        AnchorPane.setTopAnchor(otherTypeRadioButton, 166.0);
        AnchorPane.setLeftAnchor(otherTypeRadioButton, 1.0);

        AnchorPane.setTopAnchor(otherTypeField, 184.0);
        AnchorPane.setLeftAnchor(otherTypeField, 1.0);
        AnchorPane.setRightAnchor(otherTypeField, 7.0);

        AnchorPane.setTopAnchor(urlField, 58.0);
        AnchorPane.setLeftAnchor(urlField, 1.0);
        AnchorPane.setRightAnchor(urlField, 7.0);


        split.getItems().addAll(requestAnchor, projectAnchor); // split elements


        if (someTabPane.getTabs().size()>1)
            someTabPane.getTabs().add(someTabPane.getTabs().size()-1, tab);
        else
            someTabPane.getTabs().add(tab);
        tab.setText("default");
        return tab;
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

    public void setHttpUpperElement(Node node){
        upperElement = node;
    }

    public void sendHttpRequest(boolean isLoad){
        HTTPProfile profile = new HTTPProfile();

        Properties prop = new Properties();
        Properties headers = new Properties();


        SingleSelectionModel<Tab> selectionModel = httpMainTabPane.getSelectionModel();
        SplitPane split = (SplitPane)httpMainTabPane.getTabs().get(selectionModel.getSelectedIndex()).getContent();

        AnchorPane projectAnchor = (AnchorPane) split.getItems().get(1);

        TableView<Params> table = (TableView) projectAnchor.getChildren().get(9);
        for (Params param : table.getItems()) {
            headers.setProperty(param.getParamName(), param.getParamValue());
        }


        TextField httpUrlField = (TextField) projectAnchor.getChildren().get(3);
        prop.setProperty("url", httpUrlField.getText());


        RadioButton httpGetMethod = (RadioButton) projectAnchor.getChildren().get(1);
        if (httpGetMethod.isSelected())
            prop.setProperty("methodType", "GET");
        else
            prop.setProperty("methodType", "POST");

        RadioButton httpAppJson = (RadioButton) projectAnchor.getChildren().get(5);
        RadioButton httpTextXml = (RadioButton) projectAnchor.getChildren().get(6);
        RadioButton httpOtherContentType = (RadioButton) projectAnchor.getChildren().get(7);
        TextField httpContentTypeField = (TextField) projectAnchor.getChildren().get(8);
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

        AnchorPane requestAnchor = (AnchorPane) split.getItems().get(0);
        TextArea httpRequestField = (TextArea) requestAnchor.getChildren().get(1);


        String request_text = httpRequestField.getText();
        LinearRandomInt rInt = new LinearRandomInt( BigInteger.valueOf(System.currentTimeMillis()));
        LinearRandomString rStr = new LinearRandomString(7);


        while (request_text.contains("${rnd_int}")){
            rInt.next();
            request_text = request_text.replaceFirst("(?:\\$\\{rnd_int})+", rInt.getState().toString());
        }


        while (request_text.contains("${rnd_str}")){
            request_text = request_text.replaceFirst("(?:\\$\\{rnd_str})+", rStr.nextString());
        }


        HTTPRequest req = new HTTPRequest(request_text);
        client.setProfile(profile);
        HTTPResponse resp = null;
        try {
            resp = (HTTPResponse) client.sendRequest(req);
        }catch (ProfileStructureException e) {
            e.printStackTrace();
        } catch (SendRequestException e) {
            e.printStackTrace();
        }

        if (resp != null) {
            TextArea httpResponseField = (TextArea) projectAnchor.getChildren().get(14);
            if (!isLoad)
                httpResponseField.setText("Code: " + resp.getStatus() + "\n" + "Message: " + resp.getMessage());
        }
    }

    public void httpLoad(XYChart.Series series, ProgressIndicator progressIndicator, TextField tpsField, TextField countField){
        System.out.println(Thread.currentThread().getName() + "  -- Start.");

        NormalDistribution a = new NormalDistribution();

        Long thinkTime = Long.valueOf(0);
        if (httpLoadThinkTimeCkeckBox.isSelected())
            thinkTime  = Long.parseLong(httpLoadThinkTimeField.getText());

        //long duration;
        try {
            neededTps = Double.parseDouble(httpLoadNeededTpsField.getText());
        } catch (Exception e){
            System.out.println("Fill neededTps Field correctly!");
        }
        Date now = new Date();
        globalBegin = now;
        System.out.println(Thread.currentThread().getName() + now);

        if (httpLoadByDateTimeRadioButton.isSelected()) {

            long globalDuration = 0;
            try {
                globalEnd = df.parse(httpLoadWhenToStopField.getText());
                globalDuration = Math.abs(now.getTime() - globalEnd.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            localBegin = new Date();
            while (now.getTime() < globalEnd.getTime() && !httpLoadKeyToStop) {

                if (httpLoadThinkTimeCkeckBox.isSelected())
                    try {
                        if (httpLoadNormalDistributionCkeckBox.isSelected()) {
                            Double tmpSleep = a.getGaussian(thinkTime, Long.parseLong(httpLoadDeviationField.getText()));
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
                    progressIndicator.setProgress((double)Math.abs(globalBegin.getTime() - now.getTime())/(double)globalDuration);

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
                System.out.println(Thread.currentThread().getName() + "  -- globalCount: " + globalCount.getValue() + " globalTps: " + globalTps.getValue() + " localCount: " + localCount.getValue() + " Sleep for: " + temp);
                try {
                    synchronized (lockObject) {
                        sendHttpRequest(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {

            neededCount = Long.parseLong(httpLoadWhenToStopField.getText());

            while (globalCount.getValue() < neededCount && !httpLoadKeyToStop) {

                if (httpLoadThinkTimeCkeckBox.isSelected())
                    try {
                        if (httpLoadNormalDistributionCkeckBox.isSelected()) {
                            Double tmpSleep = a.getGaussian(thinkTime, Long.parseLong(httpLoadDeviationField.getText()));
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
                    progressIndicator.setProgress((double) globalCount.getValue() / (double) neededCount);

                    Platform.runLater(() -> {
                        tpsField.setText(Integer.toString(globalTps.getValue().intValue()));
                        countField.setText(Long.toString(globalCount.getValue()));

                        series.getData().add(new XYChart.Data(globalCount.getValue(), globalTps.getValue().intValue()));
                    });
                }
                double temp = 0.0;
                //System.out.println(Thread.currentThread().getName() + "  --  " + globalIterations);
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
                    synchronized (lockObject) {
                    sendHttpRequest(true);
                }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(Thread.currentThread().getName() + "  -- Done.");
    }

}

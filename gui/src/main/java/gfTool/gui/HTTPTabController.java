package gfTool.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codehaus.jackson.map.ObjectMapper;
import gfTool.HTTPClient.HTTPClient;
import gfTool.HTTPClient.HTTPProfile;
import gfTool.HTTPClient.HTTPRequest;
import gfTool.HTTPClient.HTTPResponse;
import gfTool.api.ProfileNotFoundException;
import gfTool.api.ProfileStructureException;
import gfTool.api.SendRequestException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
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

    @FXML public Button httpButton;
    @FXML public MenuItem httpProjectOpen;
    @FXML public MenuItem httpProjectSave;
    @FXML public Label httpProjectStateLabel;
    @FXML public VBox httpVBox;

    @FXML public Button httpLoadStartButton;
    @FXML public Button httpLoadStopButton;
    @FXML public TextField httpLoadNeededTpsField;
    @FXML public RadioButton httpLoadByCountRadioButton;
    @FXML public RadioButton httpLoadByDateTimeRadioButton;
    @FXML public TextField httpLoadWhenToStopField;
    @FXML public Label httpLoadTpsLabel;
    @FXML public Label httpLoadCountLabel;

    TabPane httpMainTabPane = new TabPane();
    Tab httpAddButtonTab = new Tab();
    AnchorPane httpInnerPane = new AnchorPane();


    private boolean httpLoadKeyToStop = false;

    private static Object lockObject = new Object();


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

                SaveAndOpen.projectGlobalSave(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);
                sendHttpRequest();
            }
        });


        httpLoadStartButton.setOnAction(new EventHandler<ActionEvent>() {

            private volatile double globalTps = 0.0;
            private volatile long globalCount = 0;

            @Override
            public void handle(ActionEvent event) {

                SaveAndOpen.projectGlobalSave(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);

                Thread loadThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        httpLoad(globalCount, globalTps);
                    }
                });
                loadThread.start();

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
                    SaveAndOpen.projectGlobalSave(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);
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
                    SaveAndOpen.projectGlobalSave(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);
                    httpProjectStateLabel.setText(file.getName() + "  ");
                }
            }
        });



        httpVBox.getChildren().addAll(httpInnerPane); // In this VBox 1) AnchorPane for button 2) AnchorPane named "jmsInnerPane" for all inner dynamic elements

        VBox.setVgrow(httpInnerPane, Priority.ALWAYS);


        SaveAndOpen.projectGlobalOpen(System.getenv("GFTOOL_ROOT") + "/serz/http.tab.objects", httpMainTabPane, HTTPTabController.this);
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

    public void sendHttpRequest(){
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
        HTTPRequest req = new HTTPRequest(httpRequestField.getText());
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
            synchronized (lockObject) {
                httpResponseField.setText("Code: " + resp.getStatus() + "\n" + "Message: " + resp.getMessage());
            }
        }
    }

    public void httpLoad(long globalIterations, double tps){
        System.out.println(Thread.currentThread().getName() + "  -- Start.");

        long duration;
        long i = 0;
        double neededTps = Double.parseDouble(httpLoadNeededTpsField.getText());

        Date now = new Date();
        System.out.println(Thread.currentThread().getName() + now);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        if (httpLoadByDateTimeRadioButton.isSelected()) {

            Date globalEnd = null;
            Date localBegin = new Date();
            try {
                globalEnd = df.parse(httpLoadWhenToStopField.getText());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            while (now.getTime() < globalEnd.getTime() && !httpLoadKeyToStop) {
                now = new Date();


                if (i == 0) {
                    localBegin = now;
                    duration = Math.abs(now.getTime() - localBegin.getTime());
                } else {
                    duration = Math.abs(now.getTime() - localBegin.getTime());
                    if (duration / 1000 > 60.0) {
                        localBegin = now;
                        duration = Math.abs(now.getTime() - localBegin.getTime());
                        i = 0;
                    } else {
                        tps = (i / (duration / 1000));
                    }
                    //System.out.println(Thread.currentThread().getName() + tps);
                }

                synchronized (lockObject) {
                    i++;
                    globalIterations++;
                }
                System.out.println(Thread.currentThread().getName() + globalIterations);
                if (tps > neededTps) {
                    double temp = (i / neededTps - duration / 1000) * 1000;
                    //System.out.println(temp);
                    long s = (long) temp;
                    //System.out.println("yep!");
                    try {
                        Thread.sleep(s);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {

                    sendHttpRequest();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {

            Date localBegin = new Date();
            long neededCount = Long.parseLong(httpLoadWhenToStopField.getText());

            while (globalIterations < neededCount && !httpLoadKeyToStop) {
                now = new Date();

                if (i == 0) {
                    localBegin = now;
                    duration = Math.abs(now.getTime() - localBegin.getTime());
                } else {
                    duration = Math.abs(now.getTime() - localBegin.getTime());
                    if (duration / 1000 > 60.0) {
                        localBegin = now;
                        duration = Math.abs(now.getTime() - localBegin.getTime());
                        i = 0;
                    } else {

                        tps = (i / (((double) duration )/ 1000));
                    }
                    //System.out.println(tps);
                }

                synchronized (lockObject) {
                    i++;
                    globalIterations++;
                }
                System.out.println(Thread.currentThread().getName() + "  --  " + globalIterations);
                //httpLoadTpsLabel.setText(Double.toString(tps));
                //httpLoadCountLabel.setText(Long.toString(globalIterations));

                if (tps > neededTps) {
                    double temp = (i / neededTps - duration / 1000) * 1000;
                    //System.out.println(temp);
                    long s = (long) temp;
                    //System.out.println("yep!");
                    try {
                        Thread.sleep(s);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    sendHttpRequest();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        httpLoadKeyToStop = false;
        i = 0;
        tps = 0.0;
        System.out.println(Thread.currentThread().getName() + "  -- Done.");
    }

}

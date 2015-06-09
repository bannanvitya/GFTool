package SOATestTool.gui;

import SOATestTool.Common.LinearRandomInt;
import SOATestTool.Common.LinearRandomString;
import SOATestTool.SMTPClient.SMTPClient;
import SOATestTool.SMTPClient.SMTPProfile;
import SOATestTool.SMTPClient.SMTPRequest;
import SOATestTool.api.ProfileNotFoundException;
import SOATestTool.api.ProfileStructureException;
import SOATestTool.api.ProfileUpdateException;
import SOATestTool.api.SendRequestException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class SMTPTabController implements Initializable, ClientTabControllerApi {
    private Node upperElement;

    @FXML public Button smtpButton;
    @FXML public MenuItem smtpProjectOpen;
    @FXML public MenuItem smtpProjectSave;
    @FXML public Label smtpProjectStateLabel;

    @FXML public VBox smtpVBox;

    TabPane smtpMainTabPane = new TabPane();
    Tab smtpAddButtonTab = new Tab();
    AnchorPane smtpInnerPane = new AnchorPane();




    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        smtpMainTabPane.sideProperty().setValue(Side.LEFT);
        smtpMainTabPane.tabClosingPolicyProperty().setValue(TabPane.TabClosingPolicy.SELECTED_TAB);

        smtpInnerPane.getChildren().addAll(smtpMainTabPane);

        AnchorPane.setBottomAnchor(smtpMainTabPane, 0.0);
        AnchorPane.setLeftAnchor(smtpMainTabPane, 0.0);
        AnchorPane.setRightAnchor(smtpMainTabPane, 0.0);
        AnchorPane.setTopAnchor(smtpMainTabPane, 0.0);


        smtpAddButtonTab.setText("+");
        smtpAddButtonTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                                           public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
                                                               SingleSelectionModel<Tab> selectionModel = smtpMainTabPane.getSelectionModel();
                                                               if (new_val){
                                                                   Date now = new Date();
                                                                   Tab tab = addTab(now.toString(), smtpMainTabPane);
                                                                   selectionModel.select(tab);
                                                               }
                                                           }
                                                       }
        );


        smtpButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/smtp.tab.objects", smtpMainTabPane, SMTPTabController.this);

                SMTPProfile profile = new SMTPProfile();

                SingleSelectionModel<Tab> selectionModel = smtpMainTabPane.getSelectionModel();
                SplitPane split = (SplitPane)smtpMainTabPane.getTabs().get(selectionModel.getSelectedIndex()).getContent();

                AnchorPane requestAnchor = (AnchorPane) split.getItems().get(0);
                String[] to = {((TextField)requestAnchor.getChildren().get(1)).getText()};
                String subj = ((TextField)requestAnchor.getChildren().get(3)).getText();
                TextArea requestArea = (TextArea)requestAnchor.getChildren().get(5);

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

                AnchorPane projectAnchor = (AnchorPane) split.getItems().get(1);
                RadioButton mail = (RadioButton)projectAnchor.getChildren().get(1);
                RadioButton gmail = (RadioButton)projectAnchor.getChildren().get(2);
                RadioButton other = (RadioButton)projectAnchor.getChildren().get(3);
                String otherHost = ((TextField)projectAnchor.getChildren().get(4)).getText();
                String otherPort = ((TextField)projectAnchor.getChildren().get(5)).getText();
                String host = "";
                String port = "";
                if (mail.isSelected()){
                    host = "smtp.mail.ru";
                } else if (gmail.isSelected()){
                    host = "smtp.gmail.com";
                } else if (other.isSelected()){
                    host = otherHost;
                    port = otherPort;
                }

                String from = ((TextField)projectAnchor.getChildren().get(6)).getText();
                String pass = ((PasswordField)projectAnchor.getChildren().get(7)).getText();

                try {
                    profile.setId(host);
                    profile.updateValue("mail.smtp.host", host);
                    if (!port.equals(""))
                    profile.updateValue("mail.smtp.port", port);
                    profile.updateValue("mail.smtp.user", from);
                    profile.updateValue("mail.smtp.password", pass);
                } catch (ProfileNotFoundException | ProfileStructureException | ProfileUpdateException e){
                    e.printStackTrace();
                }


                SMTPRequest req = new SMTPRequest(request_text, subj, to);

                SMTPClient client = new SMTPClient();
                client.setProfile(profile);
                try {
                    client.sendRequest(req);
                } catch (SendRequestException | ProfileStructureException e){
                    e.printStackTrace();
                }


        }
        });




        smtpProjectSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File file = project.showSaveDialog(stage);


                if(file != null){
                    SaveAndOpen.projectGlobalSave(file.getPath(), smtpMainTabPane, SMTPTabController.this);
                    SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/smtp.tab.objects", smtpMainTabPane, SMTPTabController.this);
                    smtpProjectStateLabel.setText(file.getName() + "  ");
                }
            }
        });

        smtpProjectOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                File file = project.showOpenDialog(stage);

                if (file != null){
                    SaveAndOpen.projectGlobalOpen(file.getPath(), smtpMainTabPane, SMTPTabController.this);
                    SaveAndOpen.projectGlobalSave(System.getenv("SOATOOL_ROOT") + "/serz/smtp.tab.objects", smtpMainTabPane, SMTPTabController.this);
                    smtpProjectStateLabel.setText(file.getName() + "  ");
                }
            }
        });



        smtpVBox.getChildren().addAll(smtpInnerPane); // In this VBox 1) AnchorPane for button 2) AnchorPane named "smtpInnerPane" for all inner dynamic elements

        VBox.setVgrow(smtpInnerPane, Priority.ALWAYS);


        SaveAndOpen.projectGlobalOpen(System.getenv("SOATOOL_ROOT") + "/serz/smtp.tab.objects", smtpMainTabPane, SMTPTabController.this);
        if (smtpMainTabPane.getTabs().size() == 0) {
            Date now = new Date();
            addTab(now.toString(), smtpMainTabPane);
        }

        smtpMainTabPane.getTabs().add(smtpAddButtonTab);
        SingleSelectionModel<Tab> selectionModel = smtpMainTabPane.getSelectionModel();
        selectionModel.select(smtpMainTabPane.getTabs().indexOf(smtpAddButtonTab) - 1); // add tab to create new tabs



        smtpProjectStateLabel.setText("");
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


        Label toLabel = new Label();
        toLabel.setText("To");

        TextField toField = new TextField();
        toField.setId("toField");

        Label subjLabel = new Label();
        subjLabel.setText("Subject");

        TextField subjField = new TextField();
        subjField.setId("subjField");

        Label requestLabel = new Label(); //requestAnchor elements
        requestLabel.setText("Request");

        TextArea requestArea = new TextArea();
        requestArea.setId("requestArea");
        requestArea.wrapTextProperty().setValue(true);


        requestAnchor.getChildren().addAll(toLabel, toField, subjLabel, subjField, requestLabel, requestArea);

        AnchorPane.setLeftAnchor(toLabel, 7.0);
        AnchorPane.setTopAnchor(toLabel, 7.0);

        AnchorPane.setLeftAnchor(toField, 7.0);
        AnchorPane.setRightAnchor(toField, 1.0);
        AnchorPane.setTopAnchor(toField, 25.0);

        AnchorPane.setLeftAnchor(subjLabel, 7.0);
        AnchorPane.setTopAnchor(subjLabel, 52.0);

        AnchorPane.setLeftAnchor(subjField, 7.0);
        AnchorPane.setRightAnchor(subjField, 1.0);
        AnchorPane.setTopAnchor(subjField, 70.0);

        AnchorPane.setLeftAnchor(requestLabel, 7.0);
        AnchorPane.setTopAnchor(requestLabel, 97.0);

        AnchorPane.setBottomAnchor(requestArea, 7.0);
        AnchorPane.setLeftAnchor(requestArea, 7.0);
        AnchorPane.setRightAnchor(requestArea, 1.0);
        AnchorPane.setTopAnchor(requestArea, 115.0);



        Label responseLabel = new Label();
        responseLabel.setText("Response");

        TextArea responseTextArea = new TextArea();
        responseTextArea.setId("responseTextArea");
        responseTextArea.wrapTextProperty().setValue(true);


        Label smtpTypeLabel = new Label();
        smtpTypeLabel.setText("SMTP server:");

        final ToggleGroup smtpTypeGroup = new ToggleGroup();

        RadioButton mailRadioButton = new RadioButton("smtp.mail.ru");
        mailRadioButton.setToggleGroup(smtpTypeGroup);
        mailRadioButton.setId("mailRadioButton");
        mailRadioButton.selectedProperty().setValue(true);

        RadioButton gmailRadioButton = new RadioButton("smtp.gmail.com");
        gmailRadioButton.setId("gmailRadioButton");
        gmailRadioButton.setToggleGroup(smtpTypeGroup);

        RadioButton otherSmtpTypeRadioButton = new RadioButton("or other:");
        otherSmtpTypeRadioButton.setId("otherSmtpTypeRadioButton");
        otherSmtpTypeRadioButton.setToggleGroup(smtpTypeGroup);


        TextField otherTypeField = new TextField();
        otherTypeField.setId("otherTypeField");
        otherTypeField.setPromptText("...");


        TextField portField = new TextField();
        portField.setId("portField");
        portField.setPromptText("Port");
        portField.setDisable(true);


        TextField fromField = new TextField();
        fromField.setId("fromField");
        fromField.setPromptText("From/User");

        PasswordField passField = new PasswordField();
        passField.setId("passField");
        passField.setPromptText("Password");


        otherTypeField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                if (!otherTypeField.getText().equals("")) {
                    otherSmtpTypeRadioButton.setSelected(true);
                    mailRadioButton.setTextFill(Color.GRAY);
                    gmailRadioButton.setTextFill(Color.GRAY);
                    portField.setDisable(false);
                }
                else {
                    mailRadioButton.setTextFill(Color.BLACK);
                    gmailRadioButton.setTextFill(Color.BLACK);
                }
            }
        });

        otherSmtpTypeRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mailRadioButton.setTextFill(Color.GRAY);
                gmailRadioButton.setTextFill(Color.GRAY);
                portField.setDisable(false);
            }
        });

        gmailRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mailRadioButton.setTextFill(Color.BLACK);
                gmailRadioButton.setTextFill(Color.BLACK);
                portField.setDisable(true);
            }
        });

        mailRadioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mailRadioButton.setTextFill(Color.BLACK);
                gmailRadioButton.setTextFill(Color.BLACK);
                portField.setDisable(true);
            }
        });



        projectAnchor.getChildren().addAll(smtpTypeLabel, mailRadioButton,
                gmailRadioButton, otherSmtpTypeRadioButton, otherTypeField, portField, fromField, passField, responseLabel, responseTextArea);



        AnchorPane.setTopAnchor(responseLabel, 516.0);
        AnchorPane.setLeftAnchor(responseLabel, 2.0);

        AnchorPane.setTopAnchor(responseTextArea, 539.0);
        AnchorPane.setLeftAnchor(responseTextArea, 1.0);
        AnchorPane.setRightAnchor(responseTextArea, 7.0);
        AnchorPane.setBottomAnchor(responseTextArea, 7.0);

        AnchorPane.setTopAnchor(smtpTypeLabel, 7.0);
        AnchorPane.setLeftAnchor(smtpTypeLabel, 1.0);

        AnchorPane.setTopAnchor(mailRadioButton, 33.0);
        AnchorPane.setLeftAnchor(mailRadioButton, 1.0);

        AnchorPane.setTopAnchor(gmailRadioButton, 51.0);
        AnchorPane.setLeftAnchor(gmailRadioButton, 1.0);

        AnchorPane.setTopAnchor(otherSmtpTypeRadioButton, 69.0);
        AnchorPane.setLeftAnchor(otherSmtpTypeRadioButton, 1.0);

        AnchorPane.setTopAnchor(otherTypeField, 87.0);
        AnchorPane.setLeftAnchor(otherTypeField, 1.0);
        AnchorPane.setRightAnchor(otherTypeField, 7.0);

        AnchorPane.setTopAnchor(portField, 130.0);
        AnchorPane.setLeftAnchor(portField, 1.0);
        AnchorPane.setRightAnchor(portField, 7.0);

        AnchorPane.setTopAnchor(fromField, 157.0);
        AnchorPane.setLeftAnchor(fromField, 1.0);
        AnchorPane.setRightAnchor(fromField, 7.0);

        AnchorPane.setTopAnchor(passField, 182.0);
        AnchorPane.setLeftAnchor(passField, 1.0);
        AnchorPane.setRightAnchor(passField, 7.0);


        split.getItems().addAll(requestAnchor, projectAnchor); // split elements


        if (someTabPane.getTabs().size()>1)
            someTabPane.getTabs().add(someTabPane.getTabs().size()-1, tab);
        else
            someTabPane.getTabs().add(tab);
        tab.setText("default");
        return tab;
    }


    public void setSmtpUpperElement(Node node){
        upperElement = node;
    }
}

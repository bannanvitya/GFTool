package ru.at_consulting.gfTool.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.at_consulting.gfTool.soapclient.SoapProfile;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by VKhozhaynov on 15.02.2015.
 */
public class WSDLNewProjectController implements Initializable {
    private Node initElement;
    private File wsdl;
    private SoapProfile profile = new SoapProfile();

    @FXML public Button WSDLBrowseButton;
    @FXML public Button WSDLOKButton;
    @FXML public TextField WSDLProjectNameField;
    @FXML public TextField WSDLBrowseField;


    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        WSDLBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser project = new FileChooser();
                project.setTitle("Open Project File");
                Stage stage = new Stage();
                wsdl = project.showOpenDialog(stage);
                if (wsdl != null) {
                    WSDLBrowseField.setText(wsdl.getAbsolutePath());
                }

            }
        });

        WSDLBrowseField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (wsdl != null) {
                    WSDLProjectNameField.setText(wsdl.getName());
                    profile.setUrl(wsdl.getAbsolutePath());
                } else if (WSDLBrowseField.getText().contains("?wsdl")){
                    WSDLProjectNameField.setText(WSDLBrowseField.getText().substring(0, WSDLBrowseField.getText().indexOf("?wsdl")));
                    profile.setUrl(WSDLBrowseField.getText());
                }
            }
        });



        WSDLOKButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage stage = (Stage) WSDLOKButton.getScene().getWindow();

                profile.processWsdl();
                profile.processMessagesMap();
                Map<String, String> messagesMap = profile.getMessagesMap();


                initElement.getScene().getRoot().setDisable(false);
                stage.close();
            }
        });




    }

    public void setWsdlInitElement(Node node){
        initElement = node;
    }

}

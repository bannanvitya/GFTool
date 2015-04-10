package gfTool.gui;

import gfTool.api.ActualSaveException;
import gfTool.api.GlobalOpenException;
import gfTool.api.GlobalSaveException;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by vkhozhaynov on 10.04.2015.
 */
public class SaveAndOpen {

    public static void projectGlobalOpen(String path, Node root, ClientTabControllerApi general){
        if ((new File(path)).length() != 0)
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, LinkedHashMap<String, String>> map = mapper.readValue(new File(path), Map.class);
                TabPane mainTabPane = (TabPane) root;
                for (Map.Entry<String, LinkedHashMap<String, String>> e : map.entrySet()){
                    LinkedHashMap<String, String> tabProp = e.getValue();
                    Tab t = general.addTab(e.getKey(), mainTabPane);
                    t.setText(e.getKey());
                    SplitPane split = (SplitPane)t.getContent();
                    for (Node n : split.getItems()) {
                        AnchorPane ap = (AnchorPane) n;
                        for (Node tf : ap.getChildren()) {
                            try {
                                TextField f = (TextField) tf;
                                f.setText(tabProp.get(f.getId()));
                            } catch (ClassCastException ex) {
                                try {
                                    TextArea ar = (TextArea) tf;
                                    ar.setText(tabProp.get(ar.getId()));
                                } catch (ClassCastException et) {
                                    try {
                                        RadioButton rb = (RadioButton) tf;
                                        rb.setSelected(Boolean.parseBoolean(tabProp.get(rb.getId())));
                                        System.out.println(rb.selectedProperty().getValue().toString());
                                    } catch (ClassCastException er) {
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
            }  catch (ClassCastException e){
                throw new GlobalOpenException();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }
    public static void projectGlobalSave(String path, Node root, ClientTabControllerApi general){
        try {
            Map<String, Properties> map = new HashMap<String, Properties>();
            try {
                TabPane mainTabPane = (TabPane) root;
                for (Tab t : mainTabPane.getTabs()) {
                    SplitPane split = (SplitPane) t.getContent();
                    Properties tabProp = new Properties();
                    if (split != null)
                        for (Node ap : split.getItems()) {
                            AnchorPane pane = (AnchorPane) ap;
                            for (Node tf : pane.getChildren()) {
                                try {
                                    TextField f = (TextField) tf;
                                    tabProp.put(f.getId(), f.getText());
                                } catch (ClassCastException ex) {
                                    try {
                                        TextArea ar = (TextArea) tf;
                                        tabProp.put(ar.getId(), ar.getText());
                                    } catch (ClassCastException er) {
                                            try{
                                                RadioButton rb = (RadioButton) tf;
                                                tabProp.put(rb.getId(), rb.selectedProperty().getValue().toString());
                                            } catch (ClassCastException et) {
                                                    continue;
                                            }
                                        }
                                }
                            }
                        }
                    if (!tabProp.isEmpty())
                        map.put(t.getId(), tabProp);


                    /*if (general.getClass().equals(SOAPTabController.class)) { // store messagesMap for SOAP protocol
                        Properties messagesProp = new Properties();
                        messagesProp.putAll(general.getMessageMap());
                        map.put(t.getId() + "_messagesMap", messagesProp);
                    }*/
                }
            } catch (ClassCastException e) {
                throw new GlobalSaveException();
            }
            File resultFile = new File(path);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(resultFile, map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void projectActualSave(String path, Node root, ClientTabControllerApi general){
        try {
            Properties map = new Properties();
            String tabName = "";
            try {
                TabPane mainTabPane = (TabPane) root;
            for (Tab t : mainTabPane.getTabs()) {
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
        }catch (ClassCastException e){
                throw new ActualSaveException();
            }
            File resultFile = new File(path);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(resultFile, map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}

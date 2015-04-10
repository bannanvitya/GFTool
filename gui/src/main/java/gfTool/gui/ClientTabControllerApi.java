package gfTool.gui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.Map;

/**
 * Created by vkhozhaynov on 10.04.2015.
 */
public interface ClientTabControllerApi {
    Tab addTab(String id, TabPane someTabPane);

}

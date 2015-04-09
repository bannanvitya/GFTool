package gfTool.api;

/**
 * Created by ekhatko on 7/8/14.
 */
public class AutotestConfigurator {
  public static String getProperty(String key){
    String prop = "";
    try{
      prop = System.getProperty(key);
    }catch (RuntimeException e){
      e.printStackTrace();
      new AutotestConfigurationException();
    }
    return prop;
  }
}

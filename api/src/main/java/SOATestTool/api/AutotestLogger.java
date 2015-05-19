package SOATestTool.api;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by masia on 23/03/14.
 */

public class AutotestLogger {

  private static Logger log = LoggerFactory.getLogger(AutotestLogger.class);
  private String prefix;
  private static AutotestLogger instance = null;

  public static AutotestLogger getLoggerInstance(String prefix){
    if(instance == null) {
      PropertyConfigurator.configure(System.getProperty("logging.config"));
      //  PropertyConfigurator.configure("/home/ksaraev/projects/cmd_test/logging/log4j.properties");
        instance = new AutotestLogger();
    }
    instance.prefix = prefix + ": ";
    return instance;
  }

  public void debug(String message){
    log.debug(prefix + message);
  }

  public void info(String message){
    log.info(prefix + message);
  }

  public void warn(String message){
    log.warn(prefix + message);
  }

  public void error(String message, Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      log.error(prefix + message + " --> " + e.getMessage() + "\n" + sw.toString());

  }
}

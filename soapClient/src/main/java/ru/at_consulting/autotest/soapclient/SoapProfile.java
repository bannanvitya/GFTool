package ru.at_consulting.autotest.soapclient;

import ru.at_consulting.autotest.api.*;
import ru.at_consulting.autotest.soapclient.MessageHelper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SoapProfile implements Profile {

  private static String AUTHORIZATION_OFF = "none";
  private static String PROPERTIES_TYPE = "properties";
  private static String XML_TYPE = "xml";

  //private static String SOAP_PROFILES = "/home/ksaraev/projects/cmd_test/soap_client/profiles";
  //private static String SOAP_PROFILES = "C:\\work\\cmd_test\\soap_client\\profiles"; // only for debug
  private static String SOAP_PROFILES = System.getProperty("soap_client.profiles");

  private static AutotestLogger log = AutotestLogger.getLoggerInstance(SoapProfile.class.getSimpleName());
  private String id;
  private File profileDir;
  private Map<String, MessageHelper> messages;
  private Properties serviceProp;

  public SoapProfile(SoapProfile another){
    //this.ctx = another.ctx;
    this.id = another.id;
    this.profileDir = another.profileDir;
    this.messages = another.messages;
    this.serviceProp = another.serviceProp;
  }

  public SoapProfile() {
    id = "Profile not init";
  }

  public String getURL() {
    return serviceProp != null ? serviceProp.getProperty("URL") : "Profile not initiated";
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) throws ProfileNotFoundException,
    ProfileStructureException {
    log.info("Setting SoapClient profile [" + id + "]");
    this.id = id;
    // TODO
    // Использовать последнюю версию из девелопа, в которой нет зависимости от CmdConfigurator, она вынесена отдельно в pom файл.
    // т.е. нужно сделать мердж с девелопом, находясь в этой ветке и подкорректировать
    profileDir = new File(SOAP_PROFILES + "/" + id);
    if (!profileDir.exists()) {
      ProfileNotFoundException ex = new ProfileNotFoundException();
      log.error("Cant lookup profile [" + id + "] inside [" + profileDir.
              getAbsolutePath() + "]", ex);
      throw ex;
    }
    if (!profileDir.isDirectory() || profileDir.listFiles().length == 0) {
      ProfileStructureException ex = new ProfileStructureException();
      log.error(profileDir + "!= Directory OR [" + profileDir + "] empty.", ex);
      throw ex;
    }
    try {
      initServiceProp(profileDir);
      if (!serviceProp.getProperty("authorization").equals(AUTHORIZATION_OFF)) {
        log.debug("Set authorization type: " + serviceProp.getProperty("authorization"));
      }
      initRequests(profileDir);
      log.info("SoapClient profile [" + id + "] prepared.");
    } catch (RuntimeException e) {
      log.error("Error during profile setup", e);
      throw new ProfileStructureException();
    }
  }

  @Override
  public void updateValue(String key, String newValue) throws ProfileUpdateException {
    // update each message from messages Map
    for(Map.Entry<String, MessageHelper> messageHelperEntry : messages.entrySet()){
      messageHelperEntry.getValue().updateMessage(key, newValue);
    }
  }

  @Override
  public void reset() throws ProfileUpdateException {
    try {
      initRequests(profileDir);
    } catch (ProfileStructureException e) {
      log.error("Error resetting profile", e);
      throw new ProfileUpdateException();
    }
  }

  public MessageHelper getHelper(String requestId) {
    if (messages.containsKey(requestId)) {
      return messages.get(requestId);
    } else {
      log.info("Unknown request id [" + requestId + "]");
      throw new PrepareRequestException();
    }
  }

  private void initServiceProp(File profileDir) throws ProfileStructureException {
    InputStream input = null;
    // Returns pathnames for service acess propetries in this profile
    File[] propertiesPaths = profileDir.listFiles(new FileTypeFilter(id, PROPERTIES_TYPE));
    // Check duplicate ServiceAccessProp
    if (propertiesPaths.length == 1) {
      try {
        serviceProp = new Properties();
        input = new FileInputStream(propertiesPaths[0]);
        serviceProp.load(input);
      } catch (FileNotFoundException ex) {
        log.error("Properties file not found", ex);
        throw new ProfileStructureException();
      } catch (IOException ex) {
        log.error("Problem with properties load occurred", ex);
        throw new ProfileStructureException();
      } finally {
        if (input != null) {
          try {
            input.close();
          } catch (IOException e) {
            log.error("Can't close properties input stream", e);
          }
        }
      }
    } else if (propertiesPaths.length < 1) {
      log.debug("Properties for profile " + id + " not found");
      throw new ProfileStructureException();
    } else if (propertiesPaths.length > 1) {
      log.debug("Duplicate properties file found in profile " + id);
      throw new ProfileStructureException();
    }
  }

  private void initRequests(File profileDir) throws ProfileStructureException {
    // returns pathnames for requests in this profile
    File[] messagesPaths = profileDir.listFiles(new FileTypeFilter(XML_TYPE));
    messages = new HashMap<>();
    String requestId;
    for (File fName : messagesPaths) {
      int lastIndex = fName.getName().lastIndexOf('.');
      // trim "requestName.xml" to "requestName"
      requestId = fName.getName().substring(0, lastIndex);
      messages.put(requestId, new MessageHelper(fName, serviceProp));
      log.debug("Request with ID: " + requestId + " initiated");
    }
    log.info("Requests initiated");
  }

  @Override
  public String toString() {
    return id;
  }
}

class FileTypeFilter implements FilenameFilter {

  private String name;
  private String extension;

  public FileTypeFilter(String extension) {
    this.extension = extension;
  }

  public FileTypeFilter(String name, String extension) {
    this.name = name;
    this.extension = extension;
  }

  public boolean accept(File directory, String filename) {
    boolean fileOK = true;

    if (name != null) {
      fileOK &= filename.startsWith(name);
    }

    if (extension != null) {
      fileOK &= filename.endsWith('.' + extension);
    }
    return fileOK;
  }
}

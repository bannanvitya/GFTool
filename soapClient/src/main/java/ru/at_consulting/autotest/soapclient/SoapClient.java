package ru.at_consulting.autotest.soapclient;

import ru.at_consulting.autotest.api.*;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import java.util.Map;

public class SoapClient implements Client {

  private static AutotestLogger log = AutotestLogger.getLoggerInstance(SoapClient.class.getSimpleName());
  private SOAPConnectionFactory soapConnFactory;
  private SOAPConnection connection = null;
  private SoapProfile profile;
  private MessageHelper currentReqHelper;
  private Response lastResponse;

  /**TODO Implements
   * @param requestId - Request file name without extention.
   * @return Decorator for MessageHelper
   * @throws PrepareRequestException
   */
  @Override
  public Request prepareRequest(String requestId) throws PrepareRequestException {
    prepareConnection();
    if (profile == null) {
      log.debug("Profile is null!");
      throw new PrepareRequestException();
    }
    currentReqHelper = profile.getHelper(requestId);
    return new SoapRequest(connection, currentReqHelper, lastResponse);
  }

  @Override
  public Request prepareRequest(String requestId, Map<String, String> values) throws PrepareRequestException {
    prepareConnection();
    if(profile == null){
      log.debug("Profile is null!");
      throw new PrepareRequestException();
    }
    log.debug("Prepare request [" + requestId + "] and custom values: " + values.toString());
    Profile newProfile = new SoapProfile(profile);
    for(Map.Entry<String, String> kv : values.entrySet()){
      try {
        newProfile.updateValue(kv.getKey(), kv.getValue());
      } catch (ProfileUpdateException e) {
        log.error("Could not update profile", e);
        throw new PrepareRequestException();
      }
    }
    currentReqHelper = ((SoapProfile)newProfile).getHelper(requestId);
    return new SoapRequest(connection, currentReqHelper, lastResponse);
  }

  @Override
  public Response sendRequest(Request request) throws SendRequestException {
    try {
      lastResponse = new SoapResponse(currentReqHelper.sendMessage(connection, profile.getURL()));
    } catch (Exception ex) {
      log.error("Error during message send", ex);
      throw new SendRequestException();
    }
    return lastResponse;
  }

  @Override
  public void newSession() throws SessionException {
    try {
      //  Create connection. It's(SAAJ) always waiting response.
      // Equals term "session".
      soapConnFactory = SOAPConnectionFactory.newInstance();
      connection = soapConnFactory.createConnection();
    } catch (Exception ex) {
      log.error("Unexpected error during session creation", ex);
      throw new SessionException();
    }
  }

  /**
   * TODO SoapConnect.call() not created session number. So uses hashCode.
   * Default - connect locked before waiting response.
   */
  @Override
  public String sessionId() {
    return String.valueOf(connection.hashCode());
  }

  @Override
  public void setProfile(Profile profile) {
    this.profile = (SoapProfile) profile;
  }

  @Override
  public Profile getProfile() {
    return profile;
  }

  @Override
  public void preconditions() throws PreconditionsException {
    log.info("preconditions() - Starting SOAP client");
    try {
      soapConnFactory = SOAPConnectionFactory.newInstance();
    } catch (SOAPException ex) {
      log.error(SoapClient.class.getName(), ex);
      throw new PreconditionsException();
    }
  }

  @Override
  public void postconditions() throws PostconditionsException {
    try {
      if(connection != null)
        connection.close();
    } catch (SOAPException ex) {
      log.error("Connection close exception", ex);
      throw new PostconditionsException();
    }
  }

  private void prepareConnection(){
    if(connection == null){
      log.debug("Connection is null, starting new connection");
      try {
        newSession();
      } catch (SessionException e) {
        log.error("Could not create new connection", e);
        throw new PrepareRequestException();
      }
    }
    if (profile != null){

    }
  }
}

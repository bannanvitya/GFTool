package ru.at_consulting.gfTool.soapclient;

import ru.at_consulting.gfTool.api.*;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import java.util.Map;

public class SoapClient implements Client {

  private SOAPConnectionFactory soapConnFactory;
  private SOAPConnection connection = null;
  private SoapProfile profile;
  private SoapResponse lastResponse;

  @Override
  public Request prepareRequest(String requestId) throws PrepareRequestException {
    prepareConnection();

    String request = "";
    return new SoapRequest(request);
  }

    @Override
    public Request prepareRequest(String requestId, Map<String, String> values) throws PrepareRequestException {
        return null;
    }


    @Override
  public Response sendRequest(Request request) throws SendRequestException {

    return lastResponse;
  }

  @Override
  public void newSession() throws SessionException {
    try {
      soapConnFactory = SOAPConnectionFactory.newInstance();
      connection = soapConnFactory.createConnection();
    } catch (Exception ex) {
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
    try {
      soapConnFactory = SOAPConnectionFactory.newInstance();
    } catch (SOAPException ex) {
      throw new PreconditionsException();
    }
  }

  @Override
  public void postconditions() throws PostconditionsException {
    try {
      if(connection != null)
        connection.close();
    } catch (SOAPException ex) {
      throw new PostconditionsException();
    }
  }

  private void prepareConnection(){
    if(connection == null){
      try {
        newSession();
      } catch (SessionException e) {
        throw new PrepareRequestException();
      }
    }
    if (profile != null){

    }
  }
}

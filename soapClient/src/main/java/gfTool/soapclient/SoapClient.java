package gfTool.soapclient;

import gfTool.api.*;
import gfTool.api.*;

import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Map;

public class SoapClient implements Client, Serializable {

  private SOAPConnectionFactory soapConnFactory;
  private SOAPConnection connection = null;
  private SoapProfile profile;

  @Override
  public Request prepareRequest(String requestId) throws PrepareRequestException {
      return null;
  }

    @Override
  public Request prepareRequest(String requestId, Map<String, String> values) throws PrepareRequestException {
        return null;
    }


    @Override
    public Response sendRequest(Request request) throws SendRequestException {
        return null;
  }

    public Response sendRequest(Request request, String destination) throws SendRequestException {
        SoapResponse resp = null;
        try {
            SoapRequest req = (SoapRequest)request;

            MimeHeaders hd = new MimeHeaders();
            hd.addHeader("Content-type", "text/xml");

            InputStream is = new ByteArrayInputStream(req.getId().getBytes());
            SOAPMessage message = MessageFactory.newInstance().createMessage(hd, is); //create SOAPmessage based on RAW
            message.saveChanges();

            SOAPMessage reply = connection.call(message, destination); //send message, get reply

            TransformerFactory transformerFactory
                    = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            Source sourceContent = reply.getSOAPPart().getContent();
            ByteArrayOutputStream res = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(res);
            transformer.transform(sourceContent, result);

            String strReply = new String(res.toByteArray()); //get string reply

            resp = new SoapResponse(strReply);
        } catch ( SOAPException | IOException | TransformerException ex){
            throw new SendRequestException();
        }
        return resp;
    }

    public Response sendRequest(Request request, String destination, String username, String password) throws SendRequestException {
        SoapResponse resp = null;
        try {
            SoapRequest req = (SoapRequest)request;

            String authorization = new sun.misc.BASE64Encoder().encode((username + ":" + password).getBytes());
            MimeHeaders hd = new MimeHeaders();
            hd.addHeader("Authorization", "Basic " + authorization);
            hd.addHeader("Content-type", "text/xml");

            InputStream is = new ByteArrayInputStream(req.getId().getBytes());
            SOAPMessage message = MessageFactory.newInstance().createMessage(hd, is); //create SOAPmessage based on RAW
            message.saveChanges();

            SOAPMessage reply = connection.call(message, destination); //send message, get reply

            TransformerFactory transformerFactory
                    = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            Source sourceContent = reply.getSOAPPart().getContent();
            ByteArrayOutputStream res = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(res);
            transformer.transform(sourceContent, result);

            String strReply = new String(res.toByteArray()); //get string reply

            resp = new SoapResponse(strReply);
        } catch ( SOAPException | IOException | TransformerException ex){
            throw new SendRequestException();
        }
        return resp;
    }

  @Override
  public void newSession() throws SessionException {

  }

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
          connection = soapConnFactory.createConnection();
      } catch (Exception ex) {
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
}

package ru.at_consulting.gfTool.soapclient;

import ru.at_consulting.gfTool.api.AutotestLogger;
import ru.at_consulting.gfTool.api.Response;
import ru.at_consulting.gfTool.api.XmlHelper;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.Map;

public class SoapResponse implements Response {

  private static final int CODE_FAULT = 500;
  private static final int CODE_OK = 200;

  private static AutotestLogger log = AutotestLogger.getLoggerInstance(SoapClient.class.getSimpleName());
  private SOAPMessage answ;

  public SoapResponse(SOAPMessage answer) {
    answ = answer;
  }

  @Override
  public byte[] raw() {
    return answ.toString().getBytes();
  }

  @Override
  public Object responseImpl() {
    return answ;
  }

  /**@return soap fault code if response contain fault structure, 200 otherwise. */
  @Override
  public int getCode() {
    if(answ == null){
      return -1;
    }
    try {
      if(!answ.getSOAPPart().getEnvelope().getBody().hasFault()){
        return CODE_OK;
      }
      return CODE_FAULT;
    } catch (SOAPException ex) {
      log.error("Unable to get code from response", ex);
    }
    return -1;
  }

  @Override
  public String getStatus() {
    if(answ == null){
      return "Unknown";
    }
    try {
      if(!answ.getSOAPPart().getEnvelope().getBody().hasFault()){
        return "Success";
      }
      return answ.getSOAPPart().getEnvelope().getBody().getFault().getFaultString();
    } catch (SOAPException ex) {
      log.error("Unable to extract Status from message", ex);
    }
    return "Unknown";
  }

  @Override
  public String getMessage() {
    if(answ == null)
      return "Null response";
    return MessageHelper.viewMessage(answ);
  }

  @Override
  public Map<String, String> asDict() {
    Map<String, String> map = null;
    try {
      map = XmlHelper.XmlAsMap(answ.getSOAPBody().getParentNode());
    } catch (SOAPException ex) {
      log.error("Unable to transform response to map", ex);
    }
    return map;
  }
}

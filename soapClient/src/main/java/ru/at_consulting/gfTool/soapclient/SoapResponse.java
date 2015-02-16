package ru.at_consulting.gfTool.soapclient;

import ru.at_consulting.gfTool.api.Response;
import java.util.Map;

public class SoapResponse implements Response {
  private String message;

  public SoapResponse(String msg) {
      message = msg;
  }

  @Override
  public byte[] raw() {
    return message.toString().getBytes();
  }

  @Override
  public Object responseImpl() {
    return message;
  }

  /**@return soap fault code if response contain fault structure, 200 otherwise. */
  @Override
  public int getCode() {
   return 1;
  }

  @Override
  public String getStatus() {
    return null;
  }

  @Override
  public String getMessage() {
      return null;
  }

  @Override
  public Map<String, String> asDict() {
      return null;
  }
}

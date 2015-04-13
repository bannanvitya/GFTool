package gfTool.soapclient;

import gfTool.api.Request;

import java.io.Serializable;

public class SoapRequest implements Request, Serializable {
  private String messageId;

  public SoapRequest(String msg){
      messageId = msg;
  }

    @Override
    public byte[] raw() {
        return null;
    }

    public String getId() {
        return messageId;
    }

    @Override
    public Object requestImpl() {
        return null;
    }
}

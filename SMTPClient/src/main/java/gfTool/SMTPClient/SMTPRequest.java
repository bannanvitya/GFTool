package gfTool.SMTPClient;

import gfTool.api.Request;

public class SMTPRequest implements Request {
  private String message;
    private String requestId;


  public SMTPRequest(String msg){
    message = msg;
  }


    @Override
    public byte[] raw() {
        return null;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Object requestImpl() {
        return null;
    }
}

package ru.at_consulting.gfTool.IBMMqClient;

import ru.at_consulting.gfTool.api.AutotestLogger;
import ru.at_consulting.gfTool.api.Request;
import ru.at_consulting.gfTool.api.Response;

import javax.xml.soap.SOAPConnection;

public class IBMMqRequest implements Request {
  private String message;
    private String requestId;


  public IBMMqRequest(String msg){
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

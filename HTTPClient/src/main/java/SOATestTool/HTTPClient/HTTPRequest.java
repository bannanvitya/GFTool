package SOATestTool.HTTPClient;

import SOATestTool.api.Request;

public class HTTPRequest implements Request {
  private String message;
    private String requestId;


  public HTTPRequest(String msg){
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

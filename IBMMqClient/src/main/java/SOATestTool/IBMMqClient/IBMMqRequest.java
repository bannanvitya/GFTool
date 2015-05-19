package SOATestTool.IBMMqClient;

import SOATestTool.api.Request;

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

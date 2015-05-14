package gfTool.SMTPClient;

import gfTool.api.Request;

public class SMTPRequest implements Request {
    public String message;
    public String subj;
    public String[] sendTo;


  public SMTPRequest(String message, String subj, String[] sendTo){
      this.message = message;
      this.subj = subj;
      this.sendTo = sendTo;
  }

    @Override
    public byte[] raw() {
        return ("Subj: " + subj + "\nTo: " + sendTo + "\nMessage: " + message).getBytes();
    }

    @Override
    public Object requestImpl() {
        return message;
    }
}

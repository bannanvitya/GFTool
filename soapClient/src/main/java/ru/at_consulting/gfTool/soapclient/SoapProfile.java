package ru.at_consulting.gfTool.soapclient;


import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.WSDLParser;
import ru.at_consulting.gfTool.api.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SoapProfile implements Profile {

    private String url;
    private Definitions wsdl;
    private Map<String, String> messagesMap;
    private Properties serviceProp;


 public SoapProfile(SoapProfile another){
    this.url = another.url;
    this.messagesMap = another.messagesMap;
    this.serviceProp = another.serviceProp;
  }

  public SoapProfile() {
  url = "Profile not init";
  }

    public void setWsdl(){
        WSDLParser parser = new WSDLParser();

        wsdl = parser.parse("resources/article/article.wsdl");
}


  public void setMessagesMap(){
      Map<String, String> map = WsdlHelper.parseWSDL(wsdl);
  }


  @Override
  public String getId() {
    return url;
  }

  @Override
  public void setId(String url) throws ProfileNotFoundException,
    ProfileStructureException {
    this.url = url;
  }

  @Override
  public void updateValue(String key, String newValue) throws ProfileUpdateException {
  }

  @Override
  public void reset() throws ProfileUpdateException {
  }

  @Override
  public String toString() {
    return url;
  }
}



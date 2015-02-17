package ru.at_consulting.gfTool.soapclient;


import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import ru.at_consulting.gfTool.api.*;

import java.util.List;
import java.util.Map;

public class SoapProfile implements Profile {

    private String url;
    private Definitions wsdl;
    private Map<String, String> messagesMap;
    private Map<String, SoapMsgConfig> messagesConfigMap;
    private List<Binding> bindings;


 public SoapProfile() {
  url = "Profile not init";
  }

    public void setUrl(String url){
        this.url = url;
    }

    public void processWsdl() {
        WSDLParser parser = new WSDLParser();
        this.wsdl = parser.parse(url);
    }

    public void processMessagesConfigMap(){
      this.messagesConfigMap = WsdlHelper.parseWSDL(wsdl);
    }

    public void processMessagesMap(){
      this.messagesMap = WsdlHelper.getMessagesMap(messagesConfigMap);
    }

    public void processBindings(){
      this.bindings = WsdlHelper.parseWSDLforBindings(wsdl);
    }

    public List<Binding> getBindings(){
        return bindings;
    }

    public Map<String, String> getMessagesMap(){
        return messagesMap;
    }

    public Map<String, SoapMsgConfig> getMessagesConfigMap(){
        return messagesConfigMap;
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



package gfTool.soapclient;


import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import gfTool.api.Profile;
import gfTool.api.ProfileNotFoundException;
import gfTool.api.ProfileStructureException;
import gfTool.api.ProfileUpdateException;
import gfTool.api.*;

import java.util.List;
import java.util.Map;

public class SoapProfile implements Profile {

    private String urlToWsdl;
    private String urlToSend;
    private Definitions wsdl;
    private Map<String, String> messagesMap;
    private Map<String, SoapMsgConfig> messagesConfigMap;
    private List<Binding> bindings;


    public SoapProfile() {
        urlToWsdl = "Profile not init";
    }

    public void setUrlToWsdl(String urlToWsdl){
        this.urlToWsdl = urlToWsdl;
    }

    public void processWsdl() {
        WSDLParser parser = new WSDLParser();
        this.wsdl = parser.parse(urlToWsdl);
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

    public void setMessagesMap(Map<String, String> x){
        messagesMap = x;
    }

    public Map<String, SoapMsgConfig> getMessagesConfigMap(){
        return messagesConfigMap;
    }

    public String getUrlToSend(){
        return urlToSend;
    }






    public void setUrlToSend(String urlToSend){
        this.urlToSend = urlToSend;
    }

    @Override
    public String getId() {
        return urlToWsdl;
    }

    @Override
    public void setId(String url) throws ProfileNotFoundException,
            ProfileStructureException {
        this.urlToWsdl = url;
    }

    @Override
    public void updateValue(String key, String newValue) throws ProfileUpdateException {
    }

    @Override
    public void reset() throws ProfileUpdateException {
    }

    @Override
    public String toString() {
        return urlToWsdl;
    }
}

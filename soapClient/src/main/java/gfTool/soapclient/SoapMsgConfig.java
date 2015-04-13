package gfTool.soapclient;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Port;

/**
 * This class encapsulates all required variables to craft a SOAP request message.
 * @author Albertov91
 *
 */
public class SoapMsgConfig implements Serializable {

    private Definitions wsdl;
    private int soapVersion = 0;
    private Port port;
    private BindingOperation bindOp;
    private HashMap<String, String> params;

    /* Constructors. */
    public SoapMsgConfig(){

    }

    public SoapMsgConfig(Definitions wsdl, int soapVersion,HashMap<String, String> params, Port port, BindingOperation bindOp){
        this.setWsdl(wsdl);
        this.setSoapVersion(soapVersion);
        this.setPort(port);
        this.setBindOp(bindOp);
        this.setParams(params);
    }

    public boolean isComplete(){
        if (this.wsdl == null || this.soapVersion < 1 || this.soapVersion > 2 ||
                this.port == null || this.bindOp == null) return false;
        else return true;
    }
    /* Getters and Setters. */
    public Definitions getWsdl() {
        return wsdl;
    }

    public void setWsdl(Definitions wsdl) {
        this.wsdl = wsdl;
    }

    public int getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(int soapVersion) {
        this.soapVersion = soapVersion;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public BindingOperation getBindOp() {
        return bindOp;
    }

    public void setBindOp(BindingOperation bindOp) {
        this.bindOp = bindOp;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }
}
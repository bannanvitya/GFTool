package ru.at_consulting.gfTool.soapclient;

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
public class SoapMsgConfig {

    private Definitions wsdl;
    private int soapVersion = 0;
    private Port port;
    private BindingOperation bindOp;

    /* Constructors. */
    public SoapMsgConfig(){

    }

    public SoapMsgConfig(Definitions wsdl, int soapVersion, Port port, BindingOperation bindOp){
        this.setWsdl(wsdl);
        this.setSoapVersion(soapVersion);
        this.setPort(port);
        this.setBindOp(bindOp);
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
}
package ru.at_consulting.gfTool.soapclient;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.at_consulting.gfTool.api.*;

import javax.net.ssl.*;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.*;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

public class MessageHelper {

    private static final String AUTH_NONE = "none";
    private static final String AUTH_BASIC = "basic";

    private static AutotestLogger log = AutotestLogger.getLoggerInstance(MessageHelper.class.getSimpleName());
    private SOAPMessage soapMessage;
    private Properties prop;
    private Document messageDoc;
    //  TODO
    //  private Document curResp;

    public MessageHelper(File fName, Properties serviceProp) throws ProfileStructureException {
        try {
            prop = serviceProp;
            MessageFactory messageFactory = MessageFactory.newInstance();
            soapMessage = messageFactory.createMessage();
            if (prop.getProperty("authorization").equals(AUTH_BASIC)) {
                setBasicAuthorization(prop);
            }
            //getting token if needed
            if (prop.getProperty("SAPI_URL") != null) {
                getComverseToken();
            }
            // setting Content-Type
            MimeHeaders hd = soapMessage.getMimeHeaders();
            hd.addHeader("Content-Type", "text/xml");
            messageDoc = XmlHelper.readXml(new FileInputStream(fName));
            setMessageDoc();


            //setting token in each request if needed
            if (prop.getProperty("SAPI_URL") != null) {
                // getting SOAPBody of SOAPMessage
                SOAPPart soapPart = soapMessage.getSOAPPart();
                SOAPEnvelope envelope = soapPart.getEnvelope();
                SOAPBody body = envelope.getBody();


                //setting securityToken value to value contains
                Node input = body.getChildNodes().item(1).getChildNodes().item(1);
                Integer i = 0;
                Node tempNode;
                while (input.getChildNodes().item(i) != null) {
                    tempNode = input.getChildNodes().item(i);
                    if (tempNode.getNodeType() == Node.ELEMENT_NODE)
                        if (tempNode.getNodeName().equals("securityToken")) {
                            log.debug(prop.getProperty("token"));
                            tempNode.getChildNodes().item(0).setNodeValue(prop.getProperty("token").toString());
                            break;
                        }
                    i++;
                }
                soapMessage.saveChanges();
                log.debug(viewMessage(soapMessage));
            }
        } catch (FileNotFoundException | SAXException ex) {
            log.error("Problem with response file", ex);
            throw new ProfileStructureException();
        } catch (IOException | ParserConfigurationException | SOAPException ex) {
            log.error("Problem with response file", ex);
            throw new ProfileStructureException();
        } catch (SendRequestException | KeyManagementException ex){
            log.error("Problem with response file", ex);
            throw new ProfileStructureException();
        }
    }

    public static String viewMessage (SOAPMessage message){
        String result = null;

        if (message != null)
        {
            ByteArrayOutputStream baos = null;
            try
            {
                baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                result = baos.toString();
            }
            catch (Exception e){
                log.error("Unable to parse SOAPMessage", e);
                throw new PrepareRequestException();
            }
            finally{
                if (baos != null){
                    try{
                        baos.close();
                    }
                    catch (IOException ioe){
                        log.error("Internal error converting SOAPMessage to String", ioe);
                        throw new PrepareRequestException();
                    }
                }
            }
        }
        return result;
    }


    /** Stub for authorization TODO different type autorization (basic, https...) */
    private void setBasicAuthorization(Properties prop) {
        String authorization = new sun.misc.BASE64Encoder().encode(
                (prop.getProperty("username") + ":" + prop.getProperty(
                        "password")).getBytes());
        MimeHeaders hd = soapMessage.getMimeHeaders();
        hd.addHeader("Authorization", "Basic " + authorization);
    }

    // sending request to SAPI Security to get token
    private String getComverseToken() throws SOAPException, SendRequestException, KeyManagementException {

        SOAPConnectionFactory soapConnFactory
                = SOAPConnectionFactory.newInstance();
        SOAPConnection connection
                = soapConnFactory.createConnection();

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage message = messageFactory.createMessage();

        // TODO
        // Authorization to rewrite
        String username = "d";
        String password = "d";
        String authorization = new sun.misc.BASE64Encoder().encode((username + ":" + password).getBytes());
        MimeHeaders hd = message.getMimeHeaders();
        hd.addHeader("Authorization", "Basic " + authorization);


        // construckting SOAP message
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();

        QName bodyName =
                new QName("ns:proxyLogin");
        SOAPBodyElement purchaseLineItems =
                body.addBodyElement(bodyName);
        purchaseLineItems.setAttribute("xmlns:ns", "https://org.comverse.rtbd.sec/webservice/auth");

        QName childName = new QName("String_1");
        SOAPElement order = purchaseLineItems.addChildElement(childName);
        order.addTextNode("sapiuser");

        childName = new QName("String_2");
        order = purchaseLineItems.addChildElement(childName);
        order.addTextNode("sapipass1");

        childName = new QName("String_3");
        order = purchaseLineItems.addChildElement(childName);
        order.addTextNode("SAPI");


        String destination = prop.getProperty("SAPI_URL");
        SOAPMessage reply;
        String token;
        //getting response, converting it to string & parse token value
        if (destination.contains("https")) {

            log.debug("Message to be sent={ \n" + viewMessage(message) + "}");

            //trust all certs
            doTrustToCertificates();

            reply = connection.call(message, destination);
            log.debug("Received message={ \n" + viewMessage(reply) + "}");
        } else {
            log.debug("Message to be sent={ \n" + viewMessage(message) + "}");
            reply = connection.call(message, destination);
            log.debug("Received message={ \n" + viewMessage(reply) + "}");
        }

        String stringReply = MessageHelper.viewMessage(reply);
        if (stringReply.contains("&lt;"))
            token = stringReply.substring(stringReply.lastIndexOf("&lt;Token&gt;") + 13, stringReply.indexOf("&lt;/Token&gt;"));
        else
            token = stringReply.substring(stringReply.lastIndexOf("<Token>") + 7, stringReply.indexOf("</Token>"));
        log.debug(token);
        prop.setProperty("token", token);


        return token;
    }



    /** TODO Add auto disconnect if response timeout */
    public SOAPMessage sendMessage(SOAPConnection connection, String serviceUrl) throws SendRequestException {
        try {
            if (serviceUrl.contains("https"))  {


                log.debug("Message to be sent={ \n" + viewMessage(soapMessage) + "}");
                doTrustToCertificates();

                SOAPMessage response = connection.call(soapMessage, serviceUrl);
                log.debug("Received message={ \n" + viewMessage(response) + "}");
                return response;
            }
            else {
                log.debug("Message to be sent={ \n" + viewMessage(soapMessage) + "}");
                SOAPMessage response = connection.call(soapMessage, serviceUrl);
                log.debug("Received message={ \n" + viewMessage(response) + "}");
                return response;
            }
        } catch (Exception e){
            log.error("Could not send message", e);
            throw new SendRequestException();
        }
    }

    public static void doTrustToCertificates() throws KeyManagementException, SendRequestException {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        return;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        return;
                    }
                }
        };

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not send message", e);
            throw new SendRequestException();
        }
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    log.info("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }


    private void setMessageDoc() throws SOAPException {
        soapMessage.getSOAPPart().setContent(new DOMSource(messageDoc));
        soapMessage.saveChanges();
    }

    public void updateMessage(String key, String newValue) throws ProfileUpdateException {
        // assuming that _key_ is an XPATH to find element which should be updated
        try {
            XmlHelper.setElementValue(messageDoc.getDocumentElement(), key, newValue);
            setMessageDoc();
        } catch (SOAPException e) {
            log.error("Unable to update message", e);
            throw new ProfileUpdateException();
        }

    }

    public static String getValueByXPATH(SOAPMessage message, String xPathQuery) throws SOAPException, XPathExpressionException {
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();

        String reply;
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = xpath.compile(xPathQuery);

        Object result = expr.evaluate(body, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        if (nodes.getLength() == 0)
            throw new SOAPException();

        log.debug(nodes.item(0).getNodeValue());

        reply = nodes.item(0).getNodeValue();
        return reply;
    }

    public static void setValueByXPATH (SOAPMessage message, String value, String xPath) throws SOAPException, XPathExpressionException {
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = xpath.compile(xPath);

        Object result = expr.evaluate(body, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        nodes.item(0).setNodeValue(value);

        message.saveChanges();
    }

    public String getToken(){
        return prop.getProperty("token");
    }

    public SOAPMessage getSoapMessage(){
        return soapMessage;
    }
}

package ru.at_consulting.gfTool.HTTPClient;

import ru.at_consulting.gfTool.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by VKhozhaynov on 02.02.2015.
 */
public class HTTPClient implements Client {

    private HTTPProfile profile;
    private Map<HTTPRequest, HTTPResponse> messageHelper = new HashMap<HTTPRequest, HTTPResponse>();

    @Override
    public Response sendRequest(Request request) throws SendRequestException, ProfileStructureException {
        HttpURLConnection con;
        HTTPResponse resp;
        HTTPRequest req = (HTTPRequest) request;
        Integer httpResultCode;
        String respMessage = "";
        try {
            URL object = new URL(profile.getProperties().getProperty("url"));
            con = (HttpURLConnection) object.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", profile.getProperties().getProperty("contentType"));
            con.setRequestProperty("Accept", profile.getProperties().getProperty("contentType"));
            con.setRequestMethod(profile.getProperties().getProperty("methodType"));

            if (profile.getProperties().getProperty("methodType").equals("GET") ){

                Properties headers = profile.getHeaders();
                if (headers != null) {
                    Enumeration<?> heads = headers.propertyNames();
                    while (heads.hasMoreElements()) {
                        String key = (String) heads.nextElement();
                        String value = headers.getProperty(key);
                        con.setRequestProperty(key, value);
                    }
                }

                httpResultCode = con.getResponseCode(); // Code

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }
                in.close();
                respMessage = "" + sb.toString(); // Message
            }
            else {
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(req.getMessage());
                wr.flush();

                StringBuilder sb = new StringBuilder();
                httpResultCode = con.getResponseCode();  // Code
                if (httpResultCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    respMessage = "" + sb.toString();
                } else {
                    respMessage = con.getResponseMessage(); // Message
                }
            }
            resp = new HTTPResponse(respMessage, httpResultCode);           //Response
            messageHelper.put(req, resp);                           // Request <-> Response map

        } catch (MalformedURLException e){
            throw new SendRequestException();
        } catch (ProtocolException ex){
            throw new SendRequestException();
        } catch (IOException ex){
            throw new SendRequestException();
        }
        return resp;
    }



    @Override
    public Request prepareRequest(String requestId) throws PrepareRequestException, ProfileStructureException {
        return null;
    }


    @Override
    public Request prepareRequest(String requestId, Map<String, String> values) throws PrepareRequestException {
        return null;
    }


    @Override
    public void newSession() throws SessionException {

    }

    @Override
    public String sessionId() {
        return null;
    }


    @Override
    public void setProfile(Profile profile) {
        this.profile = (HTTPProfile) profile;
    }

    @Override
    public Profile getProfile() {
        return profile;
    }

    @Override
    public void preconditions() throws PreconditionsException {

    }

    @Override
    public void postconditions() throws PostconditionsException {

    }


}

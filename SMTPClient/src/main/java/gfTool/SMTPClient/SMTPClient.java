package gfTool.SMTPClient;

import gfTool.api.*;

import java.security.Security;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

/**
 * Created by VKhozhaynov on 02.02.2015.
 */
public class SMTPClient implements Client {
    SMTPProfile profile;


    @Override
    public Response sendRequest(Request request) throws SendRequestException, ProfileStructureException {
        return null;
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
        return "";
    }

    @Override
    public void setProfile(Profile profile) {
        this.profile = (SMTPProfile) profile;
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

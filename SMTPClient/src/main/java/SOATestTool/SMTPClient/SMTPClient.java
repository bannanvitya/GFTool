package SOATestTool.SMTPClient;

import SOATestTool.api.*;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;

/**
 * Created by VKhozhaynov on 02.02.2015.
 */
public class SMTPClient implements Client {
    SMTPProfile profile;


    @Override
    public Response sendRequest(Request request) throws SendRequestException, ProfileStructureException {
        SMTPRequest req = (SMTPRequest) request;

        Properties props = profile.getProperties();

        String from = props.getProperty("mail.smtp.user");
        String host = props.getProperty("mail.smtp.host");
        String pass = props.getProperty("mail.smtp.password");


        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[req.sendTo.length];

            // To get the array of addresses
            for( int i = 0; i < req.sendTo.length; i++ ) {
                toAddress[i] = new InternetAddress(req.sendTo[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(req.subj);
            message.setText(req.message);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
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

package ru.at_consulting.gfTool.IBMMqClient;

import ru.at_consulting.gfTool.api.*;


import com.ibm.jms.JMSTextMessage;
import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueueConnection;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueueSender;
import com.ibm.mq.jms.MQQueueSession;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.Map;
import java.util.Properties;

/**
 * Created by VKhozhaynov on 02.02.2015.
 */
public class IBMMqClient implements Client{

    private MQQueueConnectionFactory factory;
    private MQQueueConnection connection;
    private MQQueueSession session = null;
    private Queue queue;
    private IBMMqProfile profile;



    @Override
    public Response sendRequest(Request request) throws SendRequestException, ProfileStructureException {

        try {
            connection.start();
            MQQueueSender sender = (MQQueueSender) session.createSender(queue);
            JMSTextMessage jmsMessage = null;
            jmsMessage = (JMSTextMessage) session.createTextMessage();

            IBMMqRequest req = (IBMMqRequest) request;
            jmsMessage.setText(req.getMessage());
            sender.send(jmsMessage);
        }catch (Exception e){
            throw new SendRequestException();
        }
        return null;
    }



    @Override
    public Request prepareRequest(String requestId) throws PrepareRequestException, ProfileStructureException {
        factory = new MQQueueConnectionFactory();
        try {
            Properties prop = profile.getProperties();
            factory.setHostName(prop.getProperty("host"));
            factory.setPort(Integer.parseInt(prop.getProperty("port")));
            factory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
            factory.setQueueManager(prop.getProperty("queueManager"));
            factory.setChannel(prop.getProperty("channel"));

            connection = (MQQueueConnection) factory.createQueueConnection(prop.getProperty("userId"), prop.getProperty("password"));

            session = (MQQueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = session.createQueue(prop.getProperty("queueName"));

        } catch (JMSException ex){
            System.out.println(ex.toString());
            throw new PrepareRequestException();
        } catch (Exception e){
            throw new ProfileStructureException();
        }
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
        return String.valueOf(connection.hashCode());
    }

    @Override
    public void setProfile(Profile profile) {
        this.profile = (IBMMqProfile) profile;
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
        try {
            session.close();
            connection.close();
        } catch (JMSException e){
            throw new PostconditionsException();
        }
    }


}

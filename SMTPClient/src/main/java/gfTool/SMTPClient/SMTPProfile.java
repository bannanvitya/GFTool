package gfTool.SMTPClient;

import gfTool.api.Profile;
import gfTool.api.ProfileNotFoundException;
import gfTool.api.ProfileStructureException;
import gfTool.api.ProfileUpdateException;

import java.util.Properties;

public class SMTPProfile implements Profile {
    private Properties serviceProp = new Properties();
    private String profileId = null;
    private static final String DEFAULT_SMTP_HOST_NAME = "smtp.gmail.com";
    private static final String DEFAULT_SMTP_PORT = "587";
    private static final String DEFAULT_SMTP_USER = "***";
    private static final String DEFAULT_SMTP_PASS = "***";

    @Override
    public String getId() {
        return profileId;
    }

    @Override
    public void setId(String id) throws ProfileNotFoundException, ProfileStructureException {
        profileId = id;
        serviceProp.put("mail.smtp.starttls.enable", "true");
        serviceProp.put("mail.smtp.host", DEFAULT_SMTP_HOST_NAME);
        serviceProp.put("mail.smtp.port", DEFAULT_SMTP_PORT);
        serviceProp.put("mail.smtp.auth", "true");

        serviceProp.put("mail.smtp.user", DEFAULT_SMTP_USER);
        serviceProp.put("mail.smtp.password", DEFAULT_SMTP_PASS);
    }

    public Properties getProperties() {
        return serviceProp;
    }

    @Override
    public void updateValue(String key, String newValue) throws ProfileUpdateException {
        serviceProp.setProperty(key, newValue);
    }

    @Override
    public void reset() throws ProfileUpdateException {

    }
}


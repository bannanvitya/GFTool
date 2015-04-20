package gfTool.SMTPClient;

import gfTool.api.Profile;
import gfTool.api.ProfileNotFoundException;
import gfTool.api.ProfileStructureException;
import gfTool.api.ProfileUpdateException;

import java.util.Properties;

public class SMTPProfile implements Profile {
    private Properties serviceProp;
    private String profileId = null;


    @Override
    public String getId() {
        return profileId;
    }


    @Override
    public void setId(String id) throws ProfileNotFoundException, ProfileStructureException {
        profileId = id;
        serviceProp = null;
    }

    public void setId(String id, Properties prop) throws ProfileNotFoundException, ProfileStructureException {
        profileId = id;
        serviceProp = prop;
    }

    public Properties getProperties() {
        return serviceProp;
    }

    @Override
    public void updateValue(String key, String newValue) throws ProfileUpdateException {

    }

    @Override
    public void reset() throws ProfileUpdateException {

    }
}


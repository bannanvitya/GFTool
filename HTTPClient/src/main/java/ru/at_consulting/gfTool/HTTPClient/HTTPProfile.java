package ru.at_consulting.gfTool.HTTPClient;

import ru.at_consulting.gfTool.api.*;

import java.util.Properties;

public class HTTPProfile implements Profile {
    private Properties serviceProp;
    private Properties headers = null;
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

    public void setId(String id, Properties prop, Properties head) throws ProfileNotFoundException, ProfileStructureException {
        profileId = id;
        serviceProp = prop;
        headers = head;
    }

    public Properties getProperties() {
        return serviceProp;
    }

    public Properties getHeaders(){
        return headers;
    }
    @Override
    public void updateValue(String key, String newValue) throws ProfileUpdateException {

    }

    @Override
    public void reset() throws ProfileUpdateException {

    }
}


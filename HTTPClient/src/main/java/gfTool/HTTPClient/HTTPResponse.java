package gfTool.HTTPClient;

import gfTool.api.Response;

import java.util.Map;

/**
 * Created by VKHozhaynov on 03.02.2015.
 */
public class HTTPResponse implements Response {
    private String message;
    private Integer code;

    public HTTPResponse(String mess, Integer cod){
        message = mess;
        code = cod;
    }

    @Override
    public byte[] raw() {
        return null;
    }

    @Override
    public Object responseImpl() {
        return null;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return code.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Map<String, String> asDict() {
        return null;
    }
}

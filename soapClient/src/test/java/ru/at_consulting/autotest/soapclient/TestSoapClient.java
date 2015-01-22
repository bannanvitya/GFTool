package ru.at_consulting.autotest.soapclient;


import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Header;
import ru.at_consulting.autotest.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


/**
 * Created by APonomareva on 29.07.2014.
 */
public class TestSoapClient {
  private static final MockServerClient mockServer = new MockServerClient("localhost", 1081);
  private static final String REGULAR_RESPONSE_BODY =
      "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bil=\"http://at-consulting/test\">" +
      "<soapenv:Body>" +
      "<bil:Test>" +
      "<id>123</id>" +
      "</bil:Test>" +
      "</soapenv:Body>" +
      "</soapenv:Envelope>";

  private static final String FAULT_RESPONSE_BODY =
      "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
          "xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" " +
          "xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">" +
      "<SOAP-ENV:Body>" +
      "<SOAP-ENV:Fault>" +
      "<faultcode xsi:type=\"xsd:string\">SOAP-ENV:Client</faultcode>" +
      "<faultstring xsi:type=\"xsd:string\">Error description</faultstring>" +
      "</SOAP-ENV:Fault>" +
      "</SOAP-ENV:Body>" +
      "</SOAP-ENV:Envelope>";


    @Test
    public void readedEnvVariable() {
        System.out.println("Path to EnvVariables: " + System.getProperty("soap_client.profiles").replace("\\", "/"));
    }

  @Test
  public void shouldHandleSuccess() throws ProfileNotFoundException, ProfileStructureException, SessionException, SendRequestException {
//    given
    mockServer.reset();
    mockServer
        .when(
            request()
                .withMethod("POST")
                .withPath("/mockService")
        )
        .respond(
            response()
                .withStatusCode(200)
                .withHeaders(
                    new Header("Content-Type", "text/xml")
                )
                .withBody(REGULAR_RESPONSE_BODY)
        );

    // when
    Client client = prepareClient("MockService");
    Request req = client.prepareRequest("success");
    Response resp = client.sendRequest(req);

    // then
    assertThat("CorrectCode", resp.getCode(), equalTo(200));
    assertThat("CorrectStatus", resp.getStatus(), equalTo("Success"));
    assertThat("CorrectMessage", resp.getMessage(), equalTo(REGULAR_RESPONSE_BODY));
    System.out.println("dict" + resp.asDict());
    assertThat("CorrectDict", resp.asDict(), IsEqual.<Map<String, String>>equalTo(new HashMap<String, String>() {{
      put("id", "123");
    }}));
  }

  @Test
  public void shouldHandleSoapFault() throws ProfileNotFoundException, ProfileStructureException, SessionException, SendRequestException {
    // given
    mockServer.reset();
    mockServer
        .when(
            request()
                .withMethod("POST")
                .withPath("/mockService")
        )
        .respond(
            response()
                .withStatusCode(500)
                .withHeaders(
                    new Header("Content-Type", "text/xml")
                )
                .withBody(FAULT_RESPONSE_BODY)
        );

    // when
    Client client = prepareClient("MockService");
    Request req = client.prepareRequest("success");
    Response resp = client.sendRequest(req);

    // then
    assertThat("CorrectCode", resp.getCode(), equalTo(500));
    assertThat("CorrectStatus", resp.getStatus(), equalTo("Error description"));
    //assertThat("CorrectMessage", resp.getMessage(), equalTo(FAULT_RESPONSE_BODY));
    assertThat("CorrectDict", resp.asDict(), IsEqual.<Map<String, String>>equalTo(new HashMap<String, String>() {{
      put("faultcode", "SOAP-ENV:Client");
      put("faultstring", "Error description");
      put("xsi:type", "xsd:string");
    }}));
  }

  private Client prepareClient(String profileId) throws ProfileNotFoundException, ProfileStructureException, SessionException {
    Profile profile = new SoapProfile();
    profile.setId(profileId);
    Client client = new SoapClient();
    client.setProfile(profile);
    client.newSession();
    return client;
  }

}
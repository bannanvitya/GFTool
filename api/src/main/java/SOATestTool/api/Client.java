package SOATestTool.api;

import java.util.Map;

/**
 * Represents any-type client, e.g. DiameterClient, SoapClient, RestClient, etc.
 */
public interface Client extends Tool {
  /**
   * Prepares request with that id.
   * <p>
   * Each request should be identified with corresponding id.
   * Profile implementation should take care of providing real request artifacts by this id
   *
   * @param           requestId the id of the request to be prepared
   * @return          <code>Request</code> representation of this request
   * @see             Request
   * @see             Profile
   */
  Request prepareRequest(String requestId) throws PrepareRequestException, SendRequestException, ProfileStructureException;

  /**
   * Prepares request with that id.
   * <p>
   * Each request should be identified with corresponding id.
   * Profile implementation should take care of providing real request artifacts by this id
   *
   * @param           requestId the id of the request to be prepared
   * @param           values the values map to update before sending request
   * @return          <code>Request</code> representation of this request
   * @see             Request
   * @see             Profile
   */
  Request prepareRequest(String requestId, Map<String,String> values) throws PrepareRequestException;

  /**
   * Sends prepared request according to profile settings
   * <p>
   * Each request should prepared with prepareRequest beforehand.
   *
   * @param           request the Request prepared in accordance with Profile
   * @return          <code>Response</code> representation of this request
   * @see             Response
   * @see             Profile
   */
  Response sendRequest(Request request) throws SendRequestException, ProfileStructureException;

  /**
   * Starts new session, all requests will be sent through this session until the next newSession call
   * <p>
   *
   * @see             Response
   */
  void newSession() throws SessionException;

  /**
   * Returns current session id
   * <p>
   *
   * @return          <code>String</code> session id
   * @see             Response
   */
  String sessionId();

}

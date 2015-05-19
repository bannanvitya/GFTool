package SOATestTool.api;

import java.util.Map;

  /**
   * The wrapper for particular response (http, diameter, soap, etc.)
   * <p>
   *
   * @see Client
   * @see Request
   *
   */
public interface Response {

  /**
   * Raw representation of this response
   *
   * @return            <code>byte[]</code> raw response
   * @see               Request
   */
	byte[] raw();

  /**
   * Implementation of this response
   *
   * @return            <code>Object</code> response implementation
   * @see               Request
   */
  Object responseImpl();

  /**
   * Returns code of this response as string
   *
   * @return            <code>int</code> response code as string
   * @see               Request
   */
	int getCode();

  /**
   * Returns status of this response as string
   *
   * @return            <code>String</code> response status as string
   * @see               Request
   */
	String getStatus();

  /**
   * Returns message of this response as message
   *
   * @return            <code>String</code> response message as string
   * @see               Request
   */
	String getMessage();

  /**
   * Returns response as Map
   *
   * @return            <code>Map</code> response as map
   * @see               Request
   */
	Map<String, String> asDict();

  /**
   * String representing this request
   *
   * @return            <code>String</code> string representing this request
   * @see               Response
   */
  String toString();
}

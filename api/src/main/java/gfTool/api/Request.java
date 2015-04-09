package gfTool.api;

/**
 * The wrapper for particular request (http, diameter, soap, etc.)
 * <p>
 *
 * @see Client
 * @see Response
 *
 */
public interface Request {

  /**
   * Raw representation of this request
   *
   * @return            <code>byte[]</code> raw data of this request
   * @see               Response
   */
  byte[] raw();

  /**
   * Implementation of this request for the concrete client
   *
   * @return            <code>Object</code> request implementation
   * @see               Response
   */
  Object requestImpl();

  /**
   * String representing this request
   *
   * @return            <code>String</code> string representing this request
   * @see               Response
   */
  String toString();
}

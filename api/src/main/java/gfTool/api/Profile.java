package gfTool.api;

/**
 * The profile of a tool (e.g. http_client profile, log_tool profile, etc.)
 * <p>
 *
 * Profile identifies different tool setting
 * and provides means to manipulate those settings
 *
 */
public interface Profile {

  /**
   * JNDI location with set of messages, corresponding to that profile
   * <p>
   *
   * @return          JNDI id of this profile (aka profile name)
   */
	String getId();

  /**
   * JNDI location with set of messages, corresponding to that profile
   * <p>
   *
   * @param           id JNDI name which identifies the set of messages for that profile
   */

  void setId(String id) throws ProfileNotFoundException, ProfileStructureException;

  /**
   * Updates this profile key with new value
   *
   * @param           key the key to be updates
   * @param           newValue new value of that key
   */
	void updateValue(String key, String newValue) throws ProfileUpdateException;

  /**
   * Resets profile to default values
   */
  void reset() throws ProfileUpdateException;

}

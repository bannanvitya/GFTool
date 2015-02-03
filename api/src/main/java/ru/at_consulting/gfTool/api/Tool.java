package ru.at_consulting.gfTool.api;

/**
 * Parent interface for Client and LogTool interfaces
 * <p>
 *
 * @see Client
 * @see LogTool
 *
 */
interface Tool {

  /**
   * Sets profile of this tool
   *
   * @param profile     Profile to be set
   * @see               Profile
   */
  void setProfile(Profile profile);

  /**
   * Gets profile of this tool
   *
   * @return            Profile of this tool
   * @see               Profile
   */
  Profile getProfile();

  /**
   * Executes corresponding preconditions (e.g. empty logs, create tmp directory)
   *
   */
  void preconditions() throws PreconditionsException;

  void postconditions() throws PostconditionsException;

}

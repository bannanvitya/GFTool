package ru.at_consulting.gfTool.api;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents log tool to get information from the log files
 */
public interface LogTool<T> extends Tool {

  /**
   * Gets logs from location specified by profile
   * <p>
   * Profile should specify the location of the logs in interest
   *
   * @see               Profile
   */
  void hitchLog() throws HitchLogException;

  /**
   * Finds substring in hitched log, specified by regex
   * <p>
   * First found substring returned or null
   *
   * @param             regex regular expression to find a substring
   * @return            <code>found_substring</code> substring identified by regex
   */
	String find(String regex) throws LogException;

  /**
   * Filters hitched log to last N lines
   *
   * @param             number the number of last strings to get
   * @return            filtered log
   */
	String getLastLines(int number) throws LogException;

    /**
     * cut specific amount of lines from log file
     *
     * @param log
     * @param number
     * @return
     * @throws LogException
     */
    String getLogLastLines(Object log, int number) throws LogException;

    /**
     * @param log
     * @param sec
     * @return
     * @throws LogException
     */
    String getLogFreshLines(Object log, int sec) throws LogException;

  /**
   * Filters hitched log between start date and end date
   *
   * @param             seconds after this time log lines are treated rotten
   * @return            filtered log
   */
	String getFreshLines(int seconds) throws LogException;

  /**
   * Filters hitched log between start date and end date
   *
   * @return            hitched log
   */
  String getAllLines() throws LogException;


    /**
     * Return log objects
     *
     * @return all hitched logs
     * @throws LogException
     */
  List<T> getAllLogs() throws LogException;



    public abstract class AbstractFactory<T, D> {

        public T create(final D dto) {
            T obj = instance();
            setProperty(
                    obj,
                    dto
            );

            save(obj);

            return obj;
        }

        public Collection<T> create(final Collection<D> dtos) {
            return new ArrayList<T>() {{
                for (D dto : dtos) {
                    add(
                            create(dto)
                    );
                }
            }};
        }

        public abstract T instance();

        protected abstract void setProperty(T obj, D dto);

        protected abstract void save(T obj);

    }


}

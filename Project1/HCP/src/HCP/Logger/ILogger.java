package HCP.Logger;
/**
 * Interface used by the TLogger to access the Logger monitor
 */
public interface ILogger {
    /**
     * TLogger waits for a new log entry in the logs MessageList
     * When there is one, returns it and removes it from the MessageList
     * @return String log entry
     */
    String waitForLog();
}

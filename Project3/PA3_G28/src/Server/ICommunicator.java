package Server;

public interface ICommunicator {
    /**
     * The TMonitor Communicator waits for a new update to send to the Monitor
     * @return the string update
     */
    String waitForUpdate();
}

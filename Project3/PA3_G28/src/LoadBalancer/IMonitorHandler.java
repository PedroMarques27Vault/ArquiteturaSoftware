package LoadBalancer;

public interface IMonitorHandler {
    /**
     * Wait for updates and return when there is one
     * @return  the update
     */
    String waitForUpdate();
    /**
     * Add new server information
     * @param servers string with server info
     */
    void putServer(String servers);
}

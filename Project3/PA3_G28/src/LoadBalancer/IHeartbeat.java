package LoadBalancer;

public interface IHeartbeat {
    /**
     * Allows the LB to start
     */
    void setStart();
    /**
     * Add new request to the list
     * @param req request as tring
     */
    void addRequest(String req);
}

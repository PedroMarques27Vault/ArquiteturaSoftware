package LoadBalancer;

public interface IRequests {
    /**
     * Wait for the load balancer to be allowed to start
     */
    void waitForStart();
    /**
     * Request new server information
     * @return new server infor
     */
    String requestServers();
    /**
     * Add new request to list to be distributed
     * @param message request as string
     */
    void putRequest(String message);
    /**
     * Returns a request from the list of requests to be distributed
     * @return the request
     */
    String pullRequest();
    /**
     * Adds a update to notify the monitor that a request has been sent to a server
     * @param req request as string
     * @param port port of the server which will receive the request
     */
    void addSentUpdate(String req, String port);
    /**
     * Add to the updates list a request that was received
     * @param req: request received
     */
    void addRecvUpdate(String req);
    /**
     * Add request which came from previous load balancer to the queue
     * @param  req the request as string
     */
    void addDenied(String req);
    /**
     * Sets the request id using idCounter
     * @param message request as string
     * @return request with updated id as string
     */
    String setRequestId(String message);
}

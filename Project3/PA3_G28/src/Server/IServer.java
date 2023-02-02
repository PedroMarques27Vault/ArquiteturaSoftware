package Server;

public interface IServer {
    /**
     * If the new request received can be processed by the server it is added to the InboundRequests queue.
     * The request is rejected if the number of total iterations left for processing summed with its iteration value is above the maximum number of iterations or if there is no space in the queue
     * The results list is resorted prioritizing the requests with earliest deadline
     * @param inputLine the request received by the server
     */
    void putNewRequest(String inputLine);
}

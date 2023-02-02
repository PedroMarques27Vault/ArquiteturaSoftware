package Server;

public interface IWorker {
    /**
     * Returns a new request which was added to the server for processing
     * The results list is resorted prioritizing the requests with earliest deadline
     * @param workerId the worker who will process the request
     * @return the request to be processed
     */
    Request getRequest(int workerId);
    /**
     * Adds the results/processed request to the list of results.
     * The results list is resorted prioritizing the requests with earliest deadline
     * @param req the request result
     * @param workerId the worker who processed the request
     */
    void putResult(Request req,int workerId);
    /**
     * One iteration is removed from the total iteration counter and the monitor is notified
     * @param req the request whose iteration was reduced from
     * @param workerId the worker processing the request
     */
    void removeOneIteration(Request req, int workerId);
}

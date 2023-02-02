package Server;

public interface ISender {
    /**
     * The TResultsSender waits for a request to be processed and retrieves the results
     * @return the results of the processed request
     */
    Request getResult();
}

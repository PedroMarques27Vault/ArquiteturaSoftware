package Client;

public interface IClient {
    /**
     * Function to add the response to the GUI
     * @param response: text to input
     */
    void addResponse(String response);
    /**
     * Function to add the request to the GUI
     * @param request: text to input
     */
    void addRequest(String request);
}

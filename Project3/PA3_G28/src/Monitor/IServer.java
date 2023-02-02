package Monitor;

import java.util.ArrayList;

public interface IServer {
    /**
     * Updates the Server's Status based on the Server's response to the heartbeat request
     * @param  port port of the server
     * @param  status status of the Server (Alive or Dead)
     */
    void setServerStatus(String port, String status);
    /**
     * Add a Server representation to the Gui. Includes the status and the details
     * @param port the port of the Server
     */
    void addServerConnection(String port);

    /**
     * Updates GUI and requestDetails hashmap with new information received from load balancers and servers
     * @param port port of the service which sent the update
     * @param type type of update. If it is simply a status update with only the detail's numbers or a RECV/SENT update which also contains the specific requests received/sent
     * @param request the request received. If it is a status update then it is empty
     * @param state the detail's numbers to update the requestsDetails hashmap
     */
    void addUpdate(String port, String type, String request, String state);
    /**
     * The thread sleeps for a specific duration (heartbeatInterval)
     */
    void sleep();
    /**
     * Add a LoadBalancer representation to the Gui. Includes the status and the details
     * @param port the port of the LB
     */
    int addLoadBalancer(String port);
    /**
     * Updates the Load Balancer's Status based on the LB's response to the heartbeat request
     * @param  id id of the LB
     * @param  status status of the LB (Alive or Dead)
     */
    void setLbStatus(String id, String status);
    /**
     * After the dead load balancer is removed from the "ALIVE" state, this function activates the next idle load balancer
     * @param deadId id of the dead load balancer
     */
    void setNextLoadBalancer(int deadId);
    /**
     * This function is only invoked by LBs.
     * The load balancer that invokes it, if it is not the one that is active (it is paused) then it waits until it
     * is its turn to start.
     * @param id: Id of the load balancer
     */
    int awaitUntilAlive(int id);
    /**
     * Returns a string which corresponds to a concatenation of current active server ports and the number of iterations
     * they have left to process.
     * @return String details of each server
     */
    String getServers();
    /**
     * It returns the unresolved requests of the load balancer that died in a list
     * @param deadId id of the dead load balancer
     * @return List of unprocessed requests
     */
    ArrayList<String> getMissingRequests(int deadId);
}

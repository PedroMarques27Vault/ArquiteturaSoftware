package Monitor;

import java.io.PrintWriter;
import java.net.Socket;

public class TLoadBalancerManagement extends Thread {
    /**
     *  Socket Connection
     */
    private Socket socket;
    /**
     *  Interface to interact with the monitor's shared region
     */
    private IServer mmonitor;
    /**
     *  Id of the service, if it is a LB it is 0+, else is -1
     */
    private final int id;

    /**
     *  The LoadBalancerManagement thread is responsible for starting the load balancer connected and send it the
     *  last LB's lost data
     * @param  _mmonitor interface to interact with the monitor's shared region
     * @param  socket Socket connection
     * @param  _id id of the the service, LB=0+, Server=-1
     */
    TLoadBalancerManagement(IServer _mmonitor, Socket socket,int _id)
    {
        this.socket = socket;
        this.mmonitor = _mmonitor;
        this.id = _id;
    }

    /**
     *  The LoadBalancerManagement start by waiting until the current service (load Balancer) is told to wake up and work
     *  After that, it sends the unprocessed requests of the last LB and tells it to start working
     */
    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter( socket.getOutputStream() );
            int deadId = mmonitor.awaitUntilAlive(id);

            for (String s: mmonitor.getMissingRequests(deadId)){
                out.println("REQ#"+s);
                out.flush();
            }
            out.println("START");
            out.flush();
        }
        catch( Exception e ) {
            System.out.println("An error Occurred at TLoadBalancerManagement");
        }
    }
}

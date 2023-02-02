package Monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class THeartbeat extends Thread {
    /**
     * Socket connection
     */
    private Socket socket;
    /**
     * Interface with the shared region monitor
     */
    private IServer mmonitor;
    /**
     * Socket Server Port where the service is listening
     */
    private final int connectionPort;

    /**
     *  id of the current service. If it is a LoadBalancer then it is 0+, else is -1
     */
    private final int id;
    /**
     *  Type of the current service (Load Balancer or Server)
     */
    private final String type;

    /**
     * This uses the Socket Connection created by TServer to ask the services for a heartbeat
     * @param _mmonitor interface to interact with the monitor
     * @param socket Connection socket
     * @param cPort Socket Server Port where the service is listening
     * @param _type type of service (Server or LB)
     * @param _id id of the current service
     */
    THeartbeat(IServer _mmonitor, Socket socket, int cPort, String _type, int _id)
    {
        this.socket = socket;
        this.mmonitor = _mmonitor;
        this.connectionPort = cPort;
        this.type = _type;
        this.id = _id;
    }

    /**
     *  The thread sleeps for heartbeatInterval ms. Then it proceeds to ask for a heartbeat. In both cases, it updates the
     *  monitor's GUI with the result. If the load balancer is dead, then it appoints the next load balancer as the active one
     */
    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintWriter out = new PrintWriter( socket.getOutputStream() );

            while(true){
                mmonitor.sleep();

                out.println("!");
                out.flush();
                String input= null;
                try{
                    input  = in.readLine();
                }catch(Exception e){
                    System.out.println("Connection Broken "+this.connectionPort);
                }
                if (this.type.equals("SERVER")){
                    if ( input == null) {
                        ((IServer)mmonitor).setServerStatus(String.valueOf(this.connectionPort), "DEAD");
                        break;
                    }else if (input.split("\\$")[0].equals("BEAT")){
                        ((IServer)mmonitor).setServerStatus(String.valueOf(this.connectionPort), "ALIVE");
                    }
                }else{
                    if ( input == null) {
                        ((IServer)mmonitor).setLbStatus(String.valueOf(id), "DEAD");
                        ((IServer)mmonitor).setNextLoadBalancer(id);
                        break;
                    }else if (input.split("\\$")[0].equals("BEAT")){
                        ((IServer)mmonitor).setLbStatus(String.valueOf(id), "ALIVE");
                    }
                }
            }
            in.close();
            out.close();
            socket.close();
        }
        catch( Exception e ) {
            System.out.println("An error Ocurred");
        }
    }
}

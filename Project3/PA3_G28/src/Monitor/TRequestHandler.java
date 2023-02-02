package Monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TRequestHandler extends Thread {
    /**
     *  Socket Connection
     */
    private Socket socket;
    /**
     *  Interface to interact with the monitor
     */
    private IServer mmonitor;
    /**
     *  Port where the service which sent the request is listening on
     */
    private final int connectionPort;
    /**
     *  The RequestHandler deals with specific instructions provided from the service to the monitor
     * @param _mmonitor interface to interact with the monitor
     * @param socket Socket Connection
     * @param cPort Port where the service which sent the request is listening on
     */
    TRequestHandler(IServer _mmonitor, Socket socket, int cPort) {
        this.socket = socket;
        this.mmonitor = _mmonitor;
        this.connectionPort = cPort;
    }

    /**
     * The RequestHandler receives updates from the services and updates the Monitor's GUI or
     * responds to server details requests from the load balancers
     */
    @Override
    public void run() {
        try {
            // Get input and output streams
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintWriter out = new PrintWriter( socket.getOutputStream() );

            while(true){
                String line = in.readLine();
                if (line == null) break;
                String[] array = line.split("#");

                if (array[0].equals("UPDATE")){
                    line = array[1];
                    (mmonitor).addUpdate(String.valueOf(connectionPort), line.split("\\$")[0], line.split("\\$")[1],line.split("\\$")[2]);
                }
                else {
                    String servers = (mmonitor).getServers();
                    out.println(servers);
                    out.flush();

                }// Close our connection
            }
            in.close();
            socket.close();

        }
        catch( Exception e ) {
            System.out.println("Handler Connection Broken "+this.connectionPort);
        }
    }
}

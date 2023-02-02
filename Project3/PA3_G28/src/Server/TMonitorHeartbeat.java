package Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class TMonitorHeartbeat extends Thread{
    /**
     * Port which allows for communication between service and monitor
     */
    private final int heartbeatPort;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * Service Socket port
     */
    private int ReceivePort;

    /**
     * Initialize TMonitorHeartbeat
     * @param _port service port
     * @param _heartbeatPort Port which allows for communication between service and monitor
     */
    public TMonitorHeartbeat(int _port, int _heartbeatPort) {
        this.stopFlag = false;
        this.ReceivePort = _port;
        this.heartbeatPort = _heartbeatPort;
    }

    /**
     * <p>stopFlag flag set to true, process ends</p>
     */
    public void stopProcess() {
        this.stopFlag = true;
    }

    /**
     * Start running socket, send and receive information
     */
    @Override
    public void run() {
        // Connect to the server
        try
        {
            Socket socket = new Socket( "127.0.0.1", heartbeatPort );
            // Create input and output streams to read from and write to the server
            PrintStream out = new PrintStream( socket.getOutputStream() );
            out.println("B:-"+this.ReceivePort+":-SERVER");

            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

            while (!this.stopFlag) {
                String inputLine;
                if ((inputLine = in.readLine()) == null) break;
                if (inputLine.equals("!")) {
                    out.println("BEAT$SERVER$"+this.ReceivePort+"$ALIVE");
                }
            }   // Close our streams
            in.close();
            out.close();
            socket.close();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }
}

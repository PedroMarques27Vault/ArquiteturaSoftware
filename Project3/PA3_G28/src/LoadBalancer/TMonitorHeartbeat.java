package LoadBalancer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class TMonitorHeartbeat  extends Thread{
    /**
     * Monitor Port
     */
    private final int heartbeatPort;

    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * Port where the LB receives information
     */
    private int ReceivePort;
    /**
     * Interface to interact with the mserver monitor
     */
    private final IHeartbeat mheartbeat;
    /**
     * The TMonitorHearbeat handles the heartbeat requests and other reuqests from the Monitor
     * @param _mloadb interface to interact with the mserver monitor
     * @param _port Port where the LB receives information
     * @param _heartbeatPort Monitor port
     */
    public TMonitorHeartbeat(IHeartbeat _mloadb,int _port, int _heartbeatPort) {
        this.stopFlag = false;
        this.ReceivePort = _port;
        this.heartbeatPort = _heartbeatPort;
        this.mheartbeat = _mloadb;
    }

    /**
     * <p>stopFlag flag set to true, process ends</p>
     */
    public void stopProcess() {
        this.stopFlag = true;
    }
    /**
     * The TMonitorHearbeat responds to the heartbeats sent from the monitor as well as requests to start and
     * recieves unprocessed requests from previous LBs
     */
    @Override
    public void run() {
        // Connect to the server
        try {
            Socket socket = new Socket( "127.0.0.1", heartbeatPort );
            // Create input and output streams to read from and write to the server
            PrintStream out = new PrintStream( socket.getOutputStream() );
            out.println("B:-"+this.ReceivePort+":-LB");

            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

            while (!this.stopFlag) {
                String inputLine;
                if ((inputLine = in.readLine()) == null) break;

                if (inputLine.equals("!")) {
                    out.println("BEAT$LB$"+this.ReceivePort+"$ALIVE");
                }else if(inputLine.equals("START")){
                    mheartbeat.setStart();
                }else if(inputLine.split("#")[0].equals("REQ")){
                    mheartbeat.addRequest(inputLine.split("#")[1]);
                }
            }
            in.close();
            out.close();
            socket.close();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }
}

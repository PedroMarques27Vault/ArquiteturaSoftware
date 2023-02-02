package LoadBalancer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class TMonitorHandler  extends Thread{
    /**
     * MServer Interface
     */
    private final IMonitorHandler mhandler;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * Port of the Monitor
     */
    private final int monitorPort;
    /**
     * Port where the Load Balancer receives information
     */
    private final int ReceivePort;
    /**
     * The TMonitorHandler is responsible for sending updates and requests to the monitor
     * @param _mcom interface to interact with the mserver
     * @param _port Port where the Load Balancer receives information
     * @param _monitorPort Port of the Monitor
     */
    public TMonitorHandler(IMonitorHandler _mcom, int _port,int _monitorPort) {
        this.stopFlag = false;
        this.mhandler = _mcom;
        this.monitorPort = _monitorPort;
        this.ReceivePort = _port;
    }

    /**
     * <p>stopFlag flag set to true, process ends</p>
     */
    public void stopProcess() {
        this.stopFlag = true;
    }
    /**
     * The handler waits for updates to send to the monitor or requests for server information
     */
    @Override
    public void run() {
        try {
            Socket socket = new Socket( "127.0.0.1", monitorPort );
            // Create input and output streams to read from and write to the server
            PrintStream out = new PrintStream( socket.getOutputStream() );

            out.println("H:-"+this.ReceivePort+":-LB");
            out.flush();
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

            while (!this.stopFlag) {
                String up = mhandler.waitForUpdate();
                out.println(up);
                out.flush();
                if (up.split("#")[0].equals("SERVERS")){
                    String servers = in.readLine();
                    mhandler.putServer(servers);
                }
            }
            in.close();
            out.close();
            socket.close();
        }
        catch( Exception e )
        {
            System.out.println("Trouble Connecting LoadBalancer to Monitor");
            System.exit(-1);
        }
    }
}

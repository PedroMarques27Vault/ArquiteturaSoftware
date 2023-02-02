package Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class TMonitorCommunicator extends Thread{
    /**
     * MServer Monitor Request Receiver Interface
     */
    private final ICommunicator mcom;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * Client socket port
     */
    private final int requestHandlerPort;
    /**
     * Service socket port
     */
    private final int ReceivePort;

    /**
     * Initialization of TMonitorCommunicator
     * @param _mcom Interface
     * @param _port Service Socket port
     * @param _requestHandlerPort Client socket port
     */
    public TMonitorCommunicator(ICommunicator _mcom, int _port, int _requestHandlerPort) {
        this.stopFlag = false;
        this.mcom = _mcom;
        this.requestHandlerPort = _requestHandlerPort;
        this.ReceivePort = _port;
    }

    /**
     * <p>stopFlag flag set to true, process ends</p>
     */
    public void stopProcess() {
        this.stopFlag = true;
    }

    /**
     * Start TMonitorCommunicator
     */
    @Override
    public void run() {
        try {
            Socket socket = new Socket( "127.0.0.1", requestHandlerPort );
            // Create input and output streams to read from and write to the server
            PrintStream out = new PrintStream( socket.getOutputStream() );

            out.println("H:-"+this.ReceivePort+":-SERVER");
            out.flush();
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

            while (!this.stopFlag) {
                String up = ((ICommunicator)mcom).waitForUpdate();
                out.println("UPDATE#"+up);
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

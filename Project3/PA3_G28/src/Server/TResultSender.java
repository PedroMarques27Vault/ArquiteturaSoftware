package Server;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;


public class TResultSender extends Thread {
    /**
     * MServer Monitor Request Receiver Interface
     */
    private final ISender msender;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * Port id
     */
    private int PortId;

    /**
     * Initialize TResultSender
     * @param _msender MServer Monitor Request Receiver Interface
     * @param _portid Port id
     */
    public TResultSender(ISender _msender, int _portid) {
        this.stopFlag = false;
        this.msender = _msender;
        this.PortId = _portid;
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
        while (!this.stopFlag) {
           Request request = ((ISender)msender).getResult();

            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(request.getClient().split(":")[0],Integer.parseInt(request.getClient().split(":")[1])), 2000);

                // Create input and output streams to read from and write to the server
                PrintStream out = new PrintStream( socket.getOutputStream() );
                out.println(request.stringify());
                out.flush();
                out.close();
                socket.close();
            }
            catch( Exception e ) {
                System.out.println("Error occurred when sending data to client");
            }
        }
    }
}

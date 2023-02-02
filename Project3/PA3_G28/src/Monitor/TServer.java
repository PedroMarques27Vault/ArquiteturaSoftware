package Monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class TServer extends Thread {
    /**
     * Interface to interact with the monitor's shared region
     */
    private final IServer mmonitor;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * Socket Server
     */
    private ServerSocket serverSocket;

    /**
     * The TServer generates a socket server which the monitor uses to receive and send information
     */
    public TServer(IServer _mmonitor, int _port) {
        this.stopFlag = false;
        this.mmonitor = _mmonitor;

        try {
            serverSocket = new ServerSocket(_port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>stopFlag flag set to true, process ends</p>
     */
    public void stopProcess() {
        this.stopFlag = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error Closing ServerSocket At Monitor");
        }
    }

    /**
     * The TServer creates the socket server, adds the new services to the Monitor's GUI and then creates TLoadBalancerManagement,
     * TRequestHandler and THeartbeat threads which will communicate with the services
     */
    @Override
    public void run() {
        while( !stopFlag ) {
            try {
                Socket socket = serverSocket.accept();

                BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                String input = in.readLine();
                int id=-1;
                if (input.split(":-")[2].equals("SERVER") && input.split(":-")[0].equals("B"))
                    mmonitor.addServerConnection(input.split(":-")[1]);
                else if (input.split(":-")[0].equals("B")){
                    id = mmonitor.addLoadBalancer(input.split(":-")[1]);
                    TLoadBalancerManagement management = new TLoadBalancerManagement(this.mmonitor, socket, id);
                    management.start();
                }
                if (input.split(":-")[0].equals("B")) {
                    THeartbeat heartbeat = new THeartbeat(this.mmonitor, socket, Integer.parseInt(input.split(":-")[1]),input.split(":-")[2], id);
                    heartbeat.start();
                }else{
                    TRequestHandler requestHandler = new TRequestHandler(this.mmonitor, socket, Integer.parseInt(input.split(":-")[1]));
                    requestHandler.start();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

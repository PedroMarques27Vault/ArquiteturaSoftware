package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TServer extends Thread {
    /**
     * MServer Monitor Request Receiver Interface
     */
    private final IServer mserver;
    /**
     * Server Port
     */
    private int port;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * Server socket
     */
    private ServerSocket serverSocket;

    /**
     * Initialize TServer
     * @param _mserver MServer Monitor Request Receiver Interface
     * @param _port Server Port
     */
    public TServer(IServer _mserver, int _port) {
        this.stopFlag = false;
        this.mserver = _mserver;
        this.port = _port;
        try {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * <p>stopFlag flag set to true, process ends</p>
     */
    public void stopProcess() {
        this.stopFlag = true;
        try {
            serverSocket.close();
        }
        catch (IOException e) {
            System.out.println("Error Closing ServerSocket At Server With Port "+String.valueOf(this.port));
        }
    }

    /**
     * Start running socket, send and receive information
     */
    @Override
    public void run() {
        try {
            while (!this.stopFlag) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = null;
                try {
                    in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                }
                catch (IOException e) {
                    System.out.println("Error Receiving Input A Server With Port "+String.valueOf(this.port));
                }

                String inputLine;
                while (!stopFlag && in!=null) {
                    try {
                        if ((inputLine = in.readLine()) == null) break;
                        ((IServer)mserver).putNewRequest(inputLine);
                    }
                    catch(SocketException e) {
                        System.out.println("Socket was closed unexpectedly At Server With Port "+String.valueOf(this.port));
                        break;
                    }
                    catch (IOException e)
                    {
                        System.out.println("Error Reading Input A Server With Port "+String.valueOf(this.port));
                        break;
                    }
                }
                try {
                    assert in != null;
                    in.close();
                }
                catch (IOException e) {
                    System.out.println("Failed to Close Input Stream At Server With Port "+String.valueOf(this.port));
                }
                try {
                    clientSocket.close();
                }
                catch (IOException e) {
                    System.out.println("Failed to Close Client Socket At Server With Port "+String.valueOf(this.port));
                }
            }
            this.stopProcess();
        }
        catch (Exception e)
        {
            System.out.println("Error Creating RequestHandler At Server With Port "+String.valueOf(this.port));
        }
    }
}

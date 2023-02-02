package LoadBalancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TReceiver extends Thread{
    /**
     * Interface to interact with the MServer
     */
    private final IRequests mrequests;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * Server socket to receive requests
     */
    private ServerSocket serverSocket;
    /**
     * Port of the Server Socket
     */
    private final int port;
    /**
     * The Treceiver receives requests from the clients
     * @param _mcom Interface to interact with the MServer
     * @param _port Port of the Server Socket
     */
    public TReceiver(IRequests _mcom, int _port) {
        this.stopFlag = false;
        this.mrequests = _mcom;
        this.port = _port;
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
            System.out.println("Error Closing ServerSocket At Client With Port "+String.valueOf(this.port));
        }
    }
    /**
     * The thread constantly keeps waiting for new requests to add to the shared region to be distributed
     */
    @Override
    public void run() {
        try {
            mrequests.waitForStart();
            try {
                serverSocket = new ServerSocket(port);
            }
            catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
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
                String message;

                while (!stopFlag && in!=null) {
                    try {
                        if ((message = in.readLine()) == null) break;
                        message = mrequests.setRequestId(message);
                        mrequests.addRecvUpdate(message);
                        mrequests.putRequest(message);

                    } catch(SocketException e) {
                        System.out.println("Socket was closed unexpectedly At Server With Port "+String.valueOf(this.port));
                        break;
                    }
                    catch (IOException e) {
                        System.out.println("Error Reading Input A Server With Port "+String.valueOf(this.port));
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
        catch (Exception e) {
            System.out.println("Error Creating RequestHandler At Server With Port "+String.valueOf(this.port));
        }
    }
}

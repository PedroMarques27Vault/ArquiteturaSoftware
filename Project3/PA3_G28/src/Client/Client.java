package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    /**
     * Load Balancer's socket port number
     */
    public static int LB_PORT;
    /**
     * Client's socket port number
     */
    public static int PORT;
    /**
     * Variable that saves the number of requests made so far
     */
    private static int current_request;
    /**
     * Socket
     */
    private static Socket socket = null;
    /**
     * Client Monitor
     */
    private static IClient monitor;
    /**
     * Client Thread
     */
    private static TReceiver worker;

    public static void main(String[] args){
        monitor = new MClient();
        current_request = 1;
    }

    /**
     * Establishes the port numbers of the Client and the Load Balancer it wants to connect to.
     * Initializes the worker thread.
     * @param port: number of the client's port
     * @param lb_port: number of the load balancer port
     */
    public static void set_port_numbers(int port, int lb_port) throws IOException {
        LB_PORT = lb_port;
        PORT = port;

        worker = new TReceiver(PORT, monitor);
        worker.startServer();
    }

    /**
     * Send requests to the Load Balancer.
     * Receives the number of iteration and the deadline of the request as arguments.
     * @param n_iterations: number iterations of the request
     * @param deadline: number of the deadline of the request
     */
    public static void send_request(int n_iterations, int deadline) throws IOException {
        try
        {
            // Connect to the LB server
            socket = new Socket(InetAddress.getLocalHost().toString().split("/")[1], LB_PORT);

            // Create output streams to read from and write to the server
            PrintStream out = new PrintStream( socket.getOutputStream() );

            // |ClientAddress:Port|RequestId|ServerId|ReplyCode|NumberOfIterations|Result|Deadline
            String req = "|" + InetAddress.getLocalHost().toString().split("/")[1] + ":" + PORT + "|0|0|0|" + n_iterations +  "|0|" + deadline;
            out.println(req );
            monitor.addRequest(req);

            out.flush();
            out.close();
            socket.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}

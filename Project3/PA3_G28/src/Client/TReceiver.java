package Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TReceiver extends Thread   {
    /**
     * Server Socket of Client
     */
    private ServerSocket serverSocket;
    /**
     * Client Socket
     */
    private Socket socket;
    /**
     * Client's socket port number
     */
    private int port;
    /**
     * State of Thread
     */
    private boolean running = false;
    /**
     * Client's Monitor
     */
    IClient monitor;

    /**
     * Initialization of the thread
     * @param port: socket port of Client
     * @param monitor: Client's Monitor
     */
    public TReceiver( int port, IClient monitor ) {
        this.monitor = monitor;
        this.port = port;
    }

    /**
     * Start the Thread
     */
    public void startServer() {
        try
        {
            serverSocket = new ServerSocket( port );
            this.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Stop the thread
     */
    public void stopServer() {
        running = false;
        this.interrupt();
    }

    /**
     * Run Thread
     */
    @Override
    public void run()
    {
        running = true;
        while( running )
        {
            try
            {
                System.out.println( "Listening for a connection" );

                // Call accept() to receive the next connection
                socket = serverSocket.accept();

                // Pass the socket to the RequestHandler thread for processing
                TRequestHandler TRequestHandler = new TRequestHandler(socket, monitor);
                TRequestHandler.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}

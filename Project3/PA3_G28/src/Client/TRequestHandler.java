package Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class TRequestHandler extends Thread{
    /**
     * Socket
     */
    private Socket socket;
    /**
     * Client's Monitor
     */
    IClient monitor;

    /**
     * Initialization of the Handler
     * @param socket: socket of Client
     * @param monitor: Client's Monitor
     */
    TRequestHandler(Socket socket, IClient monitor)
    {
        this.socket = socket;
        this.monitor = monitor;
    }

    /**
     * Receives the response from the server
     */
    @Override
    public void run()
    {
        try
        {
            // Get input streams
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            while(true){
                String line = in.readLine();
                if (line == null) break;
                this.monitor.addResponse(line);
            }
            // Close our connection
            in.close();
            socket.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}

package UC5.PPRODUCER;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TServer extends Thread{
    /**
     * Interface to interact with the MServerData monitor
     */
    private final IServer iserver;
    /**
     * Id of the current server
     */
    private final int id;
    /**
     * Port of the Socket Server
     */
    private final int SOCKET_PORT;

    /**
     * The TServer generates a JavaSocket Server which will receive data from the PSource process
     * <p>Next, it adds it to the Message queues in the MServerData monitor</p>
     *
     * @param rd interface to interact with the MServerData monitor
     * @param port port where the server is created
     * @param _id Id of the current server
     */
    public TServer(IServer rd, int port, int _id) {
        this.iserver = rd;
        this.id = _id;
        this.SOCKET_PORT = port;
    }


    /**
     * TServer life cycle
     * <p>The server thread continuously waits for new received data and adds it to the message queues in the monitor</p>
     */
    @Override
    public void run() {
        Logger logger= LoggerFactory.getLogger(TServer.class.getName());
        // server is listening on port 5056
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(this.SOCKET_PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // running infinite loop for getting
        // client request
        while (true)
        {
            Socket s = null;

            try
            {
                // socket object to receive incoming client requests
                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());



                // create a new thread object
                while (true)
                {
                    try {

                        String received = "";
                        // receive the answer from client
                        try{
                            received = dis.readUTF();
                        }catch(EOFException e){
                            logger.error(e.toString());
                        }
                        if(received.length()!=0){
                            ((IServer) iserver).putData(this.id, received);
                            if(received.equals("Exit"))
                            {
                                System.out.println("Client " + s + " sends exit...");
                                System.out.println("Closing this connection.");
                                s.close();
                                System.out.println("Connection closed");
                                break;
                            }
                        }




                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try
                {
                    // closing resources
                    dis.close();
                    dos.close();

                }catch(IOException e){
                    e.printStackTrace();
                }

                break;

            }
            catch (Exception e){
                try {
                    s.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                e.printStackTrace();
            }
        }
    }

}

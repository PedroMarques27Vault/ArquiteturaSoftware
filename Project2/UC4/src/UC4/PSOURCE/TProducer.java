package UC4.PSOURCE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.Instant;

public class TProducer extends Thread {

    /** 
     * Interface to interact with the monitor 
    */
    private final IProducer iproducer;
    
    /** 
     * Id of the current TProducer 
    */
    private final int id;

    /** 
     * Java socket server connection port 
    */
    private final int SOCKET_PORT;

    /**
     * <p>The TProducer sends the records to the TKafkaProducer</p>
     * @param _p interface to interact with the MReader monitor
     * @param port java socket server port
     * @param _id id of the current TProducer
     */
    public TProducer(IProducer _p, int port, int _id) {
        this.iproducer = _p;
        this.id = _id;
        this.SOCKET_PORT = port;
    }




    /**
     * TProducer lifecycle
     * <p>Continuously retrieves data from the MReader monitor when it exists and sends it over a server socket connection.</p>
     */
    @Override
    public void run()
    {

        Logger logger= LoggerFactory.getLogger(TProducer.class.getName());
        try
        {

            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server port 5056
            Socket s = new Socket(ip, this.SOCKET_PORT);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of
            // information between client and client handler
            while(true){
                String newdata = ((IProducer)iproducer).getData();
                if (newdata.length()==0)
                    break;
                try {
                    dos.writeUTF(newdata);
                } catch (Exception e) {
                    logger.error("UC1.PSOURCE-TPRODUCER: Exception "+e+" at "+ Timestamp.from(Instant.now()));
                }
            }

            // closing resources
            dis.close();
            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }







    }
}


package HCP.Main;

import HCP.Communication.ClientHandler;
import HCP.Entities.*;
import HCP.Enums.AGE;
import HCP.Enums.OPERATION;
import HCP.Logger.ILogger;
import HCP.Logger.MLogger;
import HCP.Monitors.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;
/**
 * Socket Server. Accepts Multiple Connections
 */
public class HCP
{
    /**
     * Socket server accepts multiple connections and saves them in an arraylist. Socket at port 5056
     */
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(5056);
        String mode = "AUTO";
        ArrayList <ClientHandler> clients = new ArrayList<ClientHandler>();
        System.out.println("Started Server Socket at "+ss);
        while (true) {
            Socket s = null;
            try {
                // socket object to receive incoming client requests
                s = ss.accept();

                System.out.println("A new client at: " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                // create a new thread object
                ClientHandler newclient = new ClientHandler(s, dis, dos);
                newclient.start();
                clients.add(newclient);

                dos.writeUTF("Client Number: "+ clients.size());
            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}



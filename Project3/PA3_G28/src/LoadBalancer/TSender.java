package LoadBalancer;


import Server.Request;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class TSender extends Thread{
    /**
     * Interface to interact with mserver
     */
    private final IRequests mrequests;

    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * The sender distributes requests along the servers
     */
    public TSender(IRequests _mcom) {
        this.stopFlag = false;
        this.mrequests = _mcom;
    }

    /**
     * <p>stopFlag flag set to true, process ends</p>
     */
    public void stopProcess() {
        this.stopFlag = true;
    }
    /**
     * Given the server ports and number of iterations per server, the unction returns the port of the server with the least number of iterations
     * @param servers string with server information
     * @return best server port
     */
    public String getBestServer(String servers){
        String minPort=null;
        int Ni = 200;

        for (String s : servers.split("#")){
            if (s.length()>0 && Integer.parseInt(s.split(":")[1])<Ni){
                minPort = s.split(":")[0];
                Ni = Integer.parseInt(s.split(":")[1]);
            }
        };
        return minPort;
    }
    /**
     * The thread waits for a new request, determines the best server and sends it the request
     */
    @Override
    public void run() {
        try {
            mrequests.waitForStart();
            while(!stopFlag){
                String req = mrequests.pullRequest();
                String servers = mrequests.requestServers();
                String port = getBestServer(servers);

                if (port!=null){
                        Request reqs = Request.fromString(req);

                        if (reqs!=null) {
                            reqs.setServerId(Integer.parseInt(port));
                            Socket socket = new Socket( "127.0.0.1", Integer.parseInt(port));
                            PrintStream out = new PrintStream( socket.getOutputStream() );

                            out.println(reqs.stringify());
                            out.flush();
                            mrequests.addSentUpdate(reqs.stringify(), port);

                            out.close();
                            socket.close();
                        }
                }else{
                    Thread.sleep(3000);
                    mrequests.addDenied(req);
                }
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }
}

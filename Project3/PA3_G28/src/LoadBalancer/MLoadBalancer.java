package LoadBalancer;

import Server.Request;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MLoadBalancer implements IRequests, IMonitorHandler, IHeartbeat{
    /**
     * ArrayList of updates for the Monitor
     */
    private ArrayList<String> updates;
    /**
     * ArrayList of requests made
     */
    private ArrayList<Request> requests;
    /**
     * ArrayList of server info
     */
    private ArrayList<String> servers;
    /**
     * Flag which signals when the LB is paused or running
     */
    private boolean pauseFlag;
    /**
     * reentrant mutual exclusion lock
     */
    private ReentrantLock rl;
    /**
     * Condition which signals when there is a new update to be reported to the monitor
     */
    private Condition newUpdate;
    /**
     * Condition which makes the LB wait for the monitor's permission to start
     */
    private Condition waitForStart;
    /**
     * Condition which signals a new request received
     */
    private Condition newRequest;
    /**
     * Condition which signals new server data has been received
     */
    private Condition serverData;
    /**
     * Incremental id counter
     */
    private int idCounter;
    /**
     * Port of the LB
     */
    private final int port;
    /**
     * Port of the Monitor
     */
    private final int monitorPort;
    /**
     * Total number of requests received
     */
    private int totalReceived;
    /**
     * Total number of requests distributed
     */
    private int totalDistributed;
    /**
     * Total number of requests being distributed
     */
    private int totalDistributing;

    /**
     * Shared Region of the Load Balancer
     * @param port port of the LB
     * @param monitorPort port of the Monitor
     */
    public MLoadBalancer(int port, int monitorPort){
        this.rl = new ReentrantLock(true);
        this.pauseFlag = false;
        this.requests = new ArrayList<>();
        this.updates = new ArrayList<>();
        this.servers = new ArrayList<>();

        this.port = port;
        this.monitorPort = monitorPort;
        this.newUpdate = rl.newCondition();
        this.waitForStart = rl.newCondition();
        this.newRequest = rl.newCondition();
        this.serverData = rl.newCondition();
        this.idCounter = 0;
        this.totalReceived = 0;
        this.totalDistributed = 0;
        this.totalDistributing = 0;
        this.generateMonitorGui();
    }

    /**
     * Wait for updates and return when there is one
     * @return  the update
     */
    @Override
    public String waitForUpdate() {
        String ret = null;
        try{
            rl.lock();
            while(updates.isEmpty()){
                try {
                    newUpdate.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ret = updates.get(0);
            updates.remove(0);

        }finally {
            rl.unlock();
        }
        return ret;
    }

    /**
     * Add new server information
     * @param servers string with server info
     */
    @Override
    public void putServer(String servers) {
        try{
            rl.lock();

            if (this.servers.size()!=0)
                this.servers.remove(0);
            this.servers.add(servers);
            serverData.signalAll();

        }finally {
            rl.unlock();
        }
    }
    /**
     * Request new server information
     * @return new server infor
     */
    @Override
    public String requestServers() {
        String ret = null;
        try{
            rl.lock();
            this.updates.add("SERVERS#");
            newUpdate.signalAll();

            allRequests.add(0, "Servers Status Requested ");
            frame.revalidate();

            while(servers.isEmpty()){
                try {
                    serverData.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ret = servers.get(0);
            servers.remove(0);

            StringBuilder str = new StringBuilder();
            for (String s: ret.split("#")){
                if (s.length()>1){
                    str.append("\t-> Server ").append(s.split(":")[0]).append(" ").append(s.split(":")[1]).append(" iterations");
                }
            }
            allRequests.add(0, str.toString());
            frame.revalidate();

        }finally {
            rl.unlock();
        }
        return ret;
    }
    /**
     * Add new request to list to be distributed
     * @param message request as string
     */
    @Override
    public void putRequest(String message) {
        try{
            rl.lock();
            this.requests.add(Request.fromString(message));

            this.requests.sort(new Comparator<Request>() {
                public int compare(Request o1, Request o2) {
                    return Integer.compare(o1.getDeadline(), o2.getDeadline());
                }
            });

            allDistributing.clear();
            for (int i = 0; i<requests.size();i++){
                allDistributing.add(i, requests.get(i).toString());
            }
            frame.revalidate();
            newRequest.signalAll();
        }finally {
            rl.unlock();
        }
    }
    /**
     * Sets the request id using idCounter
     * @param message request as string
     * @return request with updated id as string
     */
    @Override
    public String setRequestId(String message) {
        String ret = null;
        try{
            rl.lock();
            Request req = Request.fromString(message);
            assert req != null;
            req.setId(idCounter);
            this.idCounter+=1;
            ret = req.stringify();

        }finally {
            rl.unlock();
        }
        return ret;
    }
    /**
     * Returns a request from the list of requests to be distributed
     * @return the request
     */
    @Override
    public String pullRequest() {
        String ret = null;
        try{
            rl.lock();
            while(this.requests.isEmpty()){
                try {
                    this.newRequest.await();
                } catch (InterruptedException e) {
                    System.out.println("Error Pulling Request");
                }
            }
            ret = this.requests.get(0).stringify();
            this.requests.remove(0);
        }finally {
            rl.unlock();
        }
        return ret;
    }
    /**
     * Adds a update to notify the monitor that a request has been sent to a server
     * @param req request as string
     * @param port port of the server which will receive the request
     */
    @Override
    public void addSentUpdate(String req, String port) {
        try{
            rl.lock();

            this.totalDistributed +=1;
            this.totalDistributing-=1;

            sentLabel.setText(this.totalDistributed +" Sent");
            distributingLabel.setText(this.totalDistributing+" Distributing");

            updates.add("UPDATE#SENT$"+req+"$"+totalReceived+"|"+totalDistributing+"|"+totalDistributed);

            allRequests.add(0, "SENT To Server "+port+": " + Request.fromString(req).toString());

            newUpdate.signal();

        }finally {
            rl.unlock();
        }
    }

    /**
     * Add to the updates list a request that was received
     * @param req: request received
     */
    @Override
    public void addRecvUpdate(String req) {
        try{
            rl.lock();

            this.totalReceived +=1;
            this.totalDistributing += 1;

            receivedLabel.setText(this.totalReceived +" Received");
            distributingLabel.setText(this.totalDistributing+" Distributing");

            updates.add("UPDATE#RECV$"+req+"$"+totalReceived+"|"+totalDistributing+"|"+totalDistributed);
            allRequests.add(0, "RECEIVED:" + Request.fromString(req).toString());

            newUpdate.signal();

        }finally {
            rl.unlock();
        }
    }
    /**
     * Add request which came from previous load balancer to the queue
     * @param  req the request as string
     */
    @Override
    public void addDenied(String req) {
        try{
            rl.lock();
            requests.add(Request.fromString(req));
            newRequest.signalAll();
        }finally {
            rl.unlock();
        }
    }

    /**
     * Allows the LB to start
     */
    @Override
    public void setStart() {
        try{
            rl.lock();
            pauseFlag = true;

            statusLabel.setText("• Running");
            this.statusLabel.setForeground(Color.GREEN);
            frame.revalidate();
            waitForStart.signalAll();
        }finally {
            rl.unlock();
        }
    }
    /**
     * Add new request to the list
     * @param req request as tring
     */
    @Override
    public void addRequest(String req) {
        try{
            rl.lock();
            this.totalReceived +=1;
            this.totalDistributing += 1;
            this.putRequest(req);
        }finally {
            rl.unlock();
        }
    }
    /**
     * Wait for the load balancer to be allowed to start
     */
    @Override
    public void waitForStart() {
        try{
            rl.lock();
            while(!pauseFlag){
                try {
                    statusLabel.setText("• Pause");
                    this.statusLabel.setForeground(Color.ORANGE);
                    waitForStart.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }finally {
            rl.unlock();
        }
    }

    private JFrame frame;
    private DefaultListModel<String> allRequests, allDistributing;
    private JLabel statusLabel, receivedLabel, distributingLabel, sentLabel;

    /**
     * Create the LB's GUI
     */
    private void generateMonitorGui(){
        frame = new JFrame("Load Balancer Settings ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        JPanel mainPanel = new JPanel(new GridLayout(3, 1));

        //Top Panel-----------------------------------------------------------------------------------------------------
        JPanel topPanel = new JPanel(new GridLayout(3, 2));
        topPanel.setBorder(BorderFactory.createTitledBorder("LoadBalancers Settings"));

        if (pauseFlag){

            statusLabel = new JLabel("• Running");
            this.statusLabel.setForeground(Color.GREEN);
        }else{
            statusLabel = new JLabel("• Paused");
            this.statusLabel.setForeground(Color.ORANGE);
        }

        JLabel portLabel = new JLabel("Port "+port);
        JLabel monitorPortLabel = new JLabel("Monitor At "+monitorPort);
        receivedLabel = new JLabel("0 Received");
        distributingLabel = new JLabel("0 Distributing");
        sentLabel = new JLabel("0 Sent");

        topPanel.add(statusLabel);
        topPanel.add(portLabel);
        topPanel.add(monitorPortLabel);
        topPanel.add(receivedLabel);
        topPanel.add(distributingLabel);
        topPanel.add(sentLabel);

        //Middle Panel
        JPanel middlePanel = new JPanel(new GridLayout(1, 1));
        middlePanel.setBorder(BorderFactory.createTitledBorder("Waiting Requests List"));

        allDistributing = new DefaultListModel<>();
        JList<String> list1 = new JList<>(allDistributing);
        list1.setBorder(new EmptyBorder(10,10, 10, 10));
        list1.setFont(new Font("Calibri",Font.BOLD,12));

        JScrollPane scroll1 = new JScrollPane(list1);
        middlePanel.add(scroll1);

        //Bottom Panel-----------------------------------------------------------------------------------------------------
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Logs"));

        allRequests = new DefaultListModel<>();
        JList<String> list = new JList<>(allRequests);
        list.setBorder(new EmptyBorder(10,10, 10, 10));
        list.setFont(new Font("Calibri",Font.BOLD,12));

        JScrollPane scroll = new JScrollPane(list);
        bottomPanel.add(scroll);

        mainPanel.add(topPanel);
        mainPanel.add(middlePanel);
        mainPanel.add(bottomPanel);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}

package Monitor;

import Server.Request;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MMonitor implements IServer {
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Gui Frame
     */
    private JFrame frame;
    /**
     * Monitor Socket Port
     */
    private final int port;
    /**
     * Interval between heartbeats
     */
    private final int heartbeatInterval;
    /**
     * Condition which indicates a new load balancer needs to activate
     */
    private Condition newLoadBalancer;
    //Port -> (CATEGORY: LIST)  // RECV/SENT/PROC
    /**
     * Hashmap between port and list of updates(Received and Sent Requests)
     */
    private HashMap<String, ArrayList<String>> updates;
    /**
     * Hashmap between port and type of service (LoadBalancer or Server)
     */
    private HashMap<String, String> connectionType;
    /**
     * Hashmap between Server port and Server Status (Alive or Dead)
     */
    private HashMap<String, String> serverStatus;

    /**
     * Creates the Monitor's shared region
     * @param _port: Port where the monitor will listen on
     * @param _heartbeatInterval: Interval between heartbeats
     */
    public MMonitor(int _port,int _heartbeatInterval){
        this.frame = new JFrame("Server Status");
        this.rl = new ReentrantLock(true);
        this.port = _port;
        this.heartbeatInterval = _heartbeatInterval;
        this.newLoadBalancer = rl.newCondition();

        this.generateMonitorGui();
        this.buttonsPort = new HashMap<>();
        this.statusLabel= new HashMap<>();
        this.detailsLabels= new HashMap<>();
        this.updates= new HashMap<>();

        this.requestsDetails= new HashMap<>();
        this.loadBalancers= new HashMap<>();
        this.connectionType= new HashMap<>();
        this.serverStatus= new HashMap<>();
    }

    /**
     * This function is only invoked by LBs.
     * The load balancer that invokes it, if it is not the one that is active (it is paused) then it waits until it
     * is its turn to start.
     * @param id: Id of the load balancer
     */
    @Override
    public int awaitUntilAlive(int id) {
        int deadId = 0;
        try{
            rl.lock();
            while(!loadBalancers.containsKey("Active") || !loadBalancers.get("Active").get("ID").equals(String.valueOf(id))){
                try {
                    deadId = Integer.parseInt(loadBalancers.get("Active").get("ID"));
                    newLoadBalancer.await();
                } catch (InterruptedException e) {
                    System.out.println("Error Waiting for new Load Balancer to activate");
                }
            }
        }finally {
            rl.unlock();
        }
        return deadId;
    }

    /**
     * Returns a string which corresponds to a concatenation of current active server ports and the number of iterations
     * they have left to process.
     * @return String details of each server
     */
    @Override
    public String getServers() {
        String serv = null;
        try{
            rl.lock();
            StringBuilder strBuilder = new StringBuilder();

            for (Object k: serverStatus.keySet()){
                if (serverStatus.get(String.valueOf(k)).equals("ALIVE")){
                    strBuilder.append(String.valueOf(k)+":"+this.requestsDetails.get(String.valueOf(k)).get("Number of Iterations")+"#");
                }
            }
            serv = strBuilder.toString();

        }finally {
            rl.unlock();
        }
        return serv;
    }

    /**
     * It returns the unresolved requests of the load balancer that died in a list
     * @param deadId id of the dead load balancer
     * @return List of unprocessed requests
     */
    @Override
    public ArrayList<String> getMissingRequests(int deadId) {
        ArrayList<String> finalList = new ArrayList<>();
        try{
            rl.lock();
            ArrayList<String> _tempSent = new ArrayList<>();
            ArrayList<String> _tempRecv = new ArrayList<>();
            for (String s: updates.get(String.valueOf(deadId))) {
                if (s.split("\\+")[1].equals("RECV")) {
                    _tempRecv.add(s.split("\\+")[2].split("\\|")[2]);
                } else if (s.split("\\+")[1].equals("SENT")) {
                    _tempSent.add(s.split("\\+")[2].split("\\|")[2]);
                }
            }
            for (String s: updates.get(String.valueOf(deadId))) {
                Request req = null;
                if (s.split("\\+").length==4){
                    req = Request.fromString(s.split("\\+")[2]+"+"+s.split("\\+")[3]);
                }else{
                    req = Request.fromString(s.split("\\+")[2]);
                }
                if (_tempRecv.contains(String.valueOf(req.getId())) && !_tempSent.contains(String.valueOf(req.getId()))){
                    finalList.add(req.stringify());
                }
            }

        }finally {
            rl.unlock();
        }
        return finalList;
    }

    /**
     * After the dead load balancer is removed from the "ALIVE" state, this function activates the next idle load balancer
     * @param deadId id of the dead load balancer
     */
    @Override
    public void setNextLoadBalancer(int deadId) {
        try{
            rl.lock();
            for (Object key: loadBalancers.keySet().toArray()){
                if (loadBalancers.get(String.valueOf(key)).get("ID").equals(String.valueOf(deadId)))
                    loadBalancers.remove(String.valueOf(key));
            }
            if (!loadBalancers.containsKey("Active") && loadBalancers.containsKey("Paused")){
                HashMap<String,String> currentlyPaused = loadBalancers.get("Paused");
                loadBalancers.remove("Paused");
                loadBalancers.put("Active", currentlyPaused);
                this.statusLabel.get(currentlyPaused.get("ID")).setForeground(Color.GREEN);
                this.statusLabel.get(currentlyPaused.get("ID")).setText("• Running");
                this.buttonsPort.get(currentlyPaused.get("ID")).setEnabled(true);
                newLoadBalancer.signalAll();
            }
        }finally {
            rl.unlock();
        }
    }

    /**
     * Hashmap between server port/load balancer id and current details. This details include number of received, processing,
     * rejected and sent requests and number of iterations left. For the load balancer, it simply includes the number of requests
     * received, being distributed and sent to servers
     * @param deadId id of the dead load balancer
     */
    private HashMap<String, HashMap<String, Integer>> requestsDetails;

    /**
     * Updates GUI and requestDetails hashmap with new information received from load balancers and servers
     * @param port port of the service which sent the update
     * @param typeOfUpdate type of update. If it is simply a status update with only the detail's numbers or a RECV/SENT update which also contains the specific requests received/sent
     * @param request the request received. If it is a status update then it is empty
     * @param state the detail's numbers to update the requestsDetails hashmap
     */
    @Override
    public void addUpdate(String port, String typeOfUpdate, String request, String state) {
        try{
            rl.lock();
            if (connectionType.containsKey(port) && connectionType.get(port).equals("SERVER")){
                if (!typeOfUpdate.equals("STATUS"))
                    updates.get(port).add(String.valueOf(System.currentTimeMillis()+"+"+typeOfUpdate+"+"+request));

                String[] status = state.split("\\|");
                this.requestsDetails.get(port).put("Received", Integer.valueOf(status[0]));
                this.requestsDetails.get(port).put("Processing", Integer.valueOf(status[1]));
                this.requestsDetails.get(port).put("Number of Iterations", Integer.valueOf(status[2]));
                this.requestsDetails.get(port).put("Rejected", Integer.valueOf(status[3]));
                this.requestsDetails.get(port).put("Sent", Integer.valueOf(status[4]));

                detailsLabels.get(port).get("Received").setText(String.format("%s Received", status[0]));
                detailsLabels.get(port).get("Processing").setText(String.format("%s Processing", status[1]));
                detailsLabels.get(port).get("Number of Iterations").setText(String.format("%s Iterations", status[2]));
                detailsLabels.get(port).get("Rejected").setText(String.format("%s Rejected", status[3]));
                detailsLabels.get(port).get("Sent").setText(String.format("%s Sent", status[4]));

            }else if (connectionType.containsKey(port) && connectionType.get(port).equals("LB")){
                int id = -1;
                for(Object dets : loadBalancers.keySet()){
                    HashMap<String, String> temporary = loadBalancers.get(String.valueOf(dets));
                    if (temporary.get("PORT").equals(port) && dets.equals("Active")){
                        id = Integer.parseInt(temporary.get("ID"));
                    }
                }
                if (id!=-1){

                    if (!typeOfUpdate.equals("STATUS"))
                        updates.get(String.valueOf(id)).add(String.valueOf(System.currentTimeMillis()+"+"+typeOfUpdate+"+"+request));

                    String[] status = state.split("\\|");
                    this.requestsDetails.get(String.valueOf(id)).put("Received", Integer.valueOf(status[0]));
                    this.requestsDetails.get(String.valueOf(id)).put("Distributing", Integer.valueOf(status[1]));
                    this.requestsDetails.get(String.valueOf(id)).put("Sent", Integer.valueOf(status[2]));

                    detailsLabels.get(String.valueOf(id)).get("Received").setText(String.format("%s Received", status[0]));
                    detailsLabels.get(String.valueOf(id)).get("Distributing").setText(String.format("%s Distributing", status[1]));
                    detailsLabels.get(String.valueOf(id)).get("Sent").setText(String.format("%s Sent", status[2]));
                }
            }
            frame.revalidate();
        }
        finally
        {
            rl.unlock();
        }
    }

    /**
     * The thread sleeps for a specific duration (heartbeatInterval)
     */
    @Override
    public void sleep() {
        try{
            rl.lock();
            try {
                Thread.sleep(this.heartbeatInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }finally {
            rl.unlock();
        }
    }

    /**
     * Updates the Load Balancer's Status based on the LB's response to the heartbeat request
     * @param  id id of the LB
     * @param  status status of the LB (Alive or Dead)
     */
    @Override
    public void setLbStatus(String id, String status) {
        try{
            rl.lock();
            if (this.statusLabel.containsKey(id) && status.equals("DEAD")){
                this.statusLabel.get(id).setForeground(Color.RED);
                this.statusLabel.get(id).setText("• Dead");
            }else{
                if (loadBalancers.containsKey("Active") && loadBalancers.get("Active").get("ID").equals(id)){
                    this.statusLabel.get(id).setForeground(Color.GREEN);
                    this.statusLabel.get(id).setText("• Running");
                    this.buttonsPort.get(id).setEnabled(true);
                }else{
                    this.statusLabel.get(id).setForeground(Color.ORANGE);
                    this.statusLabel.get(id).setText("• Paused");
                }
            }
        }
        finally {
            rl.unlock();
        }
    }

    /**
     * Updates the Server's Status based on the Server's response to the heartbeat request
     * @param  port port of the server
     * @param  status status of the Server (Alive or Dead)
     */
    @Override
    public void setServerStatus(String port, String status) {
        try{
            rl.lock();
            if (this.statusLabel.containsKey(port) && status.equals("DEAD")){
                this.statusLabel.get(port).setForeground(Color.RED);
                this.statusLabel.get(port).setText("• Dead");
                this.buttonsPort.get(port).setEnabled(false);
                serverStatus.put(port, "DEAD");
            }else{
                this.statusLabel.get(port).setForeground(Color.GREEN);
                this.statusLabel.get(port).setText("• Running");
                this.buttonsPort.get(port).setEnabled(true);
                serverStatus.put(port, "ALIVE");
            }
        }
        finally {
            rl.unlock();
        }
    }

    HashMap<String, JButton> buttonsPort;
    HashMap<String, JLabel> statusLabel;
    HashMap<String, HashMap<String, JLabel>> detailsLabels;

    /**
     * Add a Server representation to the Gui. Includes the status and the details
     * @param port the port of the Server
     */
    @Override
    public void addServerConnection(String port) {
        try{
            rl.lock();
            if (!this.buttonsPort.containsKey(port)){
                JPanel mainPanel = new JPanel(new GridLayout(2, 1));
                mainPanel.setBorder(BorderFactory.createTitledBorder(port));

                JPanel panel = new JPanel(new GridLayout(1, 2));
                buttonsPort.put(port, new JButton("More Details+"));

                JLabel lblLed = new JLabel("• Running");
                lblLed.setForeground(Color.GREEN);
                statusLabel.put(port, lblLed);

                buttonsPort.get(port).addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showDetailsPanel(port,"SERVER");
                    }
                });

                panel.add(statusLabel.get(port));
                panel.add(buttonsPort.get(port));

                connectionType.put(port,"SERVER");
                if (!updates.containsKey(port)){
                    ArrayList<String> temp = new ArrayList<>();
                    updates.put(port,temp);
                    HashMap<String, Integer> _temp = new HashMap<>();
                    _temp.put("Number of Iterations",0);
                    _temp.put("Received",0);
                    _temp.put("Processing",0);
                    _temp.put("Sent",0);
                    _temp.put("Rejected",0);

                    this.requestsDetails.put(port, _temp);
                }
                JPanel topPanel = new JPanel(new GridLayout(2, 3));
                HashMap<String, JLabel> temp = new HashMap<>();

                temp.put("Received",new JLabel(String.format("%d Received", requestsDetails.get(port).get("Received"))));
                temp.put("Rejected",new JLabel(String.format("%d Rejected", requestsDetails.get(port).get("Rejected"))));
                temp.put("Processing",new JLabel(String.format("%d Processing", requestsDetails.get(port).get("Processing"))));
                temp.put("Number of Iterations",new JLabel(String.format("%d Iterations Left", requestsDetails.get(port).get("Number of Iterations"))));
                temp.put("Sent",new JLabel(String.format("%d Replies Sent", requestsDetails.get(port).get("Sent"))));

                detailsLabels.put(port,temp);
                topPanel.add(temp.get("Received"));
                topPanel.add(temp.get("Rejected"));
                topPanel.add(temp.get("Processing"));
                topPanel.add(temp.get("Number of Iterations"));
                topPanel.add(temp.get("Sent"));

                mainPanel.add(panel);
                mainPanel.add(topPanel);
                bottomPanel.add(mainPanel);

                frame.revalidate();
            }
        }finally {
            rl.unlock();
        }
    }

    /**
     * Hashmap between load balancers status and other information. The inner hashmap saves the current LB's id and port
     */
    private HashMap<String, HashMap<String, String>> loadBalancers;

    /**
     * Add a LoadBalancer representation to the Gui. Includes the status and the details
     * @param port the port of the LB
     */
    @Override
    public int addLoadBalancer(String port) {
        int id = 0;
        try{
            rl.lock();
            id = loadBalancers.size();
            if (loadBalancers.containsKey("Active") && loadBalancers.get("Active").get("ID").equals(String.valueOf(id))){
                id = Integer.parseInt(loadBalancers.get("Active").get("ID"))+1;
            }
            HashMap<String, String> temp = new HashMap<>();
            temp.put("ID", String.valueOf(id));
            temp.put("PORT", port);
            if (id == 0)
                loadBalancers.put("Active", temp);
            else
                loadBalancers.put("Paused", temp);

            JPanel pane = new JPanel(new GridLayout(2,1));
            JPanel mainPanel = new JPanel(new GridLayout(1, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(port+" Id "+id));
            buttonsPort.put(String.valueOf(id), new JButton("More Details+"));

            ArrayList<String> _temp = new ArrayList<>();

            if (!updates.containsKey(String.valueOf(id))){
                updates.put(String.valueOf(id),_temp);
                HashMap<String, Integer> _temp2 = new HashMap<>();
                _temp2.put("Sent",0);
                _temp2.put("Received",0);
                _temp2.put("Distributing",0);
                this.requestsDetails.put(String.valueOf(id), _temp2);
            }

            JLabel lblLed;
            if (id == 0) {
                lblLed = new JLabel("• Running");
                lblLed.setForeground(Color.GREEN);
            }else {
                lblLed = new JLabel("• Paused");
                lblLed.setForeground(Color.ORANGE);
            }
            statusLabel.put(String.valueOf(id), lblLed);

            int finalId = id;
            buttonsPort.get(String.valueOf(id)).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showDetailsPanel(String.valueOf(finalId), "LB");

                }
            });

            connectionType.put(port,"LB");

            JPanel moreInfo = new JPanel(new GridLayout(2, 2));
            HashMap<String, JLabel> _tempLabels = new HashMap<>();

            _tempLabels.put("Received",new JLabel(String.format("%d Received", requestsDetails.get(String.valueOf(id)).get("Received"))));
            _tempLabels.put("Sent",new JLabel(String.format("%d Sent", requestsDetails.get(String.valueOf(id)).get("Sent"))));
            _tempLabels.put("Distributing",new JLabel(String.format("%d Distributing", requestsDetails.get(String.valueOf(id)).get("Distributing"))));

            detailsLabels.put(String.valueOf(id),_tempLabels);
            moreInfo.add(_tempLabels.get("Received"));
            moreInfo.add(_tempLabels.get("Sent"));
            moreInfo.add(_tempLabels.get("Distributing"));

            mainPanel.add(statusLabel.get(String.valueOf(id)));
            mainPanel.add(buttonsPort.get(String.valueOf(id)));

            pane.add(mainPanel);
            pane.add(moreInfo);
            middlePanel.add(pane);
            newLoadBalancer.signalAll();
            frame.revalidate();
        }finally {
            rl.unlock();
        }
        return id;
    }

    JFrame detailsPanel;

    /**
     * Show the details of the specific object call. Includes requests received and Sent
     */
    private void showDetailsPanel(String port, String type){

        detailsPanel = new JFrame("Details "+type+" "+port);

        JPanel mainPanel = new JPanel(new GridLayout(1, 1));

        JPanel bottomPanel = new JPanel(new GridLayout(1, 1));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Request Logs"));
        int size = 1;
        if (updates.containsKey(port)) size = updates.get(port).size();
        JPanel logs = new JPanel(new GridLayout(size, 1));
        logs.setBorder(BorderFactory.createLineBorder(Color.red));
        logs.setPreferredSize(new Dimension(450, 400));

        DefaultListModel<String> l1 = new DefaultListModel<>();
        JList<String> list = new JList<>(l1);
        list.setBorder(new EmptyBorder(10,10, 10, 10));
        list.setFont(new Font("Calibri",Font.BOLD,12));
        if (updates.containsKey(port)) {
            int i = 0;
            for (String s : updates.get(port)) {
                String str = null;
                if (s.split("\\+").length==4)
                    str = String.format("Update %s-TS%s, %s, Request %s", i, s.split("\\+")[0], s.split("\\+")[1], Request.fromString(s.split("\\+")[2]+"+"+s.split("\\+")[3]).toString());
                else {
                    str = String.format("Update %s-TS%s, %s, Request %s", i, s.split("\\+")[0], s.split("\\+")[1], Request.fromString(s.split("\\+")[2]).toString());
                }l1.add(0, str);
                i += 1;
            }

            final JScrollPane scroll = new JScrollPane(list);
            bottomPanel.add(scroll);

            mainPanel.add(bottomPanel);
            detailsPanel.add(mainPanel);
        }

        detailsPanel.setSize(700, 600);
        detailsPanel.setVisible(true);
    }

    JPanel middlePanel, bottomPanel;

    /**
     * Generates the Monitor's Basic Gui
     */
    private void generateMonitorGui(){
        frame = new JFrame("Monitor Settings "+String.valueOf(this.port));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);

        JPanel mainPanel = new JPanel(new GridLayout(2, 1));

        // Middle Panel
        middlePanel = new JPanel(new GridLayout(1, 2));
        middlePanel.setBorder(BorderFactory.createTitledBorder("LoadBalancers Panel"));

        // Bottom Panel
        bottomPanel = new JPanel(new GridLayout(2, 2));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Servers"));

        mainPanel.add(middlePanel);
        mainPanel.add(bottomPanel);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}

package Server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MServer implements IServer, IWorker, ICommunicator, ISender {
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Condition which signals when a new request is recieved
     */
    private final Condition inboundIsEmpty;
    /**
     * Condition which signals when a new result is added
     */
    private final Condition resultsIsEmpty;
    /**
     * Condition which signals the monitor is to be notified of a new update
     */
    private final Condition update;
    /**
     * Maximum inbound request queue size
     */
    private final int BoundQueueSize;
    /**
     * Maximum number of iterations the server can process at a time
     */
    private final int MaximumNumberIterations;
    /**
     * Queue of inbound requests
     */
    private final ArrayList<Request> InboundRequests;
    /**
     * Queue of results
     */
    private final ArrayList<Request> Results;
    /**
     * Queue of updates to notify the monitor of
     */
    private final ArrayList<String> Updates;
    /**
     * Port where the server is hosted
     */
    private final int Port;
    /**
     * Number of Worker Threads
     */
    private final int NumberOfWorkers;
    /**
     * Frame of the GUI
     */
    private final JFrame frame;
    JLabel requestsInQueueLabel,requestsBeingProcessedLabel, requestsProcessedLabel,currentIterationsLabel, totalRejectLabel, resultsSentLabel;
    JLabel[] innerIterationsLabel, innerTotalProcessed;
    JFrame detailsPanel;
    DefaultListModel<String> allRequests;
    /**
     * Number of Current Iterations Left for processing
     */
    private  int CurrentNumberIterations;
    /**
     * Number of Processed requests
     */
    private  int totalProcessed;
    /**
     * Number of requests being processed
     */
    private  int  totalBeingProcessed;

    // Server
    /**
     * Number of rejected requests
     */
    private  int totalRejected;

    // Worker
    /**
     * Number of results sent
     */
    private  int totalResultsSent;
    /**
     * Number of Processed requests
     */
    private  int totalReceived;

    /**
     * Shared Region of the Server
     * @param BQS Size of the Inbound Requests Queue
     * @param MNI maximum number of iterations the server can process
     * @param _port port where the server is hosted
     * @param _workerCount number of worker threads
     */
    public MServer(int BQS, int MNI, int _port, int _workerCount){
        this.NumberOfWorkers = _workerCount;
        this.BoundQueueSize = BQS;
        this.MaximumNumberIterations = MNI;
        this.InboundRequests = new ArrayList<>();
        this.Updates = new ArrayList<>();
        this.CurrentNumberIterations = 0;
        this.totalReceived = 0;
        this.Port = _port;
        this.Results = new ArrayList<>();
        this.frame = new JFrame("Server Status");

        this.rl = new ReentrantLock(true);
        this.inboundIsEmpty = rl.newCondition();
        this.resultsIsEmpty = rl.newCondition();
        this.update = rl.newCondition();

        detailsPanel = new JFrame("Details");
        allRequests= new DefaultListModel<>();

        this.generateGui();
        Server.closeFrame();
    }

    // Sender

    /**
     * If the new request received can be processed by the server it is added to the InboundRequests queue.
     * The request is rejected if the number of total iterations left for processing summed with its iteration value is above the maximum number of iterations or if there is no space in the queue
     * The results list is resorted prioritizing the requests with earliest deadline
     * @param inputLine the request received by the server
     */
    @Override
    public void putNewRequest(String inputLine) {
        try{
            rl.lock();
            Request transformed = Request.fromString(inputLine);

            if (transformed != null){
                allRequests.add(0, "Received at "+System.currentTimeMillis()+":"+transformed.toString());
                detailsPanel.revalidate();
                transformed.setServerId(this.Port);
                if (this.InboundRequests.size()==this.BoundQueueSize ||
                        CurrentNumberIterations+ transformed.getNumberOfIterations()>this.MaximumNumberIterations){
                    transformed.setCode(3);
                    Results.add(transformed);
                    totalRejected+=1;
                    totalRejectLabel.setText(String.valueOf(totalRejected));
                }else{
                    this.InboundRequests.add(transformed);
                    this.InboundRequests.sort(new Comparator<Request>() {
                        public int compare(Request o1, Request o2) {
                            return Integer.compare(o1.getDeadline(), o2.getDeadline());
                        }
                    });
                    this.inboundIsEmpty.signal();
                    requestsInQueueLabel.setText(String.valueOf(this.InboundRequests.size()));
                    this.Updates.add("RECV$"+transformed.stringify()+"$"+totalReceived+"|"+totalBeingProcessed+"|"+CurrentNumberIterations+"|"+totalRejected+"|"+totalResultsSent);
                    this.totalReceived+=1;
                }
                this.update.signal();

            }
        }finally {
            rl.unlock();
        }
    }

    // MCommunicator

    /**
     * Returns a new request which was added to the server for processing
     * The results list is resorted prioritizing the requests with earliest deadline
     * @param workerId the worker who will process the request
     * @return the request to be processed
     */
    @Override
    public Request getRequest(int workerId) {
        Request toProcess = null;
        try{
            rl.lock();
            while(this.InboundRequests.isEmpty()){
                try {
                    this.inboundIsEmpty.await();
                } catch (InterruptedException e) {
                    System.out.println("Failed To Retrieve Request for Processing");
                }
            }
            toProcess = this.InboundRequests.get(0);
            this.InboundRequests.remove(0);
            totalBeingProcessed+=1;
            CurrentNumberIterations += toProcess.getNumberOfIterations();

            requestsInQueueLabel.setText(String.valueOf(this.InboundRequests.size()));
            requestsBeingProcessedLabel.setText(String.valueOf(totalBeingProcessed));
            currentIterationsLabel.setText(String.valueOf(CurrentNumberIterations));

            innerIterationsLabel[workerId].setText(String.valueOf(toProcess.getNumberOfIterations()));

            this.update.signal();
        }finally {
            rl.unlock();
        }
        return toProcess;
    }

    /**
     * Adds the results/processed request to the list of results.
     * The results list is resorted prioritizing the requests with earliest deadline
     * @param req the request result
     * @param workerId the worker who processed the request
     */
    @Override
    public void putResult(Request req, int workerId) {
        try{
            rl.lock();
            this.Results.add(req);
            this.Results.sort(new Comparator<Request>() {
                public int compare(Request o1, Request o2) {
                    return Integer.compare(o1.getDeadline(), o2.getDeadline());
                }
            });

            totalBeingProcessed-=1;
            totalProcessed+=1;
            requestsBeingProcessedLabel.setText(String.valueOf(totalBeingProcessed));
            requestsProcessedLabel.setText(String.valueOf(totalProcessed));
            innerTotalProcessed[workerId].setText(String.valueOf(Integer.parseInt(innerTotalProcessed[workerId].getText())+1));
            this.resultsIsEmpty.signalAll();

        }finally {
            rl.unlock();
        }
    }

    /**
     * One iteration is removed from the total iteration counter and the monitor is notified
     * @param req the request whose iteration was reduced from
     * @param workerId the worker processing the request
     */
    @Override
    public void removeOneIteration(Request req, int workerId) {
        try{
            rl.lock();
            CurrentNumberIterations-=1;
            currentIterationsLabel.setText(String.valueOf(CurrentNumberIterations));
            innerIterationsLabel[workerId].setText(String.valueOf(Integer.parseInt(innerIterationsLabel[workerId].getText())-1));

            this.Updates.add("STATUS$"+req.stringify()+"$"+totalReceived+"|"+totalBeingProcessed+"|"+CurrentNumberIterations+"|"+totalRejected+"|"+totalResultsSent);
            this.update.signal();
        }finally {
            rl.unlock();
        }
    }

    /**
     * The TResultsSender waits for a request to be processed and retrieves the results. The monitor is notified that a result will be sent
     * @return the results of the processed request
     */
    @Override
    public Request getResult() {
        Request req = null;
        try{
            rl.lock();
            while(this.Results.isEmpty()){
                try {
                    this.resultsIsEmpty.await();
                } catch (InterruptedException e) {
                    System.out.println("Failed To Retrieve Results to Send");
                }
            }
            req = this.Results.get(0);
            this.Results.remove(0);
            totalResultsSent+=1;

            allRequests.add(0, "Sent at "+System.currentTimeMillis()+":"+req.toString());
            detailsPanel.revalidate();
            resultsSentLabel.setText(String.valueOf(totalResultsSent));

            this.Updates.add("SENT$"+req.stringify()+"$"+totalReceived+"|"+totalBeingProcessed+"|"+CurrentNumberIterations+"|"+totalRejected+"|"+totalResultsSent);
            this.update.signal();
        }finally {
            rl.unlock();
        }
        return req;
    }

    /**
     * The TMonitor Communicator waits for a new update to send to the Monitor
     * @return the string update
     */
    @Override
    public String waitForUpdate() {
        String res;
        try{
            rl.lock();
            while(this.Updates.isEmpty()){
                try {
                    this.update.await();
                } catch (InterruptedException e) {
                    System.out.println("Failed To Retrieve Results to Send");
                }
            }
            res = this.Updates.get(0);
            this.Updates.remove(0);
        }finally {
            rl.unlock();
        }
        return res;
    }

    /**
     * Terminates the server process and all threads
     */
    private void stopServer(){
        Server.stopServer();
        JOptionPane.showMessageDialog(frame,
                "Server Has Been Stopped",
                "Server Terminated",
                JOptionPane.INFORMATION_MESSAGE);
        frame.dispose();
    }

    /**
     * Generates the GUI which contains the server live status
     */
    public void generateGui(){
        JPanel mainPanel = new JPanel(new GridLayout(2, 1));

        // Server Monitor
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.setBorder(BorderFactory.createTitledBorder("Server Status"));

        // Left Panel
        // Settings like Port, Max BQS, Max NI,
        JPanel leftPanel = new JPanel(new GridLayout(4, 1));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Server Settings"));

        JPanel portPanel = new JPanel(new GridLayout(1, 1));
        portPanel.setBorder(BorderFactory.createTitledBorder("Server Port"));
        portPanel.add(new JLabel(String.valueOf(this.Port)));

        JPanel maxBqsPanel = new JPanel(new GridLayout(1, 1));
        maxBqsPanel.setBorder(BorderFactory.createTitledBorder("Bound Queue Size"));
        maxBqsPanel.add(new JLabel(String.valueOf(this.BoundQueueSize)));

        JPanel maxNumberIterationsPanel = new JPanel(new GridLayout(1, 1));
        maxNumberIterationsPanel.setBorder(BorderFactory.createTitledBorder("Maximum Number of Iterations"));
        maxNumberIterationsPanel.add(new JLabel(String.valueOf(this.MaximumNumberIterations)));

        JPanel terminatePanel = new JPanel(new GridLayout(1, 1));
        terminatePanel.setBorder(BorderFactory.createTitledBorder("Server Control"));
        JButton end=new JButton("Stop Server");
        end.setEnabled(true);

        end.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stopServer();
                end.setEnabled(false);
            }
        });
        terminatePanel.add(end);

        // Right Panel
        // Number of Items in InboundQueue, Requests In Process, Requests Processed, Requests Denied, Current Number of Iterations, Results Sent
        JPanel rightPanel = new JPanel(new GridLayout(4, 2));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Execution Status"));

        JPanel requestsInQueuePanel = new JPanel(new GridLayout(1, 1));
        requestsInQueuePanel.setBorder(BorderFactory.createTitledBorder("Requests in Queue"));
        requestsInQueueLabel = new JLabel(String.valueOf(this.InboundRequests.size()));
        requestsInQueuePanel.add(requestsInQueueLabel);

        JPanel requestsBeingProcessedPanel = new JPanel(new GridLayout(1, 1));
        requestsBeingProcessedPanel.setBorder(BorderFactory.createTitledBorder("Requests Being Processed"));
        requestsBeingProcessedLabel = new JLabel(String.valueOf(totalBeingProcessed));
        requestsBeingProcessedPanel.add(requestsBeingProcessedLabel);

        JPanel requestsProcessedPanel = new JPanel(new GridLayout(1, 1));
        requestsProcessedPanel.setBorder(BorderFactory.createTitledBorder("Requests Processed"));
        requestsProcessedLabel = new JLabel(String.valueOf(this.totalProcessed));
        requestsProcessedPanel.add(requestsProcessedLabel);

        JPanel currentIterationsPanel = new JPanel(new GridLayout(1, 1));
        currentIterationsPanel.setBorder(BorderFactory.createTitledBorder("Current Number of Iterations"));
        currentIterationsLabel = new JLabel(String.valueOf(this.CurrentNumberIterations));
        currentIterationsPanel.add(currentIterationsLabel);

        JPanel totalRejectPanel = new JPanel(new GridLayout(1, 1));
        totalRejectPanel.setBorder(BorderFactory.createTitledBorder("Rejected Requests"));
        totalRejectLabel = new JLabel(String.valueOf(this.totalRejected));
        totalRejectPanel.add(totalRejectLabel);

        JPanel totalResultsSentPanel = new JPanel(new GridLayout(1, 1));
        totalResultsSentPanel.setBorder(BorderFactory.createTitledBorder("Results Sent"));
        resultsSentLabel = new JLabel(String.valueOf(this.totalResultsSent));
        totalResultsSentPanel.add(resultsSentLabel);

        JPanel detailsPanel = new JPanel(new GridLayout(1, 1));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Details"));
        JButton detailsButton = new JButton("Requests Details");
        detailsPanel.add(detailsButton);

        detailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDetailsPanel();
            }
        });

        // Bottom Panel
        //Thread Monitor
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Worker's Status"));

        JPanel[] workerPanels = new JPanel[3];
        innerTotalProcessed = new JLabel[3];
        innerIterationsLabel = new JLabel[3];

        for (int k=0; k<3;k++){
            workerPanels[k] = new JPanel(new GridLayout(2, 2));
            workerPanels[k].setBorder(BorderFactory.createTitledBorder("Worker "+k));

            JPanel statusPanel = new JPanel(new GridLayout(1, 1));
            statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
            if (k<this.NumberOfWorkers){
                JLabel lblLed = new JLabel("• Running");
                lblLed.setForeground(Color.GREEN);
                statusPanel.add(lblLed);
            }else{
                JLabel lblLed = new JLabel("• Stopped");
                lblLed.setForeground(Color.RED);
                statusPanel.add(lblLed);
            }

            JPanel innerIterationsPanel = new JPanel(new GridLayout(1, 1));
            innerIterationsPanel.setBorder(BorderFactory.createTitledBorder("Number Of Iterations"));
            innerIterationsLabel[k] = new JLabel("0");
            innerIterationsPanel.add(innerIterationsLabel[k]);

            JPanel innerTotalProcessedPanel = new JPanel(new GridLayout(1, 1));
            innerTotalProcessedPanel.setBorder(BorderFactory.createTitledBorder("Total Processed"));
            innerTotalProcessed[k] = new JLabel("0");
            innerTotalProcessedPanel.add(innerTotalProcessed[k]);

            workerPanels[k].add(statusPanel);
            workerPanels[k].add(innerIterationsPanel);
            workerPanels[k].add(innerTotalProcessedPanel);
            bottomPanel.add(workerPanels[k]);
        }
        leftPanel.add(portPanel);
        leftPanel.add(maxBqsPanel);
        leftPanel.add(maxNumberIterationsPanel);
        leftPanel.add(terminatePanel);

        rightPanel.add(totalRejectPanel);
        rightPanel.add(requestsInQueuePanel);
        rightPanel.add(requestsBeingProcessedPanel);
        rightPanel.add(currentIterationsPanel);
        rightPanel.add(requestsProcessedPanel);
        rightPanel.add(totalResultsSentPanel);
        rightPanel.add(detailsPanel);

        topPanel.add(leftPanel);
        topPanel.add(rightPanel);
        mainPanel.add(topPanel);
        mainPanel.add(bottomPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        this.frame.add(mainPanel);
        this.frame.setVisible(true);
    }

    /**
     * Shows details about received and sent requests
     */
    private void showDetailsPanel(){
        JPanel mainPanel = new JPanel(new GridLayout(1, 1));

        JPanel bottomPanel = new JPanel(new GridLayout(1, 1));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Request Logs"));

        JList<String> list = new JList<>(allRequests);
        list.setBorder(new EmptyBorder(10,10, 10, 10));
        list.setFont(new Font("Calibri",Font.BOLD,12));

        final JScrollPane scroll = new JScrollPane(list);
        bottomPanel.add(scroll);

        mainPanel.add(bottomPanel);
        detailsPanel.add(mainPanel);
        detailsPanel.setSize(700, 600);
        detailsPanel.setVisible(true);
    }
}

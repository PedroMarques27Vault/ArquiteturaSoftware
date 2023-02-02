package Server;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class Server {
    static HashMap<String, Integer> settings;
    static TWorker[] workers;
    static TResultSender sender;
    static TMonitorCommunicator coms;
    static TMonitorHeartbeat heartbeat;
    static TServer server;

    private static void startServer() {
        MServer mserver = new MServer(settings.get("SoQ"), settings.get("Ni"), settings.get("PORT1"), settings.get("NoT"));

        workers = new TWorker[settings.get("NoT")];
        sender = new TResultSender((ISender) mserver, settings.get("PORT1"));
        coms = new TMonitorCommunicator((ICommunicator) mserver, settings.get("PORT1"),settings.get("PORT2"));
        heartbeat = new TMonitorHeartbeat(settings.get("PORT1"),settings.get("PORT2"));
        server = new TServer((IServer) mserver, settings.get("PORT1"));

        for(int k= 0; k<settings.get("NoT");k++){
            workers[k] = new TWorker((IWorker) mserver, k, settings.get("IT"));
            workers[k].start();
        }
        coms.start();
        server.start();
        sender.start();
        heartbeat.start();
    }

    public static void stopServer() {
        for (TWorker worker : workers) {
            worker.stopProcess();
        }
        coms.stopProcess();
        server.stopProcess();
        sender.stopProcess();
        System.exit(1);
    }

    public static void closeFrame() {
       frame.dispose();
    }

    public static void main(String args[]) {
        settings = new HashMap<>();
        settings.put("NoT",3);
        settings.put("PORT1",3000);
        settings.put("SoQ",2);
        settings.put("Ni",20);
        settings.put("PORT2",5000);
        settings.put("IT",5000);
        generateGui();
    }

    private static JFrame frame;

    private static void generateGui(){
        frame = new JFrame("Server Settings");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        JPanel mainPanel = new JPanel(new GridLayout(3, 1));

        //Number Of Worker Threads
        JPanel noWorkerThreads = new JPanel(new GridLayout(2, 1));
        noWorkerThreads.setBorder(BorderFactory.createTitledBorder("Number of Worker Threads"));

        JLabel noWorkerThreadsText = new JLabel("> Current Number of Worker Threads = 3");
        noWorkerThreads.add(noWorkerThreadsText);

        JSlider noWorkerThreadSlider = new JSlider(JSlider.HORIZONTAL, 1, 3, 3);
        noWorkerThreadSlider.setPaintTicks(true);
        noWorkerThreadSlider.setPaintLabels(true);
        noWorkerThreadSlider.setMajorTickSpacing(1);
        noWorkerThreadSlider.setMinorTickSpacing(1);
        noWorkerThreadSlider.setPreferredSize(new Dimension(200,20));

        noWorkerThreadSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                settings.put("NoT", noWorkerThreadSlider.getValue());
                noWorkerThreadsText.setText("> Current Number of Worker Threads = "+String.valueOf(noWorkerThreadSlider.getValue()));
            }
        });

        noWorkerThreads.add(noWorkerThreadSlider, BorderLayout.WEST);

        // Bound Queue Size
        JPanel boundQueueSize = new JPanel(new GridLayout(2, 1));
        boundQueueSize.setBorder(BorderFactory.createTitledBorder("Maximum Bound Queue Size"));

        JLabel boundQueueSizeText = new JLabel("> Current Bound Queue Size = 2");
        boundQueueSize.add(boundQueueSizeText);

        JSlider boundQueueSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 2, 2);
        boundQueueSizeSlider.setPaintTicks(true);
        boundQueueSizeSlider.setPaintLabels(true);
        boundQueueSizeSlider.setMajorTickSpacing(1);
        boundQueueSizeSlider.setMinorTickSpacing(1);
        boundQueueSizeSlider.setPreferredSize(new Dimension(200,20));

        boundQueueSizeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                settings.put("SoQ", boundQueueSizeSlider.getValue());
                boundQueueSizeText.setText("> Current Bound Queue Size = "+String.valueOf(boundQueueSizeSlider.getValue()));
            }
        });
        boundQueueSize.add(boundQueueSizeSlider, BorderLayout.WEST);

        // Maximum Iteration Requests
        JPanel maxIterationRequests = new JPanel(new GridLayout(2, 1));
        maxIterationRequests.setBorder(BorderFactory.createTitledBorder("Maximum Iteration Requests"));

        JLabel maxIterationRequestsText = new JLabel("> Current Number of Maximum Iterations = 20");
        maxIterationRequests.add(maxIterationRequestsText);

        JSlider maxIterationRequestsSlider = new JSlider(JSlider.HORIZONTAL, 0, 20, 20);
        maxIterationRequestsSlider.setPaintTicks(true);
        maxIterationRequestsSlider.setPaintLabels(true);
        maxIterationRequestsSlider.setMajorTickSpacing(10);
        maxIterationRequestsSlider.setMinorTickSpacing(5);
        maxIterationRequestsSlider.setPreferredSize(new Dimension(200,20));

        maxIterationRequestsSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                settings.put("Ni", maxIterationRequestsSlider.getValue());
                boundQueueSizeText.setText("> Current Number of Maximum Iterations = "+String.valueOf(maxIterationRequestsSlider.getValue()));
            }
        });
        maxIterationRequests.add(maxIterationRequestsSlider, BorderLayout.WEST);

        // Iteration Time
        JPanel iterationTimeRequests = new JPanel(new GridLayout(2, 1));
        iterationTimeRequests.setBorder(BorderFactory.createTitledBorder("Iteration Time"));

        JLabel iterationTimeRequestsText = new JLabel("> Current Time of Iteration = 5000");
        iterationTimeRequests.add(iterationTimeRequestsText);

        JSlider iterationTimeRequestsSlider = new JSlider(JSlider.HORIZONTAL, 0, 5000, 5000);
        iterationTimeRequestsSlider.setPaintTicks(true);
        iterationTimeRequestsSlider.setPaintLabels(true);
        iterationTimeRequestsSlider.setMajorTickSpacing(1000);
        iterationTimeRequestsSlider.setMinorTickSpacing(500);
        iterationTimeRequestsSlider.setPreferredSize(new Dimension(200,20));

        iterationTimeRequestsSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                settings.put("IT", iterationTimeRequestsSlider.getValue());
                iterationTimeRequestsText.setText("> Current Time of Iteration = "+String.valueOf(iterationTimeRequestsSlider.getValue()));
            }
        });
        iterationTimeRequests.add(iterationTimeRequestsSlider, BorderLayout.WEST);

        // Maximum Iteration Requests
        JPanel controls = new JPanel(new GridLayout(2, 1));
        controls.setBorder(BorderFactory.createTitledBorder("Control Server Status"));

        JPanel innerControls = new JPanel(new GridLayout(1, 2));
        JButton start=new JButton("Start Server");
        start.setEnabled(true);

        innerControls.add(start);
        controls.add(innerControls);

        // Port Choice
        JPanel portSelection = new JPanel(new GridLayout(3, 1));
        portSelection.setBorder(BorderFactory.createTitledBorder("Ports"));

        JPanel serverPort = new JPanel(new GridLayout(1, 2));
        serverPort.setBorder(BorderFactory.createTitledBorder("ServerPort"));

        JTextArea portTextArea = new JTextArea(String.valueOf(settings.get("PORT1")),1,4);
        portTextArea.setBackground(Color.LIGHT_GRAY);
        portTextArea.setLineWrap(true);
        portTextArea.setWrapStyleWord(false);

        JPanel commPort = new JPanel(new GridLayout(1, 2));
        commPort.setBorder(BorderFactory.createTitledBorder("Communication Port"));

        JTextArea commPortTextArea = new JTextArea(String.valueOf(settings.get("PORT2")),1,4);
        commPortTextArea.setBackground(Color.LIGHT_GRAY);
        commPortTextArea.setLineWrap(true);
        commPortTextArea.setWrapStyleWord(false);

        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String PORT1 = portTextArea.getText();
                String PORT2 = commPortTextArea.getText();
                if (PORT1.length()!=4 || PORT2.length()!=4 ){
                    JOptionPane.showMessageDialog(frame,
                            "Server Port Must be 4 Integers",
                            "Invalid Port",
                            JOptionPane.ERROR_MESSAGE);
                }else{
                    try{
                        int number1 = Integer.parseInt(PORT1);
                        int number2 = Integer.parseInt(PORT2);
                        settings.put("PORT1", number1);
                        settings.put("PORT2", number2);
                        Server.startServer();
                    }
                    catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(frame,
                                "Server Port Must Be Only Integers",
                                "Invalid Port",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        serverPort.add(portTextArea, BorderLayout.WEST);
        commPort.add(commPortTextArea, BorderLayout.WEST);

        portSelection.add(serverPort, BorderLayout.WEST);
        portSelection.add(commPort, BorderLayout.WEST);

        mainPanel.add(noWorkerThreads);
        mainPanel.add(maxIterationRequests);
        mainPanel.add(boundQueueSize);
        mainPanel.add(iterationTimeRequests);
        mainPanel.add(controls);
        mainPanel.add(portSelection);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}

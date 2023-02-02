package LoadBalancer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadBalancer {
    /**
     * Load Balancer's Port
     */
    private static int port;
    /**
     * Monitor's Port
     */
    private static int monitorPort;
    /**
     * Heartbeat Handler thread
     */
    private static TMonitorHeartbeat theartbeat;
    /**
     * Initialize
     */
    public static void main(String[] args){
        generateGui();
    }
    /**
     * Starts the whole process and threads
     */
    private static void startProcess(){
        MLoadBalancer mbalancer = new MLoadBalancer(port, monitorPort);

        TMonitorHandler handler= new TMonitorHandler(mbalancer, port, monitorPort);
        handler.start();

        TSender sender= new TSender(mbalancer);
        sender.start();

        TReceiver recv= new TReceiver(mbalancer, port);
        recv.start();

        TMonitorHeartbeat heartbeat= new TMonitorHeartbeat(mbalancer, port, monitorPort);
        heartbeat.start();
    }
    /**
     * Stops the whole process and threads
     */
    public static void stopServer() {
        theartbeat.stopProcess();
        System.exit(1);
    }
    /**
     * Close settings GUI
     */
    public static void closeFrame() {
        frame.dispose();
    }

    private static JFrame frame;
    /**
     * Generate settings GUI
     */
    private static void generateGui(){
        frame = new JFrame("Load Balancer Settings");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 100);
        JPanel mainPanel = new JPanel(new GridLayout(1, 3));

        // Port Panel
        JPanel portPanel = new JPanel(new GridLayout(1, 1));
        portPanel.setBorder(BorderFactory.createTitledBorder("LoadBalancer Port"));

        JTextArea portTextArea = new JTextArea("4050",1,4);
        portTextArea.setBackground(Color.LIGHT_GRAY);
        portTextArea.setLineWrap(true);
        portTextArea.setWrapStyleWord(false);
        portTextArea.setFont(new Font("Serif", Font.BOLD, 20));

        portPanel.add(portTextArea);

        // Monitor Port
        JPanel monitorPortPanel = new JPanel(new GridLayout(1, 1));
        monitorPortPanel.setBorder(BorderFactory.createTitledBorder("Monitor Port"));

        JTextArea monitorPortTextArea = new JTextArea("5000",1,4);
        monitorPortTextArea.setBackground(Color.LIGHT_GRAY);
        monitorPortTextArea.setLineWrap(true);
        monitorPortTextArea.setWrapStyleWord(false);
        monitorPortTextArea.setFont(new Font("Serif", Font.BOLD, 20));

        monitorPortPanel.add(monitorPortTextArea);

        // Submit
        JPanel controlPanel = new JPanel(new GridLayout(1, 1));
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String PORT = portTextArea.getText();
                String HEART = monitorPortTextArea.getText();

                if (PORT.length()!=4){
                    JOptionPane.showMessageDialog(frame,
                            "Monitor Port Must be 4 Integers",
                            "Invalid Port",
                            JOptionPane.ERROR_MESSAGE);
                    portTextArea.setText("");
                }else{
                    try{
                        port = Integer.parseInt(PORT);
                        monitorPort = Integer.parseInt(HEART);
                        startProcess();
                        closeFrame();
                    }
                    catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(frame,
                                "Monitor Port Must Be Only Integers",
                                "Invalid Port",
                                JOptionPane.ERROR_MESSAGE);
                        portTextArea.setText("");
                    }
                }
            }
        });
        controlPanel.add(submitButton);

        mainPanel.add(portPanel);
        mainPanel.add(monitorPortPanel);
        mainPanel.add(controlPanel);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}

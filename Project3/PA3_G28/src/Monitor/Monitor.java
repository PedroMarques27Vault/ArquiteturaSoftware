package Monitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Monitor {
    /**
     * Heartbeat Interval Duration in ms
     */
    private static int port;
    /**
     * Monitor's Server Socket Port
     */
    private static int heartbeat;
    /**
     * Thread which receives requests
     */
    private static TServer server;

    public static void main(String[] args){
        generateGui();
    }
    /**
     * Initializes the shared region and threads
     */
    private static void startProcess(){
        MMonitor mserver = new MMonitor(port,heartbeat);

        server = new TServer((IServer) mserver, port);
        server.start();
    }
    /**
     * Stops the process and exits
     */
    public static void stopServer() {
        server.stopProcess();
        System.exit(1);
    }
    public static void closeFrame() {
        frame.dispose();
    }

    private static JFrame frame;
    /**
     * Generates the GUI which asks for the Monitor's Port and Heartbeat Interval Duration in ms
     */
    private static void generateGui(){
        frame = new JFrame("Monitor Settings");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 100);
        JPanel mainPanel = new JPanel(new GridLayout(1, 3));

        //Port Panel
        JPanel portPanel = new JPanel(new GridLayout(1, 1));
        portPanel.setBorder(BorderFactory.createTitledBorder("Monitor Port"));

        JTextArea portTextArea = new JTextArea("5000",1,4);
        portTextArea.setBackground(Color.LIGHT_GRAY);
        portTextArea.setLineWrap(true);
        portTextArea.setWrapStyleWord(false);
        portTextArea.setFont(new Font("Serif", Font.BOLD, 20));

        portPanel.add(portTextArea);

        // Hearbeat
        JPanel hearbeatPanel = new JPanel(new GridLayout(1, 1));
        hearbeatPanel.setBorder(BorderFactory.createTitledBorder("Hearbeat Interval"));

        JTextArea heartbeatTextArea = new JTextArea("2000",1,4);
        heartbeatTextArea.setBackground(Color.LIGHT_GRAY);
        heartbeatTextArea.setLineWrap(true);
        heartbeatTextArea.setWrapStyleWord(false);
        heartbeatTextArea.setFont(new Font("Serif", Font.BOLD, 20));

        hearbeatPanel.add(heartbeatTextArea);

        //Submit
        JPanel controlPanel = new JPanel(new GridLayout(1, 1));
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String PORT = portTextArea.getText();
                String HEART = heartbeatTextArea.getText();

                if (PORT.length()!=4){
                    JOptionPane.showMessageDialog(frame,
                            "Monitor Port Must be 4 Integers",
                            "Invalid Port",
                            JOptionPane.ERROR_MESSAGE);
                    portTextArea.setText("");
                }else{
                    try{
                        port = Integer.parseInt(PORT);
                        heartbeat = Integer.parseInt(HEART);
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
        mainPanel.add(hearbeatPanel);
        mainPanel.add(controlPanel);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}

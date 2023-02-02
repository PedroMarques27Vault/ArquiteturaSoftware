package CCP;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * Control Center Process
 */
public class CCP {
    /**
     * Socket connection with the server
     */
    private static Socket s;
    /**
     * Data Stream received from the client
     */
    private static DataInputStream dis;
    /**
     * Data Stream to send to the client
     */
    private static DataOutputStream dos;
    /**
     * Boolean to check if CCP is running
     */
    private static boolean running = false;
    /**
     * Boolean to check if CCP is running on manual mode
     */
    private static boolean manualMode = false;
    /**
     * CCP GUI Main Panel
     */
    private static JPanel textPanel;
    /**
     * Hash Map for the settings
     */
    private static HashMap<String, Integer> settings;
    /**
     * Hash Map for the GUI labels
     */
    private static HashMap<String, JLabel> textLabels;

    /**
     * Main function: initializes variables and creates the GUI
     */
    public static void main(String[] args)
    {
        settings = new HashMap<>();
        textLabels = new HashMap<>();
        startSettings();
        JFrame frame = new JFrame("Control Centre");

        initializeTextInfo(frame);
        addButtons(frame);
        options(frame);

        frame.setSize(600,800);
        try{
            connect();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }
    /**
     * Inserts default values for the settings
     */
    public static void startSettings(){
        settings.put("NoA",10);
        settings.put("NoC",10);
        settings.put("NoS",4);
        settings.put("EVT",100);
        settings.put("MDT",100);
        settings.put("PYT",100);
        settings.put("TTM",100);
    }
    /**
     * Creates the interface for the options panel and their commands for when the values are changed
     */
    public static void options(JFrame frame){
        JPanel options_panel = new JPanel(new GridLayout(7, 1));

        JSlider slider1 = new JSlider(JSlider.HORIZONTAL, 0, 50, 10);
        slider1.setBorder(BorderFactory.createTitledBorder("Number of Adults"));

        JSlider slider2 = new JSlider(JSlider.HORIZONTAL, 0, 50, 10);
        slider2.setBorder(BorderFactory.createTitledBorder("Number of Children"));

        JSlider slider3 = new JSlider(JSlider.HORIZONTAL, 2, 10, 4);
        slider3.setBorder(BorderFactory.createTitledBorder("Number of Seats"));

        slider1.setPaintTicks(true);
        slider1.setMajorTickSpacing(25);
        slider1.setMinorTickSpacing(5);
        slider1.setPaintLabels(true);
        slider1.setPreferredSize(new Dimension(250,20));

        slider1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                settings.put("NoA", slider1.getValue());
                textLabels.get("NoA").setText(" NoA: "+String.valueOf(slider1.getValue()));
            }
        });
        options_panel.add(slider1, BorderLayout.WEST);

        slider2.setPaintTicks(true);
        slider2.setMajorTickSpacing(25);
        slider2.setPaintLabels(true);

        slider2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                settings.put("NoC", slider2.getValue());
                textLabels.get("NoC").setText(" NoC: "+String.valueOf(slider2.getValue()));
            }
        });
        options_panel.add(slider2);

        slider3.setPaintTicks(true);
        slider3.setMajorTickSpacing(4);
        slider3.setMinorTickSpacing(2);
        slider3.setPaintLabels(true);

        slider3.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (slider3.getValue()%2!=0)
                    slider3.setValue(slider3.getValue()+1);
                else{

                    settings.put("NoS", slider3.getValue());
                    textLabels.get("NoS").setText(" NoS: "+String.valueOf(slider3.getValue()));
                }
            }
        });
        options_panel.add(slider3);

        int[] timeranges = new int[]{0,100,250,500,1000};

        JPanel evtRadioPanel = new JPanel();
        evtRadioPanel.setLayout(new GridLayout(1, 5));
        ButtonGroup evtBg = new ButtonGroup();
        for (int i: timeranges){
            JRadioButton evtRb = new JRadioButton(String.valueOf(i));
            evtRb.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (evtRb.isSelected()){
                        settings.put("EVT", i);
                        textLabels.get("EVT").setText(" EVT: "+String.valueOf(i));
                    }
                }
            });
            evtBg.add(evtRb);
            evtRadioPanel.add(evtRb);
        }
        evtRadioPanel.setBorder(BorderFactory.createTitledBorder("Max EV Time"));
        options_panel.add(evtRadioPanel);

        // MD Time
        JPanel mdtRadioPanel = new JPanel();
        mdtRadioPanel.setLayout(new GridLayout(1, 5));
        ButtonGroup mdtBg = new ButtonGroup();
        for (int i: timeranges){
            JRadioButton mdtRb = new JRadioButton(String.valueOf(i));
            mdtBg.add(mdtRb);
            mdtRadioPanel.add(mdtRb);
            mdtRb.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (mdtRb.isSelected()){

                        settings.put("MDT", i);
                        textLabels.get("MDT").setText(" MDT: "+String.valueOf(i));
                    }
                }
            });
        }
        mdtRadioPanel.setBorder(BorderFactory.createTitledBorder("Max MD Time"));
        options_panel.add(mdtRadioPanel);

        JPanel pytRadioPanel = new JPanel();
        pytRadioPanel.setLayout(new GridLayout(1, 5));
        ButtonGroup pytBg = new ButtonGroup();
        for (int i: timeranges){
            JRadioButton pytRb = new JRadioButton(String.valueOf(i));
            pytBg.add(pytRb);
            pytRadioPanel.add(pytRb);
            pytRb.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (pytRb.isSelected()){

                        settings.put("PYT", i);
                        textLabels.get("PYT").setText(" PYT: "+String.valueOf(i));
                    }
                }
            });
        }
        pytRadioPanel.setBorder(BorderFactory.createTitledBorder("Max PY Time"));
        options_panel.add(pytRadioPanel);

        JPanel ttmRadioPanel = new JPanel();
        ttmRadioPanel.setLayout(new GridLayout(1, 5));
        ButtonGroup ttmBg = new ButtonGroup();
        for (int i: timeranges){
            JRadioButton ttmRb = new JRadioButton(String.valueOf(i));
            ttmBg.add(ttmRb);
            ttmRadioPanel.add(ttmRb);
            ttmRb.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (ttmRb.isSelected()){

                        settings.put("TTM", i);
                        textLabels.get("TTM").setText(" TTM: "+String.valueOf(i));
                    }

                }
            });
        }
        ttmRadioPanel.setBorder(BorderFactory.createTitledBorder("Max Time to Move"));
        options_panel.add(ttmRadioPanel);

        frame.add(options_panel, BorderLayout.WEST);
        frame.pack();
        frame.setVisible(true);
    }
    /**
     * Initializes the text information of the options labels on the GUI
     * @param frame: the frame of the gui
     */
    public static void initializeTextInfo(JFrame frame) {
        textPanel = new JPanel(new GridLayout(7, 1));

        for (String k: new String[]{"NoA", "NoC", "NoS", "EVT","MDT","PYT","TTM"}){
            JLabel temp = new JLabel(String.format(" %s: %d", k, settings.get(k)));
            textLabels.put(k, temp);
            textPanel.add(temp);
        }
        frame.add(textPanel);
    }
    /**
     * Adds status buttons to the GUI and gives commands for each
     * @param frame: the frame of the GUI
     */
    public static void addButtons(JFrame frame) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 2));

        JButton start = new JButton("Start!");
        JButton sus = new JButton("Suspended!");
        JButton res = new JButton("Resume!");
        JButton stop = new JButton("Stop!");
        JButton end = new JButton("End!");
        JButton man = new JButton("Manual Mode!");
        JButton auto = new JButton("Automatic Mode!");
        JButton auth = new JButton("Move 1 Patient!");

        sus.setEnabled(false);
        stop.setEnabled(false);
        res.setEnabled(false);
        end.setEnabled(true);
        start.setEnabled(true);
        auto.setEnabled(false);
        man.setEnabled(true);
        auth.setEnabled(false);

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(start, new JButton("Started CCP.CCP").getText());

                String message = "START:";
                for (String k: settings.keySet()){
                    message+=String.format("%s$%d|", k, settings.get(k));

                }
                try {
                    CCP.dos.writeUTF(message);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                sus.setEnabled(true);
                stop.setEnabled(true);
                res.setEnabled(false);
                running = true;
                start.setEnabled(false);
                if (manualMode) auth.setEnabled(true);
            }
        });
        buttonPanel.add(start);

        sus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(sus, new JButton("Suspended").getText());
                try {
                    CCP.dos.writeUTF("SUSPEND");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                sus.setEnabled(false);
                stop.setEnabled(true);
                res.setEnabled(true);
                start.setEnabled(false);
            }
        });
        buttonPanel.add(sus);


        res.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(res, new JButton("Resumed").getText());
                try {
                    CCP.dos.writeUTF("RESUME");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                sus.setEnabled(true);
                stop.setEnabled(true);
                res.setEnabled(false);
                start.setEnabled(false);
            }
        });
        buttonPanel.add(res);

        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(stop, new JButton("Stop").getText());
                try {
                    CCP.dos.writeUTF("STOP");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                start.setEnabled(true);
                auth.setEnabled(false);
                stop.setEnabled(false);
                sus.setEnabled(false);

                running =false;

            }
        });
        buttonPanel.add(stop);

        end.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CCP.dos.writeUTF("END");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(end, new JButton("Simulation Ended").getText());

                try {
                    disconnect();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                frame.dispose();
            }
        });
        buttonPanel.add(end);


        man.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CCP.dos.writeUTF("MODE:MANUAL");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(man, new JButton("Manual Mode Enabled").getText());

                auto.setEnabled(true);
                man.setEnabled(false);
                manualMode = true;

                if (running) auth.setEnabled(true);
            }
        });
        buttonPanel.add(man);

        auto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CCP.dos.writeUTF("MODE:AUTO");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(auto, new JButton("Manual Mode Enabled").getText());

                auto.setEnabled(false);
                man.setEnabled(true);
                auth.setEnabled(false);
                manualMode = false;
            }
        });
        buttonPanel.add(auto);

        auth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CCP.dos.writeUTF("AUTH:1");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                 }
        });
        buttonPanel.add(auth);


        JPanel east = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        east.add(buttonPanel, gbc);

        frame.add(east, BorderLayout.EAST);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Disconnects from the server socket
     */
    public static void disconnect() throws IOException {
        dos.writeUTF("END");
        s.close();
    }
    /**
     * Connects to the server socket
     */
    public static void connect(){
        try {
            InetAddress ip = InetAddress.getByName("localhost");
            CCP.s = new Socket(ip, 5056);
            CCP.dis = new DataInputStream(s.getInputStream());
            CCP.dos = new DataOutputStream(s.getOutputStream());

            // Only one message
            System.out.println(dis.readUTF());

            // If client sends exit,close this connection
            // and then break from the while loop
            while (true) {
                String received = dis.readUTF();
                System.out.println(received);
                if (received.equals("EXIT"))
                    break;
            }
            dis.close();
            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

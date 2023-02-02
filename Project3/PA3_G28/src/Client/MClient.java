package Client;

import Server.Request;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class MClient implements IClient {
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;

    /**
     * Initialization of the Client's Monitor
     */
    public MClient(){
        this.rl = new ReentrantLock();
        this.generateFirstPanel();
    }

    /**
     * Function to add the response to the GUI
     * @param response: text to input
     */
    @Override
    public void addResponse(String response) {
        try{
            rl.lock();
            receivedList.add(receivedList.size(), Request.fromString(response).toString());
            frame.revalidate();
        }finally {
            rl.unlock();
        }
    }

    /**
     * Function to add the request to the GUI
     * @param request: text to input
     */
    @Override
    public void addRequest(String request) {
        try{
            rl.lock();
            System.out.println(request);
            sentList.add(sentList.size(), Request.fromString(request).toString());
            frame.revalidate();
        }finally {
            rl.unlock();
        }
    }

    // GUI
    private static JFrame frame;
    private static JPanel main_panel;
    private static JPanel upper_panel;
    private static JPanel middle_panel;
    private static JPanel left_panel;
    private static JPanel requests_panel;
    private static JPanel submission_panel;
    private static JPanel fill_panel;
    private static JPanel right_panel;
    private static JTextField port_number;
    private static JTextField lb_port_number;
    private static JTextField iterations_number;
    private static JTextField deadline;
    private static JButton submit_button;
    private static JButton submit_ports_button;
    private static DefaultListModel<String> receivedList;
    private static DefaultListModel<String> sentList;

    /**
     * Generates the GUI that asks for the socket ports of the Client and Load Balancer
     */
    private static void generateFirstPanel(){
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 150);
        main_panel = new JPanel(new GridLayout(3, 1));
        upper_panel = new JPanel(new GridLayout(1, 2));
        middle_panel = new JPanel(new GridLayout(1, 2));

        port_number = new JTextField("4000");
        port_number.setBackground(Color.LIGHT_GRAY);
        port_number.setFont(new Font("Serif", Font.BOLD, 20));

        lb_port_number = new JTextField("4050");
        lb_port_number.setBackground(Color.LIGHT_GRAY);
        lb_port_number.setFont(new Font("Serif", Font.BOLD, 20));

        upper_panel.add(new JLabel("Client PORT"));
        upper_panel.add(new JLabel("LB PORT"));
        middle_panel.add(port_number);
        middle_panel.add(lb_port_number);

        submit_ports_button = new JButton("Submit");
        main_panel.add(upper_panel);
        main_panel.add(middle_panel);
        main_panel.add(submit_ports_button);
        frame.add(main_panel);
        frame.setVisible(true);

        submit_ports_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    Integer.parseInt(port_number.getText());
                    Integer.parseInt(lb_port_number.getText());

                    try {
                        if (Integer.parseInt(port_number.getText())>999 && Integer.parseInt(lb_port_number.getText())>999){
                            Client.set_port_numbers(Integer.parseInt(port_number.getText()),
                                    Integer.parseInt(lb_port_number.getText()));
                            frame.dispose();
                            generateGui();
                        }else {
                            JOptionPane.showMessageDialog(frame,
                                    "Port values must be 4 digits long",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } catch (final NumberFormatException n) {
                    JOptionPane.showMessageDialog(frame,
                            "Port values must be numbers",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Generates the main panel of Client's GUI.
     * Has 3 internal panels: request information and submission, list of requests made and list of responses received
     */
    private static void generateGui(){
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        main_panel = new JPanel(new GridLayout(1, 2));

        // LEFT PANEL
        left_panel = new JPanel(new GridLayout(2, 1));

        //      REQUESTS
        requests_panel = new JPanel(new GridLayout(1, 1));
        requests_panel.setBorder(BorderFactory.createTitledBorder("Requests Sent"));

        sentList = new DefaultListModel<>();
        JList<String> list = new JList<>(sentList);
        list.setBorder(new EmptyBorder(10,10, 10, 10));
        list.setFont(new Font("Calibri",Font.BOLD,12));

        JScrollPane scroll = new JScrollPane(list);

        requests_panel.add(scroll);

        //      REQUESTS SUBMISSION
        submission_panel = new JPanel(new GridLayout(2, 1));
        submission_panel.setBorder(BorderFactory.createTitledBorder("Request Information"));
        fill_panel = new JPanel(new GridLayout(2, 1));
        iterations_number = new JTextField("5");
        iterations_number.setBackground(Color.LIGHT_GRAY);
        iterations_number.setFont(new Font("Serif", Font.BOLD, 20));
        deadline = new JTextField("10");
        deadline.setBackground(Color.LIGHT_GRAY);
        deadline.setFont(new Font("Serif", Font.BOLD, 20));

        fill_panel.add(new JLabel("Number of Iterations"));
        fill_panel.add(iterations_number);
        fill_panel.add(new JLabel("Deadline"));
        fill_panel.add(deadline);

        submission_panel.add(fill_panel);
        submit_button = new JButton("Make Request");
        submission_panel.add(submit_button);

        left_panel.add(submission_panel);
        left_panel.add(requests_panel);

        // RIGHT PANEL
        right_panel = new JPanel(new GridLayout(1, 1));
        right_panel.setBorder(BorderFactory.createTitledBorder("Responses Received"));

        receivedList = new DefaultListModel<>();
        list = new JList<>(receivedList);
        list.setBorder(new EmptyBorder(10,10, 10, 10));
        list.setFont(new Font("Calibri",Font.BOLD,12));

        scroll = new JScrollPane(list);
        right_panel.add(scroll);

        main_panel.add(left_panel);
        main_panel.add(right_panel);

        frame.add(main_panel);
        frame.setVisible(true);
        frame.revalidate();

        submit_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {

                try {
                    Integer.parseInt(iterations_number.getText());
                    Integer.parseInt(deadline.getText());
                    try {
                        if (Integer.parseInt(iterations_number.getText())>0 && Integer.parseInt(deadline.getText())>0){

                            if (Integer.parseInt(iterations_number.getText())<21){
                                Client.send_request(Integer.parseInt(iterations_number.getText()),
                                        Integer.parseInt(deadline.getText()));
                                frame.revalidate();
                            }else {
                                JOptionPane.showMessageDialog(frame,
                                        "Iteration values must be at most 20 ",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }else {
                            JOptionPane.showMessageDialog(frame,
                                    "Deadline and Iteration values must be numbers above 0 ",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } catch (final NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame,
                            "Deadline and Iteration values must be numbers",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}

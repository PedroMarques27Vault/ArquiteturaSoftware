package HCP.Communication;

import HCP.Entities.*;
import HCP.Enums.AGE;
import HCP.Enums.OPERATION;
import HCP.Logger.ILogger;
import HCP.Logger.MLogger;
import HCP.Logger.TLogger;
import HCP.Monitors.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
/**
 * Class responsible for handling a client socket connection and communicates with the CCP GUI
 */
public class ClientHandler extends Thread
{
    /**
     * Data Stream received from the client
     */
    private final DataInputStream dis;
    /**
     * Data Stream to send to the client
     */
    private final DataOutputStream dos;
    /**
     * Socket connection with the client
     */
    private final Socket s;
    /**
     * Current running mode. Defines if the simulation runs automatically or manually
     */
    private boolean isAutomatic = true;
    /**
     * Array of all the patients
     */
    private TPatient[] consumer;
    /**
     * Array of all the nurses of the EVH
     */
    private final TNurse[] nurse;
    /**
     * Array of all the doctors of the MDH
     */
    private final TDoctor[] doctors;
    /**
     * Cashier of the PYH
     */
    private TCashier cashier;
    /**
     * Call Centre
     */
    private TCallCentre cc;
    /**
     * Logger Monitor
     */
    private final MLogger mlogger;
    /**
     * Logger Producer
     */
    private final TLogger logger;
    /**
     * Call Centre Hall Monitor
     */
    private MCCH mcch;
    /**
     * HCP GUI Frame
     */
    JFrame frame = new JFrame("HCP");
    /**
     * List of Logged Actions for the GUI
     */
    static ArrayList<String> logActions = new ArrayList<String>();
    JPanel panel = new JPanel(new GridLayout(3, 2));
    JPanel ETH_panel = new JPanel(new GridLayout(2, 1));
    JPanel ETH = new JPanel(new GridLayout(2, 1));
    JPanel ETRs = new JPanel(new GridLayout(1, 2));
    JPanel ETR1_panel = new JPanel(new GridLayout(2, 1));
    JPanel ETR2_panel = new JPanel(new GridLayout(2, 1));

    private final JLabel ETH_Label = new JLabel("ETH", JLabel.CENTER);
    private final JLabel ETR1_Label = new JLabel("ETR1", JLabel.CENTER);
    private final JLabel ETR2_Label= new JLabel("ETR2", JLabel.CENTER);

    private final static JTextArea ETH_TextArea = new JTextArea();
    private final static JTextArea ETR1_TextArea = new JTextArea();
    private final static JTextArea ETR2_TextArea = new JTextArea();

    JPanel EVH_panel = new JPanel(new GridLayout(2, 2));
    JPanel EvR1_panel = new JPanel(new GridLayout(2, 1));
    JPanel EvR2_panel = new JPanel(new GridLayout(2, 1));
    JPanel EvR3_panel = new JPanel(new GridLayout(2, 1));
    JPanel EvR4_panel =new JPanel(new GridLayout(2, 1));

    private final JLabel EVR1_Label = new JLabel("EVR1", JLabel.CENTER);
    private final JLabel EVR2_Label = new JLabel("EVR2", JLabel.CENTER);
    private final JLabel EVR3_Label= new JLabel("EVR3", JLabel.CENTER);
    private final JLabel EVR4_Label= new JLabel("EVR4", JLabel.CENTER);

    private final static JTextArea EVR1_TextArea = new JTextArea();
    private final static JTextArea EVR2_TextArea = new JTextArea();
    private final static JTextArea EVR3_TextArea = new JTextArea();
    private final static JTextArea EVR4_TextArea = new JTextArea();

    JPanel WTH_panel = new JPanel(new GridLayout(2, 2));
    JPanel WTR1_panel = new JPanel(new GridLayout(2, 1));
    JPanel WTR2_panel = new JPanel(new GridLayout(2, 1));

    private final JLabel WTR1_Label= new JLabel("WTR1", JLabel.CENTER);
    private final JLabel WTR2_Label= new JLabel("WTR2", JLabel.CENTER);
    private final JLabel WTH_label= new JLabel("Waiting Hall", JLabel.CENTER);

    private final static JTextArea WTR1_TextArea = new JTextArea();
    private final static JTextArea WTR2_TextArea = new JTextArea();

    JPanel MDH_panel = new JPanel(new GridLayout(2, 1));
    JPanel MDW_panel = new JPanel(new GridLayout(2, 1));
    JPanel MDRs = new JPanel(new GridLayout(2, 2));
    JPanel MDR1_panel = new JPanel(new GridLayout(2, 1));
    JPanel MDR2_panel = new JPanel(new GridLayout(2, 1));
    JPanel MDR3_panel = new JPanel(new GridLayout(2, 1));
    JPanel MDR4_panel = new JPanel(new GridLayout(2, 1));

    private final JLabel MDW_Label = new JLabel("MDW", JLabel.CENTER);
    private final JLabel MDR1_Label = new JLabel("MDR1", JLabel.CENTER);
    private final JLabel MDR2_Label = new JLabel("MDR2", JLabel.CENTER);
    private final JLabel MDR3_Label= new JLabel("MDR3", JLabel.CENTER);
    private final JLabel MDR4_Label= new JLabel("MDR4", JLabel.CENTER);

    private static final JTextArea MDW_TextArea = new JTextArea();
    private static final JTextArea MDR1_TextArea = new JTextArea();
    private static final JTextArea MDR2_TextArea = new JTextArea();
    private static final JTextArea MDR3_TextArea = new JTextArea();
    private static final JTextArea MDR4_TextArea = new JTextArea();
    private static final JScrollPane scrollPane = new JScrollPane();

    JPanel PYeH_panel = new JPanel(new GridLayout(2, 1));
    private final JLabel PYeH_Label= new JLabel("PYH", JLabel.CENTER);
    private static final JTextArea PYeH_TextArea = new JTextArea();
    JPanel out_panel = new JPanel(new GridLayout(2, 1));
    private final JLabel out_Label= new JLabel("Outside HCP", JLabel.CENTER);
    private static final JTextArea out_TextArea = new JTextArea();

    /**
     * Initializes all the variables. Starts the logger and the HCP GUI
     */
    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) throws InterruptedException {
        this.mcch = null;
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.nurse = new TNurse[4];
        this.doctors = new TDoctor[4];
        mcch = null;
        mlogger = new MLogger();
        logger = new TLogger((ILogger) mlogger, "logs.txt");
        logger.start();
        initialize();
    }
    /**
     * Loads the HCP GUI and its default configurations
     */
    private void initialize(){
        TitledBorder title;
        ETH.setBackground(new Color(230,247,255));
        ETH.add(ETH_Label, BorderLayout.NORTH);
        ETH_TextArea.setEditable(false);
        ETH_TextArea.setLineWrap( true );
        ETH.add(ETH_TextArea, BorderLayout.SOUTH);

        ETR1_panel.setBackground(new Color(230,247,255));
        ETR1_panel.add(ETR1_Label, BorderLayout.NORTH);
        ETR1_panel.add(ETR1_TextArea, BorderLayout.SOUTH);
        ETR1_TextArea.setEditable(false);
        ETR1_TextArea.setLineWrap( true );
        ETR1_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        ETR2_panel.setBackground(new Color(230,247,255));
        ETR2_panel.add(ETR2_Label, BorderLayout.NORTH);
        ETR2_panel.add(ETR2_TextArea, BorderLayout.SOUTH);
        ETR2_TextArea.setEditable(false);
        ETR2_TextArea.setLineWrap( true );
        ETR2_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        ETRs.add(ETR1_panel);
        ETRs.add(ETR2_panel);

        ETH_panel.setBackground(new Color(230,247,255));
        ETH_panel.add(ETH);
        ETH_panel.add(ETRs);
        title = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Entrance Hall");
        title.setTitleJustification(TitledBorder.RIGHT);
        ETH_panel.setBorder(title);

        panel.add(ETH_panel);

        EvR1_panel.setBackground(new Color(230,247,255));
        EvR1_panel.add(EVR1_Label);
        EvR1_panel.add(EVR1_TextArea);
        EVR1_TextArea.setEditable(false);
        EVR1_TextArea.setLineWrap( true );
        EvR1_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        EvR2_panel.setBackground(new Color(230,247,255));
        EvR2_panel.add(EVR2_Label);
        EvR2_panel.add(EVR2_TextArea);
        EVR2_TextArea.setEditable(false);
        EVR2_TextArea.setLineWrap( true );
        EvR2_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        EvR3_panel.setBackground(new Color(230,247,255));
        EvR3_panel.add(EVR3_Label);
        EvR3_panel.add(EVR3_TextArea);
        EVR3_TextArea.setEditable(false);
        EVR3_TextArea.setLineWrap( true );
        EvR3_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        EvR4_panel.setBackground(new Color(230,247,255));
        EvR4_panel.add(EVR4_Label, BorderLayout.NORTH);
        EvR4_panel.add(EVR4_TextArea, BorderLayout.SOUTH);
        EVR4_TextArea.setEditable(false);
        EVR4_TextArea.setLineWrap( true );
        EvR4_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        title = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Evaluation Hall");
        title.setTitleJustification(TitledBorder.RIGHT);
        EVH_panel.setBorder(title);

        EVH_panel.setBackground(new Color(230,247,255));
        EVH_panel.add(EvR1_panel);
        EVH_panel.add(EvR2_panel);
        EVH_panel.add(EvR3_panel);
        EVH_panel.add(EvR4_panel);

        panel.add(EVH_panel);

        WTR1_panel.setBackground(new Color(230,247,255));
        WTR1_panel.add(WTR1_Label);
        WTR1_panel.add(WTR1_TextArea);
        WTR1_TextArea.setEditable(false);
        WTR1_TextArea.setLineWrap( true );

        WTR2_panel.setBackground(new Color(230,247,255));
        WTR2_panel.add(WTR2_Label, BorderLayout.NORTH);
        WTR2_panel.add(WTR2_TextArea, BorderLayout.SOUTH);
        WTR2_TextArea.setEditable(false);
        WTR2_TextArea.setLineWrap( true );

        WTH_panel.setBackground(new Color(230,247,255));
        WTH_panel.add(WTR1_panel, BorderLayout.EAST);
        WTH_panel.add(WTR2_panel, BorderLayout.WEST);
        title = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Waiting Hall");
        title.setTitleJustification(TitledBorder.RIGHT);
        WTH_panel.setBorder(title);

        panel.add(WTH_panel, BorderLayout.WEST);

        MDW_panel.setBackground(new Color(230,247,255));
        MDW_panel.add(MDW_Label);
        MDW_panel.add(MDW_TextArea);
        MDW_TextArea.setEditable(false);
        MDW_TextArea.setLineWrap( true );

        MDR1_panel.setBackground(new Color(230,247,255));
        MDR1_panel.add(MDR1_Label);
        MDR1_panel.add(MDR1_TextArea);
        MDR1_TextArea.setEditable(false);
        MDR1_TextArea.setLineWrap( true );
        MDR1_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        MDR2_panel.setBackground(new Color(230,247,255));
        MDR2_panel.add(MDR2_Label);
        MDR2_panel.add(MDR2_TextArea);
        MDR2_TextArea.setEditable(false);
        MDR2_TextArea.setLineWrap( true );
        MDR2_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        MDR3_panel.setBackground(new Color(230,247,255));
        MDR3_panel.add(MDR3_Label);
        MDR3_panel.add(MDR3_TextArea);
        MDR3_TextArea.setEditable(false);
        MDR3_TextArea.setLineWrap( true );
        MDR3_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        MDR4_panel.setBackground(new Color(230,247,255));
        MDR4_panel.add(MDR4_Label);
        MDR4_panel.add(MDR4_TextArea);
        MDR4_TextArea.setEditable(false);
        MDR4_TextArea.setLineWrap( true );
        MDR4_panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        MDRs.add(MDR1_panel, BorderLayout.EAST);
        MDRs.add(MDR2_panel, BorderLayout.WEST);
        MDRs.add(MDR3_panel, BorderLayout.EAST);
        MDRs.add(MDR4_panel, BorderLayout.WEST);

        MDH_panel.setBackground(new Color(230,247,255));
        MDH_panel.add(MDW_panel, BorderLayout.EAST);
        MDH_panel.add(MDRs, BorderLayout.WEST);
        title = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Medical Hall");
        title.setTitleJustification(TitledBorder.RIGHT);
        MDH_panel.setBorder(title);

        panel.add(MDH_panel, BorderLayout.EAST);

        Panel Final_panel = new Panel(new GridLayout(2,1));

        PYeH_panel.setBackground(new Color(230,247,255));
        PYeH_panel.add(PYeH_Label);
        PYeH_panel.add(PYeH_TextArea);
        PYeH_TextArea.setEditable(false);
        PYeH_TextArea.setLineWrap( true );

        title = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Payment Hall");
        title.setTitleJustification(TitledBorder.RIGHT);
        PYeH_panel.setBorder(title);

        out_panel.setBackground(new Color(230,247,255));
        out_panel.add(out_Label);
        out_panel.add(out_TextArea);
        out_TextArea.setEditable(false);
        out_TextArea.setLineWrap( true );

        title = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Out HCP");
        title.setTitleJustification(TitledBorder.RIGHT);
        out_panel.setBorder(title);

        Final_panel.add(PYeH_panel,BorderLayout.NORTH);
        Final_panel.add(out_panel,BorderLayout.SOUTH);
        panel.add(Final_panel);

        JPanel logPanel = new JPanel(new BorderLayout(2,2));

        title = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Logs");
        title.setTitleJustification(TitledBorder.RIGHT);
        logPanel.setBorder(title);
        logPanel.setBackground(new Color(230,247,255));

        final JList<String> list = new JList<String>(logActions.toArray(new String[logActions.size()]));
        scrollPane.setViewportView(list);
        list.setLayoutOrientation(JList.VERTICAL);
        logPanel.add(scrollPane);
        panel.add(logPanel);

        frame.add(panel);
        frame.pack();
        frame.setSize(800, 800);
        frame.setVisible(true);
    }
    /**
     * Thread run implementation
     * <p>Receives every message from the client and uses the handleRequest function to produce an action</p>
     */
    @Override
    public void run()
    {
        String received;
        String toreturn;
        while (true)
        {
            try {
                // receive the answer from client
                try{
                    received = dis.readUTF();
                }catch(Exception e){
                    break;
                }
                // write on output stream based on the
                // answer from the client
                Response r = handleRequest(received);

                toreturn = r.getMessage();
                try{
                    dos.writeUTF(toreturn);
                }catch(Exception e){
                    break;
                }
                if (r.getOperation() == OPERATION.END) {
                    break;
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        try{
            s.close();
            dis.close();
            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Handles all the messages received from the client socket
     * <p>Each message can be split by the character ':'. The first object of the splitten array indicates the action to take place</p>
     * <p>Example START:NoS$4|NoA$4|NoC$6|... indicates to start the simulation and the rest of the message indicates the settings of the simulation</p>
     * @param received: the message received from the client
     * @return Response object according to the action taken
     */
    private Response handleRequest(String received) throws IOException {

        switch (received.split(":")[0]){
            case "START":
                logActions.clear();
                ETH_TextArea.setText("");
                ETR1_TextArea.setText("");
                ETR2_TextArea.setText("");
                EVR1_TextArea.setText("");
                EVR2_TextArea.setText("");
                EVR3_TextArea.setText("");
                EVR4_TextArea.setText("");
                WTR1_TextArea.setText("");
                WTR2_TextArea.setText("");
                MDW_TextArea.setText("");
                MDR1_TextArea.setText("");
                MDR2_TextArea.setText("");
                MDR3_TextArea.setText("");
                MDR4_TextArea.setText("");
                PYeH_TextArea.setText("");

                out_TextArea.setText("");

                String optString = received.split(":")[1];
                String[] values = optString.split("\\|");

                int NoS = Integer.parseInt(values[3].split("\\$")[1]);

                int NoA = Integer.parseInt(values[0].split("\\$")[1]);
                int NoC = Integer.parseInt(values[2].split("\\$")[1]);
                int ttm = Integer.parseInt(values[6].split("\\$")[1]);

                int evt = Integer.parseInt(values[1].split("\\$")[1]);
                int mdt = Integer.parseInt(values[5].split("\\$")[1]);
                int pyt = Integer.parseInt(values[4].split("\\$")[1]);

                // Creating ETH Monitor
                mlogger.writeLog(String.format("NoA:%02d, NoC:%02d, NoS:%02d", NoA, NoC, NoS));
                mlogger.writeLog("HEADER");
                mlogger.writeLog("RUN@STT");

                METH meth = new METH(NoS, mlogger);
                this.mcch = new MCCH(NoS);
                MEVH mevh = new MEVH(NoS,mlogger);
                MWTH mwth = new MWTH(NoS,mlogger);
                MMDH mmdh = new MMDH(NoS,mlogger);
                MPYH mpyh = new MPYH(mlogger);

                this.mcch.setAutomatic(this.isAutomatic);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                cc = new TCallCentre((ICallCentre_CCH) mcch, (ICallCentre_ETH) meth,(ICallCentre_WTH) mwth, (ICallCentre_MDH) mmdh);

                for(int k = 0; k<4;k++){
                    nurse[k] = new TNurse((INurse) mevh, evt);
                    doctors[k] = new TDoctor((IDoctor) mmdh, mdt);

                }
                cashier = new TCashier((ICashier) mpyh, pyt);

                consumer = new TPatient[NoA+NoC];
                int index = 0;
                for(int k = 0; k<NoA;k++){
                    consumer[index] = new TPatient(AGE.ADULT, (IPatient_CCH) mcch, (IPatient_ETH) meth, (IPatient_EVH) mevh,(IPatient_WTH) mwth,(IPatient_MDH) mmdh, (IPatient) mpyh, ttm);
                    index++;
                }

                for(int k = 0; k<NoC;k++){
                    consumer[index] = new TPatient(AGE.CHILD, (IPatient_CCH) mcch, (IPatient_ETH) meth,(IPatient_EVH) mevh,(IPatient_WTH) mwth,(IPatient_MDH) mmdh,(IPatient) mpyh, ttm);
                    index++;
                }

                cc.start();
                cashier.start();
                for(int k = 0; k<4;k++){
                    nurse[k].start();
                    doctors[k].start();
                }

                for(int k = 0; k<NoA+NoC;k++){
                    consumer[k].start();
                }
                return new Response(OPERATION.START, "Simulation Started");

            case "SUSPEND":
                cc.suspendProcess();
                for(int k = 0; k<4;k++){
                    nurse[k].suspendProcess();
                    doctors[k].suspendProcess();
                }
                cashier.suspendProcess();

                for (int k = 0; k<this.consumer.length; k++){
                    this.consumer[k].suspendProcess();
                }

                mlogger.writeLog(String.format("SUS@STT"));
                return new Response(OPERATION.SUSPEND, "Simulation Suspended");

            case "RESUME":
                cc.resumeProcess();
                for(int k = 0; k<4;k++){
                    nurse[k].resumeProcess();
                    doctors[k].resumeProcess();
                }
                cashier.resumeProcess();
                for (int k = 0; k<this.consumer.length; k++){
                    this.consumer[k].resumeProcess();
                }
                return new Response(OPERATION.RESUME, "Simulation Resumed");

            case "STOP":
                cc.stopProcess();
                for(int k = 0; k<4;k++){
                    nurse[k].stopProcess();
                    doctors[k].stopProcess();
                }
                cashier.stopProcess();
                for (int k = 0; k<this.consumer.length; k++){
                    this.consumer[k].stopProcess();
                }

                mlogger.writeLog(String.format("STO@STT"));
                logActions.clear();
                ETH_TextArea.setText("");
                ETR1_TextArea.setText("");
                ETR2_TextArea.setText("");
                EVR1_TextArea.setText("");
                EVR2_TextArea.setText("");
                EVR3_TextArea.setText("");
                EVR4_TextArea.setText("");
                WTR1_TextArea.setText("");
                WTR2_TextArea.setText("");
                MDW_TextArea.setText("");
                MDR1_TextArea.setText("");
                MDR2_TextArea.setText("");
                MDR3_TextArea.setText("");
                MDR4_TextArea.setText("");
                PYeH_TextArea.setText("");

                out_TextArea.setText("");

                return new Response(OPERATION.STOP, "Simulation Stopped");

            case "END":
                cc.stopProcess();
                for(int k = 0; k<4;k++){
                    nurse[k].stopProcess();
                    doctors[k].stopProcess();
                }
                cashier.stopProcess();
                for (int k = 0; k<this.consumer.length; k++){
                    this.consumer[k].stopProcess();
                }
                mlogger.writeLog("END@STT");
                System.out.flush();

                logActions.clear();
                ETH_TextArea.setText("");
                ETR1_TextArea.setText("");
                ETR2_TextArea.setText("");
                EVR1_TextArea.setText("");
                EVR2_TextArea.setText("");
                EVR3_TextArea.setText("");
                EVR4_TextArea.setText("");
                WTR1_TextArea.setText("");
                WTR2_TextArea.setText("");
                MDW_TextArea.setText("");
                MDR1_TextArea.setText("");
                MDR2_TextArea.setText("");
                MDR3_TextArea.setText("");
                MDR4_TextArea.setText("");
                PYeH_TextArea.setText("");
                out_TextArea.setText("");




                return new Response(OPERATION.END, "Simulation Ended");

            case "MODE":
                if (received.split(":")[1].equals("AUTO")){
                    this.isAutomatic = true;
                    mlogger.writeLog("AUT@STT");
                    if(mcch!=null) mcch.setAutomatic(this.isAutomatic);
                }
                else {
                    this.isAutomatic = false;
                    if(mcch!=null) mcch.setAutomatic(this.isAutomatic);
                    mlogger.writeLog("MAN@STT");
                }

                return new Response(OPERATION.MODE,"Simulation is Automatic: "+this.isAutomatic);

            case "AUTH":
                if (!this.isAutomatic) {
                    if (mcch!=null) mcch.allowOneMovement();
                    return new Response(OPERATION.AUTHORIZED, "1 Patient Authorized");
                }
                return new Response(OPERATION.NONAUTHORIZED,"Simulation is in Automatic Mode");
            default:
                break;
        }
        return new Response(OPERATION.UNDEFINED, "Command Not Recognized");
    }

    /**
     * Adds a patient to the ETH in the GUI
     * @param patientName: the name of the patient
     */
    public static void enterETH(String patientName){
        ETH_TextArea.append(patientName+";");
    }

    /**
     * Adds a patient to the ETR1 in the GUI
     * @param patientName: the name of the patient
     */
    public static void enterETR1(String patientName){
        String text = ETH_TextArea.getText();

        String[] splittedText = text.split(";");

        String updatedText = "";
        for (String s : splittedText) {
            if (!s.equals(patientName))
                updatedText += s + ";";
        }

        ETH_TextArea.setText(updatedText);

        ETR1_TextArea.append(patientName+";");
    }

    /**
     * Removes a patient from the ETR in the GUI
     * @param patientName: the name of the patient
     */
    public static void leaveETR1(String patientName) {
        String text = "";
        text = ETR1_TextArea.getText();
        String[] splittedText = text.split(";");

        String updatedText = "";
        for (String s : splittedText) {
            if (!s.equals(patientName))
                updatedText += s + ";";
        }
        ETR1_TextArea.setText(updatedText);
    }


    /**
     * Adds a patient to the ETR2 in the GUI
     * @param patientName: the name of the patient
     */
    public static void enterETR2(String patientName) {
        String text = ETH_TextArea.getText();

        String[] splittedText = text.split(";");

        String updatedText = "";
        for (String s : splittedText) {
            if (!s.equals(patientName))
                updatedText += s + ";";
        }

        ETH_TextArea.setText(updatedText);

        ETR2_TextArea.append(patientName+";");
    }

    /**
     * Removes a patient from the ETR2 in the GUI
     * @param patientName: the name of the patient
     */
    public static void leaveETR2(String patientName) {
        String text = "";
        text = ETR2_TextArea.getText();
        String[] splittedText = text.split(";");

        String updatedText = "";
        for (String s : splittedText) {
            if (!s.equals(patientName))
                    updatedText += s + ";";
        }
        ETR2_TextArea.setText(updatedText);
    }
    /**
     * Adds a log entry to the logActions ArrayList in the GUI
     */
    public static void addLogEntry(String logData) {
        logActions.add(0,logData);
        final JList<String> list = new JList<String>(logActions.toArray(new String[logActions.size()]));
        scrollPane.setViewportView(list);
        list.setLayoutOrientation(JList.VERTICAL);
    }

    /**
     * Adds a patient to an EVR in the GUI
     * @param patientName: the name of the patient
     * @param room: the number of the EVR
     */
    public static void enterEVR(String patientName, int room) {
        if (room == 1) {
            EVR1_TextArea.append(patientName + ";");
        }else if(room==2) {
            EVR2_TextArea.append(patientName + ";");
        }else if(room==3) {
            EVR3_TextArea.append(patientName+";");
        }else if(room==4) {
            EVR4_TextArea.append(patientName+";");
        }
    }
    /**
     * Removes a patient from an EVR in the GUI
     * @param patientName: the name of the patient
     * @param room: the number of the EVR
     */
    public static void leaveEVR(String patientName, int room) {
        patientName = patientName.split(";")[0];
        if (room==1) {
            String text = "";
            text = EVR1_TextArea.getText();
            String[] splittedText = text.split(";");

            String updatedText = "";
            for (String s : splittedText) {
                if (!s.equals(patientName.substring(0,3)))
                    updatedText += s + ";";
            }
            EVR1_TextArea.setText(updatedText);
        }
        else if(room==2){
            String text = "";
            text = EVR2_TextArea.getText();
            String[] splittedText = text.split(";");

            String updatedText = "";
            for (String s : splittedText) {
                if (!s.equals(patientName.substring(0,3)))
                    updatedText += s + ";";
            }
            EVR2_TextArea.setText(updatedText);
        }
        else if(room==3){
            String text = "";
            text = EVR3_TextArea.getText();
            String[] splittedText = text.split(";");

            String updatedText = "";
            for (String s : splittedText) {
                if (!s.equals(patientName.substring(0,3)))
                    updatedText += s + ";";
            }
            EVR3_TextArea.setText(updatedText);
        }
        else if(room==4){
            String text = "";
            text = EVR4_TextArea.getText();
            String[] splittedText = text.split(";");

            String updatedText = "";
            for (String s : splittedText) {
                if (!s.equals(patientName.substring(0,3)))
                    updatedText += s + ";";
            }
            EVR4_TextArea.setText(updatedText);
        }
    }
    /**
     * Adds a patient to the WTR1 in the GUI
     * @param patientName: the name of the patient
     */
    public static void enterWTR1(String patientName) {
        patientName = patientName.split(";")[0];
        WTR1_TextArea.append(patientName+";");
    }
    /**
     * Removes a patient from the WTR1 in the GUI
     * @param patientName: the name of the patient
     */
    public static void leaveWTR1(String patientName) {
        patientName = patientName.split(";")[0];
        String text = "";
        text = WTR1_TextArea.getText();
        String[] splittedText = text.split(";");

        String updatedText = "";
        for (String s : splittedText) {
            if (!s.equals(patientName))
                updatedText += s + ";";
        }
        WTR1_TextArea.setText(updatedText);
    }
    /**
     * Adds a patient to the WTR2 in the GUI
     * @param patientName: the name of the patient
     */
    public static void enterWTR2(String patientName) {
        patientName = patientName.split(";")[0];
        WTR2_TextArea.append(patientName+";");
    }
    /**
     * Removes a patient from the WTR2 in the GUI
     * @param patientName: the name of the patient
     */
    public static void leaveWTR2(String patientName) {
        patientName = patientName.split(";")[0];
        String text = "";
        text = WTR2_TextArea.getText();
        String[] splittedText = text.split(";");

        String updatedText = "";
        for (String s : splittedText) {
            if (!s.equals(patientName))
                updatedText += s + ";";
        }

        WTR2_TextArea.setText(updatedText);
    }
    /**
     * Adds a patient to the MDW in the GUI
     * @param patientName: the name of the patient
     */
    public static void enterMDW(String patientName) {
        patientName = patientName.split(";")[0];
        MDW_TextArea.append(patientName+";");
    }
    /**
     * Adds a patient to an MDR in the GUI
     * @param patientName: the name of the patient
     * @param room: the number of the room
     */
    public static void enterMDR(String patientName, int room) {
        patientName = patientName.split(";")[0];
        String text = MDW_TextArea.getText();

        String[] splittedText = text.split(";");

        String updatedText = "";
        for (String s : splittedText) {
            if (!s.equals(patientName))
                updatedText += s + ";";
        }

        MDW_TextArea.setText(updatedText);

        if (room==1)
            MDR1_TextArea.append(patientName + ";");
        else if(room==2)
            MDR2_TextArea.append(patientName+";");
        else if(room==3)
            MDR3_TextArea.append(patientName+";");
        else if(room==4)
            MDR4_TextArea.append(patientName+";");
    }
    /**
     * Removes a patient from and MDR in the GUI
     * @param patientName: the name of the patient
     * @param room: the number of the room
     */
    public static void leaveMDR(String patientName, int room) {
        patientName = patientName.split(";")[0];

        JTextArea mdr = new JTextArea();
        if (room==1)
            mdr = MDR1_TextArea;
        else if(room==2)
            mdr = MDR2_TextArea;
        else if(room==3)
            mdr = MDR3_TextArea;
        else if(room==4)
            mdr = MDR4_TextArea;

        String text = "";
        text = mdr.getText();
        String[] splittedText = text.split(";");

        String updatedText = "";
        for (String s : splittedText) {
            if (!s.equals(patientName))
                updatedText += s + ";";
        }
        mdr.setText(updatedText);
    }
    /**
     * Adds a patient to the PYH in the GUI
     * @param patientName: the name of the patient
     */
    public static void enterPYH(String patientName) {
        PYeH_TextArea.append(patientName+";");
    }
    /**
     * Adds a patient to the Out HCP in the GUI
     * @param patientName: the name of the patient
     */
    public static void enterOut(String patientName) {
        out_TextArea.append(patientName+";");
    }


    /**
     * Removes a patient from the PYH in the GUI
     * @param patientName: the name of the patient
     */
    public static void leavePy(String patientName) {
        patientName = patientName.split(";")[0];
        String text = PYeH_TextArea.getText();
        String[] splittedText = text.split(";");

        String updatedText = "";
        for (String s : splittedText) {
            if (!s.equals(patientName))
                updatedText += s + ";";
        }

        PYeH_TextArea.setText(updatedText);
    }
}
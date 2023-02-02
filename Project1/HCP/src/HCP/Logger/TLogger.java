package HCP.Logger;

import HCP.Communication.ClientHandler;
import HCP.Logger.ILogger;
import HCP.Monitors.INurse;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;


/**
 * TLogger Entity Thread Class. Logs data into the console and logs.txt file
 */
public class TLogger extends Thread {
    /**
     * Logger Monitor Interface
     */
    private final ILogger logger;
    /**
     * Boolean flag for suspending process
     */
    private boolean threadSuspended;
    /**
     * Array of Log Header columns
     */
    private final String[] divisions = {"STT","ETH","ET1","ET2","EVR1","EVR2","EVR3","EVR4", "WTH","WTR1","WTR2","MDH","MDR1","MDR2","MDR3","MDR4","PYH","OUT"};
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * Log output file
     */
    private FileWriter file;

    /**
     * <b>Class Constructor</b>
     * <p>threadSuspended and stopFlag initialized as False</p>
     * @param logger: Interface  for the Logger Monitor
     * @param filename: Filename where to write logs
     */
    public TLogger(ILogger logger, String filename) {
        this.logger = logger;
        this.threadSuspended = false;
        this.stopFlag = false;
        try {
            this.file = new FileWriter(Paths.get("").toAbsolutePath().toString()+"\\src\\HCP\\Logger\\"+filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * <p>threadSuspended flag set to true, Logger waits for it to be false again</p>
     */
    public synchronized void suspendProcess(){
        this.threadSuspended = true;
    }
    /**
     * <p>threadSuspended flag set to false, suspended logger is notified and resumes process</p>
     */
    public synchronized void resumeProcess(){
        this.threadSuspended = false;
        notify();
    }

    /**
     * <p>stopFlag flag set to true, process ends</p>
     */
    public void stopProcess() {
        this.stopFlag = true;
    }

    /**
     * @param division the division where the logged action happened
     * @return index in divisions array where the input division string occurs
     */
    public int indexOf(String division){
        for (int i = 0; i<this.divisions.length;i++)
            if (this.divisions[i].equals(division)) return i;
        return -1;
    }

    /**
     *<p>Run thread method</p>
     *<p> The logger waits for a new log </p>
     *<p> If the message is "HEADER", then it writes the log header columns (divisions array) </p>
     *<p> If the message contaisn "@", then it represents an action and its placement in the log columns. </p>
     *<p> Example: </p>
     *<p> <i>MAN@STT -> Status changed to manual and the column STT is filled with MAN</i></p>
     *<p> <i>A01B@MDR1 -> Patient adult with id 01 and Blue DOS at Medical Room 1</i></p>
     */
    @Override
    public void run() {
        try {
            while (!this.stopFlag){
                synchronized(this) {
                    while (threadSuspended)
                        wait();
                }

                String message = ((ILogger)this.logger).waitForLog();
                String toPrint = "";
                if (message.equals("HEADER")) {
                    toPrint = (String.format("%6s | %6s %6s %6s | %6s %6s %6s %6s | %6s %6s %6s | %6s %6s %6s %6s %6s | %6s | %6s", "STT", "ETH", "ET1", "ET2", "EVR1", "EVR2", "EVR3", "EVR4", "WTH", "WTR1", "WTR2", "MDH", "MDR1", "MDR2", "MDR3", "MDR4", "PYH","OUT"));
                }else if (message.contains("@")){
                    int index = indexOf(message.split("@")[1]);
                    String data = message.split("@")[0];
                    StringBuilder sb = new StringBuilder("");
                    for (int i = 0; i<this.divisions.length;i++){
                        if (i == index) sb.append(String.format("%6s ", data));
                        else sb.append(String.format("%6s ", ""));
                        if (i == 0 || i == 3 || i == 7 || i==10 || i==15 || i==16)   sb.append("| ");
                    }
                    if (index == 0)
                        ClientHandler.addLogEntry("Changed Mode: "+data);
                    else
                        ClientHandler.addLogEntry(data +" Moved to "+divisions[index]);
                    toPrint = sb.toString();

                }else{
                    toPrint = message;
                }
                file.write(toPrint+"\n");
                if (toPrint.length()>0) {
                    System.out.println(toPrint);
                }


            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

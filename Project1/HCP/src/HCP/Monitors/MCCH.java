package HCP.Monitors;

import HCP.Communication.ClientHandler;
import HCP.Entities.*;
import HCP.Enums.SIGNAL;
import HCP.Logger.MLogger;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * <p> Call Centre Hall Monitor</p>
    <p>Hall where entity responsible for managing the movement of all Patients is</p>
 */

public class MCCH implements IPatient_CCH, ICallCentre_CCH {
    /**
     * List of signals relative to other patients movement
     * Example: LEFT_ETH, LEFT_EVH, LEFT_WTH_CHILD...
     */
    private MessageList signalList;
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Condition which warns the call centre of a new signal in the signalList
     */
    private final Condition awaken;
    /**
     * Condition which allows one patient to move
     */
    private final Condition allowOne;
    /**
     * Flag which determines if the patient's movement is automatic or manual
     */
    private boolean automatic;
    /**
     * <p>Initialized the monitor class and all its variables</p>
     * <p>Automatic flag is set to true by default</p>
     * <p>Adds initial signals because when simulation starts every room is empty, and patients should be able to fill them</p>
     */
    public MCCH(int NoS){
        this.rl = new ReentrantLock();
        this.awaken = rl.newCondition();
        this.allowOne = rl.newCondition();
        this.signalList = new MessageList();
        this.automatic = true;

        for (int k = 0; k<2*NoS; k++)
            signalList.push(SIGNAL.LEFT_EVH);


        for (int k = 0; k<NoS/2; k++) {
            signalList.push(SIGNAL.LEFT_WTH_CHILD);
            signalList.push(SIGNAL.LEFT_WTH_ADULT);
        }

        signalList.push(SIGNAL.LEFT_MDH_CHILD);
        signalList.push(SIGNAL.LEFT_MDH_ADULT);


        signalList.push(SIGNAL.LEFT_MDR_CHILD);
        signalList.push(SIGNAL.LEFT_MDR_ADULT);
        signalList.push(SIGNAL.LEFT_MDR_CHILD);
        signalList.push(SIGNAL.LEFT_MDR_ADULT);


    }
    /**
     * <p>Change the mode of the operation and the automatic flag value</p>
     * <p>Defines if the call centre should automatically run the simulation or wait for the user's input to run the next signal</p>
     * <p> If the process was already automatic, then the signaling will be pointless. If not, the CCH is signaled to resume all its process automatically</p>
     * @param b: defines the automatic flag boolean value
     */
    public void setAutomatic(Boolean b){
        try{
            rl.lock();
            this.automatic= b;
            this.awaken.signalAll();
            this.allowOne.signalAll();
        }finally {
            rl.unlock();
        }
    }

    /**
     * <p>The Call centre waits for a new signal to be added to the signalList MessageList, then retrieves it and removes it from the MessageList</p>
     * <p> If the process is not automatic, it awaits for the user input to allow the movement of one patient.
     * Furthermore, if the last allowed movement did not work for any reason, the call centre keeps retrieving signals until one initiates a patient's movement</p>
     * @param didLastWork: boolean flag which indicates if the last retrieved command produced successful patient movement
     * @return SIGNAL signal at the front of the MessageList
     */
    @Override
    public  SIGNAL waitForSignal(boolean didLastWork){
        SIGNAL returnSignal = null;
        try {
            this.rl.lock();
            while(this.signalList.isEmpty()) {
                try {
                    this.awaken.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!this.automatic && didLastWork) {
                try {
                    this.allowOne.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            returnSignal = (SIGNAL) this.signalList.pop();
        }
        finally {
            rl.unlock();
        }
        return returnSignal;
    }

    /**
     * <p>The Call centre re-adds a SIGNAL to the end of the MessageList</p>
     * @param signal: new SIGNAL to be added
     */
    @Override
    public void addSignal(SIGNAL signal) {
        try {
            this.rl.lock();
            signalList.push(signal);
        }finally {
            rl.unlock();
        }
    }


    /**
     * <p>The Patient adds a SIGNAL to the end of the MessageList and awakens the Call Centre</p>
     * @param signal: new SIGNAL to be added
     */
    @Override
    public void signalCch(SIGNAL signal) {
        try {
            this.rl.lock();
            signalList.push(signal);
            this.awaken.signalAll();
        }finally {
            rl.unlock();
        }

    }
    /**
     * <p>Allows for the movement of one patient once</p>
     * <p> Used in manual mode</p>
     */
    public void allowOneMovement() {
        try {
            this.rl.lock();
            ClientHandler.addLogEntry("-----Issued One Call Centre Call-----");
            this.allowOne.signalAll();
        }finally {
            rl.unlock();
        }

    }

    /**
     * <p>Not implemented</p>
     * @param p: Patient
     * @return null
     */
    @Override
    public Patient join(Patient p) {
        return null;
    }
    /**
     * <p>Not implemented</p>
     * @param p: Patient
     * @return 0
     */
    @Override
    public int leave(Patient p) {
        return 0;
    }



}

package HCP.Entities;

import HCP.Monitors.INurse;

import java.util.Random;

/**
 * Nurse Thread Class. Entity responsible for giving the DoS to the patient
 */
public class TNurse extends Thread {
    /**
     * MDH Monitor Doctor Interfaces
     */
    private final INurse evh;
    /**
     * Max evaluation time value
     */
    private final int maxEvTime;
    /**
     * Boolean flag for suspending process
     */
    private boolean threadSuspended;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;

    /**
     * <p>threadSuspended and stopFlag initialized as False</p>
     * @param evh: Interface  for the EVH Monitor
     * @param evt: Maximum evaluation time value
     */
    public TNurse(INurse evh, int evt) {
        this.evh = evh;
        this.maxEvTime = evt;
        this.threadSuspended = false;
        this.stopFlag = false;
    }
    /**
     * <p>threadSuspended flag set to true, Nurse waits for it to be false again</p>
     */
    public synchronized void suspendProcess(){
        this.threadSuspended = true;
    }
    /**
     * <p>threadSuspended flag set to false, suspended nurse is notified and resumes</p>
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

    @Override
    public void run() {
        try {

            int roomNo = ((INurse)this.evh).assignToRoom();
            while (!this.stopFlag){
                synchronized(this) {
                    while (threadSuspended)
                        wait();
                }
                ((INurse)this.evh).waitPatientEvaluation(roomNo, maxEvTime);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

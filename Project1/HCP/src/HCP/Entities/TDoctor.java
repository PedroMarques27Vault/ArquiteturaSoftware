package HCP.Entities;

import HCP.Monitors.IDoctor;
import HCP.Monitors.INurse;

import java.util.Random;

// Doctor
// Medical appointment in MDH, random time interval
/**
 * Doctor Entity Thread Class. Conducts the Medical Appointment in the MDH
 */
public class TDoctor extends Thread{
    /**
     * MDH Monitor Doctor Interfaces
     */
    private final IDoctor mdh;
    /**
     * Maximum medical appointment time duration
     */
    private final int maxMdTime;
    /**
     * Boolean flag for suspending process
     */
    private boolean threadSuspended;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;

    /**
     * <b>Class Constructor</b>
     * <p>threadSuspended and stopFlag initialized as False</p>
     * @param mdh: Interface  for the MDH Monitor
     * @param mdt: Maximum medical appointment time value
     */
    public TDoctor(IDoctor mdh, int mdt) {
        this.mdh = mdh;
        this.maxMdTime = mdt;
        this.threadSuspended = false;
        this.stopFlag = false;
    }

    /**
     * <p>threadSuspended flag set to true, Doctor waits for it to be false again</p>
     */
    public synchronized void suspendProcess(){
        this.threadSuspended = true;
    }
    /**
     * <p>threadSuspended flag set to false, suspended doctor is notified and resumes</p>
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
     * <p>Run thread method</p>
     *<p> The doctor is assigned one of the 4 available medical rooms </p>
     *<p> The doctor waits for a new patient to conduct examination assigned one of the 4 available medical rooms </p>
     */
    @Override
    public void run() {
        try {
            int roomNo = ((IDoctor)this.mdh).assignRoom();
            while (!this.stopFlag){
                synchronized(this) {
                    while (threadSuspended)
                        wait();
                }
                ((IDoctor)this.mdh).examination(roomNo, maxMdTime);
            }

        } catch (Exception e) { }

    }

}

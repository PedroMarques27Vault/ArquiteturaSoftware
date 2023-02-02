package HCP.Entities;

import HCP.Monitors.ICashier;

/**
 * Cashier Entity Thread Class. Conducts Payment in the PYH
 */
public class TCashier extends Thread{
    /**
     * Payment Hall monitor Interface
     */
    private final ICashier pyh;
    /**
     * Maximum Payment time duration
     */
    private final int maxPyTime;
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
     * @param pyh: Interface  for the MPY Monitor
     * @param mpt: Maximum payment time value
     */
    public TCashier(ICashier pyh, int mpt) {
        this.pyh = pyh;
        this.maxPyTime = mpt;
        this.threadSuspended = false;
        this.stopFlag = false;
    }

    /**
     * <p>threadSuspended flag set to true, Cashier waits for it to be false again</p>
     */
    public synchronized void suspendProcess(){
        this.threadSuspended = true;
    }
    /**
     * <p>threadSuspended flag set to false, suspended cashier is notified and resumes</p>
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
     *<p>
     * Cashier keeps waiting for new patients to pay
     * </p>
     */
    @Override
    public void run() {
        try {
            while (!this.stopFlag){
                synchronized(this) {
                    while (threadSuspended)
                        wait();
                }
                ((ICashier)this.pyh).receivePayment(maxPyTime);
            }
        } catch (Exception e) { }
    }

}




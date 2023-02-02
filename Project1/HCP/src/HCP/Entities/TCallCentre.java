package HCP.Entities;

import HCP.Enums.AGE;
import HCP.Enums.SIGNAL;
import HCP.Monitors.*;

/**
 * CallCentre Entity Thread Class. Controls the Patient's Movements
 */
public class TCallCentre extends  Thread{
    /**
     * All Monitor Call Center Interfaces -> Includes CCH, ETH, WTH and MDH
     */
    private final ICallCentre cch, eth, wth, mdh;

    /**
     * Boolean flag for suspending process
     */
    private boolean threadSuspended;

    /**
     * Boolean Flag for stopping process
     */
    private boolean stopFlag;

    /**
     * <b>Class Constructor</b>
     * <p>threadSuspended and stopFlag initialized as False</p>
     * @param cch: Interface  for the CCH Monitor
     * @param eth: Interface  for the ETH Monitor
     * @param wth: Interface  for the WTH Monitor
     * @param mdh: Interface  for the MDH Monitor
     */
    public TCallCentre(ICallCentre_CCH cch, ICallCentre_ETH eth, ICallCentre_WTH wth, ICallCentre_MDH mdh) {
        this.cch = cch;
        this.eth = eth;
        this.wth = wth;
        this.mdh = mdh;
        this.threadSuspended = false;
        this.stopFlag = false;
    }

    /**
     * <p>threadSuspended flag set to true, CCH waits for it to be false again</p>
     */
    public synchronized void suspendProcess(){
        this.threadSuspended = true;
    }
    /**
     * <p>threadSuspended flag set to false, suspended call centre is notified and resumes process</p>
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
     * While the process is running, the Call Centre keeps the simulation
     * running by receiving new SIGNAl signals.
     * If the result of the action produced by the received signal is unsatisfatory,
     * this last SIGNAL signal is added to the end of the message list in the CCH
     * </p>
     * <p> Unknown Signal launches an Error</p>
     */
    @Override
    public void run() {
        try {
            boolean lastWorked = true;
            while(!this.stopFlag){
                synchronized(this) {
                    while (threadSuspended)
                        wait();
                }
                SIGNAL nextSignal = ((ICallCentre_CCH)this.cch).waitForSignal(lastWorked);
                lastWorked = false;

                if (nextSignal == SIGNAL.LEFT_EVH){
                    if (((ICallCentre_ETH) this.eth).allowLeave() == -1) {
                        ((ICallCentre_CCH) this.cch).addSignal(SIGNAL.LEFT_EVH);
                    }else{
                        lastWorked = true;
                    }

                }else if(nextSignal == SIGNAL.LEFT_WTH_CHILD){
                    if (((ICallCentre_WTH) this.wth).allowJoin(0) == -1)
                        ((ICallCentre_CCH) this.cch).addSignal(nextSignal);
                    else{
                        lastWorked = true;
                    }

                }else if(nextSignal == SIGNAL.LEFT_WTH_ADULT){

                    if (((ICallCentre_WTH) this.wth).allowJoin(1) == -1)
                        ((ICallCentre_CCH) this.cch).addSignal(nextSignal);
                    else{
                        lastWorked = true;
                    }

                }else if(nextSignal == SIGNAL.LEFT_MDH_CHILD){
                    if (((ICallCentre_WTH) this.wth).allowLeave(0) == -1)
                        ((ICallCentre_CCH) this.cch).addSignal(nextSignal);
                    else{
                        lastWorked = true;
                    }

                }else if(nextSignal == SIGNAL.LEFT_MDH_ADULT){
                    if (((ICallCentre_WTH) this.wth).allowLeave(1) == -1)
                        ((ICallCentre_CCH) this.cch).addSignal(nextSignal);
                    else{
                        lastWorked = true;
                    }

                }else if(nextSignal == SIGNAL.LEFT_MDR_CHILD){
                    if (((ICallCentre_MDH) this.mdh).allowJoin(AGE.CHILD) == -1)
                        ((ICallCentre_CCH) this.cch).addSignal(nextSignal);
                    else{
                        lastWorked = true;
                    }

                }else if(nextSignal == SIGNAL.LEFT_MDR_ADULT){
                    if (((ICallCentre_MDH) this.mdh).allowJoin(AGE.ADULT) == -1)
                        ((ICallCentre_CCH) this.cch).addSignal(nextSignal);
                    else{
                        lastWorked = true;
                    }

                }else{
                    throw new Exception("Unknown signal received by the Call Centre");
                }
            }
            //System.out.println("Producer Broken");
        } catch (Exception e) {

        }
    }

}

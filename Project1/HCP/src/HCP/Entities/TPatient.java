package HCP.Entities;

import HCP.Enums.AGE;
import HCP.Enums.HALL;
import HCP.Enums.SIGNAL;
import HCP.Monitors.*;
/**
 * Patient Entity Thread Class. moves through the HCP halls
 */
public class TPatient extends Thread {
    /**
     * Patient Object saves user's data
     */
    private Patient patient;
    /**
     * All Patient's Monitors Interfaces -> Includes CCH, ETH, EVH, WTH, MDH, PYH
     */
    private final IPatient cch,eth,evh, wth, mdh,pyh;
    /**
     * Max time to move time value
     */
    private int ttm;
    /**
     * Next hall the patient should move to
     */
    private HALL nextHall;
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
     * <p>nextHall initialized as ETH</p>
     * @param age: Patient's age (AGE)
     * @param cch: Interface  for the CCH Monitor
     * @param eth: Interface  for the ETH Monitor
     * @param evh: Interface  for the EVH Monitor
     * @param wth: Interface  for the WTH Monitor
     * @param mdh: Interface  for the MDH Monitor
     * @param pyh: Interface  for the PYH Monitor
     * @param ttm: maximum time to move between halls value
     */
    public TPatient(AGE age, IPatient_CCH cch, IPatient_ETH eth, IPatient_EVH evh, IPatient_WTH wth, IPatient_MDH mdh,IPatient pyh, int ttm) {
        this.patient =  new Patient(age);
        this.cch = cch;
        this.eth = eth;
        this.evh = evh;
        this.wth = wth;
        this.mdh = mdh;
        this.pyh = pyh;
        this.ttm = ttm;
        this.nextHall = HALL.ETH;
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
     *<p> The patient moves through each hall </p>
     *<p> nextHall changes after each iteration to determine where the patient goes next </p>
     *<p> After each iteration, the thread sleeps for at most TTM time </p>
     */
    @Override
    public void run() {
        try {
            while(!this.stopFlag){
                synchronized(this) {
                    while (threadSuspended)
                        wait();
                }
                switch(nextHall){
                    case ETH:{
                        this.patient = (((IPatient_ETH)this.eth).join(this.patient));
                        ((IPatient_ETH)this.eth).leave(this.patient);
                        this.nextHall = HALL.EVH;
                        break;
                    }
                    case EVH:{
                        this.patient = (((IPatient_EVH)this.evh).join(this.patient));
                        ((IPatient_EVH)this.evh).leave(this.patient);
                        ((IPatient_CCH)this.cch).signalCch(SIGNAL.LEFT_EVH);
                        this.nextHall = HALL.WTH;
                        //System.out.println("Left EVH "+this.patient);
                        break;
                    }
                    case WTH:{
                        this.patient = (((IPatient_WTH)this.wth).join(this.patient));
                        ((IPatient_WTH)this.wth).leave(this.patient);
                        if (this.patient.getAge() == AGE.ADULT) ((IPatient_CCH)this.cch).signalCch(SIGNAL.LEFT_WTH_ADULT);
                        else ((IPatient_CCH)this.cch).signalCch(SIGNAL.LEFT_WTH_CHILD);

                        this.nextHall = HALL.MDH;
                        break;
                    }
                    case MDH:{
                        this.patient = (((IPatient_MDH)this.mdh).join(this.patient));
                        if (this.patient.getAge() == AGE.ADULT) ((IPatient_CCH)this.cch).signalCch(SIGNAL.LEFT_MDH_ADULT);
                        else ((IPatient_CCH)this.cch).signalCch(SIGNAL.LEFT_MDH_CHILD);

                        ((IPatient_MDH)this.mdh).joinMedicalAppointment(this.patient);
                        //System.out.println(" + + + + + Examination Complete "+this.patient);
                        ((IPatient_MDH)this.mdh).leave(this.patient);
                        //System.out.println(" - - - - - - Left MDR "+this.patient);
                        if (this.patient.getAge() == AGE.ADULT) ((IPatient_CCH)this.cch).signalCch(SIGNAL.LEFT_MDR_ADULT);
                        else ((IPatient_CCH)this.cch).signalCch(SIGNAL.LEFT_MDR_CHILD);

                        this.nextHall = HALL.PYH;
                        break;
                    }case PYH:{
                        ((IPatient)this.pyh).join(this.patient);
                        ((IPatient)this.pyh).leave(this.patient);
                        return;
                    }

                }
                int tsleep = (int)(Math.random() * ((ttm) + 1));
                try {
                    Thread.sleep(tsleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
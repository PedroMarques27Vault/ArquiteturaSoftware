package HCP.Monitors;

import HCP.Communication.ClientHandler;
import HCP.Entities.Patient;
import HCP.Entities.Room;
import HCP.Logger.MLogger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Payment Hall Monitor
 * <p>- The PYH comprises:</p>
 * <p>  o An entrance hall where patients can wait</p>
 * <p>  o A cashier where 1 patient pays his medical appointment</p>
 * <p>  o Patients pay by their order of arrival at the PYH (PYN)</p>
 * <p>  o After having payed the bill, patients leave the hospital</p>
 * <p>  o Each payment may take a variable period of time (PYT)</p>
 */
public class MPYH implements IPatient, ICashier{
    /**
     * Logger monitor reference
     */
    private final MLogger mlogger;
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Condition which signals the next client that the cashier is ready to receive payment
     */
    private final Condition nextClient;
    /**
     * Condition which signals the cashier that a patient is waiting to pay
     */
    private final Condition newClient;
    /**
     * Conditions which signals client that the payment is completed and he can leave
     */
    private final Condition canLeave;
    /**
     * Occupied when someone is paying
     */
    private final Room register;

    public MPYH(MLogger mlogger){
        this.mlogger = mlogger;
        this.rl  = new ReentrantLock();
        this.nextClient = rl.newCondition();
        this.newClient = rl.newCondition();
        this.canLeave = rl.newCondition();
        this.register = new Room(1);
    }
    /**
     * <p>Patient gets in the payment queue and awaits for its time to pay</p>
     * <p>If there's someone waiting it awaits for its turn</p>
     * <p>When it's his turn, signals the cashier</p>
     * @param p: patient waiting to pay
     * @return the same patient
     */
    @Override
    public Patient join(Patient p) {
        try{
            rl.lock();
            while(this.register.isFull()){
                try {
                    this.nextClient.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.register.addAtIndex(0,p);
            ClientHandler.enterPYH(p.toString());
            this.mlogger.writeLog(p.toString()+"@PYH");
            this.newClient.signal();

        }finally {
        rl.unlock();
    }
        return p;
    }
    /**
     * <p>Patient waits for the payment to finish to leave</p>
     * <p>When payment is complete, signals the cashier that it is leaving</p>
     * <p>Signals the next patient that it is its turn</p>
     * @param p: patient waiting to pay
     * @return 0
     */
    @Override
    public int leave(Patient p) {
        try{
            rl.lock();
            if (this.register.isFull()){
                this.register.pop();
            }
            try {
                this.canLeave.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.nextClient.signal();
            ClientHandler.leavePy(p.toString());
            this.mlogger.writeLog(p.toString()+"@OUT");
            ClientHandler.enterOut(p.toString());


        }finally {
            rl.unlock();
        }
        return 0;
    }
    /**
     * <p>Cashier waits for a patient to pay</p>
     * <p>When a patient enters, the cashier proceeds with the payment within at most maxPyTime ms</p>
     * <p>After the payment is complete the cashier waits for the patient to leave</p>
     * @param maxPyTime: maximum duration of payment process (ms)
     */
    @Override
    public void receivePayment(int maxPyTime) {
        try{
            rl.lock();
            if(this.register.isEmpty()){
                try {
                    this.newClient.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int tsleep = (int)(Math.random() * ((maxPyTime) + 1));
            try {
                Thread.sleep(tsleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.canLeave.signalAll();

        }finally {
            rl.unlock();
        }



    }
}

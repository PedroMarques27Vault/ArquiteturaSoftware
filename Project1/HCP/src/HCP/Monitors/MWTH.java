
package HCP.Monitors;

import HCP.Communication.ClientHandler;
import HCP.Entities.*;
import HCP.Enums.AGE;
import HCP.Logger.MLogger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Payment Hall Monitor
 * <p>The WTH is the first Hall where patients wait for their medical appointment:</p>
 * <p>  - The WTH comprises two rooms: WTR1 for children and WTR2 for adults</p>
 * <p>  - Each WTRi has a defined number of seats (NoS/2)</p>
 * <p>  - Upon arriving at the WTH, each patient is given a unique incremental sequential number (WTN)</p>
 * <p>  - In case there are no available seats, the patient must wait. Otherwise, he proceeds to his corresponding WTRi</p>
 * <p>  - Upon a call from the Call Centre, the patient with higher priority proceeds to the MDH</p>
 * <p>  - Priority policy for patients' medical appointments (there is no distinction between adults and children):</p>
 * <p>      o Patients with higher DoS have higher priority</p>
 * <p>      o Patients with the same DoS are ordered by their WTN</p>
 */
public class MWTH implements IPatient_WTH, ICallCentre_WTH {
    /**
     * Logger monitor reference
     */
    private final MLogger mlogger;
    /**
     * Array of 2 waiting rooms
     * <p>Each room has NoS/2 seats</p>
     * <p>Index 0 => CHILD Patients</p>
     * <p>Index 1 => ADULT Patients</p>
     */
    private Room[] waitRooms;
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Incremental waiting number counter
     */
    private int wtNumber;
    /**
     * Condition which signals when a Child can leave the WTH and proceed to the next Hall
     */
    private Condition canLeaveChild;
    /**
     * Condition which signals when an Adult can leave the WTH and proceed to the next Hall
     */
    private Condition  canLeaveAdult;

    /**
     * Condition which signals when a Child can join the Waiting Room (index 0 of waitRooms)
     */
    private Condition   spaceAvailableChild;
    /**
     * Condition which signals when an Adult can join the Waiting Room (index 1 of waitRooms)
     */
    private Condition    spaceAvailableAdult;
    /**
     * Array of 2 Conditions (for each room) which signal when a patient enters a room
     */
    private Condition[] joined;
    /**
     * Array of 2 Patients (for each room) which indicate the next patient who should leave the room
     */
    private Patient[] nextPatientToLeave;
    /**
     * Initialized the WTH Monitor
     * @param NoS: number of seats
     * @param mlogger: reference to the logger monitor
     */
    public MWTH(int NoS, MLogger mlogger){
        this.mlogger = mlogger;
        this.rl = new ReentrantLock();

        this.spaceAvailableChild = rl.newCondition();
        this.spaceAvailableAdult = rl.newCondition();

        this.canLeaveChild = rl.newCondition();
        this.canLeaveAdult = rl.newCondition();

        this.joined = new Condition[2];

        this.wtNumber = 0;

        this.waitRooms = new Room[2];
        this.nextPatientToLeave = new Patient[2];
        for (int k =0; k<2;k++) {
            this.waitRooms[k] = new Room(NoS / 2);
            this.nextPatientToLeave[k] = null;
            this.joined[k] = rl.newCondition();
        }

    }

    /**
     * <p>Patient gets a new id number, the waiting number (wtn)</p>
     * <p>The patient waits for a seat available in its waiting room. Room 0 for CHILD patients and Room 1 for ADULT patients</p>
     * @param p: patient trying to join the waiting room
     * @return the given patient with the new waiting number
     */
    @Override
    public Patient join(Patient p) {
        try{
            rl.lock();
            p.setPatientWtn(this.wtNumber);
            this.wtNumber++;

            this.mlogger.writeLog(p.toString()+"@WTH");
            if (p.getAge() == AGE.CHILD){
                while(this.waitRooms[0].isFull()){
                    try {
                        this.spaceAvailableChild.await();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.waitRooms[0].add(p);
                ClientHandler.enterWTR1(p.toString());
                this.mlogger.writeLog(p.toString()+"@WTR1");
            }else{
                while(this.waitRooms[1].isFull()){
                    try {
                        this.spaceAvailableAdult.await();             //Wait for spaceAvailable adult
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.waitRooms[1].add(p);
                ClientHandler.enterWTR2(p.toString());
                this.mlogger.writeLog(p.toString()+"@WTR2");
            }

        }finally {
            rl.unlock();
        }
        return p;
    }



    /**
     * <p>Call centre allows for one patient to leave its room and proceed to the next hall</p>
     * <p>The patient chosen to leave is the one with the biggest DoS and smallest Wtn</p>
     * @param room: number of the room to allow leave, 0 indicates CHILD and 1 means ADULT
     * @return -1 if no patient leaves, 0 otherwise
     */
    @Override
    public int allowLeave(int room) {
        int result = -1;
        try{
            rl.lock();
            if (room == 0 && rl.hasWaiters(this.canLeaveChild)){
                this.nextPatientToLeave[room] = this.waitRooms[room].getMaxDoSMinWtn();
                this.canLeaveChild.signalAll();


                result = 0;
            }else if(room == 1 && rl.hasWaiters(this.canLeaveAdult)){

                this.nextPatientToLeave[room] = this.waitRooms[room].getMaxDoSMinWtn();
                this.canLeaveAdult.signalAll();

                result = 0;
            }
        }finally {
            rl.unlock();
        }
        return result;
    }
    /**
     * <p>Call centre allows for one patient to join a room</p>
     * <p>The longest waiting patient is the first to join</p>
     * @param room: number of the room to allow join, 0 indicates CHILD and 1 means ADULT
     * @return -1 if no patient leaves, 0 otherwise
     */
    @Override
    public int allowJoin(int room) {
        int result = -1;
        try{
            rl.lock();

            if (room == 0 && !this.waitRooms[0].isFull() && rl.hasWaiters(this.spaceAvailableChild)){
                this.spaceAvailableChild.signal();
                result = 0;
            }else if(room == 1 && !this.waitRooms[1].isFull()&& rl.hasWaiters(this.spaceAvailableAdult)){
                this.spaceAvailableAdult.signal();

                result = 0;
            }


        }finally {
            rl.unlock();
        }
        return result;
    }
    /**
     * <p>A given patient tries to leave and proceed to the next hall</p>
     * <p>If its not its turn (i.e. doesn't have the biggest DoS and minimal Id) it awaits</p>
     * <p>After being allowed leaves the room</p>
     * @param p: patient that is trying to leave
     * @return -1 if no patient leaves, 0 otherwise
     */

    @Override
    public int leave(Patient p) {
        try{
            rl.lock();
            if (p.getAge() == AGE.CHILD){
                while(this.nextPatientToLeave[0] == null || this.nextPatientToLeave[0].getPatientWtn() != p.getPatientWtn()){
                    try {
                        this.canLeaveChild.await();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.waitRooms[0].popById(p.getPatientId());
                ClientHandler.leaveWTR1(p.toString());
            }else{
                while(this.nextPatientToLeave[1] == null || this.nextPatientToLeave[1].getPatientWtn() != p.getPatientWtn()){

                    try {
                        this.canLeaveAdult.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.waitRooms[1].popById(p.getPatientId());
                ClientHandler.leaveWTR2(p.toString());
            }
        }finally {
            rl.unlock();
        }
        return 0;
    }
}

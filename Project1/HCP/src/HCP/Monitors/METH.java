package HCP.Monitors;


import HCP.Entities.Patient;
import HCP.Entities.Room;
import HCP.Enums.AGE;
import HCP.Logger.MLogger;
import HCP.Communication.ClientHandler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;

/**
 * <p> Entrance Hall Monitor</p>
 * <p> ETRoom1 for children and ETR2 for adults</p>
 * <p> Each ETRi has a defined number of seats (NoS/2)</p>
 * <p> Patients can only enter the ETH if there are seats available in their corresponding ETRi</p>
 * <p> Upon entering the ETH, each patient is given a unique incremental sequential number (ETN) and then he proceeds to his corresponding ETRi</p>
 * <p> Patients leave the ETH in ascending order of their ETN and proceed to the EVH upon a call from the Call Centre.</p>
 */
public class METH implements IPatient_ETH, ICallCentre_ETH {

    /**
     * Array of rooms in the Entrance Hall, etrs[0] is for Children, etrs[1] for adults
     */
    private Room[] etrs;
    /**
     * Number of Seats
     */
    private int NoS;
    /**
     * Counter of patients that have already left the ETH, Also Represents the ID of the next patient that can leave
     */
    private int leaveCount;
    /**
     * Counter of patients that have entered one of the rooms of the ETH
     */
    private int finishedLoading;
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Condition which indicates when there is space available in the CHILD Patients Room
     */
    private final Condition spaceAvailableChild;
    /**
     * Condition which indicates when there is space available in the ADULT Patients Room
     */
    private final Condition spaceAvailableAdult;
    /**
     * Condition which indicates when a Patient can leave the ETH
     */
    private final Condition canLeave;
    /**
     * Condition which indicates when a Patient successfully leaves the ETH
     */
    private final Condition leftEth;
    /**
     * Reference to the logger monitor
     */
    private final MLogger mlogger;

    /**
     * Incremental value which represents the patient's ids
     */
    private int idCount;

    /**
     * Generates the ETH monitor
     * @param NoS: number of seats. Each room has NoS/2 seats
     * @param mlogger: logger monitor reference
     */
    public METH(int NoS, MLogger mlogger){
        this.mlogger = mlogger;

        this.etrs = new Room[2];
        for (int k=0;k<2;k++) {
            this.etrs[k] = new Room(NoS / 2);
        }
        this.rl = new ReentrantLock();
        this.NoS = NoS;
        this.finishedLoading = 0;
        this.idCount = 0;
        this.leaveCount = -1;

        this.canLeave = rl.newCondition();
        this.leftEth = rl.newCondition();
        this.spaceAvailableChild = rl.newCondition();
        this.spaceAvailableAdult = rl.newCondition();


    }

    /**
     * <p>Patient p waits in the ETH for a seat in one of the rooms, according to its age</p>
     * <p>AGE.ADULT -> go to the room at index 1</p>
     * <p>AGE.CHILD -> go to the room at index 0</p>
     * @param p: Patient trying to join a room
     */
    @Override
    public Patient join(Patient p) {
        try{
            rl.lock();
            p.setPatientId(idCount);
            idCount++;
            ClientHandler.enterETH(p.toString());
            this.mlogger.writeLog(p.toString()+"@ETH");
            if (p.getAge() == AGE.CHILD){
                while(this.etrs[0].isFull()) {
                    try {
                        this.spaceAvailableChild.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.etrs[0].add(p);
                this.mlogger.writeLog(p.toString()+"@ET1");
                ClientHandler.enterETR1(p.toString());
            }else{
                while(this.etrs[1].isFull()) {
                    try {
                        this.spaceAvailableAdult.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.etrs[1].add(p);
                this.mlogger.writeLog(p.toString()+"@ET2");
                ClientHandler.enterETR2(p.toString());
            }
            this.finishedLoading++;

        }finally {
            rl.unlock();
        }
        return p;
    }

    /**
     * <p>Patient p waits for his turn to leave the room and move on</p>
     * <p>leaveCount represents the id of the current patient that is allowed to leave</p>
     * <p>When it's his turn, Patient p leaves their room and signals the CCH that they left the ETH</p>
     * @param p: Patient trying to leave
     * @return 0 if successful, -1 otherwise
     */
    @Override
    public int leave(Patient p) {
        try{
            rl.lock();

            while(this.leaveCount!=p.getPatientId()){
                try {
                    this.canLeave.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return -1;
                }
            }
            if (p.getAge() == AGE.CHILD){
                this.etrs[0].popById(p.getPatientId());
                this.spaceAvailableChild.signal();
                ClientHandler.leaveETR1(p.toString());
            }else{
                this.etrs[1].popById(p.getPatientId());
                this.spaceAvailableAdult.signal();
                ClientHandler.leaveETR2(p.toString());
            }
            this.leftEth.signalAll();
        }finally {
            rl.unlock();
        }
        return 0;
    }
    /**
     * <p>The CallCentre allows for one patient to leave its room and move on to the next hall</p>
     * <p>Defines the next patient who is allowed to leave and signals it</p>
     * <p>Proceeds to wait for this patient's signal that it has left the ETH</p>
     * <p>When it's his turn, Patient p leaves their room and signals the CCH that they left the ETH</p>
     * @return 0 if there's a patient waiting to leave and the leaveCount flag is not bigger than the number of patients that have joined the ETH, -1 otherwise
     */
    @Override
    public int allowLeave() {
        int result = -1;
        try{
            rl.lock();
            if (rl.hasWaiters(this.canLeave) ){
                this.leaveCount = this.getMinimumId();
                this.canLeave.signalAll();
                result = 0;

                try {
                    this.leftEth.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }finally {
            rl.unlock();
        }
        return result;
    }
    /**
     * <p>Retrieves the id of the patient with the lowest id in all the rooms</p>
     * <p>This patient is the next one to leave</p>
     * @return id of the lowest id patient
     */
    private int getMinimumId() {

        int minId1 = this.etrs[0].minimumId();
        int minId2 = this.etrs[1].minimumId();

        if (minId2<0){
            if (minId1>=0) return minId1;
            return 0;
        }else{
            if (minId1<0) return minId2;
            if (minId2<minId1) return  minId2;
            return minId1;
        }

    }


}

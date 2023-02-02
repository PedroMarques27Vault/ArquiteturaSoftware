package HCP.Monitors;

import HCP.Communication.ClientHandler;
import HCP.Entities.*;
import HCP.Logger.MLogger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Evaluation Hall Monitor
 <p>  Hall where the DoS is evaluated for each patient:</p>
 <p>- EVH comprises 4 rooms: EVR1,..., EVR4</p>
 <p>- In each EVRi there is one nurse</p>
 <p>- Each EVR can assess both children and adults, one at a time</p>
 <p>- Each patient is assigned a coloured bracelet corresponding to his DoS</p>
 <p>- Each evaluation may take a variable period of time (EVT)</p>
 <p>- Patients leave the EVH after the evaluation and proceed to the WTH</p>
 */
public class MEVH implements IPatient_EVH, INurse {
    /**
     * Logger monitor reference
     */
    private final MLogger mlogger;
    /**
     * Array of 4 evaluation rooms where the nurses work
     */
    private Room[] evRooms;
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Array of 4 conditions (for each room) which signal when a patient left an evaluation room
     */
    private final Condition[] left;
    /**
     * Array of 4 conditions (for each room) which signal when a nurse completed an evaluation in a room
     */
    private final Condition[] evaluationComplete;
    /**
     * Array of 4 conditions (for each room) which signal when a patient joined an evaluation room
     */
    private final Condition[] newPatients;
    /**
     * Number of nurses with assigned rooms
     */
    private int nurseCount;
    /**
     * Number of seats. Each room has NoS/2 seats
     */
    private final int NoS;
    /**
     * <p>All variables and conditions are initialized</p>
     * <p>4 rooms are created with number of seats = NoS/2</p>
     * @param NoS: number of seats
     * @param mlogger: logger monitor reference
     */
    public MEVH(int NoS, MLogger mlogger){
        this.mlogger = mlogger;
        this.rl = new ReentrantLock();
        this.left = new Condition[4];

        this.newPatients = new Condition[4];
        this.evaluationComplete = new Condition[4];

        this.NoS = NoS;

        for (int k = 0;k<4;k++){
            this.newPatients[k] = rl.newCondition();
            this.evaluationComplete[k] = rl.newCondition();
            this.left[k] = rl.newCondition();
        }

        this.evRooms = new Room[4];
        for (int k = 0;k<4;k++)
            this.evRooms[k] = new Room(NoS/2);
    }
    /**
     * <p>A given nurse is assigned to a room</p>
     * <p>4 rooms are created with number of seats = NoS/2</p>
     * @return the room the nurse is assigned to
     */
    @Override
    public int assignToRoom() {
        try {
            rl.lock();
            this.nurseCount++;

        }finally {
            rl.unlock();
        }
        return this.nurseCount - 1;
    }
    /**
     * <p>A given nurse waits in its room for a new patient to join</p>
     * <p>After a new patient has joined, conducts evaluation of a random period of time (at most maxEvt ms)</p>
     * <p>Gives a random Degree of Severity to the patient (1=Blue, 2=Yellow, 3=Red) and signals the evaluation as complete</p>
     * <p>Then proceeds to wait for the patient to leave</p>
     * @param roomNo: number of the room assigned to the nurse (index in the evRooms array)
     * @param maxEvt: maximum evaluation time duration in ms
     * @return the patient whose evaluation has been completed
     */
    @Override
    public Patient waitPatientEvaluation(int roomNo, int maxEvt) {
        Patient p = null;
        try {
            rl.lock();

            while(this.evRooms[roomNo].isEmpty()){
                try {
                    this.newPatients[roomNo].await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            p = this.evRooms[roomNo].getFirst();

            int tsleep = (int)(Math.random() * ((maxEvt) + 1));
            int dos = 1 + (int)(Math.random() * ((2) + 1));
            try {
                Thread.sleep(tsleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            p.setDoS(dos);
            this.evaluationComplete[roomNo].signalAll();

            try {
                this.left[roomNo].await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }finally {
            rl.unlock();
        }

        return p;
    }
    /**
     * <p>Gets the number of a room which is not full</p>
     * @return the index of the room in the evRooms array, -1 if all rooms are full
     */
    public int getAvailableRoom(){
        for (int k = 0;k<4;k++){
            if (!this.evRooms[k].isFull()) return k;
        }
        return -1;
    }
    /**
     * <p>A patient joins one evaluation room with available seats</p>
     * <p>Proceeds to signal the Nurse that it has entered the room</p>
     * <p>Waits for it's evaluation to be completed and it's DoS different than 0</p>
     * <p>Then proceeds to wait for the patient to leave</p>
     * @param p: Patient joining an evaluation room
     * @return the patient whose evaluation has been completed, with a changed DoS
     */
    @Override
    public Patient join(Patient p) {
        try {
            rl.lock();
            int roomNo = getAvailableRoom();
            this.evRooms[getAvailableRoom()].add(p);
            int x = roomNo+1;
            ClientHandler.enterEVR(p.toString(), x);
            this.mlogger.writeLog(p.toString()+"@EVR"+(roomNo+1));
            while(p.getDoS() == 0){
                this.newPatients[roomNo].signal();
                try {
                    this.evaluationComplete[roomNo].await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }finally {
            rl.unlock();
        }
        return p;
    }
    /**
     * <p>A patient tries to leave it's room and proceed to the next hall</p>
     * <p>The patient leaves the room and proceeds to signal the nurse that it has left</p>
     * @param p: Patient trying to leave
     * @return 0
     */
    @Override
    public int leave(Patient p) {
        try {
            rl.lock();

            int roomNo=0;
            for (int k = 0; k<4;k++)
                if (this.evRooms[k].contains(p.getPatientId())) {
                    int x = roomNo+1;
                    roomNo = k;
                }

            this.evRooms[roomNo].popById(p.getPatientId());
            this.left[roomNo].signal();

            int x = roomNo+1;
            ClientHandler.leaveEVR(p.toString(), x);
        }finally {
            rl.unlock();
        }

        return 0;
    }
}

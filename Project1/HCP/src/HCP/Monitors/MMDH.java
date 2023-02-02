
package HCP.Monitors;

import HCP.Communication.ClientHandler;
import HCP.Entities.*;
import HCP.Enums.AGE;
import HCP.Logger.MLogger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Medical Hall Monitor
 * <p>The MDH comprises:</p>
 * <p>      o 1 waiting room (MDW)</p>
 * <p>      o 4 medical rooms: MDR1, …, MDR4</p>
 * <p>  - MDW</p>
 * <p>      o The MDW is the second and final stage where patients wait for their medical appointment</p>
 * <p>      o The MDW has 2 seats: one for one child and one for one adult</p>
 * <p>      o Patients proceed to the medical appointment whenever the corresponding MDR is available</p>
 * <p>  - MDRi:</p>
 * <p>      o MDR1 and MDR2 are for children only and MDR3 and MDR4 are for adults only</p>
 * <p>      o Each MDRi has one doctor and one seat for one patient only at a time</p>
 * <p>      o Each medical appointment may take a variable period of time (MDT)</p>
 * <p>      o Patients leave the MDRi after the medical appointment and proceed to the payment hall</p>
 */
public class MMDH implements IPatient_MDH, ICallCentre_MDH, IDoctor {
    private final MLogger mlogger;
    /**
     * Array of 4 medical rooms where the doctors work
     * <p>Each room has 1 seat</p>
     * <p>Index 0 and 1 => CHILD Rooms</p>
     * <p>Index 2 and 3 => ADULT Rooms</p>
     */
    private Room[] mdRooms;
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Condition which signals a CHILD that there's a space available in one of the CHILD Rooms
     */
    private final Condition spaceAvailableChild;
    /**
     * Condition which signals a ADULT that there's a space available in one of the CHILD Rooms
     */
    private final Condition spaceAvailableAdult;
    /**
     * Array of 4 Conditions (for each room) which signals when a patient enters a medical room
     */
    private final Condition[] newMdPatients;
    /**
     * Array of 4 Conditions (for each room) which signals when a patient's appointment is complete
     */
    private final Condition[] appointmentComplete;
    /**
     * Array of 4 Conditions (for each room) which signals when a patient leaves a medical room
     */
    private final Condition[] leftMdr;
    /**
     * Number of seats
     */
    private final int NoS;
    /**
     * Hall room, with 2 seats. One CHILD and One ADULT
     */
    private Room hallRoom;
    /**
     * Number of Doctors
     */
    private int doctorCount;
    /**
     * Initializes variables and conditions
     * @param NoS: number of Seats
     * @param mlogger: logger monitor reference
     */
    public MMDH(int NoS, MLogger mlogger){
        this.rl = new ReentrantLock();
        this.spaceAvailableChild = rl.newCondition();
        this.spaceAvailableAdult = rl.newCondition();
        this.newMdPatients = new Condition[4];
        this.appointmentComplete = new Condition[4];
        this.leftMdr = new Condition[4];
        this.NoS = NoS;
        this.doctorCount = 0;
        this.hallRoom = new Room(2);
        this.mlogger = mlogger;

        this.mdRooms = new Room[4];
        for (int k = 0;k<4;k++) {
            this.mdRooms[k] = new Room(1);
            this.newMdPatients[k] = rl.newCondition();
            this.appointmentComplete[k] = rl.newCondition();
            this.leftMdr[k] = rl.newCondition();
        }

    }





    /**
     * <p>A given patient joins the hall room. Sits in the corresponding seat  </p>
     * <p>Seat 0 for CHILD and 1 for ADULT</p>
     * <p>Waits for a seat to be available in one of the rooms</p>
     * <p>If there's a seat available in a room, leaves the hall seat</p>
     * @param p: Patient trying to join
     * @return Patient which left the hall room
     */
    @Override
    public Patient join(Patient p) {
        try{
            rl.lock();
            if(p.getAge() ==AGE.CHILD) {
                this.hallRoom.addAtIndex(0, p);
                ClientHandler.enterMDW(p.toString());
                this.mlogger.writeLog(p.toString()+"@MDH");
                try {
                    this.spaceAvailableChild.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.hallRoom.popByIndex(0);
            }
            if(p.getAge() ==AGE.ADULT) {

                this.hallRoom.addAtIndex(1, p);
                ClientHandler.enterMDW(p.toString());
                this.mlogger.writeLog(p.toString()+"@MDH");
                try {
                    this.spaceAvailableAdult.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.hallRoom.popByIndex(1);
            }
        }finally {
            rl.unlock();
        }
        return p;
    }

    /**
     * <p>The call centre allows for one patient of a given age to proceed to a medical room</p>
     * @param age: Patient trying to join
     * @return 0 if a patient was signaled to leave, -1 otherwise
     */
    @Override
    public int allowJoin(AGE age) {
        int result = -1;
        try{
            rl.lock();
            if (age==AGE.CHILD){
                if (rl.hasWaiters(this.spaceAvailableChild)){
                    this.spaceAvailableChild.signal();
                    result = 0;
                }
            }else{
                if (rl.hasWaiters(this.spaceAvailableAdult)){
                    this.spaceAvailableAdult.signal();
                    result = 0;
                }
            }
        }finally {
            rl.unlock();
        }
        return result;
    }
    /**
     * <p>The patient leaves the medical room and signals the doctor that it has left</p>
     * @param p: Patient trying to leave
     * @return -1 if the patient is not in any room, otherwise returns the room
     */
    @Override
    public int leave(Patient p) {
        int room = -1;
        try {
            rl.lock();
            for (int k =0; k<4; k++){
                if (this.mdRooms[k].contains(p.getPatientId())){
                    room = k;
                    break;
                }
            }
            if (room!=-1){
                this.mdRooms[room].pop();
                this.leftMdr[room].signal();
                int x = room + 1;
                ClientHandler.leaveMDR(p.toString(), x);
            }
        }finally {
            rl.unlock();
        }
        return room;
    }
    /**
     * <p>A doctor is assigned to an available room</p>
     * @return the number of the room
     */
    @Override
    public int assignRoom() {
        try {
            rl.lock();
            this.doctorCount++;

        }finally {
            rl.unlock();
        }
        return this.doctorCount - 1;
    }
    /**
     * <p>The doctor of the given room awaits for a new patient</p>
     * <p>After a patient joins, the doctor conducts the examination for at most maxMdTime ms </p>
     * <p>Finally, it signals the patient that the appointment is complete and awaits for the patient to leave</p>
     */
    @Override
    public void examination(int roomNo, int maxMdTime) {
        try {
            rl.lock();
            while(this.mdRooms[roomNo].isEmpty()){
                try {
                    this.newMdPatients[roomNo].await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int tsleep = (int)(Math.random() * ((maxMdTime) + 1));
            try {
                Thread.sleep(tsleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.appointmentComplete[roomNo].signalAll();
            try {
                this.leftMdr[roomNo].await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }finally {
            rl.unlock();
        }

    }
    /**
     * <p>Patient joins a medical room according to its age</p>
     * <p>After joining, signals the doctor of the room to its presence </p>
     * <p>Finally, it awaits for the doctor to conclude de medical appointment</p>
     * @return the number of the room
     */
    @Override
    public int joinMedicalAppointment(Patient patient) {
        int roomToJoin = -1;
        try{
            rl.lock();
            if (patient.getAge() == AGE.CHILD){
                roomToJoin = 0;
                if (this.mdRooms[0].isFull()) roomToJoin = 1;

                this.mdRooms[roomToJoin].add(patient);
                int x = roomToJoin + 1;
                ClientHandler.enterMDR(patient.toString(), x);
                this.mlogger.writeLog(patient.toString()+"@MDR"+(roomToJoin+1));
            }else{
                roomToJoin = 2;
                if (this.mdRooms[2].isFull()) roomToJoin = 3;
                this.mdRooms[roomToJoin].add(patient);
                int x = roomToJoin + 1;
                ClientHandler.enterMDR(patient.toString(), x);
                this.mlogger.writeLog(patient.toString()+"@MDR"+(roomToJoin+1));
            }
            this.newMdPatients[roomToJoin].signal();
            try {
                this.appointmentComplete[roomToJoin].await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }finally {
            rl.unlock();
        }
        return roomToJoin;
    }
}

package HCP.Entities;

import HCP.Enums.AGE;

/**
 * Entity which represents a room where patients can sit in
 */
public class Room {
    /**
     * Number of seats/ size of the room array
     */
    private final int size;

    /**
     * Array of patients in the room
     */
    public Patient queue[];
    /**
     * Count of patients in the room
     */
    private int count = 0;

    /**
     * <b>Class Constructor</b>
     * <p>Queue of seats is initialized with given size value</p>
     * @param size: Number of Seats/Size of the room array
     */
    public Room(int size){
        this.size = size;
        this.queue = new Patient[size];

    }
    /**
     * <p>Checks if the all the seats are occupied by patients</p>
     * @return true if the queue is full, else false
     */
    public boolean isFull(){
        for (int k = 0; k<this.size;k++)
            if (queue[k]==null) return false;
        return true;
    }
    /**
     * <p>Checks if the no seats are occupied by patients</p>
     * @return true if the queue is empty, else false
     */
    public boolean isEmpty(){
        for (int k = 0; k<this.size;k++)
            if (queue[k]!=null) return false;
        return true;
    }
    /**
     * <p>Sits a patient in an available seat</p>
     * <p>Patient is added to the last position</p>
     * <p>Count is increased by 1</p>
     * @param obj: new patient to be seated
     * @return 0 if is successful, -1 if room is full
     */
    public int add(Patient obj){
        if (this.isFull()) return -1;
        this.queue[count] = obj;
        this.count++;
        return 0;
    }
    /**
     * <p>Retrieves and removes a Patient from the room given the id</p>
     * <p>Count is decreased by 1</p>
     * @param id: id of the patient to be removed
     * @return 0 if is successful, -1 if room is empty or it does not contain patient with given id value
     */
    public Patient popById(int id) {
        if (this.isEmpty() || !this.contains(id)) return null;
        Patient toPop = null;
        Patient[] newQueue = new Patient[this.size];
        int k = 0;

        for (int i = 0; i < this.count; i++) {
            if(id == this.queue[i].getPatientId()) toPop = this.queue[i];
            else{
                newQueue[k] = this.queue[i];
                k++;
            }

        }
        this.queue = newQueue;
        this.count--;
        return toPop;
    }
    /**
     * <p>Retrieves and removes the patient in the first seat</p>
     * <p>Count is decreased by 1</p>
     * <p>Patients in the posterior seats advance forward</p>
     * @return Patient res: Patient in the first seat, if room is empty return null
     */
    public Patient pop() {
        if (this.isEmpty()) return null;
        Patient res = this.queue[0];
        Patient[] newQueue = new Patient[this.size];
        for (int i = 1; i < this.count; i++) {
            newQueue[i - 1] = this.queue[i];
        }
        this.queue = newQueue;
        this.count--;
        return res;
    }
    /**
     * <p>Retrieves but doesn't remove the patient in the first seat</p>
     * @return Patient res: Patient in the first seat, if room is empty return null
     */
    public Patient getFirst(){
        if (this.isEmpty()) return null;
        return this.queue[0];
    }
    /**
     * <p>Retrieves the number of occupied seats in the room</p>
     * @return int count: number of patients in the array
     */
    public int itemCount(){
        int countP = 0;
        for (int l = 0; l<count; l++){
            if (this.queue[l]!=null) countP++;

        }
        return  countP;
    }

    /**
     * <p>Retrieves the minimum id value for all the patients in the room</p>
     * @return int id
     */
    public int minimumId(){
        if (this.isEmpty()) return -1;
        Patient minId = this.queue[0];
        for (Patient p: this.queue){
            if (p!=null && p.getPatientId()<=minId.getPatientId()){
                minId = p;
            }

        }
        return minId.getPatientId();
    }
    /**
     * <p>Retrieves the patient with the maximum priority given its DoS (Degree of Severity, 3 is Max, 1 is minimum)</p>
     * <p>If there are patients with the same DoS, retrieve the one with minimum id value</p>
     * @return null if room is empty, else returns the correct patient
     */
    public Patient getMaxDoSMinWtn(){
        if (this.isEmpty() || this.queue[0] == null) return null;
        Patient maxDoS = this.queue[0];
        for (int i = 0; i<this.size;i++)
            if (this.queue[i]!=null && this.queue[i].getDoS()>maxDoS.getDoS()) maxDoS = this.queue[i];

        for (int i = 0; i<size; i++) {
            if (this.queue[i] != null) {
                assert maxDoS != null;
                if (this.queue[i].getDoS() == maxDoS.getDoS()) {
                    if (this.queue[i].getPatientId() < maxDoS.getPatientId()) maxDoS = this.queue[i];
                }
            }
        }

        return maxDoS;
    }
    /**
     * <p>Retrieves the number of patients aged ADULT and number of patients aged CHILD</p>
     * @return int[]  results: array with counts, position 0 is CHILD count, 1 is ADULT count
     */
    public int[] getComposition(){      //Pair CHILD, ADULT
        int[] comp = new int[2];
        for(int k = 0; k<this.size;k++){
            if (this.queue[k]!=null){
                if (this.queue[k].getAge() == AGE.CHILD) comp[0]++;
                else comp[1]++;
            }
        }
        return comp;
    }
    /**
     * <p>Checks if the room contains a patient with the given id value</p>
     * @param id: id of the patient to check
     * @return true if room contains patient with given id, else false
     */
    public boolean contains(int id){
        for (int i = 0; i<this.count;i++){
            if (this.queue[i].getPatientId()==id) return true;
        }
        return false;
    }
    /**
     * <p>Inserts a given patient in a given seat</p>
     * @param i: index of the seat
     * @param p: patient to be seated
     * @return -1 if seat at index is not available, 0 otherwise
     */
    public int addAtIndex(int i, Patient p) {
        if (i>=this.size) return -1;
        this.queue[i] =p;
        return 0;
    }
    /**
     * <p>Retrieves and removes the patient at a given seat</p>
     * @param i: index of the seat
     * @return Patient p: patient at the given index, returns null if seat at index is not available or there's no one at that seat
     */
    public Patient popByIndex(int i) {
        if (this.size<=i || this.queue[i]==null) return null;
        Patient p = this.queue[i];
        this.queue[i] =null;
        return p;
    }
}

package HCP.Entities;

import HCP.Enums.AGE;
/**
 * Contains data of one Patient
 */
public class Patient {
    /**
     * Patient's age (ADULT, CHILD)
     */
    private final AGE age;
    /**
     * Patient's number id
     */
    private int id;
    /**
     * Patient's waiting number
     */
    private int wtn = -1;
    /**
     * Patient's Degree Of Severity
     * 0 No Level, 1 Blue, 2 Yellow, 3 Red
     */
    private int DoS= 0;
    /**
     * <p>Class Constructor</p>
     * @param age: Patient's Age (ADULT, CHILD)
     */
    public Patient(AGE age) {
        this.age = age;
    }
    /**
     * <p>Class Constructor</p>
     * @param age: Patient's Age
     * @param id: Patient's id
     */
    public Patient(AGE age, int id) {
        this.age = age;
        this.id = id;
    }

    /**
     * Define the Patient's Degree of Severity
     * @param dos: Patient's new DoS
     */
    public void setDoS(int dos) {
        this.DoS = dos;
    };
    /**
     * Get the Patient's Degree of Severity
     * @return int DoS
     */
    public int getDoS(){
        return this.DoS;
    }
    /**
     * Define the patient's id value
     * @param id: Patient's new id
     */
    public void setPatientId(int id){
        this.id = id;
    }
    /**
     * Get the patient's id
     * @return int id
     */
    public int getPatientId(){
        return this.id;
    }
    /**
     * Get the patient's age
     * @return patient's AGE age
     */
    public AGE getAge(){
        return this.age;
    }

    /**
     * Get the patient's waiting number
     * @return b: boolean value of the hasPaid flag
     */
    public int getPatientWtn() {
        return this.wtn;
    }
    /**
     * Define the patient's waiting number
     * @param _wtn: new waiting number
     */
    public void setPatientWtn(int _wtn) {
        this.wtn = _wtn;
    }

    /**
     * Strigified representation of the Patient's Data
     * Example: A01B -> Patient AGE ADULT, id=01, DoS = 1(BLue)
     */
    @Override
    public String toString(){
        String[] dosColors = {"B","Y","R"};
        if (this.DoS != 0){
            return String.format("%s%02d%s", this.age.toString().charAt(0), this.id,dosColors[this.DoS-1]);
        }
        return String.format("%s%02d",this.age.toString().charAt(0), this.id);
    }


}

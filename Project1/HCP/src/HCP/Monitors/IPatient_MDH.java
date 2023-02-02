package HCP.Monitors;

import HCP.Entities.Patient;

/**
 * Interface used by the Patient to interact with the MDH monitor
 * Methods explained in the MDH monitor
 */
public interface IPatient_MDH extends IPatient{
    int joinMedicalAppointment(Patient patient);
}

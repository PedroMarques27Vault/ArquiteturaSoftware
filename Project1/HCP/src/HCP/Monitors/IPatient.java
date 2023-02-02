package HCP.Monitors;

import HCP.Entities.Patient;

/**
 * Interface used by the call centre to interact with the HALL monitors
 * Methods explained in each monitor (CCH, ETH, EVH, MDH, WTR)
 */
public interface IPatient {
    Patient join(Patient p);
    int leave(Patient p);
}

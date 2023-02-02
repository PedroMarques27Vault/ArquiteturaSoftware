package HCP.Monitors;

import HCP.Entities.Patient;

/**
 * Interface used by the nurse to interact with the EVH monitor
 * Methods explained in the EVH monitor
 */
public interface INurse {

    int assignToRoom();

    Patient waitPatientEvaluation(int roomNo, int maxEvt);
}

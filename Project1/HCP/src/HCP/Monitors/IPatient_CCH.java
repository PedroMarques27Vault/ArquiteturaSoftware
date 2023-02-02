package HCP.Monitors;

import HCP.Enums.SIGNAL;
/**
 * Interface used by the Patient to interact with the CCH monitor
 * Methods explained in the CCH monitor
 */
public interface IPatient_CCH extends IPatient{

    void signalCch(SIGNAL joinWth);

}

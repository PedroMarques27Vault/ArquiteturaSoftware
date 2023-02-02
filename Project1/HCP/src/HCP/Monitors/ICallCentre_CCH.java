package HCP.Monitors;

import HCP.Enums.SIGNAL;
/**
 * Interface used by the call centre to interact with the CCH monitor
 * Methods explained in the CCH monitor
 */
public interface ICallCentre_CCH extends ICallCentre {

    SIGNAL waitForSignal(boolean didLastWork);

    void addSignal(SIGNAL joinWth);
}

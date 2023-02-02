package HCP.Monitors;

import HCP.Enums.AGE;
/**
 * Interface used by the call centre to interact with the MDH monitor
 * Methods explained in the MDH monitor
 */
public interface ICallCentre_MDH extends ICallCentre{
    int allowJoin(AGE age);

}

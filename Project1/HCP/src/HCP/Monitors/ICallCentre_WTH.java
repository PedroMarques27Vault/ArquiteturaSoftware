package HCP.Monitors;
/**
 * Interface used by the call centre to interact with the WTH monitor
 * Methods explained in the WTH monitor
 */
public interface ICallCentre_WTH extends ICallCentre{

    int allowJoin(int roomNo);

    int allowLeave(int roomNo);

}

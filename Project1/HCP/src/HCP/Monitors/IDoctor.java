package HCP.Monitors;
/**
 * Interface used by the doctor to interact with the MDH monitor
 * Methods explained in the MDH monitor
 */
public interface IDoctor {

    int assignRoom();

    void examination(int roomNo, int maxMdTime);
}

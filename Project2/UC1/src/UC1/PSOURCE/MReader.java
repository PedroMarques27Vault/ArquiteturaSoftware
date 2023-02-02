package UC1.PSOURCE;


import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MReader implements IProducer, IReader {
    /**
     * List of records read by the TReader from the Sensor.txt file
     */
    public ArrayList<String> data;


    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Condition which indicates when there is new data read from the Sensor.txt file
     */
    private final Condition isEmpty;


    /**
     * Initializes all variables
     * <p>Reentrant lock fairness is set to true to ensure that the longest waiting thread has priority</p>
     */
    public MReader(){
        this.rl = new ReentrantLock(true);
        this.data = new ArrayList<String>();
        this.isEmpty = rl.newCondition();


    }

    /**
     * The TReader reads data from the file and adds it to the data queue, signaling the waiting producers
    */
    @Override
    public void putData(String data) {
        try{
            rl.lock();
            this.data.add(data);
            this.isEmpty.signal();
        }finally {
            rl.unlock();
        }
    }
    /**
     * The TProducer threads wait for new data
    */
    @Override
    public String getData() {
        String toReturn = "";
        try{
            rl.lock();
            while (this.data.isEmpty()) {
                try {
                    this.isEmpty.await();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            toReturn = data.get(0);

            data.remove(0);

        }finally {
            rl.unlock();
        }
        return toReturn;
    }
}
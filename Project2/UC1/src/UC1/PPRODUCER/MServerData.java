package UC1.PPRODUCER;

import UC1.AppConstants;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MServerData implements IServer, IKafkaProducer {
    /**
     * List of list of Strings. Each inner list is a message queue for each TKafkaProducer.
     */
    private ArrayList<ArrayList<String>> data;
    /**
     * reentrant mutual exclusion lock
     */
    private final ReentrantLock rl;
    /**
     * Array of conditions which indicates when there is new data for each TKafkaProducer
     */
    private final Condition[] isEmpty;

    /**
     * Initializes all the variables
     * <p>ReentrantLock is initialized with fairness = True to ensure that the longest waiting thread has priority</p>
     */
    public MServerData(){
        this.rl = new ReentrantLock(true);
        this.data = new ArrayList<ArrayList<String>>();
        this.isEmpty = new Condition[AppConstants.noKafkaProducers];

        for (int k = 0; k < AppConstants.noKafkaProducers; k++){
            this.isEmpty[k] = rl.newCondition();
            this.data.add(k, new ArrayList<String>());
        }
    }

    /**
     * <p>The TServer thread adds the new data to the message queue of the TKafkaProducer with the same id</p>
     * @param id id of the TServer inserting data
     * @param data new record to be sent to the KafkaCluster
     */
    @Override
    public void putData(int id, String data) {
        try{
            rl.lock();
            this.data.get(id).add(data);
            this.isEmpty[id].signal();
        }finally {
            rl.unlock();
        }
    }
    
    /**
     * <p>The TKafkaProducer waits to retrieve data from its message queue, removing it after retrieval</p>
     * @param producerId id of the TServer inserting data
     * @return the new data in the message queue
     */
    @Override
    public String getData(int producerId) {
        String toReturn = "";
        try{
            rl.lock();
            while (this.data.get(producerId).isEmpty()) {
                try {
                    this.isEmpty[producerId].await();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            toReturn = data.get(producerId).get(0);
            data.get(producerId).remove(0);

        }finally {
            rl.unlock();
        }
        return toReturn;
    }
}
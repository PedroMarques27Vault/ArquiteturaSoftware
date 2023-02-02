package UC3.PPRODUCER;
// Java implementation for a client
// Save file as Client.java

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;
import UC3.AppConstants;

// Client class
public class PProducer
{
    /**
     * <p>This PProducer Process defines the properties of the KafkaProducer for the specifications provided in the Work PDF</p>
     * <p>Generated N threads of TKafkaProducer and N TServers, N is defined in UC3.AppConstants.noKafkaProducers</p>
     * <p>Each TServer and TKafkaProducer are created with the same id.
     * The TServer receives data from PSource process and sends it to the TKafkaProducer with the same id, using the MServerData monitor</p>
     * <p>Records can be reordered, maximize throughput</p>
     * <p>Increase Batch Size to 200000, Default is 163834</p>
     * <p>Increase Linger time to 100, Default is 0</p>
     * <p>Set a Compression Method to lz4, default is none</p>
     * <p>Minimize the possibility of losing records but while minimizing the impact on the overall: acks= 1 ->
     *     This will mean the leader will write the record to its local log but will respond without awaiting full acknowledgement from all followers.
     *     In this case should the leader fail immediately after acknowledging the record but before the followers have replicated it then
     *     the record will be lost.
     * </p>
     * @param args arguments are ignored
     */
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConstants.address);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.put("acks", "all");
        props.put("buffer.memory", 33554432);
        props.put("compression.type", "lz4");
        props.put("linger.ms", 100);
        props.put("batch.size", 200000);
        props.put("retries", 10000);

        MServerData mserverdata = new MServerData();

        for (int i = 0; i < AppConstants.noKafkaProducers; i++) {
            TKafkaProducer kp = new TKafkaProducer(mserverdata, AppConstants.topic, props, i);
            kp.start();
            TServer server = new TServer(mserverdata, AppConstants.JAVA_SOCKET_PORT + i, i);
            server.start();
        }

    }
}


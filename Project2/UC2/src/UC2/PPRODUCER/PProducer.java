package UC2.PPRODUCER;
// Java implementation for a client
// Save file as Client.java

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import java.io.IOException;
import java.util.Properties;
import UC2.AppConstants;

// Client class
public class PProducer
{
    /**
     * <p>This PProducer Process defines the properties of the KafkaProducer for the specifications provided in the Work PDF</p>
     * <p>Generated N threads of TKafkaProducer and N TServers, N is defined in UC2.AppConstants.noKafkaProducers</p>
     * <p>Each TServer and TKafkaProducer are created with the same id.
     * The TServer receives data from PSource process and sends it to the TKafkaProducer with the same id, using the MServerData monitor</p>
     * <p>Records need to preserve ordering, records can be lost. Can't use Idempotence property</p>
     * <p>Minimize Latency:
     * By default, acks=1, which means the leader broker responds sooner to the producer before all replicas have received the message.
     *      acks=0 could also be used, so that the producer won’t wait for a response for a producer request from the broker, but then messages can potentially get lost without the producer knowing.
     *      Compression isn't very suitable for low latency applications, so compression.type=none
     *      linger.ms=0 for low latency
     * </p>
     * @param args arguments are ignored
     */
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConstants.address);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.put("linger.ms", 0);  // Default
        props.put("compression.type", "none");  // Default
        props.put("acks", "1");

        MServerData mserverdata = new MServerData();

        for (int i = 0; i < AppConstants.noKafkaProducers; i++) {
            TKafkaProducer kp = new TKafkaProducer(mserverdata, AppConstants.topic, props, i);
            kp.start();
            TServer server = new TServer(mserverdata, AppConstants.JAVA_SOCKET_PORT + i, i);
            server.start();
        }
    }
}


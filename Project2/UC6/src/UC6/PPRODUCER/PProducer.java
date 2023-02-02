package UC6.PPRODUCER;
// Java implementation for a client
// Save file as Client.java

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Properties;
import UC6.AppConstants;
// Client class
public class PProducer {
    /**
     * <p>This PProducer Process defines the properties of the KafkaProducer for the specifications provided in the Work PDF</p>
     * <p>Generated N threads of TKafkaProducer and N TServers, N is defined in UC6.AppConstants.noKafkaProducers</p>
     * <p>Each TServer and TKafkaProducer are created with the same id.
     * The TServer receives data from PSource process and sends it to the TKafkaProducer with the same id, using the MServerData monitor</p>
     * @param args arguments are ignored
     */
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConstants.address);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        MServerData mserverdata = new MServerData();

        for (int i = 0; i < AppConstants.noKafkaProducers; i++) {
            TKafkaProducer kp = new TKafkaProducer(mserverdata, AppConstants.topic, props, i);
            kp.start();
            TServer server = new TServer(mserverdata, AppConstants.JAVA_SOCKET_PORT + i, i);
            server.start();
        }
    }
}



package UC1.PCONSUMER;

import UC1.AppConstants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class PConsumer {
    /**
     * <p>This PConsumer Process defines the properties of the KafkaConsumer for the specifications provided in the Work PDF</p>
     * <p>Generated N threads of TKafkaConsumer, N is defined in UC1.AppConstants.noKafkaConsumer</p>
     * @param args arguments are ignored
     */
    public static void main(String[] args){
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConstants.address);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "UC1-CONSUMER");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "UC1-CONSUMER");


        for (int i = 0; i< AppConstants.noKafkaConsumers; i++){
            Thread _t = new TKafkaConsumer(props);
            _t.start();
        }
    }
}
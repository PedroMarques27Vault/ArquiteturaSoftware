package UC6.PCONSUMER;

import UC6.AppConstants;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class PConsumer {
    /**
     * <p>This PConsumer Process defines the properties of the KafkaConsumer for the specifications provided in the Work PDF</p>
     * <p>Generated N*K threads of TKafkaConsumer, N is defined in AppConstants.noKafkaConsumer, K is the number of Consumer Groups defined in AppConstants.noKafkaConsumerGroups</p>
     * <p>The id of the client is the conjunction of the group id and consumer id</p>
     * @param args arguments are ignored
     */
    public static void main(String[] args){
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConstants.address);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        for(int k = 0; k<AppConstants.noKafkaConsumerGroups;k++){
            props.put(ConsumerConfig.GROUP_ID_CONFIG,"KafkaconsumerGroup"+k);
            for (int i = 0;i<AppConstants.noKafkaConsumers;i++){
                Thread _t = new TKafkaConsumer(props);
                _t.start();
            }
        }
    }
}

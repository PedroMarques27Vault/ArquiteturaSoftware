package UC3.PCONSUMER;

import UC3.AppConstants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class PConsumer {
    /**
     * <p>This PConsumer Process defines the properties of the KafkaConsumer for the specifications provided in the Work PDF</p>
     * <p>Generated N threads of TKafkaConsumer, N is defined in UC3.AppConstants.noKafkaConsumer</p>
     * <p>Records can be reprocessed but try to avoid some degree of reprocessing</p>
     * <p>To Increase Throughput</p>
     * <p>fetch.max.wait.ms Sets a maximum threshold for time-based batching</p>
     * <p>fetch.min.bytes Sets a minimum threshold for size-based batching</p>
     * <p>fetch.max.wait.ms=1000, default is 500</p>
     * <p>fetch.min.bytes=100000</p>
     * <p>To Guarantee a certain degree of reprocessing </p>
     * <p>auto.commit.interval.ms = 1000, default is 5000</p>
     * @param args arguments are ignored
     */
    public static void main(String[] args){
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConstants.address);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put("fetch.max.wait.ms", 1000);
        props.put("fetch.min.bytes", 100000);
        props.put("auto.commit.interval.ms", 1000);

        for (int i = 0;i<AppConstants.noKafkaConsumers;i++){
            props.put(ConsumerConfig.GROUP_ID_CONFIG,"KafkaConsumerGroup");
            Thread _t = new TKafkaConsumer(props);
            _t.start();
        }
    }
}
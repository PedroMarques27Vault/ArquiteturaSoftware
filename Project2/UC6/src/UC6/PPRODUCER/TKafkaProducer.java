package UC6.PPRODUCER;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.swing.*;

public class TKafkaProducer extends Thread {
    /**
     * KafkaProducer interface to interact with the monitor
     */
    private final IKafkaProducer ikproducer;
    /**
     * Topic to which the KafkaProducer publishes
     */
    private final String topic;
    /**
     * Id of the current producer
     */
    private final int producerId;
    /**
     * Properties of the KafkaProducer
     */
    private final Properties properties;
    /**
     * TKafkaProducer gets data from MServerData monitor and publishes it to the KafkaCluster
     *
     * @param _p KafkaProducer interface to interact with the monitor
     * @param _topic Topic to which the KafkaProducer publishes
     * @param _props properties for the generation of the KafkaProducer
     * @param _producerId Id of the current producer
     */
    public TKafkaProducer(IKafkaProducer _p, String _topic, Properties _props, int _producerId) {
        this.ikproducer = _p;
        this.topic = _topic;
        this.properties = _props;
        this.producerId = _producerId;
        scrollPane =  new JScrollPane();
    }


    /**
     * <p>TKafkaProducer life cycle</p>
     * <p>The producer retrieves the data from the MServerData monitor and then proceeds to publish it to the KafkaCluster</p>
     */
    @Override
    public void run() {
        Producer<String, String> producer = new KafkaProducer<>(properties);
        counterValueLabels = new HashMap<>();
        startGui();
        while(true){
            String newdata = ((IKafkaProducer)ikproducer).getData(this.producerId);
            String lineNumber = newdata.split("#")[0];

            String data = newdata.split("#")[1];
            String key = data.split("\\|")[0];
            String sensor = key.split(":")[0];
            String timestamp = key.split(":")[1];

            String value = newdata.split("\\|")[1];
            totalCounter++;
            addLogEntry(String.format("Line %s - Sensor %s, Time %s: %sªC", lineNumber, sensor, timestamp,value));
            if (this.counterValueLabels.containsKey(Integer.parseInt(sensor))){
                String _c = this.counterValueLabels.get(Integer.parseInt(sensor)).getText();
                this.counterValueLabels.get(Integer.parseInt(sensor)).setText(String.valueOf(Integer.parseInt(_c)+1));
                totalCounterLabel.setText(String.valueOf(totalCounter));
                frame.revalidate();
            }else{
                JPanel temp = new JPanel(new GridLayout(2, 1));
                temp.add(new JLabel("Sensor "+sensor));

                JLabel newlabel = new JLabel("1");
                this.counterValueLabels.put(Integer.parseInt(sensor), newlabel);
                temp.add(newlabel);
                totalNoRecordsBySensor.add(temp);
                totalCounterLabel.setText(String.valueOf(totalCounter));
                frame.revalidate();
            }
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, lineNumber+"#"+key,value);
            producer.send(record);

            //producer.close();
        }
    }
    JPanel information = new JPanel(new GridLayout(2, 2));
    JPanel totalNoRecords = new JPanel(new GridLayout(2, 1));
    JPanel totalNoRecordsBySensor = new JPanel(new GridLayout(2, 1));
    JPanel allRecords = new JPanel(new GridLayout(2, 1));

    private final JLabel totalNoRecordsLabel = new JLabel("Total Number of Records", JLabel.CENTER);
    private final JLabel totalNoRecordsBySensorLabel = new JLabel("Total Number of Records By Sensor", JLabel.CENTER);
    private JLabel totalCounterLabel;
    private final JLabel allRecordsLabel= new JLabel("All Records", JLabel.CENTER);
    private final JScrollPane scrollPane;
    private int totalCounter;
    private HashMap<Integer, JLabel> counterValueLabels;
    private ArrayList<String> logActions;
    private JFrame frame;

    /**
     * Generates the GUI
     * <p>GUI includes total number of records, number of records per sensor and list of records</p>
     */
    public void startGui(){
        totalCounter = 0;
        frame = new JFrame("KafkaProducer "+this.producerId);
        logActions = new ArrayList<>();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,800);

        totalNoRecordsBySensor.setBackground(new Color(230,247,255));
        totalNoRecordsBySensor.add(totalNoRecordsBySensorLabel);

        totalNoRecordsBySensor.setBorder(BorderFactory.createLineBorder(Color.gray));

        totalNoRecords.setBackground(new Color(230,247,255));
        totalNoRecords.add(totalNoRecordsLabel);
        totalCounterLabel = new JLabel(String.valueOf(totalCounter));
        totalNoRecords.add(totalCounterLabel);
        totalNoRecords.setBorder(BorderFactory.createLineBorder(Color.gray));

        final JList<String> list = new JList<String>(logActions.toArray(new String[logActions.size()]));
        scrollPane.setViewportView(list);
        list.setLayoutOrientation(JList.VERTICAL);
        allRecords.add(allRecordsLabel);
        allRecords.add(scrollPane);

        information.add(allRecords);
        information.add(totalNoRecords, BorderLayout.EAST);
        information.add(totalNoRecordsBySensor, BorderLayout.EAST);
        frame.add(information);
        frame.setVisible(true);
    }
    /**
     * Updates the GUI's list of records
     */
    public void addLogEntry(String logData) {
        logActions.add(logData);
        final JList<String> list = new JList<String>(logActions.toArray(new String[logActions.size()]));
        scrollPane.setViewportView(list);
        list.setLayoutOrientation(JList.VERTICAL);
    }
}


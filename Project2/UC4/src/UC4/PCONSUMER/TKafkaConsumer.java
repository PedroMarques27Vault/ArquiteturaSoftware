package UC4.PCONSUMER;


import UC4.AppConstants;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

public class TKafkaConsumer extends Thread{
    /**
     * KafkaConsumer variable which subscribes to the Kafka cluster
     */
    private Consumer<String, String> consumer;
    /**
     * Number of the last line retrieved from Kafka cluster
     */
    private int lastLine;
    /**
     * TKafkaConsumer gets data from the Kafka Cluster and displays results. Kafka Consumer subscribes to the topic in UC1.AppConstants
     * <p>GUI Includes Total number of records, number of records per sensor and list of records</p>
     * @param props Properties of the KafkaConsumer
     */
    public TKafkaConsumer(Properties props) {
        this.lastLine = 0;
        this.consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(AppConstants.topic));
        recordsArea =  new JTextArea("");
        scrollPane = new JScrollPane(recordsArea);
    }

    /**
     * TKafkaConsumer life cycle
     * <p>Retrieves data from the KafkaCluster and updates the GUI with the new information</p>
     */
    @Override
    public void run() {
        counterValueLabels = new HashMap<>();

        final int giveUp = 100;
        int noRecordsCount = 0;
        startGui();
        while (true) {
            final ConsumerRecords<String, String> consumerRecords =
                    consumer.poll(1000);

            if (consumerRecords.count()==0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }

            consumerRecords.forEach(record -> {
                int lineNumber = Integer.parseInt(record.key().split("#")[0]);
                String key = record.key().split("#")[1];
                int sensor = Integer.parseInt(key.split(":")[0]);
                String timestamp = key.split(":")[1];
                String value = record.value();

                totalCounter++;
                addLogEntry(String.format("Line %d - Sensor %d, Time %s: %s??C\n", lineNumber, sensor, timestamp,value));
                if (lineNumber< lastLine)System.out.println("Order not the same");
                lastLine = lineNumber;
                if (this.counterValueLabels.containsKey(sensor)){
                    String _c = this.counterValueLabels.get(sensor).getText();
                    this.counterValueLabels.get(sensor).setText(String.valueOf(Integer.parseInt(_c)+1));
                    totalCounterLabel.setText(String.valueOf(totalCounter));
                    frame.revalidate();
                }else{
                    JPanel temp = new JPanel(new GridLayout(2, 1));
                    temp.add(new JLabel("Sensor "+sensor));

                    JLabel newlabel = new JLabel("1");
                    this.counterValueLabels.put(sensor, newlabel);
                    temp.add(newlabel);
                    totalNoRecordsBySensor.add(temp);
                    totalCounterLabel.setText(String.valueOf(totalCounter));
                    frame.revalidate();
                }
            });
            consumer.commitAsync();
        }
        consumer.close();
        System.out.println("DONE");
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
    private final JTextArea recordsArea;
    private int totalCounter;
    private HashMap<Integer, JLabel> counterValueLabels;

    /**
     * Generates the GUI
     * <p> GUI Includes Total number of records, number of records per sensor and list of records</p>
     */
    private JFrame frame;
    public void startGui(){
        totalCounter = 0;
        frame = new JFrame("KafkaConsumer");

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

        recordsArea.setEditable(false);
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
        recordsArea.setText(recordsArea.getText()+ logData);
    }
}
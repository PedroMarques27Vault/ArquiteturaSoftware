package UC6.PCONSUMER;

import UC6.AppConstants;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;


public class TKafkaConsumer extends Thread{
    /**
     * KafkaConsumer variable which subscribes to the Kafka cluster
     */
    private Consumer<String, String> consumer;
    /**
     * HashMap with list of temperatures for each sensor
     */
    private HashMap<Integer, ArrayList<Double>> allTemperatures;

    /**
     * TKafkaConsumer gets data from the Kafka Cluster and displays results. Kafka Consumer subscribes to the topic in UC1.AppConstants
     * <p>GUI Includes Total number of records, number of records per sensor and list of records</p>
     * @param props Properties of the KafkaConsumer
     */
    public TKafkaConsumer( Properties props) {
        this.consumer = new KafkaConsumer<>(props);
        this.allTemperatures = new HashMap<>();
        consumer.subscribe(Collections.singletonList(AppConstants.topic));
    }

    /**
     * TKafkaConsumer life cycle
     * <p>Retrieves data from the KafkaCluster and updates the GUI with the new information</p>
     * <p>If the data retrieved is not ordered like in the Sensor file, a warning is provided in the interface</p>
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
                if (!allTemperatures.containsKey(sensor)) allTemperatures.put(sensor, new ArrayList<Double>());
                String timestamp = key.split(":")[1];
                String value = record.value();

                totalCounter++;
                addLogEntry(String.format("Line %d - Sensor %d, Time %s: %sªC", lineNumber, sensor, timestamp,value));
                allTemperatures.get(sensor).add(Double.parseDouble(value));

                String result = getAvgTemperature(allTemperatures.get(sensor));
                System.out.println(result);
                if (this.counterValueLabels.containsKey(sensor)){
                    String _c = this.counterValueLabels.get(sensor).getText().split("\n")[0];
                    this.counterValueLabels.get(sensor).setText(String.valueOf(Integer.parseInt(_c)+1)+"\n"+result);
                    totalCounterLabel.setText(String.valueOf(totalCounter));
                    frame.revalidate();
                }else{
                    JPanel temp = new JPanel(new GridLayout(2, 1));
                    temp.add(new JLabel("Sensor "+sensor));

                    JTextArea textArea = new JTextArea(
                            "1\n"+result
                    );
                    textArea.setFont(new Font("Serif", Font.ITALIC, 16));
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.setEditable(false);

                    this.counterValueLabels.put(sensor, textArea);
                    temp.add(textArea);
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

    /**
     * Get the Average Temperature
     * <p>Using an ArrayList of the temperatures it calculates the average </p>
     */
    public String getAvgTemperature(ArrayList<Double> temps){


        double sum = 0;
        for(double temperature: temps)
            sum += temperature;
        double avg = sum/temps.size();
        String data = String.format("Avg: %fªC", avg);

        return data;
    }

    JPanel information = new JPanel(new GridLayout(2, 2));
    JPanel totalNoRecords = new JPanel(new GridLayout(2, 1));
    JPanel totalNoRecordsBySensor = new JPanel(new GridLayout(2, 1));
    JPanel allRecords = new JPanel(new GridLayout(2, 1));


    private final JLabel totalNoRecordsLabel = new JLabel("Total Number of Records", JLabel.CENTER);
    private final JLabel totalNoRecordsBySensorLabel = new JLabel("Total Number of Records By Sensor", JLabel.CENTER);
    private JLabel totalCounterLabel;
    private final JLabel allRecordsLabel= new JLabel("All Records", JLabel.CENTER);
    private final JScrollPane scrollPane = new JScrollPane();
    private int totalCounter;
    private HashMap<Integer, JTextArea> counterValueLabels;
    private ArrayList<String> logActions;

    private JFrame frame;

    /**
     * Generates the GUI
     * <p> GUI Includes Total number of records, number of records per sensor and list of records</p>
     */
    public void startGui(){
        totalCounter = 0;
        frame = new JFrame("KafkaConsumer");
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
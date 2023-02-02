package UC3.PSOURCE;
// Java implementation for a client
// Save file as Client.java

import UC3.AppConstants;

import java.io.*;

// Client class
public class PSource
{
    /**
     * PSource process generates the reader thread which reads the data from Sensor.txt file.
     * Next, the TProducer threads send the data to the servers with the same id
     * @param args arguments are ignored
     */public static void main(String[] args) throws IOException
    {
            MReader mreader = new MReader();

            TReader reader = new TReader((IReader)mreader, "../Sensor.txt");
            reader.start();
            for (int i = 0; i<AppConstants.noKafkaProducers;i++){
                TProducer p = new TProducer((IProducer) mreader, AppConstants.JAVA_SOCKET_PORT+i,i);
                p.start();
            }
    }
}



package UC2.PSOURCE;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TReader extends Thread{
    /**
     * Interface to interact with the MReader monitor
     */
    private final IReader ireader;
    /**
     * Path of the file to be read
     */
    private final String file;

    /**
     * The TReader thread reads data from a given file.
     * @param _file path to the file to be read
     * @param rd interface to interact with the monitor
     */
    public TReader(IReader rd, String _file) {
        this.ireader = rd;
        this.file =_file;
    }

    /**
     * TReader lifecycle
     * <p>Continuously aggregates 3 lines from the file into one string. Then, it puts the new data in the monitor data queue</p>
     */
    @Override
    public void run() {
        try {
            try {
                List<String> allLines = Files.readAllLines(Paths.get(this.file));
                for (int i = 0; i<allLines.size();i+=3) {
                    String data  = i/3+"#"+allLines.get(i)+":"+allLines.get(i+2)+"|"+allLines.get(i+1);
                    ((IReader)this.ireader).putData(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception e) { }
    }

}

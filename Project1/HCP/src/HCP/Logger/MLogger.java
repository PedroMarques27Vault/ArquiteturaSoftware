package HCP.Logger;

import HCP.Entities.MessageList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.io.FileWriter;

/**
 * Logger Monitor
 */
public class MLogger implements ILogger {
    /**
     * reentrant mutual exclusion lock
     */
    private ReentrantLock rl;
    /**
     * Condition which alerts the TLogger of a new log entry
     */
    private Condition awaken;
    /**
     * Dynamic list of recent logs
     */
    private MessageList logs;

    /**
     * Initializes the reentrantLock, the awaken condition and the logs MessageList
     */
    public MLogger(){
        this.rl  = new ReentrantLock();
        this.awaken = rl.newCondition();
        this.logs = new MessageList();

    }


    /**
     * TLogger waits for a new log entry in the logs MessageList
     * When there is one, returns it and removes it from the MessageList
     * @return String new log entry
     */
    @Override
    public String waitForLog() {
        String toReturn = "";
        try{
            rl.lock();
            while(this.logs.isEmpty()) {
                try {
                    this.awaken.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            toReturn = (String) this.logs.pop();

        }finally {
            rl.unlock();
        }
        return toReturn;

    }
    /**
     * Adds a new log to the logs MessageList and notifies the TLogger
     */
    public void writeLog(String message) {
        try{
            rl.lock();
            logs.push(message);
            this.awaken.signalAll();

        }finally {
            rl.unlock();
        }

    }
}

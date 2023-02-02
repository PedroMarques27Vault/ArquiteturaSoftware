package Server;

public class TWorker  extends Thread {
    /**
     * MServer Monitor RequesReceiver Interface
     */
    private final IWorker mworker;
    /**
     * Boolean flag for stopping process
     */
    private boolean stopFlag;
    /**
     * id of thread worker
     */
    private final int id;
    /**
     * Load of interactions
     */
    private final int iterationLoad;

    /**
     * Initialize TWorker
     * @param _mworker MServer Monitor RequesReceiver Interface
     * @param _id id of thread worker
     * @param _time Load of interactions
     */
    public TWorker(IWorker _mworker, int _id, int _time) {
        this.stopFlag = false;
        this.mworker = _mworker;
        this.id = _id;
        this.iterationLoad = _time;
    }

    /**
     * <p>stopFlag flag set to true, process ends</p>
     */
    public void stopProcess() {
        this.stopFlag = true;
    }

    /**
     * The worker starts by retrieving a request. According to the number of iterations in the request, the worker calculates the reponse and sleeps for each iteration.
     * After that it sends the results to the TSender
     */
    @Override
    public void run() {
        String[] pi = "14159265358979323846".split("");
        while (!this.stopFlag) {

            Request req = ((IWorker)mworker).getRequest(this.id);
            StringBuilder str = new StringBuilder();
            str.append("3.");

            for (int i = 0; i<req.getNumberOfIterations(); i++){
                try {
                    str.append(pi[i]);
                    System.out.println(id+"-"+ i);
                    Thread.sleep(iterationLoad);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((IWorker)mworker).removeOneIteration(req, this.id);
            }
            req.setResult(str.toString());
            req.setCode(2);
            req.setWorker(this.id);
            ((IWorker)mworker).putResult(req,this.id);
        }
    }
}

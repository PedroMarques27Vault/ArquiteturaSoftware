package Server;

public class Request {
    /**
     * Id of the request
     */
    private int RequestId;
    /**
     * Id of the server
     */
    private int ServerId;
    /**
     * Reply code
     */
    private int Code;
    /**
     * Number of Iterations of the request
     */
    private int NumberIterations;
    /**
     * Deadline of the request
     */
    private int Deadline;
    /**
     * Id of worker thread
     */
    private int WorkerThread;
    /**
     * Value
     */
    private String Value;
    /**
     * Id of the client
     */
    private String ClientId;
    /**
     * Address of the request
     */
    private String Address;

    /**
     * Initialization of Request
     * @param _ClientId id of the client
     * @param _RequestId id of the request
     * @param _address address of the request
     * @param _ServerId id of the server
     * @param _Code code of the reply
     * @param _Value value
     * @param _Deadline deadline of the request
     */
    private Request(String _ClientId, int _RequestId, String _address, int _ServerId, int _Code, int _NumberIterations, String _Value, int _Deadline ){
        this.ClientId = _ClientId;
        this.RequestId = _RequestId;
        this.ServerId = _ServerId;
        this.Code = _Code;
        this.NumberIterations = _NumberIterations;
        this.Value = _Value;
        this.Deadline = _Deadline;
        this.WorkerThread = -1;
        this.Address = _address;
    };

    /**
     * Initialization of Request
     * @param _ClientId id of the client
     * @param _RequestId id of the request
     * @param _address address of the request
     * @param _ServerId id of the server
     * @param _Worker id of the worker
     * @param _Code code of the reply
     * @param _Value value
     * @param _Deadline deadline of the request
     */
    public Request(String _ClientId, String _RequestId,String _address, String _ServerId, String _Worker, String _Code, String _NumberIterations, String _Value, String _Deadline) {
        this.ClientId = _ClientId;
        this.RequestId = Integer.parseInt(_RequestId);
        this.ServerId = Integer.parseInt(_ServerId);
        this.WorkerThread = Integer.parseInt(_Worker);
        this.Code = Integer.parseInt(_Code);
        this.NumberIterations = Integer.parseInt(_NumberIterations);
        this.Value = _Value;
        this.Deadline = Integer.parseInt(_Deadline);
        this.Address = _address;
    }

    /**
     * Initialization of Request
     */
    private Request(){};

    /**
     * Return deadline
     */
    public int getDeadline(){
        return this.Deadline;
    }

    /**
     * Create the request from a string
     */
    public static Request fromString(String input){
        if (input.length()==0)
            return null;
        String[] variables = input.split("\\|");
        String serverWorker=variables[3];
        String worker = "-1";
        if (serverWorker.split(":").length>1){
            worker = serverWorker.split(":")[1];
            serverWorker = serverWorker.split(":")[0];
        }

        String address = "127.0.0.1";
        if (serverWorker.split("\\+").length>1){
            address = serverWorker.split("\\+")[0];
            serverWorker = serverWorker.split("\\+")[1];
        }
        return new Request(variables[1],variables[2],address, serverWorker,worker,variables[4], variables[5],variables[6],variables[7]);
    }

    /**
     * Display information in a specific way
     */
    public String toString(){
        return String.format("(Client %s, Request ID %d, Server %d, Worker %d, Code %02d, Number of Iterations %d, Value %s, Deadline %d)", ClientId, RequestId, ServerId,WorkerThread, Code, NumberIterations, Value, Deadline);
    }

    /**
     * Display information in a specific way
     */
    public String stringify(){
        return String.format("|%s|%d|%s+%d:%d|%02d|%d|%s|%d|", ClientId, RequestId, Address, ServerId,WorkerThread, Code, NumberIterations, Value, Deadline);
    }

    /**
     * Set the reply code
     * @param i code of the reply
     */
    public void setCode(int i) {
        this.Code = i;
    }

    /**
     * Return number of iterations
     */
    public int getNumberOfIterations() {
        return this.NumberIterations;
    }

    /**
     * Set the result
     * @param _value value of result
     */
    public void setResult(String _value) {
        this.Value = _value;
    }

    /**
     * Set the server id
     * @param _value id of server
     */
    public void setServerId(int _value) {
        this.ServerId = _value;
    }

    /**
     * Set the worker id
     * @param _value worker id
     */
    public void setWorker(int _value) {
        this.WorkerThread = _value;
    }

    /**
     * Return client id
     */
    public String getClient() {
        return this.ClientId;
    }

    /**
     * Set the address
     * @param address address of request
     */
    public void setServerAddress(String address) {
        this.Address = address;
    }

    /**
     * Set the id of the request
     * @param _id id of request
     */
    public void setId(int _id) {
        this.RequestId = _id;
    }

    /**
     * Return request id
     */
    public int getId() {
        return this.RequestId;
    }
}

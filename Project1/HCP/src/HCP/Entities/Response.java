package HCP.Entities;

import HCP.Enums.OPERATION;
/**
 * Responses provided to each received message in the HCP socket
 */
public class Response {
    /**
     * Action that generates the response
     * Example: START, END, STOP,...
     */
    private OPERATION operation;
    /**
     * String description of the response
     */
    private String message;
    /**
     * <b>Class Constructor</b>
     * @param  op: operation value
     * @param  ms: message description
     */
    public Response(OPERATION op, String ms){
        this.operation = op;
        this.message=ms ;
    }
    /**
     *
     * Gets the current operation value
     * @return OPERATION operation
     */
    public OPERATION getOperation(){
        return this.operation;
    }
    /**
     * Gets the current response description
     * @return  String message: returns the current message
     */
    public String getMessage(){
        return this.message;
    }
}

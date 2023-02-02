package HCP.Entities;

import HCP.Enums.AGE;

/**
 * <p>Dynamic list of Objects</p>
 * <p>Increases size with every push(), decreases with pop()</p>
 */
public class MessageList {
    /**
     * Inner array of objects
     */
    private Object arr[];
    /**
     * Inserted Item count
     */
    private int count;

    /**
     * <b>Class Constructor</b>
     * <p>Creates a growable list</p>
     * <p>inner array initialized with length = 1</p>
     * <p>count initialized at 0</p>
     */
    public MessageList()
    {
        arr = new Object[1];
        count = 0;
    }
    /**
     * <p>Adds object to the end of the array</p>
     * <p>  Increases count by 1</p>
     * <p>  array increases size by 1</p>
     * @param data: object to be added to the list
     */
    public void push(Object data)
    {
        if (count == this.arr.length) {
            Object temp[] = new Object[this.arr.length+1];
            for (int i = 0; i < this.arr.length; i++)
                temp[i] = arr[i];
            arr = temp;
        }

        arr[count] = data;
        count++;
    }
    /**
     * <p>Is the array empty</p>
     * @return true if array does not contain any item, false otherwise
     */
    public boolean isEmpty()
    {
        return this.count == 0;
    }

    /**
     * <p>Retrieves the first element of the the array and removes it from the list</p>
     * @return Object at the front of the array (index 0)
     */
    public Object pop()
    {
        if (this.isEmpty()) return null;
        Object result;
        Object temp[] = new Object[this.arr.length-1];

        for (int i = 1; i < this.arr.length; i++)
            temp[i-1] = arr[i];

        result = arr[0];
        arr = temp;
        count--;
        return result;
    }




}

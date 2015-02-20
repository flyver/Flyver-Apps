package co.flyver.flyvercore.microcontrollers.IOIOOutputs;

import java.util.LinkedList;

import co.flyver.flyvercore.microcontrollers.IOIOOutputs.exceptions.OutputsListException;


/**
 * Created by Tihomir Nedev on 15-2-17.
 */
public class OutputsList {

    static OutputsList instance;

    LinkedList<IOIOOutput> outputs = new LinkedList<>();

    public void addOutput(IOIOOutput ioioOutput) {
        outputs.add(ioioOutput);
    }

    public LinkedList<IOIOOutput> getOutputs() {
        return outputs;
    }

    public static OutputsList getInstance() throws OutputsListException {
        if(instance == null) {
            throw new OutputsListException("Outputs list not created. Call OutputsList.create() first");
        }
        return instance;
    }

    public static void create() {
        instance = new OutputsList();
    }
}

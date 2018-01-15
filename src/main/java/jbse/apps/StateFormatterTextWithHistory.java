package jbse.apps;

import jbse.mem.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/01/18
 *
 * This class will produce the same output as StateFormatterText,
 * but allows user to get the history of all states
 *
 */
public class StateFormatterTextWithHistory extends StateFormatterText {

    /**
     * History of all reached states
     */
    private static List<State> states;

    public StateFormatterTextWithHistory(List<String> srcPath) {
        super(srcPath);
        this.states = new ArrayList<>();
    }

    @Override
    public void formatState(State state) {
        super.formatState(state);
        this.states.add(state);
    }

    /**
     * @return the list of all states reached
     */
    public static List<State> getStates() {
        return states;
    }
}

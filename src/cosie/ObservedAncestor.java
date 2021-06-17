package cosie;

import java.util.HashMap;

public class ObservedAncestor {

    public Animal ancestor;
    HashMap<Integer, Integer> childrenInNthDay;
    HashMap<Integer, Integer> descendantsInNthDay;
    public int currNumberOfDescendants = 0;
    public int observedFor;

    public ObservedAncestor(Animal ancestor, int day) {
        this.observedFor = day;
        this.ancestor = ancestor;
        this.childrenInNthDay = new HashMap<>();
        this.descendantsInNthDay = new HashMap<>();
        this.ancestor.ancestorUnderObservation = this;
    }
}

package by.progminer.JLexPHP.Math;

import by.progminer.JLexPHP.Utility.Set;
import by.progminer.JLexPHP.Spec;

public class NFA {
    
    public static final int NO_LABEL = -1;
    
    /**
     * Edge transitions on one specific character
     * are labelled with the character ASCII (Unicode)
     * codes. So none of the constants below should
     * overlap with the natural character codes.
     */
    public static final int CCL     = -1;
    public static final int EMPTY   = -2;
    public static final int EPSILON = -3;
    
    /**
     * Label for edge type:
     *      character code,
     *      CCL (character class),
     *      [STATE,
     *      SCL (state class),]
     *      EMPTY,
     *      EPSILON.
     */
    public int edge;
    
    /**
     * Set to store character classes.
     */
    public Set set;
    
    /**
     * Next state (or null if none).
     */
    public NFA next;
    
    /**
     * Another state with type == EPSILON
     * and null if not used.
     *
     * The NFA construction should result in two
     * outgoing edges only if both are EPSILON edges.
     */
    public NFA next2;
    
    /**
     * Set to null if nonaccepting state.
     */
    public Accept accept;
    
    /**
     * Says if and where pattern is anchored.
     */
    public int anchor;
    
    public int label;
    
    public SparseBitSet states;
    
    public NFA() {
        edge = EMPTY;
        set = null;
        next = null;
        next2 = null;
        accept = null;
        anchor = Spec.NONE;
        label = NO_LABEL;
        states = null;
    }
    
    /**
     * Converts this NFA state into a copy of
     * the input one.
     */
    public void mimic(NFA nfa) {
        edge = nfa.edge;
        
        if (null != nfa.set) {
            if (null == set) {
                set = new Set();
            }
            
            set.mimic(nfa.set);
        } else {
            set = null;
        }
        
        next = nfa.next;
        next2 = nfa.next2;
        accept = nfa.accept;
        anchor = nfa.anchor;
        
        if (null != nfa.states) {
            states = (SparseBitSet) nfa.states.clone();
        } else {
            states = null;
        }
    }
}

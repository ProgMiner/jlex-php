package by.progminer.JLexPHP;

class NFA {
    
    static final int NO_LABEL = -1;
    
    /**
     * Edge transitions on one specific character
     * are labelled with the character ASCII (Unicode)
     * codes. So none of the constants below should
     * overlap with the natural character codes.
     */
    static final int CCL     = -1;
    static final int EMPTY   = -2;
    static final int EPSILON = -3;
    
    /**
     * Label for edge type:
     *      character code,
     *      CCL (character class),
     *      [STATE,
     *      SCL (state class),]
     *      EMPTY,
     *      EPSILON.
     */
    int edge;
    
    /**
     * Set to store character classes.
     */
    CSet set;
    
    /**
     * Next state (or null if none).
     */
    NFA next;
    
    /**
     * Another state with type == EPSILON
     * and null if not used.
     *
     * The NFA construction should result in two
     * outgoing edges only if both are EPSILON edges.
     */
    NFA next2;
    
    /**
     * Set to null if nonaccepting state.
     */
    Accept accept;
    
    /**
     * Says if and where pattern is anchored.
     */
    int anchor;
    
    int label;
    
    SparseBitSet states;
    
    NFA() {
        edge = EMPTY;
        set = null;
        next = null;
        next2 = null;
        accept = null;
        anchor = CSpec.NONE;
        label = NO_LABEL;
        states = null;
    }
    
    /**
     * Converts this NFA state into a copy of
     * the input one.
     */
    void mimic(NFA nfa) {
        edge = nfa.edge;
        
        if (null != nfa.set) {
            if (null == set) {
                set = new CSet();
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

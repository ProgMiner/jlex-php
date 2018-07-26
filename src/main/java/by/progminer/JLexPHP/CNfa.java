package by.progminer.JLexPHP;

class CNfa {
    
    static final int NO_LABEL = -1;
    
    /**
     * Edge transitions on one specific character
     * are labelled with the character Ascii (Unicode)
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
    int m_edge;
    
    /**
     * Set to store character classes.
     */
    CSet m_set;
    
    /**
     * Next state (or null if none).
     */
    CNfa m_next;
    
    /**
     * Another state with type == EPSILON
     * and null if not used.
     *
     * The NFA construction should result in two
     * outgoing edges only if both are EPSILON edges.
     */
    CNfa m_next2;
    
    /**
     * Set to null if nonaccepting state.
     */
    Accept m_accept;
    
    /**
     * Says if and where pattern is anchored.
     */
    int m_anchor;
    
    int m_label;
    
    SparseBitSet m_states;
    
    CNfa() {
        m_edge = EMPTY;
        m_set = null;
        m_next = null;
        m_next2 = null;
        m_accept = null;
        m_anchor = CSpec.NONE;
        m_label = NO_LABEL;
        m_states = null;
    }
    
    /**
     * Converts this NFA state into a copy of
     * the input one.
     */
    void mimic(CNfa nfa) {
        m_edge = nfa.m_edge;
        
        if (null != nfa.m_set) {
            if (null == m_set) {
                m_set = new CSet();
            }
            
            m_set.mimic(nfa.m_set);
        } else {
            m_set = null;
        }
        
        m_next = nfa.m_next;
        m_next2 = nfa.m_next2;
        m_accept = nfa.m_accept;
        m_anchor = nfa.m_anchor;
        
        if (null != nfa.m_states) {
            m_states = (SparseBitSet) nfa.m_states.clone();
        } else {
            m_states = null;
        }
    }
}

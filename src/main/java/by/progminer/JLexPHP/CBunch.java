package by.progminer.JLexPHP;

import java.util.Vector;

class CBunch {
    
    /**
     * Vector of CNfa states in dfa state.
     */
    Vector <CNfa> m_nfa_set;
    
    /**
     * BitSet representation of CNfa labels.
     */
    SparseBitSet m_nfa_bit;
    
    /**
     * Accepting actions, or null if nonaccepting state.
     */
    CAccept m_accept;
    
    /**
     * Anchors on regular expression.
     */
    int m_anchor;
    
    /**
     * CNfa index corresponding to accepting actions.
     */
    int m_accept_index;
    
    CBunch() {
        m_nfa_set = null;
        m_nfa_bit = null;
        m_accept = null;
        m_anchor = CSpec.NONE;
        m_accept_index = -1;
    }
}

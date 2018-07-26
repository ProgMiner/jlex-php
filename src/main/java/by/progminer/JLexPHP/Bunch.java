package by.progminer.JLexPHP;

import java.util.Vector;

class Bunch {
    
    /**
     * Vector of CNfa states in dfa state.
     */
    Vector <CNfa> nfaSet;
    
    /**
     * BitSet representation of CNfa labels.
     */
    SparseBitSet nfaBit;
    
    /**
     * Accepting actions, or null if nonaccepting state.
     */
    Accept accept;
    
    /**
     * Anchors on regular expression.
     */
    int anchor;
    
    /**
     * CNfa index corresponding to accepting actions.
     */
    int acceptIndex;
    
    Bunch() {
        nfaSet = null;
        nfaBit = null;
        accept = null;
        anchor = CSpec.NONE;
        acceptIndex = -1;
    }
}

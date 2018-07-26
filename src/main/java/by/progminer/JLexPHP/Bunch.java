package by.progminer.JLexPHP;

import java.util.Vector;

class Bunch {
    
    /**
     * Vector of NFA states in dfa state.
     */
    Vector <NFA> nfaSet;
    
    /**
     * BitSet representation of NFA labels.
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
     * NFA index corresponding to accepting actions.
     */
    int acceptIndex;
    
    Bunch() {
        nfaSet = null;
        nfaBit = null;
        accept = null;
        anchor = Spec.NONE;
        acceptIndex = -1;
    }
}

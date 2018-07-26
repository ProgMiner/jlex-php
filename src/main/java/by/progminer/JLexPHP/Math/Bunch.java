package by.progminer.JLexPHP.Math;

import by.progminer.JLexPHP.Spec;

import java.util.Vector;

public class Bunch {
    
    /**
     * Vector of NFA states in dfa state.
     */
    public Vector <NFA> nfaSet;
    
    /**
     * BitSet representation of NFA labels.
     */
    public SparseBitSet nfaBit;
    
    /**
     * Accepting actions, or null if nonaccepting state.
     */
    public Accept accept;
    
    /**
     * Anchors on regular expression.
     */
    public int anchor;
    
    /**
     * NFA index corresponding to accepting actions.
     */
    public int acceptIndex;
    
    public Bunch() {
        nfaSet = null;
        nfaBit = null;
        accept = null;
        anchor = Spec.NONE;
        acceptIndex = -1;
    }
}

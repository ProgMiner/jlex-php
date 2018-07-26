package by.progminer.JLexPHP;

import java.util.Vector;

class DFA {
    
    int group;
    boolean mark;
    
    Accept accept;
    int anchor;
    
    Vector <NFA> nfaSet;
    SparseBitSet nfaBit;
    
    int label;
    
    DFA(int label) {
        group = 0;
        mark = false;
        
        accept = null;
        anchor = CSpec.NONE;
        
        nfaSet = null;
        nfaBit = null;
        
        this.label = label;
    }
}

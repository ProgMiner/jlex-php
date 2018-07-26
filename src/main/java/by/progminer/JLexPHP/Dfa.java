package by.progminer.JLexPHP;

import java.util.Vector;

class Dfa {
    
    int group;
    boolean mark;
    
    Accept accept;
    int anchor;
    
    Vector nfaSet;
    SparseBitSet nfaBit;
    
    int label;
    
    Dfa(int label) {
        group = 0;
        mark = false;
        
        accept = null;
        anchor = CSpec.NONE;
        
        nfaSet = null;
        nfaBit = null;
        
        this.label = label;
    }
}

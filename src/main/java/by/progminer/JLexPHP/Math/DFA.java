package by.progminer.JLexPHP.Math;

import by.progminer.JLexPHP.Spec;

import java.util.Vector;

public class DFA {
    
    public int group;
    public boolean mark;
    
    public Accept accept;
    public int anchor;
    
    public Vector <NFA> nfaSet;
    public SparseBitSet nfaBit;
    
    public int label;
    
    public DFA(int label) {
        group = 0;
        mark = false;
        
        accept = null;
        anchor = Spec.NONE;
        
        nfaSet = null;
        nfaBit = null;
        
        this.label = label;
    }
}

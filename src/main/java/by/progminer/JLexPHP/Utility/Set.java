package by.progminer.JLexPHP.Utility;

import by.progminer.JLexPHP.Math.SparseBitSet;

import java.util.Enumeration;

public class Set {
    
    private SparseBitSet set;
    private boolean complement;
    
    public Set() {
        set = new SparseBitSet();
        complement = false;
    }
    
    public void complement() {
        complement = true;
    }
    
    public void add(int i) {
        set.set(i);
    }
    
    /**
     * Add ignoring case.
     */
    public void addNCase(char c) {
        // Do this in a Unicode-friendly way.
        // (note that duplicate adds have no effect)
        
        add(c);
        add(Character.toLowerCase(c));
        add(Character.toTitleCase(c));
        add(Character.toUpperCase(c));
    }
    
    public boolean contains(int i) {
        boolean result = set.get(i);
        
        if (complement) {
            return !result;
        }
        
        return result;
    }
    
    public void mimic(Set set) {
        complement = set.complement;
        this.set = (SparseBitSet) set.set.clone();
    }
    
    /**
     * Map set using character classes [CSA]
     */
    public void map(Set set, int[] mapping) {
        complement = set.complement;
        this.set.clearAll();
        
        for (Enumeration <Integer> e = set.set.elements(); e.hasMoreElements(); ) {
            int oldValue = e.nextElement();
            
            // skip unmapped characters
            if (oldValue < mapping.length) {
                this.set.set(mapping[oldValue]);
            }
        }
    }
}

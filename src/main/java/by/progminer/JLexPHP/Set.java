package by.progminer.JLexPHP;

import java.util.Enumeration;

class Set {
    
    private SparseBitSet set;
    private boolean complement;
    
    Set() {
        set = new SparseBitSet();
        complement = false;
    }
    
    void complement() {
        complement = true;
    }
    
    void add(int i) {
        set.set(i);
    }
    
    /**
     * Add ignoring case.
     */
    void addNCase(char c) {
        // Do this in a Unicode-friendly way.
        // (note that duplicate adds have no effect)
        
        add(c);
        add(Character.toLowerCase(c));
        add(Character.toTitleCase(c));
        add(Character.toUpperCase(c));
    }
    
    boolean contains(int i) {
        boolean result = set.get(i);
        
        if (complement) {
            return !result;
        }
        
        return result;
    }
    
    void mimic(Set set) {
        complement = set.complement;
        this.set = (SparseBitSet) set.set.clone();
    }
    
    /**
     * Map set using character classes [CSA]
     */
    void map(Set set, int[] mapping) {
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

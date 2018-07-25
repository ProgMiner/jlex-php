package by.progminer.JLexPHP;

import java.util.Enumeration;

class CSet {
    
    private SparseBitSet m_set;
    private boolean m_complement;
    
    CSet() {
        m_set = new SparseBitSet();
        m_complement = false;
    }
    
    void complement() {
        m_complement = true;
    }
    
    void add(int i) {
        m_set.set(i);
    }
    
    /**
     * Add ignoring case.
     */
    void addncase(char c) {
        // Do this in a Unicode-friendly way.
        // (note that duplicate adds have no effect)
        
        add(c);
        add(Character.toLowerCase(c));
        add(Character.toTitleCase(c));
        add(Character.toUpperCase(c));
    }
    
    boolean contains(int i) {
        boolean result = m_set.get(i);
        
        if (m_complement) {
            return !result;
        }
        
        return result;
    }
    
    void mimic(CSet set) {
        m_complement = set.m_complement;
        m_set = (SparseBitSet) set.m_set.clone();
    }
    
    /**
     * Map set using character classes [CSA]
     */
    void map(CSet set, int[] mapping) {
        m_complement = set.m_complement;
        m_set.clearAll();
        
        for (Enumeration <Integer> e = set.m_set.elements(); e.hasMoreElements(); ) {
            int old_value = e.nextElement();
            
            // skip unmapped characters
            if (old_value < mapping.length) {
                m_set.set(mapping[old_value]);
            }
        }
    }
}

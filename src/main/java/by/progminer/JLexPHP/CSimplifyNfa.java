package by.progminer.JLexPHP;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Extract character classes from NFA and simplify.
 *
 * @author C. Scott Ananian 25-Jul-1999
 */
class CSimplifyNfa {
    
    /**
     * Character class mapping.
     */
    private int[] ccls;
    
    /**
     * Original charset size
     */
    private int original_charset_size;
    
    /**
     * Reduced charset size
     */
    private int mapped_charset_size;
    
    void simplify(CSpec m_spec) {
        computeClasses(m_spec); // initialize fields.
        
        // now rewrite the NFA using our character class mapping.
        for (Enumeration <NFA> e = m_spec.m_NFA_states.elements(); e.hasMoreElements(); ) {
            NFA nfa = e.nextElement();
            
            if (nfa.edge == nfa.EMPTY || nfa.edge == nfa.EPSILON) {
                continue; // no change.
            }
            
            if (nfa.edge == nfa.CCL) {
                CSet ncset = new CSet();
                
                ncset.map(nfa.set, ccls); // map it.
                nfa.set = ncset;
            } else {
                // single character
                
                nfa.edge = ccls[nfa.edge]; // map it.
            }
        }
        
        // now update spec with the mapping.
        m_spec.m_ccls_map = ccls;
        m_spec.m_dtrans_ncols = mapped_charset_size;
    }
    
    /**
     * Compute minimum set of character classes needed to disambiguate
     * edges.  We optimistically assume that every character belongs to
     * a single character class, and then incrementally split classes
     * as we see edges that require discrimination between characters in
     * the class. [CSA, 25-Jul-1999]
     */
    private void computeClasses(CSpec m_spec) {
        this.original_charset_size = m_spec.m_dtrans_ncols;
        this.ccls = new int[original_charset_size]; // initially all zero.
        
        int nextcls = 1;
        SparseBitSet clsA = new SparseBitSet(), clsB = new SparseBitSet();
        Hashtable<Integer, Integer> h = new Hashtable <Integer, Integer> ();
        
        System.out.print("Working on character classes.");
        
        for (Enumeration <NFA> e = m_spec.m_NFA_states.elements(); e.hasMoreElements(); ) {
            NFA nfa = e.nextElement();
            
            if (nfa.edge == nfa.EMPTY || nfa.edge == nfa.EPSILON) {
                continue; // no discriminatory information.
            }
            
            clsA.clearAll();
            clsB.clearAll();
            
            for (int i = 0; i < ccls.length; i++) {
                if (
                    nfa.edge == i || // edge labeled with a character
                    (nfa.edge == nfa.CCL && nfa.set.contains(i)) // set of characters
                ) {
                    clsA.set(ccls[i]);
                } else {
                    clsB.set(ccls[i]);
                }
            }
            
            // now figure out which character classes we need to split.
            
            clsA.and(clsB); // split the classes which show up on both sides of edge
            
            System.out.print(clsA.size() == 0? ".": ":");
            
            if (clsA.size() == 0) {
                continue; // nothing to do.
            }
            
            // and split them.
            
            h.clear(); // h will map old to new class name
            for (int i = 0; i < ccls.length; i++) {
                if (clsA.get(ccls[i])) { // a split class
                    if (nfa.edge == i || (nfa.edge == nfa.CCL && nfa.set.contains(i))) {
                        // on A side
                        
                        int split = ccls[i];
                        if (!h.containsKey(split)) {
                            h.put(split, nextcls++); // make new class
                        }
                        
                        ccls[i] = h.get(split);
                    }
                }
            }
        }
        
        System.out.println();
        System.out.println("NFA has " + nextcls + " distinct character classes.");
        
        this.mapped_charset_size = nextcls;
    }
}

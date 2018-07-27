package by.progminer.JLexPHP.Utility;

import by.progminer.JLexPHP.Math.NFA;
import by.progminer.JLexPHP.Math.SparseBitSet;
import by.progminer.JLexPHP.Spec;

import java.util.Hashtable;

/**
 * Extract character classes from NFA and simplify.
 *
 * @author C. Scott Ananian 25-Jul-1999
 */
public class SimplifyNFA {
    
    /**
     * Character class mapping.
     */
    private int[] cCls;
    
    /**
     * Original charset size
     */
    private int originalCharsetSize;
    
    /**
     * Reduced charset size
     */
    private int mappedCharsetSize;
    
    public void simplify(Spec spec) {
        computeClasses(spec); // initialize fields
        
        for (NFA nfa: spec.nfaStates) {
            if (nfa.edge == NFA.EMPTY || nfa.edge == NFA.EPSILON) {
                continue; // no change
            }
            
            if (nfa.edge == NFA.CCL) {
                Set nSet = new Set();
                
                nSet.map(nfa.set, cCls); // map it
                nfa.set = nSet;
            } else {
                // single character
                
                nfa.edge = cCls[nfa.edge]; // map it
            }
        }
        
        // now update spec with the mapping
        spec.cClsMap = cCls;
        spec.dTransNCols = mappedCharsetSize;
    }
    
    /**
     * Compute minimum set of character classes needed to disambiguate
     * edges. We optimistically assume that every character belongs to
     * a single character class, and then incrementally split classes
     * as we see edges that require discrimination between characters in
     * the class. [CSA, 25-Jul-1999]
     */
    private void computeClasses(Spec spec) {
        this.originalCharsetSize = spec.dTransNCols;
        this.cCls = new int[originalCharsetSize]; // initially all zero
        
        int nextCls = 1;
        SparseBitSet clsA = new SparseBitSet(), clsB = new SparseBitSet();
        Hashtable<Integer, Integer> h = new Hashtable <Integer, Integer> ();
        
        System.err.print("Working on character classes.");
        
        for (NFA nfa: spec.nfaStates) {
            if (nfa.edge == NFA.EMPTY || nfa.edge == NFA.EPSILON) {
                continue; // no discriminatory information
            }
            
            clsA.clearAll();
            clsB.clearAll();
            
            for (int i = 0; i < cCls.length; i++) {
                if (
                    nfa.edge == i || // edge labeled with a character
                    (nfa.edge == NFA.CCL && nfa.set.contains(i)) // set of characters
                ) {
                    clsA.set(cCls[i]);
                } else {
                    clsB.set(cCls[i]);
                }
            }
            
            // now figure out which character classes we need to split
            
            clsA.and(clsB); // split the classes which show up on both sides of edge
            
            System.err.print(clsA.size() == 0? ".": ":");
            
            if (clsA.size() == 0) {
                continue; // nothing to do
            }
            
            // and split them
            
            h.clear(); // h will map old to new class name
            for (int i = 0; i < cCls.length; i++) {
                if (clsA.get(cCls[i])) { // a split class
                    if (nfa.edge == i || (nfa.edge == NFA.CCL && nfa.set.contains(i))) {
                        // on A side
                        
                        int split = cCls[i];
                        if (!h.containsKey(split)) {
                            h.put(split, nextCls++); // make new class
                        }
                        
                        cCls[i] = h.get(split);
                    }
                }
            }
        }
        
        System.err.println();
        System.err.println("NFA has " + nextCls + " distinct character classes.");
        
        this.mappedCharsetSize = nextCls;
    }
}

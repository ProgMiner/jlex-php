package by.progminer.JLexPHP;

class Alloc {
    
    static Dfa newDfa(CSpec spec) {
        Dfa dfa;
        
        dfa = new Dfa(spec.m_dfa_states.size());
        spec.m_dfa_states.addElement(dfa);
        
        return dfa;
    }
    
    static CNfaPair newNfaPair() {
        return new CNfaPair();
    }
    
    /**
     * Returns a new CNfaPair that matches a new
     * line: (\r\n?|[\n\uu2028\uu2029])
     *
     * Added by CSA 8-Aug-1999, updated 10-Aug-1999
     */
    static CNfaPair newNLPair(CSpec spec) {
        CNfaPair pair = newNfaPair();
        
        pair.m_end = newNfa(spec); // newline accepting state
        
        pair.m_start = newNfa(spec); // new state with two epsilon edges
        
        pair.m_start.m_next = newNfa(spec);
        pair.m_start.m_next.m_edge = CNfa.CCL;
        
        pair.m_start.m_next.m_set = new CSet();
        pair.m_start.m_next.m_set.add('\n');
        
        if (spec.m_dtrans_ncols - CSpec.NUM_PSEUDO > 2029) {
            pair.m_start.m_next.m_set.add(2028); // U+2028 is LS, the line separator
            pair.m_start.m_next.m_set.add(2029); // U+2029 is PS, the paragraph sep.
        }
        
        pair.m_start.m_next.m_next = pair.m_end; // accept '\n', U+2028, or U+2029
        
        pair.m_start.m_next2 = newNfa(spec);
        pair.m_start.m_next2.m_edge = '\r';
        
        pair.m_start.m_next2.m_next = newNfa(spec);
        pair.m_start.m_next2.m_next.m_next = pair.m_end; // accept '\r';
        
        pair.m_start.m_next2.m_next.m_next2 = newNfa(spec);
        pair.m_start.m_next2.m_next.m_next2.m_edge = '\n';
        pair.m_start.m_next2.m_next.m_next2.m_next = pair.m_end; // accept '\r\n';
        
        return pair;
    }
    
    static CNfa newNfa(CSpec spec) {
        CNfa p;
        
        // UNDONE: Buffer this?
        
        p = new CNfa();
        
        spec.m_nfa_states.addElement(p);
        p.m_edge = CNfa.EPSILON;
        
        return p;
    }
}
package by.progminer.JLexPHP;

class Alloc {
    
    static DFA newDFA(CSpec spec) {
        DFA dfa;
        
        dfa = new DFA(spec.m_DFA_states.size());
        spec.m_DFA_states.addElement(dfa);
        
        return dfa;
    }
    
    static CNfaPair newNFAPair() {
        return new CNfaPair();
    }
    
    /**
     * Returns a new CNfaPair that matches a new
     * line: (\r\n?|[\n\uu2028\uu2029])
     *
     * Added by CSA 8-Aug-1999, updated 10-Aug-1999
     */
    static CNfaPair newNLPair(CSpec spec) {
        CNfaPair pair = newNFAPair();
        
        pair.m_end = newNFA(spec); // newline accepting state
        
        pair.m_start = newNFA(spec); // new state with two epsilon edges
        
        pair.m_start.next = newNFA(spec);
        pair.m_start.next.edge = NFA.CCL;
        
        pair.m_start.next.set = new CSet();
        pair.m_start.next.set.add('\n');
        
        if (spec.m_dtrans_ncols - CSpec.NUM_PSEUDO > 2029) {
            pair.m_start.next.set.add(2028); // U+2028 is LS, the line separator
            pair.m_start.next.set.add(2029); // U+2029 is PS, the paragraph sep.
        }
        
        pair.m_start.next.next = pair.m_end; // accept '\n', U+2028, or U+2029
        
        pair.m_start.next2 = newNFA(spec);
        pair.m_start.next2.edge = '\r';
        
        pair.m_start.next2.next = newNFA(spec);
        pair.m_start.next2.next.next = pair.m_end; // accept '\r';
        
        pair.m_start.next2.next.next2 = newNFA(spec);
        pair.m_start.next2.next.next2.edge = '\n';
        pair.m_start.next2.next.next2.next = pair.m_end; // accept '\r\n';
        
        return pair;
    }
    
    static NFA newNFA(CSpec spec) {
        NFA p;
        
        // UNDONE: Buffer this?
        
        p = new NFA();
        
        spec.m_NFA_states.addElement(p);
        p.edge = NFA.EPSILON;
        
        return p;
    }
}

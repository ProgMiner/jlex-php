package by.progminer.JLexPHP;

class Alloc {
    
    static DFA newDFA(CSpec spec) {
        DFA dfa;
        
        dfa = new DFA(spec.m_DFA_states.size());
        spec.m_DFA_states.addElement(dfa);
        
        return dfa;
    }
    
    static NFAPair newNFAPair() {
        return new NFAPair();
    }
    
    /**
     * Added by CSA 8-Aug-1999, updated 10-Aug-1999
     *
     * @return a new NFAPair that matches a new line: (\r\n?|[\n\uu2028\uu2029])
     */
    static NFAPair newNLPair(CSpec spec) {
        NFAPair pair = newNFAPair();
        
        pair.end = newNFA(spec); // newline accepting state
        
        pair.start = newNFA(spec); // new state with two epsilon edges
        
        pair.start.next = newNFA(spec);
        pair.start.next.edge = NFA.CCL;
        
        pair.start.next.set = new Set();
        pair.start.next.set.add('\n');
        
        if (spec.m_dtrans_ncols - CSpec.NUM_PSEUDO > 2029) {
            pair.start.next.set.add(2028); // U+2028 is LS, the line separator
            pair.start.next.set.add(2029); // U+2029 is PS, the paragraph sep.
        }
        
        pair.start.next.next = pair.end; // accept '\n', U+2028, or U+2029
        
        pair.start.next2 = newNFA(spec);
        pair.start.next2.edge = '\r';
        
        pair.start.next2.next = newNFA(spec);
        pair.start.next2.next.next = pair.end; // accept '\r';
        
        pair.start.next2.next.next2 = newNFA(spec);
        pair.start.next2.next.next2.edge = '\n';
        pair.start.next2.next.next2.next = pair.end; // accept '\r\n';
        
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

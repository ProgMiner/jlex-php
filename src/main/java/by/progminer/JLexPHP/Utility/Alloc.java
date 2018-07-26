package by.progminer.JLexPHP.Utility;

import by.progminer.JLexPHP.*;
import by.progminer.JLexPHP.Math.DFA;
import by.progminer.JLexPHP.Math.NFA;
import by.progminer.JLexPHP.Math.NFAPair;

public class Alloc {
    
    public static DFA newDFA(Spec spec) {
        DFA dfa;
        
        dfa = new DFA(spec.dfaStates.size());
        spec.dfaStates.addElement(dfa);
        
        return dfa;
    }
    
    public static NFAPair newNFAPair() {
        return new NFAPair();
    }
    
    /**
     * Added by CSA 8-Aug-1999, updated 10-Aug-1999
     *
     * @return a new NFAPair that matches a new line: (\r\n?|[\n\uu2028\uu2029])
     */
    public static NFAPair newNLPair(Spec spec) {
        NFAPair pair = newNFAPair();
        
        pair.end = newNFA(spec); // newline accepting state
        
        pair.start = newNFA(spec); // new state with two epsilon edges
        
        pair.start.next = newNFA(spec);
        pair.start.next.edge = NFA.CCL;
        
        pair.start.next.set = new Set();
        pair.start.next.set.add('\n');
        
        if (spec.dTransNCols - Spec.NUM_PSEUDO > 2029) {
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
    
    public static NFA newNFA(Spec spec) {
        NFA p;
        
        // UNDONE: Buffer this?
        
        p = new NFA();
        
        spec.nfaStates.addElement(p);
        p.edge = NFA.EPSILON;
        
        return p;
    }
}

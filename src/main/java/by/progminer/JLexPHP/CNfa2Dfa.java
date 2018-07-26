package by.progminer.JLexPHP;

import java.util.Stack;
import java.util.Vector;

class CNfa2Dfa {
    
    private static final int NOT_IN_DSTATES = -1;
    
    private CSpec m_spec;
    private int m_unmarked_dfa;
    private LexGen m_lexGen;
    
    CNfa2Dfa() {
        reset();
    }
    
    private void set(LexGen lexGen, CSpec spec) {
        m_lexGen = lexGen;
        m_spec = spec;
        m_unmarked_dfa = 0;
    }
    
    private void reset() {
        m_lexGen = null;
        m_spec = null;
        m_unmarked_dfa = 0;
    }
    
    /**
     * High-level access function to module.
     */
    void make_dfa(LexGen lexGen, CSpec spec) {
        reset();
        set(lexGen, spec);
        
        make_dtrans();
        free_nfa_states();
        
        if (m_spec.m_verbose && CUtility.OLD_DUMP_DEBUG) {
            System.out.println(m_spec.m_dfa_states.size() + " DFA states in original machine.");
        }
        
        free_dfa_states();
    }
    
    /**
     * Creates uncompressed DTrans transition table.
     */
    private void make_dtrans() {
        Dfa dfa;
        Bunch bunch;
        int i;
        int nextstate;
        int size;
        DTrans dtrans;
        CNfa nfa;
        int istate;
        int nstates;
        
        System.out.print("Working on DFA states.");
        
        /* Reference passing type and initializations. */
        bunch = new Bunch();
        m_unmarked_dfa = 0;
        
        /* Allocate mapping array. */
        nstates = m_spec.m_state_rules.length;
        m_spec.m_state_dtrans = new int[nstates];
        
        for (istate = 0; nstates > istate; ++istate) {
            /* CSA bugfix: if we skip all zero size rules, then
               an specification with no rules produces an illegal
               lexer (0 states) instead of a lexer that rejects
               everything (1 nonaccepting state). [27-Jul-1999]
               
            if (0 == m_spec.m_state_rules[istate].size())
              {
            m_spec.m_state_dtrans[istate] = DTrans.F;
            continue;
              }
            */
            
            /* Create start state and initialize fields. */
            bunch.nfaSet = (Vector <CNfa>) m_spec.m_state_rules[istate].clone();
            sortStates(bunch.nfaSet);
            
            bunch.nfaBit = new SparseBitSet();
            
            /* Initialize bit set. */
            size = bunch.nfaSet.size();
            for (i = 0; size > i; ++i) {
                nfa = bunch.nfaSet.elementAt(i);
                bunch.nfaBit.set(nfa.m_label);
            }
            
            bunch.accept = null;
            bunch.anchor = CSpec.NONE;
            bunch.acceptIndex = CUtility.INT_MAX;
            
            e_closure(bunch);
            add_to_dstates(bunch);
            
            m_spec.m_state_dtrans[istate] = m_spec.m_dtrans_vector.size();
            
            /* Main loop of DTrans creation. */
            while (null != (dfa = get_unmarked())) {
                System.out.print(".");
                System.out.flush();
                
                if (CUtility.DEBUG) {
                    CUtility.ASSERT(!dfa.mark);
                }
                
                /* Get first unmarked node, then mark it. */
                dfa.mark = true;
                
                /* Allocate new DTrans, then initialize fields. */
                dtrans = new DTrans(m_spec.m_dtrans_vector.size(), m_spec);
                dtrans.accept = dfa.accept;
                dtrans.anchor = dfa.anchor;
                
                /* Set DTrans array for each character transition. */
                for (i = 0; i < m_spec.m_dtrans_ncols; ++i) {
                    if (CUtility.DEBUG) {
                        CUtility.ASSERT(0 <= i);
                        CUtility.ASSERT(m_spec.m_dtrans_ncols > i);
                    }
                    
                    /* Create new dfa set by attempting character transition. */
                    move(dfa.nfaSet, i, bunch);
                    if (null != bunch.nfaSet) {
                        e_closure(bunch);
                    }
                    
                    if (CUtility.DEBUG) {
                        CUtility.ASSERT((null == bunch.nfaSet
                                             && null == bunch.nfaBit)
                                            || (null != bunch.nfaSet
                                                    && null != bunch.nfaBit));
                    }
                    
                    /* Create new state or set state to empty. */
                    if (null == bunch.nfaSet) {
                        nextstate = DTrans.F;
                    } else {
                        nextstate = in_dstates(bunch);
                        
                        if (NOT_IN_DSTATES == nextstate) {
                            nextstate = add_to_dstates(bunch);
                        }
                    }
                    
                    if (CUtility.DEBUG) {
                        CUtility.ASSERT(nextstate < m_spec.m_dfa_states.size());
                    }
                    
                    dtrans.dtrans[i] = nextstate;
                }
                
                if (CUtility.DEBUG) {
                    CUtility.ASSERT(m_spec.m_dtrans_vector.size() == dfa.label);
                }
                
                m_spec.m_dtrans_vector.addElement(dtrans);
            }
        }
        
        System.out.println();
    }
    
    /***************************************************************
     Function: free_dfa_states
     **************************************************************/
    private void free_dfa_states
    (
    ) {
        m_spec.m_dfa_states = null;
        m_spec.m_dfa_sets = null;
    }
    
    /***************************************************************
     Function: free_nfa_states
     **************************************************************/
    private void free_nfa_states
    (
    ) {
        /* UNDONE: Remove references to nfas from within dfas. */
        /* UNDONE: Don't free CAccepts. */
        
        m_spec.m_nfa_states = null;
        m_spec.m_nfa_start = null;
        m_spec.m_state_rules = null;
    }
    
    /***************************************************************
     Function: e_closure
     Description: Alters and returns input set.
     **************************************************************/
    private void e_closure
    (
        Bunch bunch
    ) {
        Stack <CNfa> nfa_stack;
        int size;
        int i;
        CNfa state;
        
        /* Debug checks. */
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != bunch);
            CUtility.ASSERT(null != bunch.nfaSet);
            CUtility.ASSERT(null != bunch.nfaBit);
        }
        
        bunch.accept = null;
        bunch.anchor = CSpec.NONE;
        bunch.acceptIndex = CUtility.INT_MAX;
        
        /* Create initial stack. */
        nfa_stack = new Stack <CNfa> ();
        size = bunch.nfaSet.size();
        for (i = 0; i < size; ++i) {
            state = bunch.nfaSet.elementAt(i);
            
            if (CUtility.DEBUG) {
                CUtility.ASSERT(bunch.nfaBit.get(state.m_label));
            }
            
            nfa_stack.push(state);
        }
        
        /* Main loop. */
        while (!nfa_stack.empty()) {
            state = nfa_stack.pop();
            
            if (CUtility.OLD_DUMP_DEBUG) {
                if (null != state.m_accept) {
                    System.out.println(
                        "Looking at accepting state " + state.m_label +
                        " with <" + new String(state.m_accept.action, 0, state.m_accept.actionLength) + ">"
                    );
                }
            }
            
            if (null != state.m_accept && state.m_label < bunch.acceptIndex) {
                bunch.acceptIndex = state.m_label;
                bunch.accept = state.m_accept;
                bunch.anchor = state.m_anchor;
                
                if (CUtility.OLD_DUMP_DEBUG) {
                    System.out.println(
                        "Found accepting state " + state.m_label +
                        " with <" + new String(state.m_accept.action, 0, state.m_accept.actionLength) + ">"
                    );
                }
                
                if (CUtility.DEBUG) {
                    CUtility.ASSERT(null != bunch.accept);
                    CUtility.ASSERT(
                        CSpec.NONE == bunch.anchor ||
                        0 != (bunch.anchor & CSpec.END) ||
                        0 != (bunch.anchor & CSpec.START)
                    );
                }
            }
            
            if (CNfa.EPSILON == state.m_edge) {
                if (null != state.m_next) {
                    if (!bunch.nfaSet.contains(state.m_next)) {
                        if (CUtility.DEBUG) {
                            CUtility.ASSERT(!bunch.nfaBit.get(state.m_next.m_label));
                        }
                        
                        bunch.nfaBit.set(state.m_next.m_label);
                        bunch.nfaSet.addElement(state.m_next);
                        nfa_stack.push(state.m_next);
                    }
                }
                
                if (null != state.m_next2) {
                    if (!bunch.nfaSet.contains(state.m_next2)) {
                        if (CUtility.DEBUG) {
                            CUtility.ASSERT(!bunch.nfaBit.get(state.m_next2.m_label));
                        }
                        
                        bunch.nfaBit.set(state.m_next2.m_label);
                        bunch.nfaSet.addElement(state.m_next2);
                        nfa_stack.push(state.m_next2);
                    }
                }
            }
        }
        
        if (null != bunch.nfaSet) {
            sortStates(bunch.nfaSet);
        }
    }
    
    /***************************************************************
     Function: move
     Description: Returns null if resulting NFA set is empty.
     **************************************************************/
    void move(Vector nfa_set, int b, Bunch bunch) {
        int size;
        int index;
        CNfa state;
        
        bunch.nfaSet = null;
        bunch.nfaBit = null;
        
        size = nfa_set.size();
        for (index = 0; index < size; ++index) {
            state = (CNfa) nfa_set.elementAt(index);
            
            if (b == state.m_edge || (CNfa.CCL == state.m_edge && state.m_set.contains(b))) {
                if (null == bunch.nfaSet) {
                    if (CUtility.DEBUG) {
                        CUtility.ASSERT(null == bunch.nfaBit);
                    }
                    
                    bunch.nfaSet = new Vector <CNfa> ();
                    
                    // bunch.nfaBit = new SparseBitSet(m_spec.m_nfa_states.size());
                    bunch.nfaBit = new SparseBitSet();
                }
                
                bunch.nfaSet.addElement(state.m_next);
                
                // System.out.println("Size of bitset: " + bunch.nfaBit.size());
                // System.out.println("Reference index: " + state.m_next.label);
                // System.out.flush();
                
                bunch.nfaBit.set(state.m_next.m_label);
            }
        }
        
        if (null != bunch.nfaSet) {
            if (CUtility.DEBUG) {
                CUtility.ASSERT(null != bunch.nfaBit);
            }
            
            sortStates(bunch.nfaSet);
        }
    }
    
    /***************************************************************
     Function: sortStates
     **************************************************************/
    private void sortStates(Vector <CNfa> nfa_set) {
        CNfa elem;
        int begin;
        int size;
        int index;
        int value;
        int smallest_index;
        int smallest_value;
        CNfa begin_elem;
        
        size = nfa_set.size();
        for (begin = 0; begin < size; ++begin) {
            elem = nfa_set.elementAt(begin);
            smallest_value = elem.m_label;
            smallest_index = begin;
            
            for (index = begin + 1; index < size; ++index) {
                elem = nfa_set.elementAt(index);
                value = elem.m_label;
                
                if (value < smallest_value) {
                    smallest_index = index;
                    smallest_value = value;
                }
            }
            
            begin_elem = nfa_set.elementAt(begin);
            elem = nfa_set.elementAt(smallest_index);
            nfa_set.setElementAt(elem, begin);
            nfa_set.setElementAt(begin_elem, smallest_index);
        }
        
        if (CUtility.OLD_DEBUG) {
            System.out.print("NFA vector indices: ");
            
            for (index = 0; index < size; ++index) {
                elem = nfa_set.elementAt(index);
                System.out.print(elem.m_label + " ");
            }
            System.out.println();
        }
    }
    
    /***************************************************************
     Function: get_unmarked
     Description: Returns next unmarked DFA state.
     **************************************************************/
    private Dfa get_unmarked() {
        int size;
        Dfa dfa;
        
        size = m_spec.m_dfa_states.size();
        while (m_unmarked_dfa < size) {
            dfa = m_spec.m_dfa_states.elementAt(m_unmarked_dfa);
            
            if (!dfa.mark) {
                if (CUtility.OLD_DUMP_DEBUG) {
                    System.out.print("*");
                    System.out.flush();
                }
                
                if (m_spec.m_verbose && CUtility.OLD_DUMP_DEBUG) {
                    System.out.println("---------------");
                    System.out.print("working on DFA state "
                                         + m_unmarked_dfa
                                         + " = NFA states: ");
                    m_lexGen.printSet(dfa.nfaSet);
                    System.out.println();
                }
                
                return dfa;
            }
            
            ++m_unmarked_dfa;
        }
        
        return null;
    }
    
    /***************************************************************
     function: add_to_dstates
     Description: Takes as input a Bunch with details of
     a dfa state that needs to be created.
     1) Allocates a new dfa state and saves it in
     the appropriate CSpec vector.
     2) Initializes the fields of the dfa state
     with the information in the Bunch.
     3) Returns index of new dfa.
     **************************************************************/
    private int add_to_dstates
    (
        Bunch bunch
    ) {
        Dfa dfa;
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != bunch.nfaSet);
            CUtility.ASSERT(null != bunch.nfaBit);
            CUtility.ASSERT(null != bunch.accept
                                || CSpec.NONE == bunch.anchor);
        }
        
        /* Allocate, passing CSpec so dfa label can be set. */
        dfa = Alloc.newDfa(m_spec);
        
        /* Initialize fields, including the mark field. */
        dfa.nfaSet = (Vector) bunch.nfaSet.clone();
        dfa.nfaBit = (SparseBitSet) bunch.nfaBit.clone();
        dfa.accept = bunch.accept;
        dfa.anchor = bunch.anchor;
        dfa.mark = false;
        
        /* Register dfa state using BitSet in CSpec Hashtable. */
        m_spec.m_dfa_sets.put(dfa.nfaBit, dfa);
        // registerCDfa(dfa);
        
        if (CUtility.OLD_DUMP_DEBUG) {
            System.out.print("Registering set : ");
            m_lexGen.printSet(dfa.nfaSet);
            System.out.println();
        }
        
        return dfa.label;
    }
    
    /***************************************************************
     Function: in_dstates
     **************************************************************/
    private int in_dstates
    (
        Bunch bunch
    ) {
        Dfa dfa;
        
        if (CUtility.OLD_DEBUG) {
            System.out.print("Looking for set : ");
            m_lexGen.printSet(bunch.nfaSet);
        }
        
        dfa = m_spec.m_dfa_sets.get(bunch.nfaBit);
        
        if (null != dfa) {
            if (CUtility.OLD_DUMP_DEBUG) {
                System.out.println(" FOUND!");
            }
            
            return dfa.label;
        }
        
        if (CUtility.OLD_DUMP_DEBUG) {
            System.out.println(" NOT FOUND!");
        }
        return NOT_IN_DSTATES;
    }
    
}

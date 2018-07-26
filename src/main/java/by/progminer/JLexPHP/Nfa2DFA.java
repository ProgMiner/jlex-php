package by.progminer.JLexPHP;

import java.util.Stack;
import java.util.Vector;

class Nfa2DFA {

    private static final int NOT_IN_DSTATES = -1;

    private CSpec spec;
    private int unmarkedDFA;
    private LexGen lexGen;

    Nfa2DFA() {
        reset();
    }

    private void set(LexGen lexGen, CSpec spec) {
        this.lexGen = lexGen;
        this.spec = spec;
        unmarkedDFA = 0;
    }

    private void reset() {
        lexGen = null;
        spec = null;
        unmarkedDFA = 0;
    }

    /**
     * High-level access function to module.
     */
    void makeDFA(LexGen lexGen, CSpec spec) {
        reset();
        set(lexGen, spec);

        makeDTrans();
        free_nfa_states();

        if (this.spec.m_verbose && CUtility.OLD_DUMP_DEBUG) {
            System.out.println(this.spec.m_DFA_states.size() + " DFA states in original machine.");
        }

        free_dfa_states();
    }

    /**
     * Creates uncompressed DTrans transition table.
     */
    private void makeDTrans() {
        DFA dfa;
        Bunch bunch;
        int i;
        int nextstate;
        int size;
        DTrans dTrans;
        NFA nfa;
        int istate;
        int nstates;

        System.out.print("Working on DFA states.");

        /* Reference passing type and initializations. */
        bunch = new Bunch();
        unmarkedDFA = 0;

        /* Allocate mapping array. */
        nstates = spec.m_state_rules.length;
        spec.m_state_dtrans = new int[nstates];

        for (istate = 0; nstates > istate; ++istate) {
            /* CSA bugfix: if we skip all zero size rules, then
               an specification with no rules produces an illegal
               lexer (0 states) instead of a lexer that rejects
               everything (1 nonaccepting state). [27-Jul-1999]

            if (0 == spec.m_state_rules[istate].size())
              {
            spec.m_state_dtrans[istate] = DTrans.F;
            continue;
              }
            */

            /* Create start state and initialize fields. */
            bunch.nfaSet = (Vector <NFA>) spec.m_state_rules[istate].clone();
            sortStates(bunch.nfaSet);

            bunch.nfaBit = new SparseBitSet();

            /* Initialize bit set. */
            size = bunch.nfaSet.size();
            for (i = 0; size > i; ++i) {
                nfa = bunch.nfaSet.elementAt(i);
                bunch.nfaBit.set(nfa.label);
            }

            bunch.accept = null;
            bunch.anchor = CSpec.NONE;
            bunch.acceptIndex = CUtility.INT_MAX;

            e_closure(bunch);
            add_to_dstates(bunch);

            spec.m_state_dtrans[istate] = spec.m_dtrans_vector.size();

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
                dTrans = new DTrans(spec.m_dtrans_vector.size(), spec);
                dTrans.accept = dfa.accept;
                dTrans.anchor = dfa.anchor;

                /* Set DTrans array for each character transition. */
                for (i = 0; i < spec.m_dtrans_ncols; ++i) {
                    if (CUtility.DEBUG) {
                        CUtility.ASSERT(0 <= i);
                        CUtility.ASSERT(spec.m_dtrans_ncols > i);
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
                        CUtility.ASSERT(nextstate < spec.m_DFA_states.size());
                    }

                    dTrans.dtrans[i] = nextstate;
                }

                if (CUtility.DEBUG) {
                    CUtility.ASSERT(spec.m_dtrans_vector.size() == dfa.label);
                }

                spec.m_dtrans_vector.addElement(dTrans);
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
        spec.m_DFA_states = null;
        spec.m_dfa_sets = null;
    }

    /***************************************************************
     Function: free_nfa_states
     **************************************************************/
    private void free_nfa_states
    (
    ) {
        /* UNDONE: Remove references to nfas from within dfas. */
        /* UNDONE: Don't free CAccepts. */

        spec.m_NFA_states = null;
        spec.m_NFA_start = null;
        spec.m_state_rules = null;
    }

    /***************************************************************
     Function: e_closure
     Description: Alters and returns input set.
     **************************************************************/
    private void e_closure
    (
        Bunch bunch
    ) {
        Stack <NFA> NFA_stack;
        int size;
        int i;
        NFA state;

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
        NFA_stack = new Stack <NFA> ();
        size = bunch.nfaSet.size();
        for (i = 0; i < size; ++i) {
            state = bunch.nfaSet.elementAt(i);

            if (CUtility.DEBUG) {
                CUtility.ASSERT(bunch.nfaBit.get(state.label));
            }

            NFA_stack.push(state);
        }

        /* Main loop. */
        while (!NFA_stack.empty()) {
            state = NFA_stack.pop();

            if (CUtility.OLD_DUMP_DEBUG) {
                if (null != state.accept) {
                    System.out.println(
                        "Looking at accepting state " + state.label +
                        " with <" + new String(state.accept.action, 0, state.accept.actionLength) + ">"
                    );
                }
            }

            if (null != state.accept && state.label < bunch.acceptIndex) {
                bunch.acceptIndex = state.label;
                bunch.accept = state.accept;
                bunch.anchor = state.anchor;

                if (CUtility.OLD_DUMP_DEBUG) {
                    System.out.println(
                        "Found accepting state " + state.label +
                        " with <" + new String(state.accept.action, 0, state.accept.actionLength) + ">"
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

            if (NFA.EPSILON == state.edge) {
                if (null != state.next) {
                    if (!bunch.nfaSet.contains(state.next)) {
                        if (CUtility.DEBUG) {
                            CUtility.ASSERT(!bunch.nfaBit.get(state.next.label));
                        }

                        bunch.nfaBit.set(state.next.label);
                        bunch.nfaSet.addElement(state.next);
                        NFA_stack.push(state.next);
                    }
                }

                if (null != state.next2) {
                    if (!bunch.nfaSet.contains(state.next2)) {
                        if (CUtility.DEBUG) {
                            CUtility.ASSERT(!bunch.nfaBit.get(state.next2.label));
                        }

                        bunch.nfaBit.set(state.next2.label);
                        bunch.nfaSet.addElement(state.next2);
                        NFA_stack.push(state.next2);
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
        NFA state;

        bunch.nfaSet = null;
        bunch.nfaBit = null;

        size = nfa_set.size();
        for (index = 0; index < size; ++index) {
            state = (NFA) nfa_set.elementAt(index);

            if (b == state.edge || (NFA.CCL == state.edge && state.set.contains(b))) {
                if (null == bunch.nfaSet) {
                    if (CUtility.DEBUG) {
                        CUtility.ASSERT(null == bunch.nfaBit);
                    }

                    bunch.nfaSet = new Vector <NFA> ();

                    // bunch.nfaBit = new SparseBitSet(spec.m_NFA_states.size());
                    bunch.nfaBit = new SparseBitSet();
                }

                bunch.nfaSet.addElement(state.next);

                // System.out.println("Size of bitset: " + bunch.nfaBit.size());
                // System.out.println("Reference index: " + state.next.label);
                // System.out.flush();

                bunch.nfaBit.set(state.next.label);
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
    private void sortStates(Vector <NFA> NFA_set) {
        NFA elem;
        int begin;
        int size;
        int index;
        int value;
        int smallest_index;
        int smallest_value;
        NFA begin_elem;

        size = NFA_set.size();
        for (begin = 0; begin < size; ++begin) {
            elem = NFA_set.elementAt(begin);
            smallest_value = elem.label;
            smallest_index = begin;

            for (index = begin + 1; index < size; ++index) {
                elem = NFA_set.elementAt(index);
                value = elem.label;

                if (value < smallest_value) {
                    smallest_index = index;
                    smallest_value = value;
                }
            }

            begin_elem = NFA_set.elementAt(begin);
            elem = NFA_set.elementAt(smallest_index);
            NFA_set.setElementAt(elem, begin);
            NFA_set.setElementAt(begin_elem, smallest_index);
        }

        if (CUtility.OLD_DEBUG) {
            System.out.print("NFA vector indices: ");

            for (index = 0; index < size; ++index) {
                elem = NFA_set.elementAt(index);
                System.out.print(elem.label + " ");
            }
            System.out.println();
        }
    }

    /***************************************************************
     Function: get_unmarked
     Description: Returns next unmarked DFA state.
     **************************************************************/
    private DFA get_unmarked() {
        int size;
        DFA dfa;

        size = spec.m_DFA_states.size();
        while (unmarkedDFA < size) {
            dfa = spec.m_DFA_states.elementAt(unmarkedDFA);

            if (!dfa.mark) {
                if (CUtility.OLD_DUMP_DEBUG) {
                    System.out.print("*");
                    System.out.flush();
                }

                if (spec.m_verbose && CUtility.OLD_DUMP_DEBUG) {
                    System.out.println("---------------");
                    System.out.print("working on DFA state "
                                         + unmarkedDFA
                                         + " = NFA states: ");
                    lexGen.printSet(dfa.nfaSet);
                    System.out.println();
                }

                return dfa;
            }

            ++unmarkedDFA;
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
        DFA dfa;

        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != bunch.nfaSet);
            CUtility.ASSERT(null != bunch.nfaBit);
            CUtility.ASSERT(null != bunch.accept
                                || CSpec.NONE == bunch.anchor);
        }

        /* Allocate, passing CSpec so DFA label can be set. */
        dfa = Alloc.newDFA(spec);

        /* Initialize fields, including the mark field. */
        dfa.nfaSet = (Vector) bunch.nfaSet.clone();
        dfa.nfaBit = (SparseBitSet) bunch.nfaBit.clone();
        dfa.accept = bunch.accept;
        dfa.anchor = bunch.anchor;
        dfa.mark = false;

        /* Register DFA state using BitSet in CSpec Hashtable. */
        spec.m_dfa_sets.put(dfa.nfaBit, dfa);
        // registerCDfa(DFA);

        if (CUtility.OLD_DUMP_DEBUG) {
            System.out.print("Registering set : ");
            lexGen.printSet(dfa.nfaSet);
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
        DFA dfa;

        if (CUtility.OLD_DEBUG) {
            System.out.print("Looking for set : ");
            lexGen.printSet(bunch.nfaSet);
        }

        dfa = spec.m_dfa_sets.get(bunch.nfaBit);

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

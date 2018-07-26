package by.progminer.JLexPHP;

import java.util.Stack;
import java.util.Vector;

class Nfa2DFA {

    private static final int NOT_IN_DSTATES = -1;

    private Spec spec;
    private int unmarkedDFA;
    private LexGen lexGen;

    Nfa2DFA() {
        reset();
    }

    private void set(LexGen lexGen, Spec spec) {
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
    void makeDFA(LexGen lexGen, Spec spec) {
        reset();
        set(lexGen, spec);

        makeDTrans();
        freeNFAStates();

        if (this.spec.verbose && Utility.OLD_DUMP_DEBUG) {
            System.out.println(this.spec.dfaStates.size() + " DFA states in original machine.");
        }

        freeDFAStates();
    }

    /**
     * Creates uncompressed DTrans transition table.
     */
    private void makeDTrans() {
        System.out.print("Working on DFA states.");

        // Reference passing type and initializations
        Bunch bunch = new Bunch();
        unmarkedDFA = 0;

        // Allocate mapping array
        int nStates = spec.stateRules.length;
        spec.stateDTrans = new int[nStates];

        for (int i = 0; nStates > i; ++i) {
            // CSA bugfix: if we skip all zero size rules, then
            // an specification with no rules produces an illegal
            // lexer (0 states) instead of a lexer that rejects
            // everything (1 nonaccepting state). [27-Jul-1999]

            // if (0 == spec.stateRules[i].size()) {
            //     spec.stateDTrans[i] = DTrans.F;
            //     continue;
            // }

            // Create start state and initialize fields
            bunch.nfaSet = (Vector <NFA>) spec.stateRules[i].clone();
            sortStates(bunch.nfaSet);

            bunch.nfaBit = new SparseBitSet();

            // Initialize bit set
            for (int j = 0; bunch.nfaSet.size() > j; ++j) {
                bunch.nfaBit.set(bunch.nfaSet.elementAt(j).label);
            }

            bunch.accept = null;
            bunch.anchor = Spec.NONE;
            bunch.acceptIndex = Utility.INT_MAX;

            eClosure(bunch);
            addToDStates(bunch);

            spec.stateDTrans[i] = spec.dTransVector.size();

            // Main loop of DTrans creation
            DFA dfa;
            while (null != (dfa = getUnmarked())) {
                System.out.print(".");
                System.out.flush();

                if (Utility.DEBUG) {
                    Utility.ASSERT(!dfa.mark);
                }

                // Get first unmarked node, then mark it
                dfa.mark = true;

                // Allocate new DTrans, then initialize fields
                DTrans dTrans = new DTrans(spec.dTransVector.size(), spec);
                dTrans.accept = dfa.accept;
                dTrans.anchor = dfa.anchor;

                // Set DTrans array for each character transition
                for (int j = 0; j < spec.dTransNCols; ++j) {
                    if (Utility.DEBUG) {
                        Utility.ASSERT(spec.dTransNCols > j);
                    }

                    // Create new dfa set by attempting character transition
                    move(dfa.nfaSet, j, bunch);
                    if (null != bunch.nfaSet) {
                        eClosure(bunch);
                    }

                    if (Utility.DEBUG) {
                        Utility.ASSERT(
                            (null == bunch.nfaSet && null == bunch.nfaBit) ||
                            (null != bunch.nfaSet && null != bunch.nfaBit)
                        );
                    }

                    // Create new state or set state to empty
                    int nextState;
                    if (null == bunch.nfaSet) {
                        nextState = DTrans.F;
                    } else {
                        nextState = inDStates(bunch);

                        if (NOT_IN_DSTATES == nextState) {
                            nextState = addToDStates(bunch);
                        }
                    }

                    if (Utility.DEBUG) {
                        Utility.ASSERT(nextState < spec.dfaStates.size());
                    }

                    dTrans.dtrans[j] = nextState;
                }

                if (Utility.DEBUG) {
                    Utility.ASSERT(spec.dTransVector.size() == dfa.label);
                }

                spec.dTransVector.addElement(dTrans);
            }
        }

        System.out.println();
    }

    private void freeDFAStates() {
        spec.dfaStates = null;
        spec.dfaSets = null;
    }

    private void freeNFAStates() {
        // TODO: UNDONE: Remove references to NFAs from within DFAs
        // TODO: UNDONE: Don't free CAccepts

        spec.nfaStates = null;
        spec.nfaStart = null;
        spec.stateRules = null;
    }

    /**
     * Alters and returns input set.
     */
    private void eClosure(Bunch bunch) {
        // Debug checks
        if (Utility.DEBUG) {
            Utility.ASSERT(null != bunch);

            // For IDE
            if (null == bunch) { throw new Error(); }

            Utility.ASSERT(null != bunch.nfaSet);
            Utility.ASSERT(null != bunch.nfaBit);
        }

        bunch.accept = null;
        bunch.anchor = Spec.NONE;
        bunch.acceptIndex = Utility.INT_MAX;

        // Create initial stack
        Stack <NFA> NFAStack = new Stack <NFA> ();
        int size = bunch.nfaSet.size();
        for (int i = 0; i < size; ++i) {
            NFA state = bunch.nfaSet.elementAt(i);

            if (Utility.DEBUG) {
                Utility.ASSERT(bunch.nfaBit.get(state.label));
            }

            NFAStack.push(state);
        }

        // Main loop
        while (!NFAStack.empty()) {
            NFA state = NFAStack.pop();

            if (Utility.OLD_DUMP_DEBUG) {
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

                if (Utility.OLD_DUMP_DEBUG) {
                    System.out.println(
                        "Found accepting state " + state.label +
                        " with <" + new String(state.accept.action, 0, state.accept.actionLength) + ">"
                    );
                }

                if (Utility.DEBUG) {
                    Utility.ASSERT(null != bunch.accept);
                    Utility.ASSERT(
                        Spec.NONE == bunch.anchor ||
                        0 != (bunch.anchor & Spec.END) ||
                        0 != (bunch.anchor & Spec.START)
                    );
                }
            }

            if (NFA.EPSILON == state.edge) {
                if (null != state.next) {
                    if (!bunch.nfaSet.contains(state.next)) {
                        if (Utility.DEBUG) {
                            Utility.ASSERT(!bunch.nfaBit.get(state.next.label));
                        }

                        bunch.nfaBit.set(state.next.label);
                        bunch.nfaSet.addElement(state.next);
                        NFAStack.push(state.next);
                    }
                }

                if (null != state.next2) {
                    if (!bunch.nfaSet.contains(state.next2)) {
                        if (Utility.DEBUG) {
                            Utility.ASSERT(!bunch.nfaBit.get(state.next2.label));
                        }

                        bunch.nfaBit.set(state.next2.label);
                        bunch.nfaSet.addElement(state.next2);
                        NFAStack.push(state.next2);
                    }
                }
            }
        }

        if (null != bunch.nfaSet) {
            sortStates(bunch.nfaSet);
        }
    }

    /**
     * Returns null if resulting NFA set is empty.
     */
    void move(Vector <NFA> nfaSet, int b, Bunch bunch) {
        bunch.nfaSet = null;
        bunch.nfaBit = null;

        for (int i = 0; i < nfaSet.size(); ++i) {
            NFA state = nfaSet.elementAt(i);

            if (b == state.edge || (NFA.CCL == state.edge && state.set.contains(b))) {
                if (null == bunch.nfaSet) {
                    if (Utility.DEBUG) {
                        Utility.ASSERT(null == bunch.nfaBit);
                    }

                    bunch.nfaSet = new Vector <NFA> ();

                    // bunch.nfaBit = new SparseBitSet(spec.nfaStates.size());
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
            if (Utility.DEBUG) {
                Utility.ASSERT(null != bunch.nfaBit);
            }

            sortStates(bunch.nfaSet);
        }
    }

    private void sortStates(Vector <NFA> NFASet) {
        int size = NFASet.size();

        for (int begin = 0; begin < size; ++begin) {
            int smallestValue = NFASet.elementAt(begin).label;
            int smallestIndex = begin;

            for (int index = begin + 1; index < size; ++index) {
                int value = NFASet.elementAt(index).label;

                if (value < smallestValue) {
                    smallestIndex = index;
                    smallestValue = value;
                }
            }

            NFA beginElem = NFASet.elementAt(begin);
            NFASet.setElementAt(NFASet.elementAt(smallestIndex), begin);
            NFASet.setElementAt(beginElem, smallestIndex);
        }

        if (Utility.OLD_DEBUG) {
            System.out.print("NFA vector indices: ");

            for (int index = 0; index < size; ++index) {
                System.out.print(NFASet.elementAt(index).label + " ");
            }

            System.out.println();
        }
    }

    /**
     * Returns next unmarked DFA state.
     */
    private DFA getUnmarked() {
        while (unmarkedDFA < spec.dfaStates.size()) {
            DFA dfa = spec.dfaStates.elementAt(unmarkedDFA);

            if (!dfa.mark) {
                if (Utility.OLD_DUMP_DEBUG) {
                    System.out.print("*");
                    System.out.flush();
                }

                if (spec.verbose && Utility.OLD_DUMP_DEBUG) {
                    System.out.println("---------------");
                    System.out.print("working on DFA state " + unmarkedDFA + " = NFA states: ");
                    lexGen.printSet(dfa.nfaSet);
                    System.out.println();
                }

                return dfa;
            }

            ++unmarkedDFA;
        }

        return null;
    }

    /**
     * @param bunch a Bunch with details of
     *              a DFA state that needs to be created.
     *
     * 1) Allocates a new DFA state and saves it in
     * the appropriate Spec vector.
     * 2) Initializes the fields of the DFA state
     * with the information in the Bunch.
     *
     * @return index of new dfa.
     */
    private int addToDStates(Bunch bunch) {
        if (Utility.DEBUG) {
            Utility.ASSERT(null != bunch.nfaSet);
            Utility.ASSERT(null != bunch.nfaBit);
            Utility.ASSERT(
                null != bunch.accept ||
                Spec.NONE == bunch.anchor
            );
        }

        // Allocate, passing Spec so DFA label can be set
        DFA dfa = Alloc.newDFA(spec);

        // Initialize fields, including the mark field
        dfa.nfaSet = (Vector <NFA>) bunch.nfaSet.clone();
        dfa.nfaBit = (SparseBitSet) bunch.nfaBit.clone();
        dfa.accept = bunch.accept;
        dfa.anchor = bunch.anchor;
        dfa.mark = false;

        // Register DFA state using BitSet in Spec Hashtable
        spec.dfaSets.put(dfa.nfaBit, dfa);
        // registerCDfa(DFA);

        if (Utility.OLD_DUMP_DEBUG) {
            System.out.print("Registering set : ");
            lexGen.printSet(dfa.nfaSet);
            System.out.println();
        }

        return dfa.label;
    }

    private int inDStates(Bunch bunch) {
        if (Utility.OLD_DEBUG) {
            System.out.print("Looking for set : ");
            lexGen.printSet(bunch.nfaSet);
        }

        DFA dfa = spec.dfaSets.get(bunch.nfaBit);

        if (null != dfa) {
            if (Utility.OLD_DUMP_DEBUG) {
                System.out.println(" FOUND!");
            }

            return dfa.label;
        }

        if (Utility.OLD_DUMP_DEBUG) {
            System.out.println(" NOT FOUND!");
        }

        return NOT_IN_DSTATES;
    }

}

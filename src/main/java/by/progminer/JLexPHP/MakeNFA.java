package by.progminer.JLexPHP;

import java.io.IOException;
import java.util.Vector;

class MakeNFA {

    private Spec spec;
    private LexGen lexGen;
    private Input input;
    MakeNFA() {
        reset();
    }

    /**
     * Resets MakeNFA member variables.
     */
    private void reset() {
        input = null;
        lexGen = null;
        spec = null;
    }

    /**
     * Sets MakeNFA member variables.
     */
    private void set(LexGen lexGen, Spec spec, Input input) {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != lexGen);
            CUtility.ASSERT(null != spec);
        }

        this.input = input;
        this.lexGen = lexGen;
        this.spec = spec;
    }

    /**
     * Expands character class to include special BOL and
     * EOF characters. Puts numeric index of these characters in
     * input Spec.
     */
    void allocateBolEof(Spec spec) {
        //noinspection ConstantConditions
        CUtility.ASSERT(Spec.NUM_PSEUDO == 2);

        spec.BOL = spec.dTransNCols++;
        spec.EOF = spec.dTransNCols++;
    }

    /**
     * High level access function to module.
     * Deposits result in input Spec.
     */
    void thompson(LexGen lexGen, Spec spec, Input input) throws IOException {
        // Set member variables
        reset();
        set(lexGen, spec, input);

        int size = this.spec.states.size();
        this.spec.stateRules = new Vector[size];

        for (int i = 0; i < size; ++i) {
            this.spec.stateRules[i] = new Vector <NFA> ();
        }

        this.spec.nfaStart = machine();

        // Set labels in created NFA machine
        size = this.spec.nfaStates.size();
        for (int i = 0; i < size; ++i) {
            NFA elem = this.spec.nfaStates.elementAt(i);
            elem.label = i;
        }

        // Debugging output
        if (CUtility.DO_DEBUG) {
            this.lexGen.printNFA();
        }

        if (this.spec.verbose) {
            System.out.println("NFA comprised of " + (this.spec.nfaStates.size() + 1) + " states.");
        }

        reset();
    }

    private void discardNFA(NFA nfa) {
        spec.nfaStates.removeElement(nfa);
    }

    private void processStates(SparseBitSet states, NFA current) {
        int size = spec.states.size();

        for (int i = 0; i < size; ++i) {
            if (states.get(i)) {
                spec.stateRules[i].addElement(current);
            }
        }
    }

    /**
     * Recursive descent regular expression parser.
     */
    private NFA machine() throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("machine", spec.lexeme, spec.currentToken);
        }

        NFA start = Alloc.newNFA(spec);
        NFA p = start;

        SparseBitSet states = lexGen.getStates();

        // Begin: Added for states
        spec.currentToken = LexGen.EOS;
        lexGen.advance();
        // End: Added for states

        if (LexGen.END_OF_INPUT != spec.currentToken) {
            // CSA fix

            p.next = rule();
            processStates(states, p.next);
        }

        while (LexGen.END_OF_INPUT != spec.currentToken) {
            // Make state changes HERE
            states = lexGen.getStates();

            // Begin: Added for states
            lexGen.advance();
            if (LexGen.END_OF_INPUT == spec.currentToken) {
                break;
            }
            // End: Added for states

            p.next2 = Alloc.newNFA(spec);
            p = p.next2;
            p.next = rule();

            processStates(states, p.next);
        }

        // CSA: add pseudo-rules for BOL and EOF
        SparseBitSet all_states = new SparseBitSet();
        for (int i = 0; i < spec.states.size(); ++i) {
            all_states.set(i);
        }

        p.next2 = Alloc.newNFA(spec);
        p = p.next2;

        p.next = Alloc.newNFA(spec);

        p.next.edge = NFA.CCL;
        p.next.next = Alloc.newNFA(spec);

        p.next.set = new Set();
        p.next.set.add(spec.BOL);
        p.next.set.add(spec.EOF);

        // do-nothing accept rule
        p.next.next.accept = new Accept(new char[0], 0, input.lineNumber + 1);
        processStates(all_states, p.next);
        // CSA: done

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("machine", spec.lexeme, spec.currentToken);
        }

        return start;
    }

    /**
     * Recursive descent regular expression parser.
     */
    private NFA rule() throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("rule", spec.lexeme, spec.currentToken);
        }

        int anchor = Spec.NONE;
        NFA start, end;

        NFAPair pair = Alloc.newNFAPair();
        if (LexGen.AT_BOL == spec.currentToken) {
            anchor = anchor | Spec.START;
            lexGen.advance();
            expr(pair);

            // CSA: fixed beginning-of-line operator. 8-aug-1999

            start = Alloc.newNFA(spec);
            start.edge = spec.BOL;
            start.next = pair.start;

            end = pair.end;
        } else {
            expr(pair);
            start = pair.start;
            end = pair.end;
        }

        if (LexGen.AT_EOL == spec.currentToken) {
            lexGen.advance();

            // CSA: fixed end-of-line operator. 8-aug-1999

            NFAPair nlPair = Alloc.newNLPair(spec);

            end.next = Alloc.newNFA(spec);
            end.next.next = nlPair.start;

            end.next.next2 = Alloc.newNFA(spec);
            end.next.next2.edge = spec.EOF;
            end.next.next2.next = nlPair.end;

            end = nlPair.end;

            anchor = anchor | Spec.END;
        }

        // Check for null rules. Charles Fischer found this bug [CSA]
        if (end == null) {
            Error.parseError(Error.E_ZERO, input.lineNumber);

            // For IDE
            throw new java.lang.Error();
        }

        // Handle end of regular expression. See page 103
        end.accept = lexGen.packAccept();
        end.anchor = anchor;

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("rule", spec.lexeme, spec.currentToken);
        }

        return start;
    }

    /**
     * Recursive descent regular expression parser.
     */
    private void expr(NFAPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("expr", spec.lexeme, spec.currentToken);
        }

        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != pair);
        }

        NFAPair e2Pair = Alloc.newNFAPair();

        catExpr(pair);
        while (LexGen.OR == spec.currentToken) {
            lexGen.advance();
            catExpr(e2Pair);

            NFA p = Alloc.newNFA(spec);
            p.next2 = e2Pair.start;
            p.next = pair.start;
            pair.start = p;

            p = Alloc.newNFA(spec);
            e2Pair.end.next = p;
            pair.end.next = p;
            pair.end = p;
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("expr", spec.lexeme, spec.currentToken);
        }
    }

    /**
     * Recursive descent regular expression parser.
     */
    private void catExpr(NFAPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("catExpr", spec.lexeme, spec.currentToken);
        }

        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != pair);
        }

        NFAPair e2Pair = Alloc.newNFAPair();

        if (firstInCat(spec.currentToken)) {
            factor(pair);
        }

        while (firstInCat(spec.currentToken)) {
            factor(e2Pair);

            // Destroy
            pair.end.mimic(e2Pair.start);
            discardNFA(e2Pair.start);

            pair.end = e2Pair.end;
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("catExpr", spec.lexeme, spec.currentToken);
        }
    }

    /**
     * Recursive descent regular expression parser.
     */
    private boolean firstInCat(int token) {
        switch (token) {
        case LexGen.CLOSE_PAREN:
        case LexGen.AT_EOL:
        case LexGen.OR:
        case LexGen.EOS:
            return false;

        case LexGen.CLOSURE:
        case LexGen.PLUS_CLOSE:
        case LexGen.OPTIONAL:
            Error.parseError(Error.E_CLOSE, input.lineNumber);
            return false;

        case LexGen.CCL_END:
            Error.parseError(Error.E_BRACKET, input.lineNumber);
            return false;

        case LexGen.AT_BOL:
            Error.parseError(Error.E_BOL, input.lineNumber);
            return false;

        default:
            break;
        }

        return true;
    }

    /**
     * Recursive descent regular expression parser.
     */
    private void factor(NFAPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("factor", spec.lexeme, spec.currentToken);
        }

        term(pair);

        if (
            LexGen.CLOSURE == spec.currentToken ||
            LexGen.PLUS_CLOSE == spec.currentToken ||
            LexGen.OPTIONAL == spec.currentToken
        ) {
            NFA start = Alloc.newNFA(spec),
                 end = Alloc.newNFA(spec);

            start.next = pair.start;
            pair.end.next = end;

            if (
                LexGen.CLOSURE == spec.currentToken ||
                LexGen.OPTIONAL == spec.currentToken
            ) {
                start.next2 = end;
            }

            if (
                LexGen.CLOSURE == spec.currentToken ||
                LexGen.PLUS_CLOSE == spec.currentToken
            ) {
                pair.end.next2 = pair.start;
            }

            pair.start = start;
            pair.end = end;
            lexGen.advance();
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("factor", spec.lexeme, spec.currentToken);
        }
    }

    /**
     * Recursive descent regular expression parser.
     */
    private void term(NFAPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("term", spec.lexeme, spec.currentToken);
        }

        if (LexGen.OPEN_PAREN == spec.currentToken) {
            lexGen.advance();
            expr(pair);

            if (LexGen.CLOSE_PAREN == spec.currentToken) {
                lexGen.advance();
            } else {
                Error.parseError(Error.E_SYNTAX, input.lineNumber);
            }
        } else {
            NFA start = Alloc.newNFA(spec);
            pair.start = start;

            start.next = Alloc.newNFA(spec);
            pair.end = start.next;

            boolean isAlphaL = (LexGen.L == spec.currentToken && Character.isLetter(spec.lexeme));
            if (
                LexGen.ANY != spec.currentToken &&
                LexGen.CCL_START != spec.currentToken &&
                (!spec.ignoreCase || !isAlphaL)
            ) {
                start.edge = spec.lexeme;
                lexGen.advance();
            } else {
                start.edge = NFA.CCL;

                start.set = new Set();

                // Match case-insensitive letters using character class
                if (spec.ignoreCase && isAlphaL) {
                    start.set.addNCase(spec.lexeme);
                }

                // Match dot (.) using character class
                else if (LexGen.ANY == spec.currentToken) {
                    start.set.add('\n');
                    start.set.add('\r');

                    // CSA: exclude BOL and EOF from character classes
                    start.set.add(spec.BOL);
                    start.set.add(spec.EOF);
                    start.set.complement();
                } else {
                    lexGen.advance();

                    if (LexGen.AT_BOL == spec.currentToken) {
                        lexGen.advance();

                        // CSA: exclude BOL and EOF from character classes
                        start.set.add(spec.BOL);
                        start.set.add(spec.EOF);
                        start.set.complement();
                    }

                    if (LexGen.CCL_END != spec.currentToken) {
                        doDash(start.set);
                    }
                }

                lexGen.advance();
            }
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("term", spec.lexeme, spec.currentToken);
        }
    }

    /**
     * Recursive descent regular expression parser.
     */
    private void doDash(Set set) throws IOException {
        int first = -1;

        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("doDash", spec.lexeme, spec.currentToken);
        }

        while (
            LexGen.EOS != spec.currentToken &&
            LexGen.CCL_END != spec.currentToken
        ) {
            // DASH loses its special meaning if it is first in class.

            if (LexGen.DASH == spec.currentToken && -1 != first) {
                lexGen.advance();

                // DASH loses its special meaning if it is last in class.
                if (spec.currentToken == LexGen.CCL_END) {
                    // 'first' already in set.

                    set.add('-');
                    break;
                }

                for (; first <= spec.lexeme; ++first) {
                    if (spec.ignoreCase) {
                        set.addNCase((char) first);
                    } else {
                        set.add(first);
                    }
                }
            } else {
                first = spec.lexeme;

                if (spec.ignoreCase) {
                    set.addNCase(spec.lexeme);
                } else {
                    set.add(spec.lexeme);
                }
            }

            lexGen.advance();
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("doDash", spec.lexeme, spec.currentToken);
        }
    }
}

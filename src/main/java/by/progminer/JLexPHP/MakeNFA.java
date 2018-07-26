package by.progminer.JLexPHP;

import java.io.IOException;
import java.util.Vector;

class MakeNFA {

    private CSpec spec;
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
    private void set(LexGen lexGen, CSpec spec, Input input) {
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
     * input CSpec.
     */
    void allocateBolEof(CSpec spec) {
        //noinspection ConstantConditions
        CUtility.ASSERT(CSpec.NUM_PSEUDO == 2);

        spec.BOL = spec.m_dtrans_ncols++;
        spec.EOF = spec.m_dtrans_ncols++;
    }

    /**
     * High level access function to module.
     * Deposits result in input CSpec.
     */
    void thompson(LexGen lexGen, CSpec spec, Input input) throws IOException {
        // Set member variables
        reset();
        set(lexGen, spec, input);

        int size = this.spec.m_states.size();
        this.spec.m_state_rules = new Vector[size];

        for (int i = 0; i < size; ++i) {
            this.spec.m_state_rules[i] = new Vector <NFA> ();
        }

        this.spec.m_NFA_start = machine();

        // Set labels in created NFA machine
        size = this.spec.m_NFA_states.size();
        for (int i = 0; i < size; ++i) {
            NFA elem = this.spec.m_NFA_states.elementAt(i);
            elem.label = i;
        }

        // Debugging output
        if (CUtility.DO_DEBUG) {
            this.lexGen.printNFA();
        }

        if (this.spec.m_verbose) {
            System.out.println("NFA comprised of " + (this.spec.m_NFA_states.size() + 1) + " states.");
        }

        reset();
    }

    private void discardNFA(NFA nfa) {
        spec.m_NFA_states.removeElement(nfa);
    }

    private void processStates(SparseBitSet states, NFA current) {
        int size = spec.m_states.size();

        for (int i = 0; i < size; ++i) {
            if (states.get(i)) {
                spec.m_state_rules[i].addElement(current);
            }
        }
    }

    /**
     * Recursive descent regular expression parser.
     */
    private NFA machine() throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("machine", spec.m_lexeme, spec.m_current_token);
        }

        NFA start = Alloc.newNFA(spec);
        NFA p = start;

        SparseBitSet states = lexGen.getStates();

        // Begin: Added for states
        spec.m_current_token = LexGen.EOS;
        lexGen.advance();
        // End: Added for states

        if (LexGen.END_OF_INPUT != spec.m_current_token) {
            // CSA fix

            p.next = rule();
            processStates(states, p.next);
        }

        while (LexGen.END_OF_INPUT != spec.m_current_token) {
            // Make state changes HERE
            states = lexGen.getStates();

            // Begin: Added for states
            lexGen.advance();
            if (LexGen.END_OF_INPUT == spec.m_current_token) {
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
        for (int i = 0; i < spec.m_states.size(); ++i) {
            all_states.set(i);
        }

        p.next2 = Alloc.newNFA(spec);
        p = p.next2;

        p.next = Alloc.newNFA(spec);

        p.next.edge = NFA.CCL;
        p.next.next = Alloc.newNFA(spec);

        p.next.set = new CSet();
        p.next.set.add(spec.BOL);
        p.next.set.add(spec.EOF);

        // do-nothing accept rule
        p.next.next.accept = new Accept(new char[0], 0, input.lineNumber + 1);
        processStates(all_states, p.next);
        // CSA: done

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("machine", spec.m_lexeme, spec.m_current_token);
        }

        return start;
    }

    /**
     * Recursive descent regular expression parser.
     */
    private NFA rule() throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("rule", spec.m_lexeme, spec.m_current_token);
        }

        int anchor = CSpec.NONE;
        NFA start, end;

        CNfaPair pair = Alloc.newNFAPair();
        if (LexGen.AT_BOL == spec.m_current_token) {
            anchor = anchor | CSpec.START;
            lexGen.advance();
            expr(pair);

            // CSA: fixed beginning-of-line operator. 8-aug-1999

            start = Alloc.newNFA(spec);
            start.edge = spec.BOL;
            start.next = pair.m_start;

            end = pair.m_end;
        } else {
            expr(pair);
            start = pair.m_start;
            end = pair.m_end;
        }

        if (LexGen.AT_EOL == spec.m_current_token) {
            lexGen.advance();

            // CSA: fixed end-of-line operator. 8-aug-1999

            CNfaPair nlPair = Alloc.newNLPair(spec);

            end.next = Alloc.newNFA(spec);
            end.next.next = nlPair.m_start;

            end.next.next2 = Alloc.newNFA(spec);
            end.next.next2.edge = spec.EOF;
            end.next.next2.next = nlPair.m_end;

            end = nlPair.m_end;

            anchor = anchor | CSpec.END;
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
            CUtility.leave("rule", spec.m_lexeme, spec.m_current_token);
        }

        return start;
    }

    /**
     * Recursive descent regular expression parser.
     */
    private void expr(CNfaPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("expr", spec.m_lexeme, spec.m_current_token);
        }

        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != pair);
        }

        CNfaPair e2Pair = Alloc.newNFAPair();

        catExpr(pair);
        while (LexGen.OR == spec.m_current_token) {
            lexGen.advance();
            catExpr(e2Pair);

            NFA p = Alloc.newNFA(spec);
            p.next2 = e2Pair.m_start;
            p.next = pair.m_start;
            pair.m_start = p;

            p = Alloc.newNFA(spec);
            e2Pair.m_end.next = p;
            pair.m_end.next = p;
            pair.m_end = p;
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("expr", spec.m_lexeme, spec.m_current_token);
        }
    }

    /**
     * Recursive descent regular expression parser.
     */
    private void catExpr(CNfaPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("catExpr", spec.m_lexeme, spec.m_current_token);
        }

        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != pair);
        }

        CNfaPair e2Pair = Alloc.newNFAPair();

        if (firstInCat(spec.m_current_token)) {
            factor(pair);
        }

        while (firstInCat(spec.m_current_token)) {
            factor(e2Pair);

            // Destroy
            pair.m_end.mimic(e2Pair.m_start);
            discardNFA(e2Pair.m_start);

            pair.m_end = e2Pair.m_end;
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("catExpr", spec.m_lexeme, spec.m_current_token);
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
    private void factor(CNfaPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("factor", spec.m_lexeme, spec.m_current_token);
        }

        term(pair);

        if (
            LexGen.CLOSURE == spec.m_current_token ||
            LexGen.PLUS_CLOSE == spec.m_current_token ||
            LexGen.OPTIONAL == spec.m_current_token
        ) {
            NFA start = Alloc.newNFA(spec),
                 end = Alloc.newNFA(spec);

            start.next = pair.m_start;
            pair.m_end.next = end;

            if (
                LexGen.CLOSURE == spec.m_current_token ||
                LexGen.OPTIONAL == spec.m_current_token
            ) {
                start.next2 = end;
            }

            if (
                LexGen.CLOSURE == spec.m_current_token ||
                LexGen.PLUS_CLOSE == spec.m_current_token
            ) {
                pair.m_end.next2 = pair.m_start;
            }

            pair.m_start = start;
            pair.m_end = end;
            lexGen.advance();
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("factor", spec.m_lexeme, spec.m_current_token);
        }
    }

    /**
     * Recursive descent regular expression parser.
     */
    private void term(CNfaPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("term", spec.m_lexeme, spec.m_current_token);
        }

        if (LexGen.OPEN_PAREN == spec.m_current_token) {
            lexGen.advance();
            expr(pair);

            if (LexGen.CLOSE_PAREN == spec.m_current_token) {
                lexGen.advance();
            } else {
                Error.parseError(Error.E_SYNTAX, input.lineNumber);
            }
        } else {
            NFA start = Alloc.newNFA(spec);
            pair.m_start = start;

            start.next = Alloc.newNFA(spec);
            pair.m_end = start.next;

            boolean isAlphaL = (LexGen.L == spec.m_current_token && Character.isLetter(spec.m_lexeme));
            if (
                LexGen.ANY != spec.m_current_token &&
                LexGen.CCL_START != spec.m_current_token &&
                (!spec.m_ignorecase || !isAlphaL)
            ) {
                start.edge = spec.m_lexeme;
                lexGen.advance();
            } else {
                start.edge = NFA.CCL;

                start.set = new CSet();

                // Match case-insensitive letters using character class
                if (spec.m_ignorecase && isAlphaL) {
                    start.set.addncase(spec.m_lexeme);
                }

                // Match dot (.) using character class
                else if (LexGen.ANY == spec.m_current_token) {
                    start.set.add('\n');
                    start.set.add('\r');

                    // CSA: exclude BOL and EOF from character classes
                    start.set.add(spec.BOL);
                    start.set.add(spec.EOF);
                    start.set.complement();
                } else {
                    lexGen.advance();

                    if (LexGen.AT_BOL == spec.m_current_token) {
                        lexGen.advance();

                        // CSA: exclude BOL and EOF from character classes
                        start.set.add(spec.BOL);
                        start.set.add(spec.EOF);
                        start.set.complement();
                    }

                    if (LexGen.CCL_END != spec.m_current_token) {
                        doDash(start.set);
                    }
                }

                lexGen.advance();
            }
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("term", spec.m_lexeme, spec.m_current_token);
        }
    }

    /**
     * Recursive descent regular expression parser.
     */
    private void doDash(CSet set) throws IOException {
        int first = -1;

        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("doDash", spec.m_lexeme, spec.m_current_token);
        }

        while (
            LexGen.EOS != spec.m_current_token &&
            LexGen.CCL_END != spec.m_current_token
        ) {
            // DASH loses its special meaning if it is first in class.

            if (LexGen.DASH == spec.m_current_token && -1 != first) {
                lexGen.advance();

                // DASH loses its special meaning if it is last in class.
                if (spec.m_current_token == LexGen.CCL_END) {
                    // 'first' already in set.

                    set.add('-');
                    break;
                }

                for (; first <= spec.m_lexeme; ++first) {
                    if (spec.m_ignorecase) {
                        set.addncase((char) first);
                    } else {
                        set.add(first);
                    }
                }
            } else {
                first = spec.m_lexeme;

                if (spec.m_ignorecase) {
                    set.addncase(spec.m_lexeme);
                } else {
                    set.add(spec.m_lexeme);
                }
            }

            lexGen.advance();
        }

        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("doDash", spec.m_lexeme, spec.m_current_token);
        }
    }
}

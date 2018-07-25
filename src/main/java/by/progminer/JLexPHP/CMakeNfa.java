package by.progminer.JLexPHP;

import java.io.IOException;
import java.util.Vector;

class CMakeNfa {
    
    private CSpec m_spec;
    private CLexGen m_lexGen;
    private CInput m_input;
    
    CMakeNfa() {
        reset();
    }
    
    /**
     * Resets CMakeNfa member variables.
     */
    private void reset() {
        m_input = null;
        m_lexGen = null;
        m_spec = null;
    }
    
    /**
     * Sets CMakeNfa member variables.
     */
    private void set(CLexGen lexGen, CSpec spec, CInput input) {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != input);
            CUtility.ASSERT(null != lexGen);
            CUtility.ASSERT(null != spec);
        }
        
        m_input = input;
        m_lexGen = lexGen;
        m_spec = spec;
    }
    
    /**
     * Expands character class to include special BOL and
     * EOF characters. Puts numeric index of these characters in
     * input CSpec.
     */
    void allocate_BOL_EOF(CSpec spec) {
        //noinspection ConstantConditions
        CUtility.ASSERT(CSpec.NUM_PSEUDO == 2);
        
        spec.BOL = spec.m_dtrans_ncols++;
        spec.EOF = spec.m_dtrans_ncols++;
    }
    
    /**
     * High level access function to module.
     * Deposits result in input CSpec.
     */
    void thompson(CLexGen lexGen, CSpec spec, CInput input) throws IOException {
        // Set member variables.
        
        reset();
        set(lexGen, spec, input);
        
        int size = m_spec.m_states.size();
        m_spec.m_state_rules = new Vector[size];
        
        for (int i = 0; i < size; ++i) {
            m_spec.m_state_rules[i] = new Vector <CNfa> ();
        }
        
        m_spec.m_nfa_start = machine();
        
        // Set labels in created nfa machine.
        
        size = m_spec.m_nfa_states.size();
        for (int i = 0; i < size; ++i) {
            CNfa elem = m_spec.m_nfa_states.elementAt(i);
            elem.m_label = i;
        }
        
        // Debugging output.
        if (CUtility.DO_DEBUG) {
            m_lexGen.print_nfa();
        }
        
        if (m_spec.m_verbose) {
            System.out.println(
                "NFA comprised of " +
                    (m_spec.m_nfa_states.size() + 1) +
                    " states."
            );
        }
        
        reset();
    }
    
    private void discardCNfa(CNfa nfa) {
        m_spec.m_nfa_states.removeElement(nfa);
    }
    
    private void processStates(SparseBitSet states, CNfa current) {
        int size = m_spec.m_states.size();
        
        for (int i = 0; i < size; ++i) {
            if (states.get(i)) {
                m_spec.m_state_rules[i].addElement(current);
            }
        }
    }
    
    /**
     * Recursive descent regular expression parser.
     */
    private CNfa machine() throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("machine", m_spec.m_lexeme, m_spec.m_current_token);
        }
        
        CNfa start = CAlloc.newCNfa(m_spec);
        CNfa p = start;
        
        SparseBitSet states = m_lexGen.getStates();
        
        // Begin: Added for states.
        m_spec.m_current_token = CLexGen.EOS;
        m_lexGen.advance();
        
        // End: Added for states.
        
        if (CLexGen.END_OF_INPUT != m_spec.m_current_token) {
            // CSA fix.
            
            p.m_next = rule();
            processStates(states, p.m_next);
        }
        
        while (CLexGen.END_OF_INPUT != m_spec.m_current_token) {
            // Make state changes HERE.
            
            states = m_lexGen.getStates();
            
            // Begin: Added for states.
            m_lexGen.advance();
            if (CLexGen.END_OF_INPUT == m_spec.m_current_token) {
                break;
            }
            // End: Added for states.
            
            p.m_next2 = CAlloc.newCNfa(m_spec);
            p = p.m_next2;
            p.m_next = rule();
            
            processStates(states, p.m_next);
        }
        
        // CSA: add pseudo-rules for BOL and EOF
        SparseBitSet all_states = new SparseBitSet();
        for (int i = 0; i < m_spec.m_states.size(); ++i) {
            all_states.set(i);
        }
        
        p.m_next2 = CAlloc.newCNfa(m_spec);
        p = p.m_next2;
        
        p.m_next = CAlloc.newCNfa(m_spec);
        
        p.m_next.m_edge = CNfa.CCL;
        p.m_next.m_next = CAlloc.newCNfa(m_spec);
        
        p.m_next.m_set = new CSet();
        p.m_next.m_set.add(m_spec.BOL);
        p.m_next.m_set.add(m_spec.EOF);
        
        // do-nothing accept rule
        p.m_next.m_next.m_accept = new CAccept(new char[0], 0, m_input.m_line_number + 1);
        processStates(all_states, p.m_next);
        // CSA: done.
        
        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("machine", m_spec.m_lexeme, m_spec.m_current_token);
        }
        
        return start;
    }
    
    /**
     * Recursive descent regular expression parser.
     */
    private CNfa rule() throws IOException {
        int anchor = CSpec.NONE;
        CNfa start, end;
        
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("rule", m_spec.m_lexeme, m_spec.m_current_token);
        }
        
        CNfaPair pair = CAlloc.newCNfaPair();
        if (CLexGen.AT_BOL == m_spec.m_current_token) {
            anchor = anchor | CSpec.START;
            m_lexGen.advance();
            expr(pair);
            
            // CSA: fixed beginning-of-line operator. 8-aug-1999
            
            start = CAlloc.newCNfa(m_spec);
            start.m_edge = m_spec.BOL;
            start.m_next = pair.m_start;
            
            end = pair.m_end;
        } else {
            expr(pair);
            start = pair.m_start;
            end = pair.m_end;
        }
        
        if (CLexGen.AT_EOL == m_spec.m_current_token) {
            m_lexGen.advance();
            
            // CSA: fixed end-of-line operator. 8-aug-1999
            
            CNfaPair nlpair = CAlloc.newNLPair(m_spec);
            
            end.m_next = CAlloc.newCNfa(m_spec);
            end.m_next.m_next = nlpair.m_start;
            
            end.m_next.m_next2 = CAlloc.newCNfa(m_spec);
            end.m_next.m_next2.m_edge = m_spec.EOF;
            end.m_next.m_next2.m_next = nlpair.m_end;
            
            end = nlpair.m_end;
            
            anchor = anchor | CSpec.END;
        }
        
        // Check for null rules. Charles Fischer found this bug. [CSA]
        if (end == null) {
            CError.parse_error(CError.E_ZERO, m_input.m_line_number);
            
            // For IDE detecting
            throw new Error();
        }
    
        // Handle end of regular expression.  See page 103.
        end.m_accept = m_lexGen.packAccept();
        end.m_anchor = anchor;
        
        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("rule", m_spec.m_lexeme, m_spec.m_current_token);
        }
        
        return start;
    }
    
    /**
     * Recursive descent regular expression parser.
     */
    private void expr(CNfaPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("expr", m_spec.m_lexeme, m_spec.m_current_token);
        }
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != pair);
        }
        
        CNfaPair e2_pair = CAlloc.newCNfaPair();
        
        cat_expr(pair);
        
        while (CLexGen.OR == m_spec.m_current_token) {
            m_lexGen.advance();
            cat_expr(e2_pair);
            
            CNfa p = CAlloc.newCNfa(m_spec);
            p.m_next2 = e2_pair.m_start;
            p.m_next = pair.m_start;
            pair.m_start = p;
            
            p = CAlloc.newCNfa(m_spec);
            pair.m_end.m_next = p;
            e2_pair.m_end.m_next = p;
            pair.m_end = p;
        }
        
        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("expr", m_spec.m_lexeme, m_spec.m_current_token);
        }
    }
    
    /**
     * Recursive descent regular expression parser.
     */
    private void cat_expr(CNfaPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("cat_expr", m_spec.m_lexeme, m_spec.m_current_token);
        }
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != pair);
        }
        
        CNfaPair e2_pair = CAlloc.newCNfaPair();
        
        if (first_in_cat(m_spec.m_current_token)) {
            factor(pair);
        }
        
        while (first_in_cat(m_spec.m_current_token)) {
            factor(e2_pair);
            
            // Destroy
            pair.m_end.mimic(e2_pair.m_start);
            discardCNfa(e2_pair.m_start);
            
            pair.m_end = e2_pair.m_end;
        }
        
        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("cat_expr", m_spec.m_lexeme, m_spec.m_current_token);
        }
    }
    
    /**
     * Recursive descent regular expression parser.
     */
    private boolean first_in_cat(int token) {
        switch (token) {
        case CLexGen.CLOSE_PAREN:
        case CLexGen.AT_EOL:
        case CLexGen.OR:
        case CLexGen.EOS:
            return false;
        
        case CLexGen.CLOSURE:
        case CLexGen.PLUS_CLOSE:
        case CLexGen.OPTIONAL:
            CError.parse_error(CError.E_CLOSE, m_input.m_line_number);
            return false;
        
        case CLexGen.CCL_END:
            CError.parse_error(CError.E_BRACKET, m_input.m_line_number);
            return false;
        
        case CLexGen.AT_BOL:
            CError.parse_error(CError.E_BOL, m_input.m_line_number);
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
            CUtility.enter("factor", m_spec.m_lexeme, m_spec.m_current_token);
        }
        
        term(pair);
        
        if (
            CLexGen.CLOSURE == m_spec.m_current_token ||
            CLexGen.PLUS_CLOSE == m_spec.m_current_token ||
            CLexGen.OPTIONAL == m_spec.m_current_token
        ) {
            CNfa start = CAlloc.newCNfa(m_spec),
                 end = CAlloc.newCNfa(m_spec);
            
            start.m_next = pair.m_start;
            pair.m_end.m_next = end;
            
            if (
                CLexGen.CLOSURE == m_spec.m_current_token ||
                CLexGen.OPTIONAL == m_spec.m_current_token
            ) {
                start.m_next2 = end;
            }
            
            if (
                CLexGen.CLOSURE == m_spec.m_current_token ||
                CLexGen.PLUS_CLOSE == m_spec.m_current_token
            ) {
                pair.m_end.m_next2 = pair.m_start;
            }
            
            pair.m_start = start;
            pair.m_end = end;
            m_lexGen.advance();
        }
        
        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("factor", m_spec.m_lexeme, m_spec.m_current_token);
        }
    }
    
    /**
     * Recursive descent regular expression parser.
     */
    private void term(CNfaPair pair) throws IOException {
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("term", m_spec.m_lexeme, m_spec.m_current_token);
        }
        
        if (CLexGen.OPEN_PAREN == m_spec.m_current_token) {
            m_lexGen.advance();
            expr(pair);
            
            if (CLexGen.CLOSE_PAREN == m_spec.m_current_token) {
                m_lexGen.advance();
            } else {
                CError.parse_error(CError.E_SYNTAX, m_input.m_line_number);
            }
        } else {
            CNfa start = CAlloc.newCNfa(m_spec);
            pair.m_start = start;
            
            start.m_next = CAlloc.newCNfa(m_spec);
            pair.m_end = start.m_next;
            
            boolean isAlphaL = (CLexGen.L == m_spec.m_current_token && Character.isLetter(m_spec.m_lexeme));
            if (
                CLexGen.ANY != m_spec.m_current_token &&
                CLexGen.CCL_START != m_spec.m_current_token &&
                (!m_spec.m_ignorecase || !isAlphaL)
            ) {
                start.m_edge = m_spec.m_lexeme;
                m_lexGen.advance();
            } else {
                start.m_edge = CNfa.CCL;
                
                start.m_set = new CSet();
                
                // Match case-insensitive letters using character class.
                if (m_spec.m_ignorecase && isAlphaL) {
                    start.m_set.addncase(m_spec.m_lexeme);
                }
                
                // Match dot (.) using character class.
                else if (CLexGen.ANY == m_spec.m_current_token) {
                    start.m_set.add('\n');
                    start.m_set.add('\r');
                    
                    // CSA: exclude BOL and EOF from character classes
                    start.m_set.add(m_spec.BOL);
                    start.m_set.add(m_spec.EOF);
                    start.m_set.complement();
                } else {
                    m_lexGen.advance();
                    
                    if (CLexGen.AT_BOL == m_spec.m_current_token) {
                        m_lexGen.advance();
                        
                        // CSA: exclude BOL and EOF from character classes
                        start.m_set.add(m_spec.BOL);
                        start.m_set.add(m_spec.EOF);
                        start.m_set.complement();
                    }
                    
                    if (CLexGen.CCL_END != m_spec.m_current_token) {
                        dodash(start.m_set);
                    }
                }
                
                m_lexGen.advance();
            }
        }
        
        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("term", m_spec.m_lexeme, m_spec.m_current_token);
        }
    }
    
    /**
     * Recursive descent regular expression parser.
     */
    private void dodash(CSet set) throws IOException {
        int first = -1;
        
        if (CUtility.DESCENT_DEBUG) {
            CUtility.enter("dodash", m_spec.m_lexeme, m_spec.m_current_token);
        }
        
        while (
            CLexGen.EOS != m_spec.m_current_token &&
            CLexGen.CCL_END != m_spec.m_current_token
        ) {
            // DASH loses its special meaning if it is first in class.
            
            if (CLexGen.DASH == m_spec.m_current_token && -1 != first) {
                m_lexGen.advance();
                
                // DASH loses its special meaning if it is last in class.
                if (m_spec.m_current_token == CLexGen.CCL_END) {
                    // 'first' already in set.
                    
                    set.add('-');
                    break;
                }
                
                for (; first <= m_spec.m_lexeme; ++first) {
                    if (m_spec.m_ignorecase) {
                        set.addncase((char) first);
                    } else {
                        set.add(first);
                    }
                }
            } else {
                first = m_spec.m_lexeme;
                
                if (m_spec.m_ignorecase) {
                    set.addncase(m_spec.m_lexeme);
                } else {
                    set.add(m_spec.m_lexeme);
                }
            }
            
            m_lexGen.advance();
        }
        
        if (CUtility.DESCENT_DEBUG) {
            CUtility.leave("dodash", m_spec.m_lexeme, m_spec.m_current_token);
        }
    }
}

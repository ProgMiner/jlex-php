package by.progminer.JLexPHP;

import java.io.PrintWriter;
import java.util.Enumeration;

class CEmit {
    
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean NOT_EDBG = false;
    
    private CSpec m_spec;
    private PrintWriter m_outstream;
    
    CEmit() {
        reset();
    }
    
    /**
     * Clears member variables.
     */
    private void reset() {
        m_spec = null;
        m_outstream = null;
    }
    
    /**
     * Initializes member variables.
     */
    private void set(CSpec spec, PrintWriter outstream) {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(null != outstream);
        }
        
        m_spec = spec;
        m_outstream = outstream;
    }
    
    /**
     * Emits import packages at top of
     * generated source file.
     */
    /*
    void emit_imports(CSpec spec, PrintWriter outstream) throws IOException {
        set(spec, outstream);

        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != m_spec);
            CUtility.ASSERT(null != m_outstream);
        }

        m_outstream.println("import java.lang.String;");
        m_outstream.println("import java.lang.System;");
        m_outstream.println("import java.io.BufferedReader;");
        m_outstream.println("import java.io.InputStream;");

        reset();
	}*/
    
    /**
     * Debugging output.
     */
    private void print_details() {
        System.out.println("---------------------- Transition Table ----------------------");
        
        for (int i = 0; i < m_spec.m_row_map.length; ++i) {
            System.out.print("State " + i);
            
            CAccept accept = m_spec.m_accept_vector.elementAt(i);
            if (null == accept) {
                System.out.println(" [nonaccepting]");
            } else {
                System.out.println(
                    " [accepting, line " + accept.m_line_number +
                        " <" + new String(accept.m_action, 0, accept.m_action_read) + ">]"
                );
            }
            
            CDTrans dtrans = m_spec.m_dtrans_vector.elementAt(m_spec.m_row_map[i]);
            
            boolean tr = false;
            int state = dtrans.m_dtrans[m_spec.m_col_map[0]];
            
            if (CDTrans.F != state) {
                tr = true;
                
                System.out.print("\tgoto " + state + " on [" + ((char) 0));
            }
            
            for (int j = 1; j < m_spec.m_dtrans_ncols; ++j) {
                int next = dtrans.m_dtrans[m_spec.m_col_map[j]];
                
                if (state == next) {
                    if (CDTrans.F != state) {
                        System.out.print((char) j);
                    }
                } else {
                    state = next;
                    
                    if (tr) {
                        tr = false;
                        
                        System.out.println("]");
                    }
                    
                    if (CDTrans.F != state) {
                        tr = true;
                        
                        System.out.print("\tgoto " + state + " on [" + ((char) j));
                    }
                }
            }
            
            if (tr) {
                System.out.println("]");
            }
        }
        
        System.out.println("---------------------- Transition Table ----------------------");
    }
    
    /**
     * High-level access function to module.
     */
    void emit(CSpec spec, PrintWriter outstream) {
        set(spec, outstream);
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != m_spec);
            CUtility.ASSERT(null != m_outstream);
        }
        
        if (CUtility.OLD_DEBUG) {
            print_details();
        }
        
        emit_header();
        emit_construct();
        emit_helpers();
        emit_driver();
        emit_footer();
        
        reset();
    }
    
    /**
     * Emits constructor, member variables, and constants.
     */
    private void emit_construct() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != m_spec);
            CUtility.ASSERT(null != m_outstream);
        }
        
        // Constants
        m_outstream.println("\tconst YY_BUFFER_SIZE = 512;");
        
        m_outstream.println("\tconst YY_F = -1;");
        m_outstream.println("\tconst YY_NO_STATE = -1;");
        
        m_outstream.println("\tconst YY_NOT_ACCEPT = 0;");
        m_outstream.println("\tconst YY_START = 1;");
        m_outstream.println("\tconst YY_END = 2;");
        m_outstream.println("\tconst YY_NO_ANCHOR = 4;");
        
        // Internal
        m_outstream.println("\tconst YY_BOL = " + m_spec.BOL + ";");
        m_outstream.println("\t$YY_EOF = " + m_spec.EOF + ";");
        
        // External
        if (m_spec.m_integer_type || m_spec.m_yyeof)
            m_outstream.println("\tconst YYEOF = -1;");
        
        /* User specified class code. */
        if (null != m_spec.m_class_code) {
            m_outstream.print(new String(m_spec.m_class_code, 0, m_spec.m_class_read));
        }
        
        // Member Variables
        
        if (m_spec.m_count_chars) {
            m_outstream.println("\tprotected $yy_count_chars = true;");
        }
        
        if (m_spec.m_count_lines) {
            m_outstream.println("\tprotected $yy_count_lines = true;");
        }
        
        m_outstream.println();
        
        // Constructor
        
        m_outstream.println("\tpublic function __construct($stream) {");
        
        m_outstream.println("\t\tparent::__construct($stream);");
        m_outstream.println("\t\t$this->yy_lexical_state = self::YYINITIAL;");
        
        // User specified constructor code.
        if (null != m_spec.m_init_code) {
            m_outstream.print(new String(m_spec.m_init_code, 0, m_spec.m_init_read));
        }
        
        m_outstream.println("\t}");
        m_outstream.println();
        
    }
    
    /**
     * Emits constants that serve as lexical states,
     * including YYINITIAL.
     */
    private void emit_states() {
        Enumeration <String> states = m_spec.m_states.keys();
        
        while (states.hasMoreElements()) {
            String state = states.nextElement();
            
            if (CUtility.DEBUG) {
                CUtility.ASSERT(null != state);
            }
            
            m_outstream.println("\tconst " + state + " = " + m_spec.m_states.get(state).toString() + ";");
        }
        
        m_outstream.println("\tstatic $yy_state_dtrans = [");
        for (int index = 0; index < m_spec.m_state_dtrans.length; ++index) {
            m_outstream.print("\t\t" + m_spec.m_state_dtrans[index]);
            
            if (index < m_spec.m_state_dtrans.length - 1) {
                m_outstream.println(",");
            } else {
                m_outstream.println();
            }
        }
        m_outstream.println("\t];");
    }
    
    /**
     * Emits helper functions, particularly
     * error handling and input buffering.
     */
    private void emit_helpers() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != m_spec);
            CUtility.ASSERT(null != m_outstream);
        }
        
        if (null != m_spec.m_eof_code) {
            // Function yy_do_eof
            
            m_outstream.print("\tprivate function yy_do_eof () {");
            
            m_outstream.println("\t\tif (false === $this->yy_eof_done) {");
            m_outstream.print(new String(m_spec.m_eof_code, 0, m_spec.m_eof_read));
            m_outstream.println("\t\t}");
            m_outstream.println("\t\t$this->yy_eof_done = true;");
            m_outstream.println("\t}");
        }
        
        emit_states();
        
    }
    
    /**
     * Emits class header.
     */
    private void emit_header() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != m_spec);
            CUtility.ASSERT(null != m_outstream);
        }
        
        m_outstream.println();
        
        if (m_spec.m_public) {
            m_outstream.print("public ");
        }
        
        m_outstream.print("class ");
        m_outstream.print(new String(m_spec.m_class_name, 0, m_spec.m_class_name.length));
        m_outstream.print(" extends JLexPHP\\Base ");
        
        if (m_spec.m_implements_name.length > 0) {
            m_outstream.print(" implements ");
            m_outstream.print(new String(m_spec.m_implements_name, 0, m_spec.m_implements_name.length));
        }
        
        m_outstream.println(" {");
    }
    
    /**
     * Emits transition table.
     */
    private void emit_table() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != m_spec);
            CUtility.ASSERT(null != m_outstream);
        }
        
        m_outstream.println("\tstatic $yy_acpt = [");
        
        int size = m_spec.m_accept_vector.size();
        for (int elem = 0; elem < size; ++elem) {
            CAccept accept = m_spec.m_accept_vector.elementAt(elem);
            
            m_outstream.print("\t\t/* " + elem + " */ ");
            if (null != accept) {
                boolean is_start = (0 != (m_spec.m_anchor_array[elem] & CSpec.START));
                boolean is_end = (0 != (m_spec.m_anchor_array[elem] & CSpec.END));
                
                if (is_start && is_end) {
                    m_outstream.print("3 /* self::YY_START | self::YY_END */");
                } else if (is_start) {
                    m_outstream.print("self::YY_START");
                } else if (is_end) {
                    m_outstream.print("self::YY_END");
                } else {
                    m_outstream.print("self::YY_NO_ANCHOR");
                }
            } else {
                m_outstream.print("self::YY_NOT_ACCEPT");
            }
            
            if (elem < size - 1) {
                m_outstream.print(",");
            }
            
            m_outstream.println();
        }
        
        m_outstream.println("\t];");
        
        // CSA: modified yy_cmap to use string packing 9-Aug-1999
        
        int[] yy_cmap = new int[m_spec.m_ccls_map.length];
        for (int i = 0; i < m_spec.m_ccls_map.length; ++i) {
            yy_cmap[i] = m_spec.m_col_map[m_spec.m_ccls_map[i]];
        }
        
        m_outstream.print("\t\tstatic $yy_cmap = ");
        emit_table_as_array(yy_cmap);
        m_outstream.println();
        
        // CSA: modified yy_rmap to use string packing 9-Aug-1999
        
        m_outstream.print("\t\tstatic $yy_rmap = ");
        emit_table_as_array(m_spec.m_row_map);
        m_outstream.println();
        
        // 6/24/98 Raimondas Lencevicius
        // modified to use
        //    int[][] unpackFromString(int size1, int size2, String st)
        
        size = m_spec.m_dtrans_vector.size();
        int[][] yy_nxt = new int[size][];
        for (int elem = 0; elem < size; elem++) {
            CDTrans dtrans = m_spec.m_dtrans_vector.elementAt(elem);
            
            CUtility.ASSERT(dtrans.m_dtrans.length == m_spec.m_dtrans_ncols);
            
            yy_nxt[elem] = dtrans.m_dtrans;
        }
        
        m_outstream.print("\t\tstatic $yy_nxt = ");
        emit_table_as_array_2d(yy_nxt);
        m_outstream.println();
    }
    
    private void emit_table_as_array(int[] ia) {
        m_outstream.println("[");
        
        for (int i = 0; i < ia.length; ++i) {
            m_outstream.print(" " + ia[i] + ",");
            
            if (i % 20 == 19) {
                m_outstream.println();
            }
        }
        
        m_outstream.println("];");
    }
    
    private void emit_table_as_array_2d(int[][] ia) {
        m_outstream.println("[");
        
        for (int[] anIa : ia) {
            m_outstream.println("[");
            
            for (int i = 0; i < anIa.length; ++i) {
                m_outstream.print(" " + anIa[i] + ",");
                
                if (i % 20 == 19) {
                    m_outstream.println();
                }
            }
            
            m_outstream.println();
            m_outstream.println("],");
        }
        m_outstream.println("];");
    }
    
    /**
     * Output an integer table as a string.
     * <p>
     * Written by Raimondas Lencevicius 6/24/98;
     * reorganized by CSA 9-Aug-1999.
     * <p>
     * From his original comments:
     * yy_nxt[][] values are coded into a string
     * by printing integers and representing
     * integer sequences as "value:length" pairs.
     */
    private void emit_table_as_string(int[][] ia) {
        int sequenceLength = 0;          // RL - length of the number sequence
        boolean sequenceStarted = false; // RL - has number sequence started?
        int previousInt = -20;           // RL - Bogus -20 state.
        
        // RL - Output matrix size
        
        m_outstream.print(ia.length);
        m_outstream.print(",");
        m_outstream.print(ia.length > 0 ? ia[0].length : 0);
        m_outstream.println(",");
        
        StringBuffer outstr = new StringBuffer();
        
        // RL - Output matrix
        
        for (int[] anIa : ia) {
            for (int writeInt : anIa) {
                if (writeInt == previousInt) {
                    // RL - sequence?
                    
                    if (sequenceStarted) {
                        sequenceLength++;
                    } else {
                        outstr.append(writeInt);
                        outstr.append(":");
                        
                        sequenceLength = 2;
                        sequenceStarted = true;
                    }
                } else {
                    // RL - no sequence or end sequence
                    
                    if (sequenceStarted) {
                        outstr.append(sequenceLength);
                        outstr.append(",");
                        
                        sequenceLength = 0;
                        sequenceStarted = false;
                    } else {
                        if (previousInt != -20) {
                            outstr.append(previousInt);
                            outstr.append(",");
                        }
                    }
                }
                
                previousInt = writeInt;
                
                // CSA: output in 75 character chunks.
                
                if (outstr.length() > 75) {
                    String s = outstr.toString();
                    m_outstream.println("\"" + s.substring(0, 75) + "\" .");
                    outstr = new StringBuffer(s.substring(75));
                }
            }
        }
        
        if (sequenceStarted) {
            outstr.append(sequenceLength);
        } else {
            outstr.append(previousInt);
        }
        
        // CSA: output in 75 character chunks.
        
        if (outstr.length() > 75) {
            String s = outstr.toString();
            m_outstream.println("\"" + s.substring(0, 75) + "\" +");
            outstr = new StringBuffer(s.substring(75));
        }
        
        m_outstream.print("\"" + outstr + "\"");
    }
    
    private void emit_driver() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != m_spec);
            CUtility.ASSERT(null != m_outstream);
        }
        
        emit_table();
        
        if (m_spec.m_integer_type) {
            m_outstream.print("\tpublic function ");
            m_outstream.print(new String(m_spec.m_function_name));
            m_outstream.println(" ()");
        } else if (m_spec.m_intwrap_type) {
            m_outstream.print("\tpublic function ");
            m_outstream.print(new String(m_spec.m_function_name));
            m_outstream.println(" ()");
        } else {
            m_outstream.print("\tpublic function /*");
            m_outstream.print(new String(m_spec.m_type_name));
            m_outstream.print("*/ ");
            m_outstream.print(new String(m_spec.m_function_name));
            m_outstream.println(" ()");
        }
        
        m_outstream.println("\t\t$yy_anchor = self::YY_NO_ANCHOR;");
        m_outstream.println("\t\t$yy_state = self::$yy_state_dtrans[$this->yy_lexical_state];");
        m_outstream.println("\t\t$yy_next_state = self::YY_NO_STATE;");
        m_outstream.println("\t\t$yy_last_accept_state = self::YY_NO_STATE;");
        m_outstream.println("\t\t$yy_initial = true;");
        m_outstream.println();
        
        m_outstream.println("\t\t$this->yy_mark_start();");
        m_outstream.println("\t\t$yy_this_accept = self::$yy_acpt[$yy_state];");
        m_outstream.println("\t\tif (self::YY_NOT_ACCEPT != $yy_this_accept) {");
        m_outstream.println("\t\t\t$yy_last_accept_state = $yy_state;");
        m_outstream.println("\t\t\t$this->yy_mark_end();");
        m_outstream.println("\t\t}");
        
        if (NOT_EDBG) {
            m_outstream.println("\t\techo \"Begin\\n\";");
        }
        
        m_outstream.println("\t\twhile (true) {");
        
        m_outstream.println("\t\t\tif ($yy_initial && $this->yy_at_bol) " +
                                "$yy_lookahead = self::YY_BOL;");
        m_outstream.println("\t\t\telse $yy_lookahead = $this->yy_advance();");
        m_outstream.println("\t\t\t$yy_next_state = "
                                + "self::$yy_nxt[self::$yy_rmap[$yy_state]][self::$yy_cmap[$yy_lookahead]];");
        
        if (NOT_EDBG) {
            m_outstream.println("print(\"Current state: \""
                                    + " . $yy_state . ");
            m_outstream.println(". \"\tCurrent input: \"");
            m_outstream.println(" . $yy_lookahead);");
        }
        
        if (NOT_EDBG) {
            m_outstream.println("\t\t\tprint(\"State = \""
                                    + ". $yy_state . \"\\n\");");
            m_outstream.println("\t\t\tprint(\"Accepting status = \""
                                    + ". $yy_this_accept);");
            m_outstream.println("\t\t\tprint(\"Last accepting state = \""
                                    + ". $yy_last_accept_state);");
            m_outstream.println("\t\t\tprint(\"Next state = \""
                                    + ". $yy_next_state);");
            m_outstream.println("\t\t\tprint(\"Lookahead input = \""
                                    + ". $yy_lookahead);");
        }
        
        // handle bare EOF.
        
        m_outstream.println("\t\t\tif ($this->YY_EOF == $yy_lookahead && true == $yy_initial) {");
        
        if (null != m_spec.m_eof_code) {
            m_outstream.println("\t\t\t\t$this->yy_do_eof();");
        }
        
        if (m_spec.m_integer_type) {
            m_outstream.println("\t\t\t\treturn self::YYEOF;");
        } else if (null != m_spec.m_eof_value_code) {
            m_outstream.print(new String(m_spec.m_eof_value_code, 0, m_spec.m_eof_value_read));
        } else {
            m_outstream.println("\t\t\t\treturn null;");
        }
        
        m_outstream.println("\t\t\t}");
        
        m_outstream.println("\t\t\tif (self::YY_F != $yy_next_state) {");
        m_outstream.println("\t\t\t\t$yy_state = $yy_next_state;");
        m_outstream.println("\t\t\t\t$yy_initial = false;");
        m_outstream.println("\t\t\t\t$yy_this_accept = self::$yy_acpt[$yy_state];");
        m_outstream.println("\t\t\t\tif (self::YY_NOT_ACCEPT != $yy_this_accept) {");
        m_outstream.println("\t\t\t\t\t$yy_last_accept_state = $yy_state;");
        m_outstream.println("\t\t\t\t\t$this->yy_mark_end();");
        m_outstream.println("\t\t\t\t}");
        m_outstream.println("\t\t\t}");
        
        m_outstream.println("\t\t\telse {");
        
        m_outstream.println("\t\t\t\tif (self::YY_NO_STATE == $yy_last_accept_state) {");
        
        m_outstream.println("\t\t\t\t\tthrow new Exception(\"Lexical Error: Unmatched Input.\");");
        m_outstream.println("\t\t\t\t}");
        
        m_outstream.println("\t\t\t\telse {");
        
        m_outstream.println("\t\t\t\t\t$yy_anchor = self::$yy_acpt[$yy_last_accept_state];");
        m_outstream.println("\t\t\t\t\tif (0 != (self::YY_END & $yy_anchor)) {");
        m_outstream.println("\t\t\t\t\t\t$this->yy_move_end();");
        m_outstream.println("\t\t\t\t\t}");
        m_outstream.println("\t\t\t\t\t$this->yy_to_mark();");
        
        m_outstream.println("\t\t\t\t\tswitch ($yy_last_accept_state) {");
        
        emit_actions("\t\t\t\t\t\t");
        
        m_outstream.println("\t\t\t\t\t\tdefault:");
        m_outstream.println("\t\t\t\t\t\t$this->yy_error('INTERNAL',false);");
        m_outstream.println("\t\t\t\t\tcase -1:");
        m_outstream.println("\t\t\t\t\t}");
        
        m_outstream.println("\t\t\t\t\t$yy_initial = true;");
        m_outstream.println("\t\t\t\t\t$yy_state = self::$yy_state_dtrans[$this->yy_lexical_state];");
        m_outstream.println("\t\t\t\t\t$yy_next_state = self::YY_NO_STATE;");
        m_outstream.println("\t\t\t\t\t$yy_last_accept_state = self::YY_NO_STATE;");
        
        m_outstream.println("\t\t\t\t\t$this->yy_mark_start();");
        
        m_outstream.println("\t\t\t\t\t$yy_this_accept = self::$yy_acpt[$yy_state];");
        m_outstream.println("\t\t\t\t\tif (self::YY_NOT_ACCEPT != $yy_this_accept) {");
        m_outstream.println("\t\t\t\t\t\t$yy_last_accept_state = $yy_state;");
        m_outstream.println("\t\t\t\t\t\t$this->yy_mark_end();");
        m_outstream.println("\t\t\t\t\t}");
        
        m_outstream.println("\t\t\t\t}");
        m_outstream.println("\t\t\t}");
        m_outstream.println("\t\t}");
        m_outstream.println("\t}");
    }
    
    private void emit_actions(String tabs) {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(m_spec.m_accept_vector.size() == m_spec.m_anchor_array.length);
        }
        
        int bogus_index = -2;
        int size = m_spec.m_accept_vector.size();
        for (int elem = 0; elem < size; ++elem) {
            CAccept accept = m_spec.m_accept_vector.elementAt(elem);
            
            if (null != accept) {
                m_outstream.println(tabs + "case " + elem + ":");
                m_outstream.print(tabs + "\t");
                m_outstream.print(new String(accept.m_action, 0, accept.m_action_read));
                m_outstream.println();
                m_outstream.println(tabs + "case " + bogus_index + ":");
                m_outstream.println(tabs + "\tbreak;");
                
                --bogus_index;
            }
        }
    }
    
    private void emit_footer() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != m_spec);
            CUtility.ASSERT(null != m_outstream);
        }
        
        m_outstream.println("}");
    }
}
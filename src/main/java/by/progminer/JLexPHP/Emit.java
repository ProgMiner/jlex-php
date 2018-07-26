package by.progminer.JLexPHP;

import java.io.PrintWriter;
import java.util.Map;

class Emit {
    
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean NOT_EDBG = false;
    
    private CSpec spec;
    private PrintWriter outstream;
    
    Emit() {
        reset();
    }
    
    /**
     * Clears member variables.
     */
    private void reset() {
        spec = null;
        outstream = null;
    }
    
    /**
     * Initializes member variables.
     */
    private void set(CSpec spec, PrintWriter outstream) {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(null != outstream);
        }
        
        this.spec = spec;
        this.outstream = outstream;
    }
    
    /**
     * Emits import packages at top of
     * generated source file.
     */
    /*
    void emit_imports(CSpec spec, PrintWriter outstream) throws IOException {
        set(spec, outstream);

        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(null != outstream);
        }

        outstream.println("import java.lang.String;");
        outstream.println("import java.lang.System;");
        outstream.println("import java.io.BufferedReader;");
        outstream.println("import java.io.InputStream;");

        reset();
	}*/
    
    /**
     * Debugging output.
     */
    private void printDetails() {
        System.out.println("---------------------- Transition Table ----------------------");
        
        for (int i = 0; i < spec.m_row_map.length; ++i) {
            System.out.print("State " + i);
            
            Accept accept = spec.m_accept_vector.elementAt(i);
            if (null == accept) {
                System.out.println(" [nonaccepting]");
            } else {
                System.out.println(
                    " [accepting, line " + accept.lineNumber +
                        " <" + new String(accept.action, 0, accept.actionLength) + ">]"
                );
            }
            
            DTrans dTrans = spec.m_dtrans_vector.elementAt(spec.m_row_map[i]);
            
            boolean tr = false;
            int state = dTrans.dtrans[spec.m_col_map[0]];
            
            if (DTrans.F != state) {
                tr = true;
                
                System.out.print("\tgoto " + state + " on [" + ((char) 0));
            }
            
            for (int j = 1; j < spec.m_dtrans_ncols; ++j) {
                int next = dTrans.dtrans[spec.m_col_map[j]];
                
                if (state == next) {
                    if (DTrans.F != state) {
                        System.out.print((char) j);
                    }
                } else {
                    state = next;
                    
                    if (tr) {
                        tr = false;
                        
                        System.out.println("]");
                    }
                    
                    if (DTrans.F != state) {
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
    void all(CSpec spec, PrintWriter outstream) {
        set(spec, outstream);
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != this.spec);
            CUtility.ASSERT(null != this.outstream);
        }
        
        if (CUtility.OLD_DEBUG) {
            printDetails();
        }
        
        header();
        constructor();
        helpers();
        driver();
        footer();
        
        reset();
    }
    
    /**
     * Emits constructor, member variables, and constants.
     */
    private void constructor() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(null != outstream);
        }
        
        // Constants
        outstream.println("\tconst YY_BUFFER_SIZE = 512;");
        
        outstream.println("\tconst YY_F = -1;");
        outstream.println("\tconst YY_NO_STATE = -1;");
        
        outstream.println("\tconst YY_NOT_ACCEPT = 0;");
        outstream.println("\tconst YY_START = 1;");
        outstream.println("\tconst YY_END = 2;");
        outstream.println("\tconst YY_NO_ANCHOR = 4;");
        
        // Internal
        outstream.println("\tconst YY_BOL = " + spec.BOL + ";");
        outstream.println("\tconst YY_EOF = " + spec.EOF + ";");
        
        // External
        if (spec.m_integer_type || spec.m_yyeof)
            outstream.println("\tconst YYEOF = -1;");
        
        /* User specified class code. */
        if (null != spec.m_class_code) {
            outstream.print(new String(spec.m_class_code, 0, spec.m_class_read));
        }
        
        // Member Variables
        
        if (spec.m_count_chars) {
            outstream.println("\tprotected $yy_count_chars = true;");
        }
        
        if (spec.m_count_lines) {
            outstream.println("\tprotected $yy_count_lines = true;");
        }
        
        outstream.println();
        
        // Constructor
        
        outstream.println("\tpublic function __construct($stream) {");
        
        outstream.println("\t\tparent::__construct($stream);");
        outstream.println("\t\t$this->yy_lexical_state = self::YYINITIAL;");
        
        // User specified constructor code.
        if (null != spec.m_init_code) {
            outstream.print(new String(spec.m_init_code, 0, spec.m_init_read));
        }
        
        outstream.println("\t}");
        outstream.println();
        
    }
    
    /**
     * Emits constants that serve as lexical states,
     * including YYINITIAL.
     */
    private void states() {
        for (Map.Entry <String, Integer> entry: spec.m_states.entrySet()) {
            outstream.println("\tconst " + entry.getKey() + " = " + entry.getValue() + ";");
        }
        
        outstream.println("\tstatic $yy_state_dtrans = [");
        for (int index = 0; index < spec.m_state_dtrans.length; ++index) {
            outstream.print("\t\t" + spec.m_state_dtrans[index]);
            
            if (index < spec.m_state_dtrans.length - 1) {
                outstream.println(",");
            } else {
                outstream.println();
            }
        }
        outstream.println("\t];");
    }
    
    /**
     * Emits helper functions, particularly
     * error handling and input buffering.
     */
    private void helpers() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(null != outstream);
        }
        
        if (null != spec.m_eof_code) {
            // Function yy_do_eof
            
            outstream.print("\tprivate function yy_do_eof () {");
            
            outstream.println("\t\tif (false === $this->yy_eof_done) {");
            outstream.print(new String(spec.m_eof_code, 0, spec.m_eof_read));
            outstream.println("\t\t}");
            outstream.println("\t\t$this->yy_eof_done = true;");
            outstream.println("\t}");
        }
        
        states();
        
    }
    
    /**
     * Emits class header.
     */
    private void header() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(null != outstream);
        }
        
        outstream.println();
        
        if (spec.m_public) {
            outstream.print("public ");
        }
        
        outstream.print("class ");
        outstream.print(new String(spec.m_class_name, 0, spec.m_class_name.length));
        outstream.print(" extends JLexPHP\\Base ");
        
        if (spec.m_implements_name.length > 0) {
            outstream.print(" implements ");
            outstream.print(new String(spec.m_implements_name, 0, spec.m_implements_name.length));
        }
        
        outstream.println(" {");
    }
    
    /**
     * Emits transition table.
     */
    private void table() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(null != outstream);
        }
        
        outstream.println("\tstatic $yy_acpt = [");
        
        int size = spec.m_accept_vector.size();
        for (int elem = 0; elem < size; ++elem) {
            Accept accept = spec.m_accept_vector.elementAt(elem);
            
            outstream.print("\t\t/* " + elem + " */ ");
            if (null != accept) {
                boolean is_start = (0 != (spec.m_anchor_array[elem] & CSpec.START));
                boolean is_end = (0 != (spec.m_anchor_array[elem] & CSpec.END));
                
                if (is_start && is_end) {
                    outstream.print("3 /* self::YY_START | self::YY_END */");
                } else if (is_start) {
                    outstream.print("self::YY_START");
                } else if (is_end) {
                    outstream.print("self::YY_END");
                } else {
                    outstream.print("self::YY_NO_ANCHOR");
                }
            } else {
                outstream.print("self::YY_NOT_ACCEPT");
            }
            
            if (elem < size - 1) {
                outstream.print(",");
            }
            
            outstream.println();
        }
        
        outstream.println("\t];");
        
        // CSA: modified yy_cmap to use string packing 9-Aug-1999
        
        int[] yy_cmap = new int[spec.m_ccls_map.length];
        for (int i = 0; i < spec.m_ccls_map.length; ++i) {
            yy_cmap[i] = spec.m_col_map[spec.m_ccls_map[i]];
        }
        
        outstream.print("\t\tstatic $yy_cmap = ");
        tableAsArray(yy_cmap);
        outstream.println();
        
        // CSA: modified yy_rmap to use string packing 9-Aug-1999
        
        outstream.print("\t\tstatic $yy_rmap = ");
        tableAsArray(spec.m_row_map);
        outstream.println();
        
        // 6/24/98 Raimondas Lencevicius
        // modified to use
        //    int[][] unpackFromString(int size1, int size2, String st)
        
        size = spec.m_dtrans_vector.size();
        int[][] yy_nxt = new int[size][];
        for (int elem = 0; elem < size; elem++) {
            DTrans dTrans = spec.m_dtrans_vector.elementAt(elem);
            
            CUtility.ASSERT(dTrans.dtrans.length == spec.m_dtrans_ncols);
            
            yy_nxt[elem] = dTrans.dtrans;
        }
        
        outstream.print("\t\tstatic $yy_nxt = ");
        tableAsArray2D(yy_nxt);
        outstream.println();
    }
    
    private void tableAsArray(int[] ia) {
        outstream.println("[");
        
        for (int i = 0; i < ia.length; ++i) {
            outstream.print(" " + ia[i] + ",");
            
            if (i % 20 == 19) {
                outstream.println();
            }
        }
        
        outstream.println("];");
    }
    
    private void tableAsArray2D(int[][] ia) {
        outstream.println("[");
        
        for (int[] anIa : ia) {
            outstream.println("[");
            
            for (int i = 0; i < anIa.length; ++i) {
                outstream.print(" " + anIa[i] + ",");
                
                if (i % 20 == 19) {
                    outstream.println();
                }
            }
            
            outstream.println();
            outstream.println("],");
        }
        outstream.println("];");
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
    private void tableAsString(int[][] ia) {
        int sequenceLength = 0;          // RL - length of the number sequence
        boolean sequenceStarted = false; // RL - has number sequence started?
        int previousInt = -20;           // RL - Bogus -20 state.
        
        // RL - Output matrix size
        
        outstream.print(ia.length);
        outstream.print(",");
        outstream.print(ia.length > 0 ? ia[0].length : 0);
        outstream.println(",");
        
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
                    outstream.println("\"" + s.substring(0, 75) + "\" .");
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
            outstream.println("\"" + s.substring(0, 75) + "\" +");
            outstr = new StringBuffer(s.substring(75));
        }
        
        outstream.print("\"" + outstr + "\"");
    }
    
    private void driver() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(null != outstream);
        }
        
        table();
    
        outstream.print("\tpublic function ");
        
        if (!spec.m_integer_type && !spec.m_intwrap_type) {
            outstream.print("/*");
            outstream.print(new String(spec.m_type_name));
            outstream.print("*/ ");
        }
    
        outstream.print(new String(spec.m_function_name));
        outstream.println(" () {");
        
        outstream.println("\t\t$yy_anchor = self::YY_NO_ANCHOR;");
        outstream.println("\t\t$yy_state = self::$yy_state_dtrans[$this->yy_lexical_state];");
        outstream.println("\t\t$yy_next_state = self::YY_NO_STATE;");
        outstream.println("\t\t$yy_last_accept_state = self::YY_NO_STATE;");
        outstream.println("\t\t$yy_initial = true;");
        outstream.println();
        
        outstream.println("\t\t$this->yy_mark_start();");
        outstream.println();
        
        outstream.println("\t\t$yy_this_accept = self::$yy_acpt[$yy_state];");
        outstream.println("\t\tif (self::YY_NOT_ACCEPT !== $yy_this_accept) {");
        outstream.println("\t\t\t$yy_last_accept_state = $yy_state;");
        outstream.println("\t\t\t$this->yy_mark_end();");
        outstream.println("\t\t}");
        outstream.println();
        
        if (NOT_EDBG) {
            outstream.println("\t\techo \"Begin\\n\";");
            outstream.println();
        }
        
        outstream.println("\t\twhile (true) {");
    
        outstream.println("\t\t\t$yy_lookahead = self::YY_BOL;");
        outstream.println();
        
        outstream.println("\t\t\tif (!$yy_initial || !$this->yy_at_bol) {");
        outstream.println("\t\t\t\t$yy_lookahead = $this->yy_advance();");
        outstream.println("\t\t\t}");
        outstream.println();
        
        outstream.println("\t\t\t$yy_next_state = self::$yy_nxt[self::$yy_rmap[$yy_state]][self::$yy_cmap[$yy_lookahead]];");
        outstream.println();
        
        if (NOT_EDBG) {
            outstream.println("\t\t\techo \"Current state: $yy_state\tCurrent input: $yy_lookahead\";");
            outstream.println();
            
            outstream.println("\t\t\techo \"State = $yy_state\\n\";");
            outstream.println("\t\t\techo \"Accepting status = $yy_this_accept\";");
            outstream.println("\t\t\techo \"Last accepting state = $yy_last_accept_state\";");
            outstream.println("\t\t\techo \"Next state = $yy_next_state\";");
            outstream.println("\t\t\techo \"Lookahead input = $yy_lookahead;\"");
            outstream.println();
        }
        
        // handle bare EOF.
        
        outstream.println("\t\t\tif (self::YY_EOF === $yy_lookahead && $yy_initial) {");
        
        if (null != spec.m_eof_code) {
            outstream.println("\t\t\t\t$this->yy_do_eof();");
        }
        
        if (spec.m_integer_type) {
            outstream.println("\t\t\t\treturn self::YYEOF;");
        } else if (null != spec.m_eof_value_code) {
            outstream.print(new String(spec.m_eof_value_code, 0, spec.m_eof_value_read));
        } else {
            outstream.println("\t\t\t\treturn null;");
        }
        
        outstream.println("\t\t\t}");
        outstream.println();
        
        outstream.println("\t\t\tif (self::YY_F !== $yy_next_state) {");
        outstream.println("\t\t\t\t$yy_state = $yy_next_state;");
        outstream.println("\t\t\t\t$yy_initial = false;");
        outstream.println();
        
        outstream.println("\t\t\t\t$yy_this_accept = self::$yy_acpt[$yy_state];");
        outstream.println("\t\t\t\tif (self::YY_NOT_ACCEPT !== $yy_this_accept) {");
        outstream.println("\t\t\t\t\t$yy_last_accept_state = $yy_state;");
        outstream.println("\t\t\t\t\t$this->yy_mark_end();");
        outstream.println("\t\t\t\t}");
        outstream.println("\t\t\t} else {");
        outstream.println("\t\t\t\tif (self::YY_NO_STATE === $yy_last_accept_state) {");
        outstream.println("\t\t\t\t\tthrow new \\Exception(\"Lexical Error: Unmatched Input.\");");
        outstream.println("\t\t\t\t} else {");
        outstream.println("\t\t\t\t\t$yy_anchor = self::$yy_acpt[$yy_last_accept_state];");
        outstream.println();
        
        outstream.println("\t\t\t\t\tif (0 !== (self::YY_END & $yy_anchor)) {");
        outstream.println("\t\t\t\t\t\t$this->yy_move_end();");
        outstream.println("\t\t\t\t\t}");
        outstream.println();
        
        outstream.println("\t\t\t\t\t$this->yy_to_mark();");
        outstream.println();
        
        outstream.println("\t\t\t\t\tswitch ($yy_last_accept_state) {");
        actions("\t\t\t\t\t");
        
        outstream.println("\t\t\t\t\t\tdefault:");
        outstream.println("\t\t\t\t\t\t\t$this->yy_error('INTERNAL', false);");
        outstream.println();
        
        outstream.println("\t\t\t\t\t\tcase -1:");
        outstream.println();
        
        outstream.println("\t\t\t\t\t}");
        outstream.println();
        
        outstream.println("\t\t\t\t\t$yy_initial = true;");
        outstream.println("\t\t\t\t\t$yy_state = self::$yy_state_dtrans[$this->yy_lexical_state];");
        outstream.println("\t\t\t\t\t$yy_next_state = self::YY_NO_STATE;");
        outstream.println("\t\t\t\t\t$yy_last_accept_state = self::YY_NO_STATE;");
        outstream.println();
        
        outstream.println("\t\t\t\t\t$this->yy_mark_start();");
        outstream.println();
        
        outstream.println("\t\t\t\t\t$yy_this_accept = self::$yy_acpt[$yy_state];");
        outstream.println("\t\t\t\t\tif (self::YY_NOT_ACCEPT !== $yy_this_accept) {");
        outstream.println("\t\t\t\t\t\t$yy_last_accept_state = $yy_state;");
        outstream.println("\t\t\t\t\t\t$this->yy_mark_end();");
        outstream.println("\t\t\t\t\t}");
        outstream.println("\t\t\t\t}");
        outstream.println("\t\t\t}");
        outstream.println("\t\t}");
        outstream.println("\t}");
    }
    
    private void actions(String tabs) {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(spec.m_accept_vector.size() == spec.m_anchor_array.length);
        }
        
        int bogus_index = -2;
        int size = spec.m_accept_vector.size();
        for (int elem = 0; elem < size; ++elem) {
            Accept accept = spec.m_accept_vector.elementAt(elem);
            
            if (null != accept) {
                outstream.print(tabs + "case " + elem + ": ");
                outstream.println(new String(accept.action, 0, accept.actionLength));
                
                outstream.println(tabs + "case " + bogus_index + ":");
                outstream.println(tabs + "\tbreak;");
                outstream.println();
                
                --bogus_index;
            }
        }
    }
    
    private void footer() {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
            CUtility.ASSERT(null != outstream);
        }
        
        outstream.println("}");
    }
}
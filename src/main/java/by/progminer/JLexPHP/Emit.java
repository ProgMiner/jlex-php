package by.progminer.JLexPHP;

import by.progminer.JLexPHP.Math.Accept;
import by.progminer.JLexPHP.Math.DTrans;
import by.progminer.JLexPHP.Utility.Utility;

import java.io.PrintWriter;
import java.util.Map;

public class Emit {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean NOT_EDBG = false;

    private Spec spec;
    private PrintWriter out;

    public Emit(Spec spec, PrintWriter out) {
        if (Utility.DEBUG) {
            assert null != spec;
            assert null != out;
        }

        this.spec = spec;
        this.out = out;
    }

    /**
     * Debugging output.
     */
    private void printDetails() {
        System.err.println("---------------------- Transition Table ----------------------");

        for (int i = 0; i < spec.rowMap.length; ++i) {
            System.err.print("State " + i);

            Accept accept = spec.acceptVector.elementAt(i);
            if (null == accept) {
                System.err.println(" [nonaccepting]");
            } else {
                System.err.println(
                    " [accepting, line " + accept.lineNumber +
                        " <" + new String(accept.action, 0, accept.actionLength) + ">]"
                );
            }

            DTrans dTrans = spec.dTransVector.elementAt(spec.rowMap[i]);

            boolean tr = false;
            int state = dTrans.dtrans[spec.colMap[0]];

            if (DTrans.F != state) {
                tr = true;

                System.err.print("\tgoto " + state + " on [" + ((char) 0));
            }

            for (int j = 1; j < spec.dTransNCols; ++j) {
                int next = dTrans.dtrans[spec.colMap[j]];

                if (state == next) {
                    if (DTrans.F != state) {
                        System.err.print((char) j);
                    }
                } else {
                    state = next;

                    if (tr) {
                        tr = false;

                        System.err.println("]");
                    }

                    if (DTrans.F != state) {
                        tr = true;

                        System.err.print("\tgoto " + state + " on [" + ((char) j));
                    }
                }
            }

            if (tr) {
                System.err.println("]");
            }
        }

        System.err.println("---------------------- Transition Table ----------------------");
    }

    /**
     * High-level access function to module.
     */
    public void all() {
        if (Utility.DEBUG) {
            assert null != this.spec;
            assert null != this.out;
        }

        if (Utility.OLD_DEBUG) {
            printDetails();
        }

        header();
        constructor();
        helpers();
        driver();
        footer();
    }

    /**
     * Emits constructor, member variables, and constants.
     */
    private void constructor() {
        if (Utility.DEBUG) {
            assert null != spec;
            assert null != out;
        }

        // Constants
        out.println("\tconst YY_BUFFER_SIZE = 512;");

        out.println("\tconst YY_F = -1;");
        out.println("\tconst YY_NO_STATE = -1;");

        out.println("\tconst YY_NOT_ACCEPT = 0;");
        out.println("\tconst YY_START = 1;");
        out.println("\tconst YY_END = 2;");
        out.println("\tconst YY_NO_ANCHOR = 4;");

        // Internal
        out.println("\tconst YY_BOL = " + spec.BOL + ";");
        out.println("\tconst YY_EOF = " + spec.EOF + ";");

        // External
        if (spec.integerType || spec.yyeof) {
            out.println("\tconst YYEOF = -1;");
        }

        /* User specified class code. */
        if (null != spec.classCode) {
            out.print(new String(spec.classCode, 0, spec.classLength));
        }

        // Member Variables

        if (spec.countChars) {
            out.println("\tprotected $yy_count_chars = true;");
        }

        if (spec.countLines) {
            out.println("\tprotected $yy_count_lines = true;");
        }

        out.println();

        // Constructor

        out.println("\tpublic function __construct($stream) {");

        out.println("\t\tparent::__construct($stream);");
        out.println("\t\t$this->yy_lexical_state = self::YYINITIAL;");

        // User specified constructor code.
        if (null != spec.initCode) {
            out.print(new String(spec.initCode, 0, spec.initLength));
        }

        out.println("\t}");
        out.println();

    }

    /**
     * Emits constants that serve as lexical states,
     * including YYINITIAL.
     */
    private void states() {
        for (Map.Entry <String, Integer> entry: spec.states.entrySet()) {
            out.println("\tconst " + entry.getKey() + " = " + entry.getValue() + ";");
        }

        out.println("\tstatic $yy_state_dtrans = [");
        for (int index = 0; index < spec.stateDTrans.length; ++index) {
            out.print("\t\t" + spec.stateDTrans[index]);

            if (index < spec.stateDTrans.length - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }
        out.println("\t];");
    }

    /**
     * Emits helper functions, particularly
     * error handling and input buffering.
     */
    private void helpers() {
        if (Utility.DEBUG) {
            assert null != spec;
            assert null != out;
        }

        if (null != spec.eofCode) {
            // Function yy_do_eof

            out.print("\tprivate function yy_do_eof () {");

            out.println("\t\tif (false === $this->yy_eof_done) {");
            out.print(new String(spec.eofCode, 0, spec.eofLength));
            out.println("\t\t}");
            out.println("\t\t$this->yy_eof_done = true;");
            out.println("\t}");
        }

        states();

    }

    /**
     * Emits class header.
     */
    private void header() {
        if (Utility.DEBUG) {
            assert null != spec;
            assert null != out;
        }

        out.println();

        if (spec.public_) {
            out.print("public ");
        }

        out.print("class ");
        out.print(new String(spec.className, 0, spec.className.length));
        out.print(" extends JLexPHP\\Base ");

        if (spec.implementsName.length > 0) {
            out.print(" implements ");
            out.print(new String(spec.implementsName, 0, spec.implementsName.length));
        }

        out.println(" {");
    }

    /**
     * Emits transition table.
     */
    private void table() {
        if (Utility.DEBUG) {
            assert null != spec;
            assert null != out;
        }

        out.println("\tstatic $yy_acpt = [");

        int size = spec.acceptVector.size();
        for (int elem = 0; elem < size; ++elem) {
            Accept accept = spec.acceptVector.elementAt(elem);

            out.print("\t\t/* " + elem + " */ ");
            if (null != accept) {
                boolean is_start = (0 != (spec.anchorArray[elem] & Spec.START));
                boolean is_end = (0 != (spec.anchorArray[elem] & Spec.END));

                if (is_start && is_end) {
                    out.print("3 /* self::YY_START | self::YY_END */");
                } else if (is_start) {
                    out.print("self::YY_START");
                } else if (is_end) {
                    out.print("self::YY_END");
                } else {
                    out.print("self::YY_NO_ANCHOR");
                }
            } else {
                out.print("self::YY_NOT_ACCEPT");
            }

            if (elem < size - 1) {
                out.print(",");
            }

            out.println();
        }

        out.println("\t];");

        // CSA: modified yy_cmap to use string packing 9-Aug-1999

        int[] yy_cmap = new int[spec.cClsMap.length];
        for (int i = 0; i < spec.cClsMap.length; ++i) {
            yy_cmap[i] = spec.colMap[spec.cClsMap[i]];
        }

        out.print("\t\tstatic $yy_cmap = ");
        tableAsArray(yy_cmap);
        out.println();

        // CSA: modified yy_rmap to use string packing 9-Aug-1999

        out.print("\t\tstatic $yy_rmap = ");
        tableAsArray(spec.rowMap);
        out.println();

        // 6/24/98 Raimondas Lencevicius
        // modified to use
        //    int[][] unpackFromString(int size1, int size2, String st)

        size = spec.dTransVector.size();
        int[][] yy_nxt = new int[size][];
        for (int elem = 0; elem < size; elem++) {
            DTrans dTrans = spec.dTransVector.elementAt(elem);

            assert dTrans.dtrans.length == spec.dTransNCols;

            yy_nxt[elem] = dTrans.dtrans;
        }

        out.print("\t\tstatic $yy_nxt = ");
        tableAsArray2D(yy_nxt);
        out.println();
    }

    private void tableAsArray(int[] ia) {
        out.println("[");

        for (int i = 0; i < ia.length; ++i) {
            out.print(" " + ia[i] + ",");

            if (i % 20 == 19) {
                out.println();
            }
        }

        out.println("];");
    }

    private void tableAsArray2D(int[][] ia) {
        out.println("[");

        for (int[] anIa : ia) {
            out.println("[");

            for (int i = 0; i < anIa.length; ++i) {
                out.print(" " + anIa[i] + ",");

                if (i % 20 == 19) {
                    out.println();
                }
            }

            out.println();
            out.println("],");
        }
        out.println("];");
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

        out.print(ia.length);
        out.print(",");
        out.print(ia.length > 0 ? ia[0].length : 0);
        out.println(",");

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
                    out.println("\"" + s.substring(0, 75) + "\" .");
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
            out.println("\"" + s.substring(0, 75) + "\" +");
            outstr = new StringBuffer(s.substring(75));
        }

        out.print("\"" + outstr + "\"");
    }

    private void driver() {
        if (Utility.DEBUG) {
            assert null != spec;
            assert null != out;
        }

        table();

        out.print("\tpublic function ");

        if (!spec.integerType && !spec.intWrapType) {
            out.print("/*");
            out.print(new String(spec.typeName));
            out.print("*/ ");
        }

        out.print(new String(spec.functionName));
        out.println(" () {");

        out.println("\t\t$yy_anchor = self::YY_NO_ANCHOR;");
        out.println("\t\t$yy_state = self::$yy_state_dtrans[$this->yy_lexical_state];");
        out.println("\t\t$yy_next_state = self::YY_NO_STATE;");
        out.println("\t\t$yy_last_accept_state = self::YY_NO_STATE;");
        out.println("\t\t$yy_initial = true;");
        out.println();

        out.println("\t\t$this->yy_mark_start();");
        out.println();

        out.println("\t\t$yy_this_accept = self::$yy_acpt[$yy_state];");
        out.println("\t\tif (self::YY_NOT_ACCEPT !== $yy_this_accept) {");
        out.println("\t\t\t$yy_last_accept_state = $yy_state;");
        out.println("\t\t\t$this->yy_mark_end();");
        out.println("\t\t}");
        out.println();

        if (NOT_EDBG) {
            out.println("\t\techo \"Begin\\n\";");
            out.println();
        }

        out.println("\t\twhile (true) {");

        out.println("\t\t\t$yy_lookahead = self::YY_BOL;");
        out.println();

        out.println("\t\t\tif (!$yy_initial || !$this->yy_at_bol) {");
        out.println("\t\t\t\t$yy_lookahead = $this->yy_advance();");
        out.println("\t\t\t}");
        out.println();

        out.println("\t\t\t$yy_next_state = self::$yy_nxt[self::$yy_rmap[$yy_state]][self::$yy_cmap[$yy_lookahead]];");
        out.println();

        if (NOT_EDBG) {
            out.println("\t\t\techo \"Current state: $yy_state\tCurrent input: $yy_lookahead\";");
            out.println();

            out.println("\t\t\techo \"State = $yy_state\\n\";");
            out.println("\t\t\techo \"Accepting status = $yy_this_accept\";");
            out.println("\t\t\techo \"Last accepting state = $yy_last_accept_state\";");
            out.println("\t\t\techo \"Next state = $yy_next_state\";");
            out.println("\t\t\techo \"Lookahead input = $yy_lookahead;\"");
            out.println();
        }

        // handle bare EOF

        out.println("\t\t\tif (self::YY_EOF === $yy_lookahead && $yy_initial) {");

        if (null != spec.eofCode) {
            out.println("\t\t\t\t$this->yy_do_eof();");
        }

        if (spec.integerType) {
            out.println("\t\t\t\treturn self::YYEOF;");
        } else if (null != spec.eofValueCode) {
            out.print(new String(spec.eofValueCode, 0, spec.eofValueLength));
        } else {
            out.println("\t\t\t\treturn null;");
        }

        out.println("\t\t\t}");
        out.println();

        out.println("\t\t\tif (self::YY_F !== $yy_next_state) {");
        out.println("\t\t\t\t$yy_state = $yy_next_state;");
        out.println("\t\t\t\t$yy_initial = false;");
        out.println();

        out.println("\t\t\t\t$yy_this_accept = self::$yy_acpt[$yy_state];");
        out.println("\t\t\t\tif (self::YY_NOT_ACCEPT !== $yy_this_accept) {");
        out.println("\t\t\t\t\t$yy_last_accept_state = $yy_state;");
        out.println("\t\t\t\t\t$this->yy_mark_end();");
        out.println("\t\t\t\t}");
        out.println("\t\t\t} else {");
        out.println("\t\t\t\tif (self::YY_NO_STATE === $yy_last_accept_state) {");
        out.println("\t\t\t\t\tthrow new \\Exception(\"Lexical Error: Unmatched Input.\");");
        out.println("\t\t\t\t} else {");
        out.println("\t\t\t\t\t$yy_anchor = self::$yy_acpt[$yy_last_accept_state];");
        out.println();

        out.println("\t\t\t\t\tif (0 !== (self::YY_END & $yy_anchor)) {");
        out.println("\t\t\t\t\t\t$this->yy_move_end();");
        out.println("\t\t\t\t\t}");
        out.println();

        out.println("\t\t\t\t\t$this->yy_to_mark();");
        out.println();

        out.println("\t\t\t\t\tswitch ($yy_last_accept_state) {");
        actions("\t\t\t\t\t");

        out.println("\t\t\t\t\t\tdefault:");
        out.println("\t\t\t\t\t\t\t$this->yy_error('INTERNAL', false);");
        out.println();

        out.println("\t\t\t\t\t\tcase -1:");
        out.println();

        out.println("\t\t\t\t\t}");
        out.println();

        out.println("\t\t\t\t\t$yy_initial = true;");
        out.println("\t\t\t\t\t$yy_state = self::$yy_state_dtrans[$this->yy_lexical_state];");
        out.println("\t\t\t\t\t$yy_next_state = self::YY_NO_STATE;");
        out.println("\t\t\t\t\t$yy_last_accept_state = self::YY_NO_STATE;");
        out.println();

        out.println("\t\t\t\t\t$this->yy_mark_start();");
        out.println();

        out.println("\t\t\t\t\t$yy_this_accept = self::$yy_acpt[$yy_state];");
        out.println("\t\t\t\t\tif (self::YY_NOT_ACCEPT !== $yy_this_accept) {");
        out.println("\t\t\t\t\t\t$yy_last_accept_state = $yy_state;");
        out.println("\t\t\t\t\t\t$this->yy_mark_end();");
        out.println("\t\t\t\t\t}");
        out.println("\t\t\t\t}");
        out.println("\t\t\t}");
        out.println("\t\t}");
        out.println("\t}");
    }

    private void actions(String tabs) {
        if (Utility.DEBUG) {
            assert spec.acceptVector.size() == spec.anchorArray.length;
        }

        int bogus_index = -2;
        int size = spec.acceptVector.size();
        for (int elem = 0; elem < size; ++elem) {
            Accept accept = spec.acceptVector.elementAt(elem);

            if (null != accept) {
                out.print(tabs + "case " + elem + ": ");
                out.println(new String(accept.action, 0, accept.actionLength));

                out.println(tabs + "case " + bogus_index + ":");
                out.println(tabs + "\tbreak;");
                out.println();

                --bogus_index;
            }
        }
    }

    private void footer() {
        if (Utility.DEBUG) {
            assert null != spec;
            assert null != out;
        }

        out.println("}");
    }
}
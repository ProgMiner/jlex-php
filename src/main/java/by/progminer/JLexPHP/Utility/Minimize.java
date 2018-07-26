package by.progminer.JLexPHP.Utility;

import by.progminer.JLexPHP.Math.Accept;
import by.progminer.JLexPHP.Math.DTrans;
import by.progminer.JLexPHP.Math.SparseBitSet;
import by.progminer.JLexPHP.Spec;

import java.util.Vector;

public class Minimize {

    Spec spec;
    Vector <Vector <DTrans> > group;
    int inGroup[];
    
    public Minimize() {
        reset();
    }

    /**
     * Resets member variables.
     */
    private void reset() {
        spec = null;
        group = null;
        inGroup = null;
    }

    /**
     * Sets member variables.
     */
    private void set(Spec spec) {
        if (Utility.DEBUG) {
            Utility.ASSERT(null != spec);
        }

        this.spec = spec;
        group = null;
        inGroup = null;
    }

    /**
     * High-level access function to module.
     */
    public void minDFA(Spec spec) {
        set(spec);

        // Remove redundant states.
        minimize();

        // Column and row compression.
        // Save accept states in auxilary vector.
        reduce();

        reset();
    }

    /**
     * Copies source column into destination column.
     */
    private void colCopy(int dest, int src) {
        int n = spec.dTransVector.size();

        for (int i = 0; i < n; ++i) {
            DTrans dTrans = spec.dTransVector.elementAt(i);
            dTrans.dtrans[dest] = dTrans.dtrans[src];
        }
    }

    /**
     * Truncates each column to the 'correct' length.
     */
    private void truncCol() {
        int n = spec.dTransVector.size();

        for (int i = 0; i < n; ++i) {
            DTrans dTrans = spec.dTransVector.elementAt(i);
            int[] nDTrans = new int[spec.dTransNCols];

            System.arraycopy(dTrans.dtrans, 0, nDTrans, 0, nDTrans.length);
            dTrans.dtrans = nDTrans;
        }
    }

    /**
     * Copies source row into destination row.
     */
    private void rowCopy(int dest, int src) {
        spec.dTransVector.setElementAt(spec.dTransVector.elementAt(src), dest);
    }

    private boolean colEquiv(int col1, int col2) {
        int n = spec.dTransVector.size();

        for (int i = 0; i < n; ++i) {
            DTrans dTrans = spec.dTransVector.elementAt(i);

            if (dTrans.dtrans[col1] != dTrans.dtrans[col2]) {
                return false;
            }
        }

        return true;
    }

    private boolean rowEquiv(int row1, int row2) {
        DTrans dTrans1 = spec.dTransVector.elementAt(row1),
               dTrans2 = spec.dTransVector.elementAt(row2);

        for (int i = 0; i < spec.dTransNCols; ++i) {
            if (dTrans1.dtrans[i] != dTrans2.dtrans[i]) {
                return false;
            }
        }

        return true;
    }

    private void reduce() {
        SparseBitSet set = new SparseBitSet();

        // Save accept nodes and anchor entries
        int size = spec.dTransVector.size();

        spec.anchorArray = new int[size];
        spec.acceptVector = new Vector <Accept> ();

        for (int i = 0; i < size; ++i) {
            DTrans dTrans = spec.dTransVector.elementAt(i);

            spec.acceptVector.addElement(dTrans.accept);
            spec.anchorArray[i] = dTrans.anchor;

            dTrans.accept = null;
        }

        // Allocate column map
        spec.colMap = new int[spec.dTransNCols];
        for (int i = 0; i < spec.dTransNCols; ++i) {
            spec.colMap[i] = -1;
        }

        int reducedNCols;

        // Process columns for reduction
        for (reducedNCols = 0; ; ++reducedNCols) {
            if (Utility.DEBUG) {
                for (int i = 0; i < reducedNCols; ++i) {
                    Utility.ASSERT(-1 != spec.colMap[i]);
                }
            }

            int i;
            for (i = reducedNCols; i < spec.dTransNCols; ++i) {
                if (-1 == spec.colMap[i]) {
                    break;
                }
            }

            if (i >= spec.dTransNCols) {
                break;
            }

            if (Utility.DEBUG) {
                Utility.ASSERT(!set.get(i));
                Utility.ASSERT(-1 == spec.colMap[i]);
            }

            set.set(i);

            spec.colMap[i] = reducedNCols;

            // UNDONE: Optimize by doing all comparisons in one batch
            for (int j = i + 1; j < spec.dTransNCols; ++j) {
                if (-1 == spec.colMap[j] && colEquiv(i, j)) {
                    spec.colMap[j] = reducedNCols;
                }
            }
        }

        // Reduce columns
        int k = 0;
        for (int i = 0; i < spec.dTransNCols; ++i) {
            if (set.get(i)) {
                ++k;

                set.clear(i);

                int j = spec.colMap[i];

                if (Utility.DEBUG) {
                    Utility.ASSERT(j <= i);
                }

                if (j == i) {
                    continue;
                }

                colCopy(j, i);
            }
        }

        spec.dTransNCols = reducedNCols;

        // truncate dTrans at proper length (freeing extra)
        truncCol();

        if (Utility.DEBUG) {
            Utility.ASSERT(k == reducedNCols);
        }

        // Allocate row map
        int nRows = spec.dTransVector.size();

        spec.rowMap = new int[nRows];
        for (int i = 0; i < nRows; ++i) {
            spec.rowMap[i] = -1;
        }

        int reducedNRows;

        // Process rows to reduce
        for (reducedNRows = 0; ; ++reducedNRows) {
            if (Utility.DEBUG) {
                for (int i = 0; i < reducedNRows; ++i) {
                    Utility.ASSERT(-1 != spec.rowMap[i]);
                }
            }

            int i;
            for (i = reducedNRows; i < nRows; ++i) {
                if (-1 == spec.rowMap[i]) {
                    break;
                }
            }

            if (i >= nRows) {
                break;
            }

            if (Utility.DEBUG) {
                Utility.ASSERT(!set.get(i));
                Utility.ASSERT(-1 == spec.rowMap[i]);
            }

            set.set(i);

            spec.rowMap[i] = reducedNRows;

            // TODO: UNDONE: Optimize by doing all comparisons in one batch
            for (int j = i + 1; j < nRows; ++j) {
                if (-1 == spec.rowMap[j] && rowEquiv(i, j)) {
                    spec.rowMap[j] = reducedNRows;
                }
            }
        }

        // Reduce rows
        k = 0;
        for (int i = 0; i < nRows; ++i) {
            if (set.get(i)) {
                ++k;

                set.clear(i);

                int j = spec.rowMap[i];

                if (Utility.DEBUG) {
                    Utility.ASSERT(j <= i);
                }

                if (j == i) {
                    continue;
                }

                rowCopy(j, i);
            }
        }

        spec.dTransVector.setSize(reducedNRows);

        if (Utility.DEBUG) {
            // System.out.println("k = " + k + "\nreducedNRows = " + reducedNRows + "");

            Utility.ASSERT(k == reducedNRows);
        }
    }

    /**
     * Updates DTrans table after minimization
     * using groups, removing redundant transition table states.
     */
    private void fixDTrans() {
        Vector <DTrans> newVector = new Vector <DTrans> ();

        int size = spec.stateDTrans.length;
        for (int i = 0; i < size; ++i) {
            if (DTrans.F != spec.stateDTrans[i]) {
                spec.stateDTrans[i] = inGroup[spec.stateDTrans[i]];
            }
        }

        size = group.size();
        for (int i = 0; i < size; ++i) {
            Vector <DTrans> dTransGroup = group.elementAt(i);

            DTrans first = dTransGroup.elementAt(0);
            newVector.addElement(first);

            for (int c = 0; c < spec.dTransNCols; ++c) {
                if (DTrans.F != first.dtrans[c]) {
                    first.dtrans[c] = inGroup[first.dtrans[c]];
                }
            }
        }

        group = null;
        spec.dTransVector = newVector;
    }

    /**
     * Removes redundant transition table states.
     */
    private void minimize() {
        initGroups();

        int groupCount = group.size();
        int oldGroupCount = groupCount - 1;

        while (oldGroupCount != groupCount) {
            oldGroupCount = groupCount;

            if (Utility.DEBUG) {
                Utility.ASSERT(group.size() == groupCount);
            }

            for (int i = 0; i < groupCount; ++i) {
                Vector <DTrans> dTransGroup = group.elementAt(i);

                int group_size = dTransGroup.size();
                if (group_size <= 1) {
                    continue;
                }

                Vector <DTrans> newGroup = new Vector <DTrans> ();
                boolean added = false;

                DTrans first = dTransGroup.elementAt(0);
                for (int j = 1; j < group_size; ++j) {
                    DTrans next = dTransGroup.elementAt(j);

                    for (int c = 0; c < spec.dTransNCols; ++c) {
                        int goto_first = first.dtrans[c];
                        int goto_next = next.dtrans[c];

                        if (
                            goto_first != goto_next &&
                            (
                                goto_first == DTrans.F ||
                                goto_next == DTrans.F ||
                                inGroup[goto_next] != inGroup[goto_first]
                            )
                        ) {
                            if (Utility.DEBUG) {
                                Utility.ASSERT(dTransGroup.elementAt(j) == next);
                            }

                            dTransGroup.removeElementAt(j);

                            --j;
                            --group_size;

                            newGroup.addElement(next);
                            if (!added) {
                                added = true;

                                ++groupCount;
                                group.addElement(newGroup);
                            }

                            inGroup[next.label] = group.size() - 1;

                            if (Utility.DEBUG) {
                                Utility.ASSERT(group.contains(newGroup));
                                Utility.ASSERT(group.contains(dTransGroup));
                                Utility.ASSERT(dTransGroup.contains(first));
                                Utility.ASSERT(!dTransGroup.contains(next));
                                Utility.ASSERT(!newGroup.contains(first));
                                Utility.ASSERT(newGroup.contains(next));
                                Utility.ASSERT(dTransGroup.size() == group_size);
                                Utility.ASSERT(i == inGroup[first.label]);
                                Utility.ASSERT((group.size() - 1) == inGroup[next.label]);
                            }

                            break;
                        }
                    }
                }
            }
        }

        System.out.println(group.size() + " states after removal of redundant states.");

        if (spec.verbose && Utility.OLD_DUMP_DEBUG) {
            System.out.println();
            System.out.println("States grouped as follows after minimization");

            printGroups();
        }

        fixDTrans();
    }

    private void initGroups() {
        group = new Vector <Vector <DTrans> > ();
        int groupCount = 0;

        int size = spec.dTransVector.size();
        inGroup = new int[size];

        for (int i = 0; i < size; ++i) {
            boolean isGroupFound = false;
            DTrans dTrans = spec.dTransVector.elementAt(i);

            if (Utility.DEBUG) {
                Utility.ASSERT(i == dTrans.label);
                Utility.ASSERT(groupCount == group.size());
            }

            for (int j = 0; j < groupCount; ++j) {
                Vector <DTrans> dTransGroup = group.elementAt(j);

                if (Utility.DEBUG) {
                    Utility.ASSERT(0 < dTransGroup.size());
                }

                DTrans first = dTransGroup.elementAt(0);

                if (Utility.SLOW_DEBUG) {
                    int s = dTransGroup.size();
                    Utility.ASSERT(0 < s);

                    for (int k = 1; k < s; ++k) {
                        Utility.ASSERT(dTransGroup.elementAt(k).accept == first.accept);
                    }
                }

                if (first.accept == dTrans.accept) {
                    dTransGroup.addElement(dTrans);
                    inGroup[i] = j;
                    isGroupFound = true;

                    if (Utility.DEBUG) {
                        Utility.ASSERT(j == inGroup[dTrans.label]);
                    }

                    break;
                }
            }

            if (!isGroupFound) {
                Vector <DTrans> dTransGroup = new Vector <DTrans> ();
                dTransGroup.addElement(dTrans);

                inGroup[i] = group.size();
                group.addElement(dTransGroup);

                ++groupCount;
            }
        }

        if (spec.verbose && Utility.OLD_DUMP_DEBUG) {
            System.out.println("Initial grouping:");
            printGroups();
            System.out.println();
        }
    }

    private void printSet(Vector <DTrans> dTransGroup) {
        for (int i = 0; i < dTransGroup.size(); ++i) {
            System.out.print(dTransGroup.elementAt(i).label + " ");
        }
    }

    private void printGroups() {
        for (int i = 0; i < group.size(); ++i) {
            System.out.print("\tGroup " + i + " {");
            printSet(group.elementAt(i));
            System.out.println("}");
            System.out.println();
        }

        System.out.println();
        for (int i = 0; i < spec.dTransVector.size(); ++i) {
            System.out.println("\tstate " + i + " is in group " + inGroup[i]);
        }
    }
}

package by.progminer.JLexPHP;

import java.util.Vector;

class Minimize {

    Spec spec;
    Vector <Vector <DTrans> > group;
    int inGroup[];

    Minimize() {
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
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
        }

        this.spec = spec;
        group = null;
        inGroup = null;
    }

    /**
     * High-level access function to module.
     */
    void minDFA(Spec spec) {
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
    private void col_copy(int dest, int src) {
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
            if (CUtility.DEBUG) {
                for (int i = 0; i < reducedNCols; ++i) {
                    CUtility.ASSERT(-1 != spec.colMap[i]);
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

            if (CUtility.DEBUG) {
                CUtility.ASSERT(!set.get(i));
                CUtility.ASSERT(-1 == spec.colMap[i]);
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

                if (CUtility.DEBUG) {
                    CUtility.ASSERT(j <= i);
                }

                if (j == i) {
                    continue;
                }

                col_copy(j, i);
            }
        }

        spec.dTransNCols = reducedNCols;

        // truncate dTrans at proper length (freeing extra)
        truncCol();

        if (CUtility.DEBUG) {
            CUtility.ASSERT(k == reducedNCols);
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
            if (CUtility.DEBUG) {
                for (int i = 0; i < reducedNRows; ++i) {
                    CUtility.ASSERT(-1 != spec.rowMap[i]);
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

            if (CUtility.DEBUG) {
                CUtility.ASSERT(!set.get(i));
                CUtility.ASSERT(-1 == spec.rowMap[i]);
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

                if (CUtility.DEBUG) {
                    CUtility.ASSERT(j <= i);
                }

                if (j == i) {
                    continue;
                }

                rowCopy(j, i);
            }
        }

        spec.dTransVector.setSize(reducedNRows);

        if (CUtility.DEBUG) {
            // System.out.println("k = " + k + "\nreducedNRows = " + reducedNRows + "");

            CUtility.ASSERT(k == reducedNRows);
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

            if (CUtility.DEBUG) {
                CUtility.ASSERT(group.size() == groupCount);
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
                            if (CUtility.DEBUG) {
                                CUtility.ASSERT(dTransGroup.elementAt(j) == next);
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

                            if (CUtility.DEBUG) {
                                CUtility.ASSERT(group.contains(newGroup));
                                CUtility.ASSERT(group.contains(dTransGroup));
                                CUtility.ASSERT(dTransGroup.contains(first));
                                CUtility.ASSERT(!dTransGroup.contains(next));
                                CUtility.ASSERT(!newGroup.contains(first));
                                CUtility.ASSERT(newGroup.contains(next));
                                CUtility.ASSERT(dTransGroup.size() == group_size);
                                CUtility.ASSERT(i == inGroup[first.label]);
                                CUtility.ASSERT((group.size() - 1) == inGroup[next.label]);
                            }

                            break;
                        }
                    }
                }
            }
        }

        System.out.println(group.size() + " states after removal of redundant states.");

        if (spec.verbose && CUtility.OLD_DUMP_DEBUG) {
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

            if (CUtility.DEBUG) {
                CUtility.ASSERT(i == dTrans.label);
                CUtility.ASSERT(groupCount == group.size());
            }

            for (int j = 0; j < groupCount; ++j) {
                Vector <DTrans> dTransGroup = group.elementAt(j);

                if (CUtility.DEBUG) {
                    CUtility.ASSERT(0 < dTransGroup.size());
                }

                DTrans first = dTransGroup.elementAt(0);

                if (CUtility.SLOW_DEBUG) {
                    int s = dTransGroup.size();
                    CUtility.ASSERT(0 < s);

                    for (int k = 1; k < s; ++k) {
                        CUtility.ASSERT(dTransGroup.elementAt(k).accept == first.accept);
                    }
                }

                if (first.accept == dTrans.accept) {
                    dTransGroup.addElement(dTrans);
                    inGroup[i] = j;
                    isGroupFound = true;

                    if (CUtility.DEBUG) {
                        CUtility.ASSERT(j == inGroup[dTrans.label]);
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

        if (spec.verbose && CUtility.OLD_DUMP_DEBUG) {
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

package by.progminer.JLexPHP;

import java.util.Vector;

class CMinimize {
    
    CSpec m_spec;
    Vector <Vector <CDTrans> > m_group;
    int m_ingroup[];
    
    CMinimize() {
        reset();
    }
    
    /**
     * Resets member variables.
     */
    private void reset() {
        m_spec = null;
        m_group = null;
        m_ingroup = null;
    }
    
    /**
     * Sets member variables.
     */
    private void set(CSpec spec) {
        if (CUtility.DEBUG) {
            CUtility.ASSERT(null != spec);
        }
        
        m_spec = spec;
        m_group = null;
        m_ingroup = null;
    }
    
    /**
     * High-level access function to module.
     */
    void min_dfa(CSpec spec) {
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
        int n = m_spec.m_dtrans_vector.size();
        
        for (int i = 0; i < n; ++i) {
            CDTrans dtrans = m_spec.m_dtrans_vector.elementAt(i);
            dtrans.m_dtrans[dest] = dtrans.m_dtrans[src];
        }
    }
    
    /**
     * Truncates each column to the 'correct' length.
     */
    private void trunc_col() {
        int n = m_spec.m_dtrans_vector.size();
        
        for (int i = 0; i < n; ++i) {
            CDTrans dtrans = m_spec.m_dtrans_vector.elementAt(i);
            int[] ndtrans = new int[m_spec.m_dtrans_ncols];
            
            System.arraycopy(dtrans.m_dtrans, 0, ndtrans, 0, ndtrans.length);
            dtrans.m_dtrans = ndtrans;
        }
    }
    
    /**
     * Copies source row into destination row.
     */
    private void row_copy(int dest, int src) {
        CDTrans dtrans = m_spec.m_dtrans_vector.elementAt(src);
        
        m_spec.m_dtrans_vector.setElementAt(dtrans, dest);
    }
    
    private boolean col_equiv(int col1, int col2) {
        int n = m_spec.m_dtrans_vector.size();
        
        for (int i = 0; i < n; ++i) {
            CDTrans dtrans = m_spec.m_dtrans_vector.elementAt(i);
            
            if (dtrans.m_dtrans[col1] != dtrans.m_dtrans[col2]) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean row_equiv(int row1, int row2) {
        CDTrans dtrans1 = m_spec.m_dtrans_vector.elementAt(row1),
            dtrans2 = m_spec.m_dtrans_vector.elementAt(row2);
        
        for (int i = 0; i < m_spec.m_dtrans_ncols; ++i) {
            if (dtrans1.m_dtrans[i] != dtrans2.m_dtrans[i]) {
                return false;
            }
        }
        
        return true;
    }
    
    //////
    
    private void reduce() {
        int i;
        int j;
        int k;
        int nrows;
        int reduced_ncols;
        int reduced_nrows;
        SparseBitSet set;
        CDTrans dtrans;
        int size;
        
        set = new SparseBitSet();
        
        /* Save accept nodes and anchor entries. */
        size = m_spec.m_dtrans_vector.size();
        
        m_spec.m_anchor_array = new int[size];
        m_spec.m_accept_vector = new Vector <CAccept> ();
        
        for (i = 0; i < size; ++i) {
            dtrans = m_spec.m_dtrans_vector.elementAt(i);
            
            m_spec.m_accept_vector.addElement(dtrans.m_accept);
            m_spec.m_anchor_array[i] = dtrans.m_anchor;
            
            dtrans.m_accept = null;
        }
        
        /* Allocate column map. */
        m_spec.m_col_map = new int[m_spec.m_dtrans_ncols];
        for (i = 0; i < m_spec.m_dtrans_ncols; ++i) {
            m_spec.m_col_map[i] = -1;
        }
        
        /* Process columns for reduction. */
        for (reduced_ncols = 0; ; ++reduced_ncols) {
            if (CUtility.DEBUG) {
                for (i = 0; i < reduced_ncols; ++i) {
                    CUtility.ASSERT(-1 != m_spec.m_col_map[i]);
                }
            }
            
            for (i = reduced_ncols; i < m_spec.m_dtrans_ncols; ++i) {
                if (-1 == m_spec.m_col_map[i]) {
                    break;
                }
            }
            
            if (i >= m_spec.m_dtrans_ncols) {
                break;
            }
            
            if (CUtility.DEBUG) {
                CUtility.ASSERT(!set.get(i));
                CUtility.ASSERT(-1 == m_spec.m_col_map[i]);
            }
            
            set.set(i);
            
            m_spec.m_col_map[i] = reduced_ncols;
            
            /* UNDONE: Optimize by doing all comparisons in one batch. */
            for (j = i + 1; j < m_spec.m_dtrans_ncols; ++j) {
                if (-1 == m_spec.m_col_map[j] && col_equiv(i, j)) {
                    m_spec.m_col_map[j] = reduced_ncols;
                }
            }
        }
        
        /* Reduce columns. */
        k = 0;
        for (i = 0; i < m_spec.m_dtrans_ncols; ++i) {
            if (set.get(i)) {
                ++k;
                
                set.clear(i);
                
                j = m_spec.m_col_map[i];
                
                if (CUtility.DEBUG) {
                    CUtility.ASSERT(j <= i);
                }
                
                if (j == i) {
                    continue;
                }
                
                col_copy(j, i);
            }
        }
        m_spec.m_dtrans_ncols = reduced_ncols;
        /* truncate m_dtrans at proper length (freeing extra) */
        trunc_col();
        
        if (CUtility.DEBUG) {
            CUtility.ASSERT(k == reduced_ncols);
        }
        
        /* Allocate row map. */
        nrows = m_spec.m_dtrans_vector.size();
        m_spec.m_row_map = new int[nrows];
        for (i = 0; i < nrows; ++i) {
            m_spec.m_row_map[i] = -1;
        }
        
        /* Process rows to reduce. */
        for (reduced_nrows = 0; ; ++reduced_nrows) {
            if (CUtility.DEBUG) {
                for (i = 0; i < reduced_nrows; ++i) {
                    CUtility.ASSERT(-1 != m_spec.m_row_map[i]);
                }
            }
            
            for (i = reduced_nrows; i < nrows; ++i) {
                if (-1 == m_spec.m_row_map[i]) {
                    break;
                }
            }
            
            if (i >= nrows) {
                break;
            }
            
            if (CUtility.DEBUG) {
                CUtility.ASSERT(!set.get(i));
                CUtility.ASSERT(-1 == m_spec.m_row_map[i]);
            }
            
            set.set(i);
            
            m_spec.m_row_map[i] = reduced_nrows;
            
            /* UNDONE: Optimize by doing all comparisons in one batch. */
            for (j = i + 1; j < nrows; ++j) {
                if (-1 == m_spec.m_row_map[j] && row_equiv(i, j)) {
                    m_spec.m_row_map[j] = reduced_nrows;
                }
            }
        }
        
        /* Reduce rows. */
        k = 0;
        for (i = 0; i < nrows; ++i) {
            if (set.get(i)) {
                ++k;
                
                set.clear(i);
                
                j = m_spec.m_row_map[i];
                
                if (CUtility.DEBUG) {
                    CUtility.ASSERT(j <= i);
                }
                
                if (j == i) {
                    continue;
                }
                
                row_copy(j, i);
            }
        }
        m_spec.m_dtrans_vector.setSize(reduced_nrows);
        
        if (CUtility.DEBUG) {
            /*System.out.println("k = " + k + "\nreduced_nrows = " + reduced_nrows + "");*/
            
            CUtility.ASSERT(k == reduced_nrows);
        }
    }
    
    /***************************************************************
     Function: fix_dtrans
     Description: Updates CDTrans table after minimization
     using groups, removing redundant transition table states.
     **************************************************************/
    private void fix_dtrans() {
        Vector <CDTrans> new_vector = new Vector <CDTrans> ();
    
        int size = m_spec.m_state_dtrans.length;
        for (int i = 0; i < size; ++i) {
            if (CDTrans.F != m_spec.m_state_dtrans[i]) {
                m_spec.m_state_dtrans[i] = m_ingroup[m_spec.m_state_dtrans[i]];
            }
        }
        
        size = m_group.size();
        for (int i = 0; i < size; ++i) {
            Vector <CDTrans> dtrans_group = m_group.elementAt(i);
            CDTrans first = dtrans_group.elementAt(0);
            new_vector.addElement(first);
            
            for (int c = 0; c < m_spec.m_dtrans_ncols; ++c) {
                if (CDTrans.F != first.m_dtrans[c]) {
                    first.m_dtrans[c] = m_ingroup[first.m_dtrans[c]];
                }
            }
        }
        
        m_group = null;
        m_spec.m_dtrans_vector = new_vector;
    }
    
    /***************************************************************
     Function: minimize
     Description: Removes redundant transition table states.
     **************************************************************/
    private void minimize() {
        init_groups();
    
        int group_count = m_group.size();
        int old_group_count = group_count - 1;
        
        while (old_group_count != group_count) {
            old_group_count = group_count;
            
            if (CUtility.DEBUG) {
                CUtility.ASSERT(m_group.size() == group_count);
            }
            
            for (int i = 0; i < group_count; ++i) {
                Vector <CDTrans> dtrans_group = m_group.elementAt(i);
    
                int group_size = dtrans_group.size();
                if (group_size <= 1) {
                    continue;
                }
    
                Vector <CDTrans> new_group = new Vector <CDTrans> ();
                boolean added = false;
    
                CDTrans first = dtrans_group.elementAt(0);
                for (int j = 1; j < group_size; ++j) {
                    CDTrans next = dtrans_group.elementAt(j);
                    
                    for (int c = 0; c < m_spec.m_dtrans_ncols; ++c) {
                        int goto_first = first.m_dtrans[c];
                        int goto_next = next.m_dtrans[c];
                        
                        if (
                            goto_first != goto_next &&
                            (
                                goto_first == CDTrans.F ||
                                goto_next == CDTrans.F ||
                                m_ingroup[goto_next] != m_ingroup[goto_first]
                            )
                        ) {
                            if (CUtility.DEBUG) {
                                CUtility.ASSERT(dtrans_group.elementAt(j) == next);
                            }
                            
                            dtrans_group.removeElementAt(j);
                            
                            --j;
                            --group_size;
                            
                            new_group.addElement(next);
                            if (!added) {
                                added = true;
                                
                                ++group_count;
                                m_group.addElement(new_group);
                            }
                            
                            m_ingroup[next.m_label] = m_group.size() - 1;
                            
                            if (CUtility.DEBUG) {
                                CUtility.ASSERT(m_group.contains(new_group));
                                CUtility.ASSERT(m_group.contains(dtrans_group));
                                CUtility.ASSERT(dtrans_group.contains(first));
                                CUtility.ASSERT(!dtrans_group.contains(next));
                                CUtility.ASSERT(!new_group.contains(first));
                                CUtility.ASSERT(new_group.contains(next));
                                CUtility.ASSERT(dtrans_group.size() == group_size);
                                CUtility.ASSERT(i == m_ingroup[first.m_label]);
                                CUtility.ASSERT((m_group.size() - 1) == m_ingroup[next.m_label]);
                            }
                            
                            break;
                        }
                    }
                }
            }
        }
        
        System.out.println(m_group.size() + " states after removal of redundant states.");
        
        if (m_spec.m_verbose && CUtility.OLD_DUMP_DEBUG) {
            System.out.println();
            System.out.println("States grouped as follows after minimization");
            
            pgroups();
        }
        
        fix_dtrans();
    }
    
    /***************************************************************
     Function: init_groups
     Description:
     **************************************************************/
    private void init_groups() {
        int i;
        int j;
        int group_count;
        int size;
        CDTrans dtrans;
        Vector <CDTrans> dtrans_group;
        CDTrans first;
        boolean group_found;
        
        m_group = new Vector <Vector <CDTrans> > ();
        group_count = 0;
        
        size = m_spec.m_dtrans_vector.size();
        m_ingroup = new int[size];
        
        for (i = 0; i < size; ++i) {
            group_found = false;
            dtrans = m_spec.m_dtrans_vector.elementAt(i);
            
            if (CUtility.DEBUG) {
                CUtility.ASSERT(i == dtrans.m_label);
                CUtility.ASSERT(group_count == m_group.size());
            }
            
            for (j = 0; j < group_count; ++j) {
                dtrans_group = m_group.elementAt(j);
                
                if (CUtility.DEBUG) {
                    CUtility.ASSERT(0 < dtrans_group.size());
                }
                
                first = dtrans_group.elementAt(0);
                
                if (CUtility.SLOW_DEBUG) {
                    CDTrans check;
                    int k;
                    int s;
                    
                    s = dtrans_group.size();
                    CUtility.ASSERT(0 < s);
                    
                    for (k = 1; k < s; ++k) {
                        check = dtrans_group.elementAt(k);
                        CUtility.ASSERT(check.m_accept == first.m_accept);
                    }
                }
                
                if (first.m_accept == dtrans.m_accept) {
                    dtrans_group.addElement(dtrans);
                    m_ingroup[i] = j;
                    group_found = true;
                    
                    if (CUtility.DEBUG) {
                        CUtility.ASSERT(j == m_ingroup[dtrans.m_label]);
                    }
                    
                    break;
                }
            }
            
            if (!group_found) {
                dtrans_group = new Vector <CDTrans> ();
                dtrans_group.addElement(dtrans);
                
                m_ingroup[i] = m_group.size();
                m_group.addElement(dtrans_group);
                
                ++group_count;
            }
        }
        
        if (m_spec.m_verbose && CUtility.OLD_DUMP_DEBUG) {
            System.out.println("Initial grouping:");
            pgroups();
            System.out.println();
        }
    }
    
    /***************************************************************
     Function: pset
     **************************************************************/
    private void pset(Vector dtrans_group) {
        int i;
        int size;
        CDTrans dtrans;
        
        size = dtrans_group.size();
        for (i = 0; i < size; ++i) {
            dtrans = (CDTrans) dtrans_group.elementAt(i);
            System.out.print(dtrans.m_label + " ");
        }
    }
    
    /***************************************************************
     Function: pgroups
     **************************************************************/
    private void pgroups() {
        int i;
        int dtrans_size;
        int group_size;
        
        group_size = m_group.size();
        for (i = 0; i < group_size; ++i) {
            System.out.print("\tGroup " + i + " {");
            pset(m_group.elementAt(i));
            System.out.println("}");
            System.out.println();
        }
        
        System.out.println();
        dtrans_size = m_spec.m_dtrans_vector.size();
        for (i = 0; i < dtrans_size; ++i) {
            System.out.println("\tstate " + i + " is in group " + m_ingroup[i]);
        }
    }
}

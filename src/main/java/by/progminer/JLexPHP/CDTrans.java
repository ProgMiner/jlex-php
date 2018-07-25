package by.progminer.JLexPHP;


class CDTrans {
    
    static final int F = -1;
    
    int m_dtrans[];
    CAccept m_accept;
    int m_anchor;
    int m_label;
    
    CDTrans(int label, CSpec spec) {
        m_dtrans = new int[spec.m_dtrans_ncols];
        m_accept = null;
        m_anchor = CSpec.NONE;
        m_label = label;
    }
}

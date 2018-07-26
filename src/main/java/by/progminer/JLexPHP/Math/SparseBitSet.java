package by.progminer.JLexPHP.Math;

/*
 * SparseBitSet 25-Jul-1999.
 * C. Scott Ananian <cananian@alumni.princeton.edu>
 *
 * Re-implementation of the standard java.util.BitSet to support sparse
 * sets, which we need to efficiently support unicode character classes.
 */

import by.progminer.JLexPHP.Utility.Utility;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * A set of bits. The set automatically grows as more bits are
 * needed.
 *
 * @author C. Scott Ananian
 * @version 1.00, 25 Jul 1999
 */
public final class SparseBitSet implements Cloneable {
    
    /**
     * Binary operation
     */
    private interface BinOp {
        long op(long a, long b);
    }
    
    /**
     * Binary operations for binOp().
     */
    private static final BinOp AND = new BinOp() {
        public final long op(long a, long b) {
            return a & b;
        }
    };
    private static final BinOp OR = new BinOp() {
        public final long op(long a, long b) {
            return a | b;
        }
    };
    private static final BinOp XOR = new BinOp() {
        public final long op(long a, long b) {
            return a ^ b;
        }
    };
    
    /**
     * Sorted array of bit-block offsets.
     */
    public int offs[];
    
    /**
     * Array of bit-blocks; each holding BITS bits.
     */
    public long bits[];
    
    /**
     * Number of blocks currently in use.
     */
    public int size;
    
    /**
     * log base 2 of BITS, for the identity: x/BITS == x >> LG_BITS
     */
    private static final int LG_BITS = 6;
    
    /**
     * Number of bits in a block.
     */
    private static final int BITS = 1 << LG_BITS;
    
    /**
     * BITS-1, using the identity: x % BITS == x & (BITS-1)
     */
    private static final int BITS_M1 = BITS - 1;
    
    /**
     * Creates an empty set.
     */
    public SparseBitSet() {
        bits = new long[4];
        offs = new int[4];
        size = 0;
    }
    
    /**
     * Creates an empty set with the same size as the given set.
     */
    public SparseBitSet(SparseBitSet set) {
        bits = new long[set.size];
        offs = new int[set.size];
        size = 0;
    }
    
    private void newBlock(int idx, int bNum) {
        if (size == bits.length) { // resize
            long[] nBits = new long[size * 3];
            int[] nOffs = new int[size * 3];
            
            System.arraycopy(bits, 0, nBits, 0, size);
            System.arraycopy(offs, 0, nOffs, 0, size);
            
            bits = nBits;
            offs = nOffs;
        }
        
        Utility.ASSERT(size < bits.length);
        insertBlock(idx, bNum);
    }
    
    private void insertBlock(int idx, int bNum) {
        Utility.ASSERT(idx <= size);
        Utility.ASSERT(idx == size || offs[idx] != bNum);
        
        System.arraycopy(bits, idx, bits, idx + 1, size - idx);
        System.arraycopy(offs, idx, offs, idx + 1, size - idx);
        
        offs[idx] = bNum;
        bits[idx] = 0; //clear them bits
        
        size++;
    }
    
    private int bSearch(int bNum) {
        int l = 0, r = size; // search interval is [l, r)
        
        while (l < r) {
            int p = (l + r) / 2;
            
            if (bNum < offs[p]) {
                
                r = p;
            } else if (bNum > offs[p]) {
                
                l = p + 1;
            } else {
                
                return p;
            }
        }
        
        Utility.ASSERT(l == r);
        
        return l; // index at which the bNum *should* be, if it's not
    }
    
    /**
     * Sets a bit.
     *
     * @param bit the bit to be set
     */
    public void set(int bit) {
        int bNum = bit >> LG_BITS;
        int idx = bSearch(bNum);
        
        if (idx >= size || offs[idx] != bNum) {
            newBlock(idx, bNum);
        }
        
        bits[idx] |= (1L << (bit & BITS_M1));
    }
    
    /**
     * Clears a bit.
     *
     * @param bit the bit to be cleared
     */
    public void clear(int bit) {
        int bNum = bit >> LG_BITS;
        int idx = bSearch(bNum);
        
        if (idx >= size || offs[idx] != bNum) {
            newBlock(idx, bNum);
        }
        
        bits[idx] &= ~(1L << (bit & BITS_M1));
    }
    
    /**
     * Clears all bits.
     */
    public void clearAll() {
        size = 0;
    }
    
    /**
     * Gets a bit.
     *
     * @param bit the bit to be gotten
     */
    public boolean get(int bit) {
        int bNum = bit >> LG_BITS;
        int idx = bSearch(bNum);
        
        if (idx >= size || offs[idx] != bNum) {
            return false;
        }
        
        return 0 != (bits[idx] & (1L << (bit & BITS_M1)));
    }
    
    /**
     * Logically ANDs this bit set with the specified set of bits.
     *
     * @param set the bit set to be ANDed with
     */
    public void and(SparseBitSet set) {
        binOp(this, set, AND);
    }
    
    /**
     * Logically ORs this bit set with the specified set of bits.
     *
     * @param set the bit set to be ORed with
     */
    public void or(SparseBitSet set) {
        binOp(this, set, OR);
    }
    
    /**
     * Logically XORs this bit set with the specified set of bits.
     *
     * @param set the bit set to be XORed with
     */
    public void xor(SparseBitSet set) {
        binOp(this, set, XOR);
    }
    
    private static void binOp(SparseBitSet a, SparseBitSet b, BinOp op) {
        int nSize = a.size + b.size;
        long[] nBits;
        int[] nOffs;
        
        int aZero, aSize;
        
        // be very clever and avoid allocating more memory if we can
        if (a.bits.length < nSize) { // oh well, have to make working space
            nBits = new long[nSize];
            nOffs = new int[nSize];
            
            aZero = 0;
            aSize = a.size;
        } else { // reduce, reuse, recycle!
            nBits = a.bits;
            nOffs = a.offs;
            
            aZero = a.bits.length - a.size;
            aSize = a.bits.length;
            
            System.arraycopy(a.bits, 0, a.bits, aZero, a.size);
            System.arraycopy(a.offs, 0, a.offs, aZero, a.size);
        }
        
        // ok, crunch through and binOp those sets!
        nSize = 0;
        for (int i = aZero, j = 0; i < aSize || j < b.size; ) {
            long nb;
            int no;
            
            if (i < aSize && (j >= b.size || a.offs[i] < b.offs[j])) {
                nb = op.op(a.bits[i], 0);
                no = a.offs[i];
                
                i++;
            } else if (j < b.size && (i >= aSize || a.offs[i] > b.offs[j])) {
                nb = op.op(0, b.bits[j]);
                no = b.offs[j];
                
                j++;
            } else { // equal keys; merge
                nb = op.op(a.bits[i], b.bits[j]);
                no = a.offs[i];
                
                i++;
                j++;
            }
            
            if (nb != 0) {
                nBits[nSize] = nb;
                nOffs[nSize] = no;
                
                nSize++;
            }
        }
        
        a.bits = nBits;
        a.offs = nOffs;
        a.size = nSize;
    }
    
    /**
     * Gets the hashcode.
     */
    public int hashCode() {
        long h = 1234;
        
        for (int i = 0; i < size; i++) {
            h ^= bits[i] * offs[i];
        }
        
        return (int) ((h >> 32) ^ h);
    }
    
    /**
     * Calculates and returns the set's size.
     */
    public int size() {
        return (size == 0)? 0: ((1 + offs[size - 1]) << LG_BITS);
    }
    
    /**
     * Compares this object against the specified object.
     *
     * @param obj the object to commpare with
     * @return true if the objects are the same; false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof SparseBitSet) {
            return equals(this, (SparseBitSet) obj);
        }
        
        return false;
    }
    
    /**
     * Compares two SparseBitSets for equality.
     *
     * @return true if the objects are the same; false otherwise.
     */
    public static boolean equals(SparseBitSet a, SparseBitSet b) {
        for (int i = 0, j = 0; i < a.size || j < b.size; ) {
            if (i < a.size && (j >= b.size || a.offs[i] < b.offs[j])) {
                
                if (a.bits[i++] != 0) return false;
            } else if (i >= a.size || a.offs[i] > b.offs[j]) {
                
                if (b.bits[j++] != 0) return false;
            } else { // equal keys
                
                if (a.bits[i++] != b.bits[j++]) return false;
            }
        }
        
        return true;
    }
    
    /**
     * Clones the SparseBitSet.
     */
    public Object clone() {
        try {
            SparseBitSet set = (SparseBitSet) super.clone();
            set.bits = bits.clone();
            set.offs = offs.clone();
            
            return set;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
    
    /**
     * Return an <code>Enumeration</code> of <code>Integer</code>s
     * which represent set bit indices in this SparseBitSet.
     */
    public Enumeration <Integer> elements() {
        return new Enumeration <Integer> () {
            int idx = -1, bit = BITS;
            
            {
                advance();
            }
            
            public boolean hasMoreElements() {
                return (idx < size);
            }
            
            public Integer nextElement() {
                int r = bit + (offs[idx] << LG_BITS);
                advance();
                
                return r;
            }
            
            private void advance() {
                while (idx < size) {
                    while (++bit < BITS) {
                        if (0 != (bits[idx] & (1L << bit))) {
                            return;
                        }
                    }
                    
                    idx++;
                    bit = -1;
                }
            }
        };
    }
    
    /**
     * Converts the SparseBitSet to a String.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append('{');
        for (Enumeration e = elements(); e.hasMoreElements(); ) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(e.nextElement());
        }
        sb.append('}');
        
        return sb.toString();
    }
    
    /**
     * Check validity.
     */
    private boolean isValid() {
        if (bits.length != offs.length) {
            return false;
        }
        
        if (size > bits.length) {
            return false;
        }
        
        if (size != 0 && 0 <= offs[0]) {
            return false;
        }
        
        for (int i = 1; i < size; i++) {
            if (offs[i] < offs[i - 1]) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Self-test.
     */
    public static void main(String[] args) {
        final int ITER = 500;
        final int RANGE = 65536;
        
        SparseBitSet a = new SparseBitSet();
        Utility.ASSERT(!a.get(0) && !a.get(1));
        Utility.ASSERT(!a.get(123329));
        
        a.set(0);
        Utility.ASSERT(a.get(0) && !a.get(1));
        
        a.set(1);
        Utility.ASSERT(a.get(0) && a.get(1));
        
        a.clearAll();
        Utility.ASSERT(!a.get(0) && !a.get(1));
        
        Random r = new Random();
        Vector <Integer> v = new Vector <Integer> ();
        for (int n = 0; n < ITER; n++) {
            int rr = ((r.nextInt() >>> 1) % RANGE) << 1;
            
            a.set(rr);
            v.addElement(rr);
            
            // check that all the numbers are there.
            Utility.ASSERT(a.get(rr) && !a.get(rr + 1) && !a.get(rr - 1));
            for (int i = 0; i < v.size(); i++) {
                Utility.ASSERT(a.get(v.elementAt(i)));
            }
        }
        
        SparseBitSet b = (SparseBitSet) a.clone();
        Utility.ASSERT(a.equals(b) && b.equals(a));
        
        for (int n = 0; n < ITER / 2; n++) {
            int rr = (r.nextInt() >>> 1) % v.size();
            int m = v.elementAt(rr);
            
            b.clear(m);
            v.removeElementAt(rr);
            
            // check that numbers are removed properly.
            Utility.ASSERT(!b.get(m));
        }
        
        Utility.ASSERT(!a.equals(b));
        
        SparseBitSet c = (SparseBitSet) a.clone();
        SparseBitSet d = (SparseBitSet) a.clone();
        
        c.and(a);
        Utility.ASSERT(c.equals(a) && a.equals(c));
        
        c.xor(a);
        Utility.ASSERT(!c.equals(a) && c.size() == 0);
        
        d.or(b);
        Utility.ASSERT(d.equals(a) && !b.equals(d));
        
        d.and(b);
        Utility.ASSERT(!d.equals(a) && b.equals(d));
        
        d.xor(a);
        Utility.ASSERT(!d.equals(a) && !b.equals(d));
        
        c.or(d);
        c.or(b);
        Utility.ASSERT(c.equals(a) && a.equals(c));
        
        c = (SparseBitSet) d.clone();
        c.and(b);
        Utility.ASSERT(c.size() == 0);
        
        System.out.println("Success.");
    }
}

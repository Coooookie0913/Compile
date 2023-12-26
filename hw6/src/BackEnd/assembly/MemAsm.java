package BackEnd.assembly;

import BackEnd.Register;

public class MemAsm extends Assembly{
    public enum Op {
        LW,
        SW
    }
    
    private Op op;
    private Register rd;
    private Register base;
    private int offset;
    private String label;
    
    //lw rd offset(base)
    public MemAsm(Op op,Register rd,Register base,int offset) {
        this.op = op;
        this.rd = rd;
        this.base = base;
        this.offset = offset;
        this.label = null;
    }
    
    //lw rd label+offset
    public MemAsm(Op op,Register rd,String label,int offset) {
        this.op = op;
        this.rd = rd;
        this.base = null;
        this.offset = offset;
        this.label = label;
    }
    
    public String toString() {
        if (label == null) {
            return op.toString().toLowerCase() + " " + rd + " " + offset + "(" + base + ")";
        } else {
            return op.toString().toLowerCase() + " " + rd + " " + label + "+" + offset;
        }
    }
}

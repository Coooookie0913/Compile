package BackEnd.assembly;

import BackEnd.Register;

public class ShiftAsm extends Assembly {
    public enum Op {
        SLL,
        SRL
    }
    private Op op;
    private Register dst;
    private Register src;
    private int num;
    
    public ShiftAsm(Op op, Register dst, Register src, int num) {
        this.op = op;
        this.dst = dst;
        this.src = src;
        this.num = num;
    }
    
    public String toString() {
        if (op == Op.SLL) {
            return "sll " + dst + " " + src + " " + num;
        }
        else {
            return "srl " + dst + " " + src + " " + num;
        }
    }
}

package BackEnd.assembly;

import BackEnd.Register;

public class JumpAsm extends Assembly{
    public enum Op {
        J,
        JAL,
        JR
    }
    
    private Op op;
    private String target;
    private Register rd;
    
    //j jal
    public JumpAsm(Op op, String target) {
        this.op = op;
        this.target = target;
        this.rd = null;
    }
    
    public JumpAsm(Op op, Register rd) {
        this.op = op;
        this.target = null;
        this.rd = rd;
    }
    
    public String toString() {
        if (op == Op.JR) {
            return op.toString().toLowerCase() + " " + rd;
        } else {
            return op.toString().toLowerCase() + " " + target;
        }
    }
}

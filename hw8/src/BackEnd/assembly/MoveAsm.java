package BackEnd.assembly;

import BackEnd.Register;

public class MoveAsm extends Assembly{
    private Register dst;
    private Register src;
    
    public MoveAsm(Register dst,Register src) {
        this.dst = dst;
        this.src = src;
    }
    
    public Register getDst() {
        return dst;
    }
    
    public Register getSrc() {
        return src;
    }
    
    public String toString() {
        return "move " + dst + " " + src;
    }
}

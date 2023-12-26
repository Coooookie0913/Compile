package BackEnd.assembly;

import BackEnd.Register;

public class MoveAsm extends Assembly{
    private Register dst;
    private Register src;
    
    public MoveAsm(Register dst,Register src) {
        this.dst = dst;
        this.src = src;
    }
    
    public String toString() {
        return "move " + dst + " " + src;
    }
}

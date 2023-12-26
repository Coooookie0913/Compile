package BackEnd.assembly;

import BackEnd.Register;

public class LiAsm extends Assembly{
    private Register rd;
    private int number;
    
    public LiAsm(Register rd,int number) {
        this.rd = rd;
        this.number = number;
    }
    
    public int getImm() {
        return number;
    }
    
    public Register getRd() {
        return rd;
    }
    
    public String toString() {
        return "li " + rd + " " + number;
    }
}

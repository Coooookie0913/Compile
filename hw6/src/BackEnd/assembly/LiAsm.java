package BackEnd.assembly;

import BackEnd.Register;

public class LiAsm extends Assembly{
    private Register rd;
    private int number;
    
    public LiAsm(Register rd,int number) {
        this.rd = rd;
        this.number = number;
    }
    
    public String toString() {
        return "li " + rd + " " + number;
    }
}

package BackEnd.assembly;

import BackEnd.Register;

public class LaAsm extends Assembly{
    private Register target;
    private String label;
    
    public LaAsm(Register target,String label) {
        this.target = target;
        this.label = label;
    }
    
    public String toString() {
        return "la " + target + ", " + label;
    }
}

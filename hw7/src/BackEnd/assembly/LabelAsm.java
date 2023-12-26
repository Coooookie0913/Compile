package BackEnd.assembly;

public class LabelAsm extends Assembly{
    private String label;
    
    public LabelAsm(String label) {
        this.label = label;
    }
    
    public String toString() {
        return label + ":";
    }
}

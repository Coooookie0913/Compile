package BackEnd.assembly;

import javax.swing.text.SimpleAttributeSet;

public class Space extends GlobalAsm{
    private String name;
    private int size;
    
    public Space(String name,int size) {
        this.name = name;
        this.size = size;
    }
    
    public String toString() {
        return name + ": .space " + size;
    }
}

package BackEnd.assembly;

public class Word extends GlobalAsm{
    private String name;
    private int value;
    
    public Word(String name,int value) {
        this.name = name;
        this.value = value;
    }
    
    public String toString() {
        return name + ": .word " + value;
    }
}

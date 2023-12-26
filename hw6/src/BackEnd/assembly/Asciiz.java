package BackEnd.assembly;

public class Asciiz extends GlobalAsm{
    private String name;
    private String content;
    
    public Asciiz(String name, String content) {
        this.name = name;
        this.content = content;
    }
    
    public String toString() {
        return name + ": .asciiz \"" + content.replace("\n","\\n") + "\"";
    }
}

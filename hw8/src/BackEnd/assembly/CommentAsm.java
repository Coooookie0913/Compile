package BackEnd.assembly;

public class CommentAsm extends Assembly{
    private String comment;
    
    public CommentAsm(String comment) {
        this.comment = comment;
    }
    
    public String toString() {
        return comment;
    }
}

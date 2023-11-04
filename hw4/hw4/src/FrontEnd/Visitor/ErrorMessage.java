package FrontEnd.Visitor;

public class ErrorMessage implements Comparable<ErrorMessage>{
    private int line;
    private ErrorType type;
    
    public ErrorMessage(int line, ErrorType type) {
        this.line = line;
        this.type = type;
    }
    
    public int getLine() {
        return line;
    }
    
    public ErrorType getType() {
        return type;
    }
    
    @Override
    public int compareTo(ErrorMessage o) {
        return this.line - o.line;
    }
}

package FrontEnd;

public class Token {
    private TokenType type;
    private String content;
    private int line;
    
    public Token(TokenType type,String content,int line) {
        this.type = type;
        this.content = content;
        this.line = line;
    }
    
    public int getLine() {
        return line;
    }
    
    public TokenType getType() {
        return type;
    }
    
    public String getContent() {
        return content;
    }
}

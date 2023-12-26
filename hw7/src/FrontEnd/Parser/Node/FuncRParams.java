package FrontEnd.Parser.Node;

import java.util.ArrayList;

//FuncRParams → Exp { ',' Exp }
public class FuncRParams extends Node{
    public FuncRParams(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
}

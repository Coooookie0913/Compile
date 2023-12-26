package FrontEnd.Parser.Node;

import LLVM.Value;

import java.util.ArrayList;

public class Node {
    private int startLine;
    private int endLine;
    private SyntaxType SyntaxType;
    private ArrayList<Node> children;
    
    public Node(int startLine, int endLine, SyntaxType SyntaxType, ArrayList<Node> children) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.SyntaxType = SyntaxType;
        //this.children = children;
        if (children == null) {
            this.children = new ArrayList<>();
        } else {
            this.children = new ArrayList<>();
            for(Node child : children) {
                this.children.add(child);
            }
        }
    }
    
    
    
    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }
    
    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }
    
    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }
    
    public void setSyntaxType(SyntaxType syntaxType) {
        SyntaxType = syntaxType;
    }
    
    public SyntaxType getSyntaxType() {
        return this.SyntaxType;
    }
    
    public ArrayList<Node> getChildren() {
        return this.children;
    }
    
    public String getSynName() {
        return this.SyntaxType.getSyntaxTypeName();
    }
    
    public Value genIR() {
        //TokenNode
        if (children.size() == 0) {
            return null;
        } else {
            for (Node node : children) {
                node.genIR();
            }
        }
        return null;
    }
    
    public int calculate() {
        return 0;
    }
}

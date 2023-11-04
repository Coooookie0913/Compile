package FrontEnd.Visitor;

import java.util.ArrayList;

public class VarSymbol extends Symbol{
    private int dim;
    
    public VarSymbol(String name,SymbolType type,int dim) {
        this.name = name;
        this.type = type;
        this.dim = dim;
    }
    
    public int getDim() {
        return dim;
    }
}

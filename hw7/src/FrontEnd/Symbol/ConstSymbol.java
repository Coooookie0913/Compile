package FrontEnd.Symbol;

import java.util.ArrayList;

public class ConstSymbol extends Symbol{
    //有几个'[]'
    private int dim;
    //被压成一维以前的维数，0维变量的size是0
    private ArrayList<Integer> dims;
    
    public ConstSymbol(String name,SymbolType type,int dim) {
        this.name = name;
        this.type = type;
        this.dim = dim;
    }
    
    public int getDim() {
        return dim;
    }
    
    public void setDims(ArrayList<Integer> dims) {
        this.dims = dims;
    }
    
    public ArrayList<Integer> getDims() {
        return dims;
    }
}

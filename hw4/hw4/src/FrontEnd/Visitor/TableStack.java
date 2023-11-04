package FrontEnd.Visitor;

import java.util.Stack;

public class TableStack {
    private Stack<SymbolTable> stack;
    private int depth;
    private int cur;
    
    private FuncSymbol curFunc;
    
    public TableStack() {
        stack = new Stack<>();
        depth = -1;
        cur = -1;
    }
    
    public void enterBlock() {
        depth++;
        cur++;
        SymbolTable symbolTable = new SymbolTable();
        stack.push(symbolTable);
    }
    
    public void leaveBlock() {
        depth--;
        cur--;
        stack.pop();
    }
    
    public SymbolTable peek() {
        return stack.peek();
    }
    
    public SymbolTable get(int index) {
        return stack.get(index);
    }
    
    public int getDepth() {
        return depth;
    }
    
    public int getCur() {
        return cur;
    }
    
    public FuncSymbol getCurFunc() {
        return curFunc;
    }
    
    public void setCurFunc(FuncSymbol funcSymbol) {
        this.curFunc = funcSymbol;
    }
    
    public Symbol getSymbol(String name) {
        int i;
        boolean flag = false;
        Symbol symbol = null;
        for (i = depth; i >= 0; i--) {
            SymbolTable symbolTable = stack.get(i);
            if (symbolTable.check(name)) {
                symbol = symbolTable.getSymbol(name);
                break;
            }
        }
        return symbol;
    }
    
    public boolean checkSymbol(String name) {
        int i;
        for (i = depth; i >= 0; i--) {
            SymbolTable symbolTable = stack.get(i);
            if (symbolTable.check(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean checkFuncSymbol(String name) {
        int i;
        for (i = depth; i >= 0; i--) {
            SymbolTable symbolTable = stack.get(i);
            if (symbolTable.checkFuncSymbol(name)) {
                return true;
            }
        }
        return false;
    }
}

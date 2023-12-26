package LLVM;

import java.util.ArrayList;
import java.util.Objects;

public class Module extends Value{
    private ArrayList<String> declareList;
    private ArrayList<GlobalVar> globalVarList;
    private ArrayList<Function> functionList;
    
    public Module() {
        super(LLVMType.MODULE, "module");
        this.declareList = new ArrayList<>();
        this.globalVarList = new ArrayList<>();
        this.functionList = new ArrayList<>();
    }
    
    public void addGlobalVal(GlobalVar globalVar) {
        globalVarList.add(globalVar);
    }
    
    public void addFunction(Function function) {
        functionList.add(function);
    }
    
    public ArrayList<GlobalVar> getGlobalVarList() {
        return globalVarList;
    }
    
    public ArrayList<Function> getFunctionList() {
        return functionList;
    }
    
    public Function getFunctionByName(String funcName) {
        for (Function function : functionList) {
            if (Objects.equals(function.getName(), funcName)) {
                return function;
            }
        }
        return null;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("declare i32 @getint()\n");
        sb.append("declare void @putint(i32)\n");
        sb.append("declare void @putch(i32)\n");
        sb.append("declare void @putstr(i8*)\n\n");
        for (GlobalVar globalVar : globalVarList) {
            sb.append(globalVar.toString());
            sb.append("\n");
        }
        for (Function function : functionList) {
            sb.append(function.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}

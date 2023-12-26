package LLVM;

import FrontEnd.Symbol.Symbol;

import java.util.ArrayList;

//TODO a[3][3]在调用a[2]时会出问题
//函数的参数
public class Param extends Value{
    private Function parentFunction;
    private String targetType;
    private Symbol symbol;
    //int p[][3][2]
    //0表示没有
    private ArrayList<Integer> paramDims;
    private Boolean isPointer;
    private boolean isArray;
    public Param(LLVMType llvmType, String name, String targetType,ArrayList<Integer> paramDims) {
        super(llvmType, name);
        this.parentFunction = null;
        this.targetType = targetType;
        this.paramDims = paramDims;
        this.isPointer = false;
        if (paramDims.size() > 0) {
            isArray = false;
        } else {
            isArray = true;
        }
    }
    
    public void setParentFunction(Function parentFunction) {
        this.parentFunction = parentFunction;
    }
    
    public Function getParentFunction() {
        return parentFunction;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public String toString() {
        return targetType + " " + getName();
    }
    
    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }
    
    public Symbol getSymbol() {
        return symbol;
    }
    
    public Boolean getPointer() {
        return isPointer;
    }
    
    public ArrayList<Integer> getParamDims() {
        return paramDims;
    }
    
    public Boolean getIsArray() {
        return isArray;
    }
    
    public void setIsPointer(Boolean isPointer) {
        this.isPointer = isPointer;
    }
}

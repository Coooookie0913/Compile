package FrontEnd.Parser.Node;

import FrontEnd.Symbol.FuncSymbol;
import FrontEnd.Symbol.SymbolType;
import LLVM.*;

import java.util.ArrayList;

//MainFuncDef → 'int' 'main' '(' ')' Block
public class MainFuncDef extends Node{
    public MainFuncDef(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    @Override
    public Value genIR() {
        String name = IRBuilder.getInstance().genFuncName("main");
        //符号表
        FuncSymbol funcSymbol = new FuncSymbol(name, SymbolType.func, "integer",0,new ArrayList<>());
        IRBuilder.getInstance().getTableStack().enterBlock();
        IRBuilder.getInstance().setLocalVarCnt(0);
        IRBuilder.getInstance().getTableStack().setCurFunc(funcSymbol);
        //IR-function
        LLVMType llvmType = LLVMType.FUNCTION;
        LLVMType returnType = LLVMType.INT32;
        Function function = new Function(llvmType,name,returnType);
        IRBuilder.getInstance().addFunction(function);
        //IR-BasicBlock
        BasicBlock basicBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),function);
        IRBuilder.getInstance().addBasicBlock(basicBlock);
        IRBuilder.getInstance().setCurBasicBlock(basicBlock);
        //现在开始没有全局变量
        IRBuilder.getInstance().setGlobal(false);
        //对于其中指令再各自生成 ir
        super.genIR();
        //确定最后一条语句是 ret
        IRBuilder.getInstance().checkAndPadRet();
        //符号表
        IRBuilder.getInstance().getTableStack().leaveBlock();
        return null;
    }
}

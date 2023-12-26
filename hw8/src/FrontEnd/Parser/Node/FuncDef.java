package FrontEnd.Parser.Node;

import FrontEnd.Symbol.FuncSymbol;
import FrontEnd.Symbol.Symbol;
import FrontEnd.Symbol.SymbolType;
import LLVM.*;
import LLVM.Instruction.AllocaInstruction;
import LLVM.Instruction.StoreInstruction;

import java.util.ArrayList;
import java.util.Objects;

// FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
public class FuncDef extends Node{
    public FuncDef(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    @Override
    public Value genIR() {
        //不会有全局变量
        IRBuilder.getInstance().setGlobal(false);
        LLVMType retType;
        LLVMType llvmType = LLVMType.FUNCTION;
        String name = IRBuilder.getInstance().genFuncName(((TokenNode)getChildren().get(1)).getToken().getContent());
        if (Objects.equals(((TokenNode) getChildren().get(0).getChildren().get(0)).getToken().getContent(), "void")) {
            retType = LLVMType.VOID;
        } else {
            retType = LLVMType.INT32;
        }
        Boolean paramFlag = false;
        for (Node node : getChildren()) {
            if (node instanceof  FuncFParams) {
                paramFlag = true;
            }
        }
        Function function = new Function(llvmType,name,retType);
        IRBuilder.getInstance().addFunction(function);
        //建立新的符号表
        IRBuilder.getInstance().getTableStack().enterBlock();
        IRBuilder.getInstance().setLocalVarCnt(0);
        ArrayList<Param> paramArrayList = new ArrayList<>();
        //有形参
        if (paramFlag) {
            paramArrayList = ((FuncFParams)getChildren().get(3)).getParamList();
            for (Param param : paramArrayList) {
                function.addParam(param);
                //加入符号表
                Symbol symbol = param.getSymbol();
                IRBuilder.getInstance().getTableStack().peek().addSymbol(symbol);
            }
        }
        BasicBlock basicBlock = new BasicBlock(LLVMType.BASIC_BLOCK,
                IRBuilder.getInstance().genBasicBlockName(),function);
        IRBuilder.getInstance().addBasicBlock(basicBlock);
        IRBuilder.getInstance().setCurBasicBlock(basicBlock);
        //如果有形参，需要将形参都store
        if (paramFlag) {
            for (Param param : paramArrayList) {
                Symbol symbol = param.getSymbol();
                AllocaInstruction allocaInstr = new AllocaInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),param.getTargetType());
                IRBuilder.getInstance().addInstr(allocaInstr);
                StoreInstruction storeInstr = new StoreInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),param,allocaInstr,param.getTargetType());
                IRBuilder.getInstance().addInstr(storeInstr);
                //在函数内 有新的变量代表这个形参
                //LocalVar localVar = new LocalVar(LLVMType.LOCAL_VAL, allocaInstr.getName(),param.getIsArray(),new ArrayList<>(), param.getTargetType(),ne)
                symbol.setIr(allocaInstr);
            }
        }
        //加入符号表
        //这里的 name 是带前缀的
        //TODO 输入每个形参的维度 （貌似在目前的处理下，都是1）
        FuncSymbol funcSymbol = new FuncSymbol(name, SymbolType.func,retType.toString(), paramArrayList.size(), null);
        IRBuilder.getInstance().getTableStack().peek().addSymbol(funcSymbol);
        if (paramFlag) {
            getChildren().get(5).genIR();
        } else {
            getChildren().get(4).genIR();
        }
        IRBuilder.getInstance().checkAndPadRet();
        IRBuilder.getInstance().getTableStack().leaveBlock();
        return null;
    }
}

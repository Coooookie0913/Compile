package FrontEnd.Parser.Node;

import FrontEnd.Lexer.TokenType;
import FrontEnd.Symbol.ConstSymbol;
import FrontEnd.Symbol.SymbolType;
import LLVM.*;
import LLVM.Instruction.AllocaInstruction;
import LLVM.Instruction.StoreInstruction;

import java.util.ArrayList;

//ConstDef -> Ident {'[' ConstExp ']'} '=' ConstInitVal
public class ConstDef extends Node{
    public ConstDef(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public Value genIR() {
        String name;
        //如果是全局变量
        if (IRBuilder.getInstance().isGlobal()) {
            name = IRBuilder.getInstance().genGlobalVarName();
            LLVMType llvmType = LLVMType.GLOBAL_VAL;
            // 0 维变量
            //Ident = ConstInitVal;
            if (((TokenNode)getChildren().get(1)).getToken().getType() == TokenType.ASSIGN) {
                //LLVMType llvmType = LLVMType.GLOBAL_VAL;
                boolean isArray = false;
                String targetType = "i32";
                ArrayList<Value> constInitVal = ((ConstInitVal)getChildren().get(2)).getInitVal();
                GlobalVar globalVar = new GlobalVar(llvmType,name,isArray,constInitVal,targetType,new ArrayList<>());
                IRBuilder.getInstance().addGlobalVar(globalVar);
                //加入符号表
                String varName = ((TokenNode)getChildren().get(0)).getToken().getContent();
                ConstSymbol symbol = new ConstSymbol(varName, SymbolType.con, 0);
                symbol.setDims(new ArrayList<>());
                symbol.setIr(globalVar);
                IRBuilder.getInstance().getTableStack().peek().addSymbol(symbol);
            }
            //数组（压成一维）
            //ConstDef -> Ident {'[' ConstExp ']'} '=' ConstInitVal
            else {
                int dim = 1;
                int i;
                ArrayList<Integer> initDims = new ArrayList<>();
                for (i = 1; i < getChildren().size(); i++) {
                    if (getChildren().get(i) instanceof TokenNode) {
                        if (((TokenNode)getChildren().get(i)).getToken().getType() == TokenType.ASSIGN) {
                            break;
                        }
                    }
                    if (getChildren().get(i) instanceof ConstExp) {
                        int subDim = ((ConstExp)getChildren().get(i)).calculate();
                        initDims.add(subDim);
                        dim = dim*subDim;
                    }
                }
                i++;
                String targetType = "[" + String.valueOf(dim) + " x " + "i32]";
                Boolean isArray = true;
                ArrayList<Value> constInitVal = ((ConstInitVal)getChildren().get(i)).getInitVal();
                GlobalVar globalVar = new GlobalVar(llvmType,name,isArray,constInitVal,targetType,initDims);
                IRBuilder.getInstance().addGlobalVar(globalVar);
                //加入符号表
                String varName = ((TokenNode)getChildren().get(0)).getToken().getContent();
                ConstSymbol symbol = new ConstSymbol(varName, SymbolType.con, 1);
                symbol.setDims(initDims);
                symbol.setIr(globalVar);
                IRBuilder.getInstance().getTableStack().peek().addSymbol(symbol);
            }
        }
        //局部常量
        //先 alloc 然后 Store
        else {
            name = IRBuilder.getInstance().genLocalVarName();
            LLVMType llvmType = LLVMType.LOCAL_VAL;
            //0 维变量
            if (((TokenNode)getChildren().get(1)).getToken().getType() == TokenType.ASSIGN) {
                //Ident = ConstInitVal;
                Boolean isArray = false;
                String targetType = "i32";
                ArrayList<Value> constInitVal = ((ConstInitVal)getChildren().get(2)).getInitVal();
                
                BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
                //alloca
                AllocaInstruction allocaInstr = new AllocaInstruction(LLVMType.INSTR,name,basicBlock,"i32");
                IRBuilder.getInstance().addInstr(allocaInstr);
                //store
                Init initVal = new Init(LLVMType.INITVAL,null,constInitVal,false);
                StoreInstruction storeInstr = new StoreInstruction(LLVMType.INSTR,"a store instr",basicBlock,initVal,allocaInstr,"i32");
                IRBuilder.getInstance().addInstr(storeInstr);
                //加入符号表
                String varName = ((TokenNode)getChildren().get(0)).getToken().getContent();
                ConstSymbol constSymbol = new ConstSymbol(varName,SymbolType.var,0);
                constSymbol.setDims(new ArrayList<>());
                LocalVar localVar = new LocalVar(llvmType, allocaInstr.getName(),isArray,constInitVal,targetType,new ArrayList<>(),allocaInstr);
                constSymbol.setIr(localVar);
                IRBuilder.getInstance().getTableStack().peek().addSymbol(constSymbol);
            }
            //ConstDef -> Ident {'[' ConstExp ']'} '=' ConstInitVal
            else {
                int dim = 1;
                int i;
                ArrayList<Integer> initDims = new ArrayList<>();
                for (i = 1; i < getChildren().size(); i++) {
                    if (getChildren().get(i) instanceof TokenNode) {
                        if (((TokenNode)getChildren().get(i)).getToken().getType() == TokenType.ASSIGN) {
                            break;
                        }
                    }
                    if (getChildren().get(i) instanceof ConstExp) {
                        int subDim = ((ConstExp)getChildren().get(i)).calculate();
                        dim = dim*subDim;
                        initDims.add(subDim);
                    }
                }
                i++;
                String targetType = "[" + String.valueOf(dim) + " x " + "i32]";
                Boolean isArray = true;
                ArrayList<Value> constInitVal = ((ConstInitVal)getChildren().get(i)).getInitVal();
                BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
                //alloca
                AllocaInstruction allocaInstruction = new AllocaInstruction(LLVMType.INSTR,name,basicBlock,targetType);
                IRBuilder.getInstance().addInstr(allocaInstruction);
                //store
                Init initVal = new Init(LLVMType.INITVAL,null,constInitVal,true);
                StoreInstruction storeInstruction = new StoreInstruction(LLVMType.INSTR,"a store instr",basicBlock,initVal,allocaInstruction,targetType);
                IRBuilder.getInstance().addInstr(storeInstruction);
                //加入符号表
                String varName = ((TokenNode)getChildren().get(0)).getToken().getContent();
                ConstSymbol constSymbol = new ConstSymbol(varName,SymbolType.var,1);
                constSymbol.setDims(initDims);
                LocalVar localVar = new LocalVar(LLVMType.LOCAL_VAL, allocaInstruction.getName(),isArray,constInitVal,targetType,initDims,allocaInstruction);
                constSymbol.setIr(localVar);
                IRBuilder.getInstance().getTableStack().peek().addSymbol(constSymbol);
            }
        }
        return null;
    }
}

package FrontEnd.Parser.Node;

import FrontEnd.Lexer.TokenType;
import FrontEnd.Symbol.SymbolType;
import FrontEnd.Symbol.VarSymbol;
import LLVM.*;
import LLVM.Instruction.AllocaInstruction;
import LLVM.Instruction.GetElementPtrInstruction;
import LLVM.Instruction.StoreInstruction;

import java.util.ArrayList;

//VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
public class VarDef extends Node{
    public VarDef(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public Value genIR() {
        String name;
        String varName = ((TokenNode)getChildren().get(0)).getToken().getContent();
        Boolean initFlag = false;
        for (Node node : getChildren()) {
            if (node instanceof InitVal) {
                initFlag = true;
            }
        }
        //全局变量
        if (IRBuilder.getInstance().isGlobal()) {
            name = IRBuilder.getInstance().genGlobalVarName();
            LLVMType llvmType = LLVMType.GLOBAL_VAL;
            //有初始值 （全局变量的initVal一定是constant，也可能是前面的全局变量）
            if (initFlag) {
                // 0 维变量
                //Ident = InitVal;(const)
                if (((TokenNode)getChildren().get(1)).getToken().getType() == TokenType.ASSIGN) {
                    boolean isArray = false;
                    String targetType = "i32";
                    ArrayList<Value> constInitVal = ((InitVal)getChildren().get(2)).getConstInitVal();
                    GlobalVar globalVar = new GlobalVar(llvmType,name,isArray,constInitVal,targetType,new ArrayList<>());
                    IRBuilder.getInstance().addGlobalVar(globalVar);
                    //加入符号表
                    VarSymbol symbol = new VarSymbol(varName, SymbolType.var, 0);
                    symbol.setIr(globalVar);
                    symbol.setDims(new ArrayList<>());
                    IRBuilder.getInstance().getTableStack().peek().addSymbol(symbol);
                }
                //数组（一维）
                //VarDef -> Ident {'[' ConstExp ']'} '=' InitVal
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
                    ArrayList<Value> constInitVal = ((InitVal)getChildren().get(i)).getConstInitVal();
                    GlobalVar globalVar = new GlobalVar(llvmType,name,isArray,constInitVal,targetType,initDims);
                    IRBuilder.getInstance().addGlobalVar(globalVar);
                    //加入符号表
                    VarSymbol symbol = new VarSymbol(varName, SymbolType.con, 1);
                    symbol.setIr(globalVar);
                    symbol.setDims(initDims);
                    IRBuilder.getInstance().getTableStack().peek().addSymbol(symbol);
                }
            }
            //无初始值
            if (!initFlag) {
                //0维
                //Ident
                if (getChildren().size() == 1) {
                    Boolean isArray = false;
                    String targetType = "i32";
                    ArrayList<Value> valueArrayList = new ArrayList<>();
                    GlobalVar globalVar = new GlobalVar(llvmType,name,isArray,valueArrayList,targetType,new ArrayList<>());
                    IRBuilder.getInstance().addGlobalVar(globalVar);
                    //加入符号表
                    VarSymbol varSymbol = new VarSymbol(varName,SymbolType.var,0);
                    varSymbol.setIr(globalVar);
                    varSymbol.setDims(new ArrayList<>());
                    IRBuilder.getInstance().getTableStack().peek().addSymbol(varSymbol);
                }
                //数组
                else {
                    Boolean isArray = true;
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
                    ArrayList<Value> valueArrayList = new ArrayList<>();
                    GlobalVar globalVar = new GlobalVar(llvmType,name,isArray,valueArrayList,targetType,initDims);
                    IRBuilder.getInstance().addGlobalVar(globalVar);
                    //加入符号表
                    VarSymbol varSymbol = new VarSymbol(varName,SymbolType.var,1);
                    varSymbol.setIr(globalVar);
                    varSymbol.setDims(initDims);
                    IRBuilder.getInstance().getTableStack().peek().addSymbol(varSymbol);
                }
            }
        }
        //局部变量
        else {
            name =  IRBuilder.getInstance().genLocalVarName();
            LLVMType llvmType = LLVMType.LOCAL_VAL;
            //有初始值
            if (initFlag) {
                //0 维变量
                if (((TokenNode)getChildren().get(1)).getToken().getType() == TokenType.ASSIGN) {
                    //Ident = InitVal;
                    Boolean isArray = false;
                    String targetType = "i32";
                    ArrayList<Value> InitVal = ((InitVal)getChildren().get(2)).getValueOfVar();
                    LocalVar localVar = new LocalVar(llvmType,name,isArray,InitVal,targetType,new ArrayList<>());
                    BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
                    //alloca
                    AllocaInstruction allocaInstr = new AllocaInstruction(LLVMType.INSTR,name,basicBlock,"i32");
                    IRBuilder.getInstance().addInstr(allocaInstr);
                    //store
                    Init initVal = new Init(LLVMType.INITVAL,null,InitVal,false);
                    StoreInstruction storeInstr = new StoreInstruction(LLVMType.INSTR,"a store instr",basicBlock,initVal,allocaInstr,"i32");
                    IRBuilder.getInstance().addInstr(storeInstr);
                    //加入符号表
                    VarSymbol varSymbol = new VarSymbol(varName,SymbolType.var,0);
                    varSymbol.setIr(localVar);
                    varSymbol.setDims(new ArrayList<>());
                    IRBuilder.getInstance().getTableStack().peek().addSymbol(varSymbol);
                }
                //Ident { '[' ConstExp ']' } '=' InitVal
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
                    ArrayList<Value> constInitVal = ((InitVal)getChildren().get(i)).getConstInitVal();
                    BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
                    LocalVar localVar = new LocalVar(LLVMType.LOCAL_VAL,name,isArray,constInitVal,targetType,initDims);
                    //alloca
                    AllocaInstruction allocaInstruction = new AllocaInstruction(LLVMType.INSTR,name,basicBlock,targetType);
                    IRBuilder.getInstance().addInstr(allocaInstruction);
                    //store
                    //TODO
                    Init initVal = new Init(LLVMType.INITVAL,null,constInitVal,true);
                    //StoreInstruction storeInstruction = new StoreInstruction(LLVMType.INSTR,"a store instr",basicBlock,initVal,allocaInstruction,targetType);
                    //IRBuilder.getInstance().addInstr(storeInstruction);
                    int j;
                    for (j = 0; j < dim; j++) {
                        GetElementPtrInstruction getElementPtrInstr = new GetElementPtrInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),basicBlock,allocaInstruction,targetType,new Constant(LLVMType.INSTR,Integer.toString(j)));
                        IRBuilder.getInstance().addInstr(getElementPtrInstr);
                        StoreInstruction storeInstr = new StoreInstruction(LLVMType.INSTR,null,basicBlock,constInitVal.get(j),getElementPtrInstr,"i32");
                        IRBuilder.getInstance().addInstr(storeInstr);
                    }
                    //加入符号表
                    VarSymbol varSymbol = new VarSymbol(varName,SymbolType.var,1);
                    varSymbol.setIr(localVar);
                    varSymbol.setDims(initDims);
                    IRBuilder.getInstance().getTableStack().peek().addSymbol(varSymbol);
                }
            }
            //无初始值
            else {
                //0维
                //Ident
                BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
                if (getChildren().size() == 1) {
                    Boolean isArray = false;
                    String targetType = "i32";
                    ArrayList<Value> valueArrayList = new ArrayList<>();
                    LocalVar localVar = new LocalVar(llvmType,name,isArray,valueArrayList,targetType,new ArrayList<>());
                    //alloca
                    AllocaInstruction allocaInstruction = new AllocaInstruction(LLVMType.INSTR,name,basicBlock,"i32");
                    IRBuilder.getInstance().addInstr(allocaInstruction);
                    //加入符号表
                    VarSymbol varSymbol = new VarSymbol(varName,SymbolType.var,0);
                    varSymbol.setIr(localVar);
                    varSymbol.setDims(new ArrayList<>());
                    IRBuilder.getInstance().getTableStack().peek().addSymbol(varSymbol);
                }
                //VarDef → Ident { '[' ConstExp ']' }
                else {
                    int dim = 1;
                    int i;
                    ArrayList<Integer> initDims = new ArrayList<>();
                    for (i = 1; i < getChildren().size(); i++) {
                        if (getChildren().get(i) instanceof ConstExp) {
                            int subDim = ((ConstExp)getChildren().get(i)).calculate();
                            dim = dim*subDim;
                            initDims.add(subDim);
                        }
                    }
                    String targetType = "[" + String.valueOf(dim) + " x " + "i32]";
                    Boolean isArray = true;
                    //alloca
                    AllocaInstruction allocaInstruction = new AllocaInstruction(LLVMType.INSTR,name,basicBlock,targetType);
                    IRBuilder.getInstance().addInstr(allocaInstruction);
                    ArrayList<Value> valueArrayList = new ArrayList<>();
                    LocalVar localVar = new LocalVar(llvmType,name,isArray,valueArrayList,targetType,initDims);
                    //加入符号表
                    VarSymbol varSymbol = new VarSymbol(varName,SymbolType.var,1);
                    varSymbol.setIr(localVar);
                    varSymbol.setDims(initDims);
                    IRBuilder.getInstance().getTableStack().peek().addSymbol(varSymbol);
                }
            }
        }
        return null;
    }
}

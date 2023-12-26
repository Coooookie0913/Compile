package FrontEnd.Parser.Node;

import FrontEnd.Lexer.TokenType;
import FrontEnd.Symbol.FuncSymbol;
import LLVM.*;
import LLVM.Instruction.*;

import java.util.ArrayList;

//UnaryExp -> PrimaryExp | Ident '('[FuncRParams]')' | UnaryOp UnaryExp
//UnaryOp → '+' | '−' | '!'
//PrimaryExp → '(' Exp ')' | LVal | Number
//LVal  Ident '('[FuncRParams]')' ！ 不会出现在 constExp
public class UnaryExp extends Node{
    public UnaryExp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //只有 constExp 才会调用 calculate 方法
    public int calculate() {
        //UnaryOp UnaryExp
        Node firstNode = getChildren().get(0);
        int ans = 0;
        if (firstNode instanceof UnaryOp) {
            UnaryExp unaryExp = ((UnaryExp)getChildren().get(1));
            if (((TokenNode)firstNode.getChildren().get(0)).getToken().getType() == TokenType.PLUS) {
                ans += unaryExp.calculate();
            }
            else if (((TokenNode)firstNode.getChildren().get(0)).getToken().getType() == TokenType.MINU) {
                ans -= unaryExp.calculate();
            }
            else {
                ans = unaryExp.calculate() == 0 ? 1 : 0;
            }
        }
        //PrimaryExp
        else {
            ans = firstNode.calculate();
        }
        return ans;
    }
    
    @Override
    public Value genIR() {
        if (getChildren().get(0) instanceof PrimaryExp) {
            return ((PrimaryExp)getChildren().get(0)).genIR();
        } else if (getChildren().get(0) instanceof UnaryOp) {
            TokenType op = ((TokenNode)getChildren().get(0).getChildren().get(0)).getToken().getType();
            //加法
            if (op == TokenType.PLUS) {
                return getChildren().get(1).genIR();
            }
            //减法
            else if (op == TokenType.MINU) {
                Value operand1 = new Value(LLVMType.INT32,"0");
                Value operand2 = getChildren().get(1).genIR();
                String name = IRBuilder.getInstance().genLocalVarName();
                BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
                Instr instr = new SubInstruction(LLVMType.INSTR,name,basicBlock,operand1,operand2);
                IRBuilder.getInstance().addInstr(instr);
                return instr;
            }
            //非运算
            else {
                //TODO 非运算
                //%3 = load i32, i32* %2, align 4
                //%4 = icmp ne i32 %3, 0
                //zext 返回i32
                //load
                Value operand = getChildren().get(1).genIR();
                String loadName = IRBuilder.getInstance().genLocalVarName();
                BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
                //LoadInstruction loadInstr = new LoadInstruction(LLVMType.INSTR,loadName,basicBlock,operand,"i32");
                //IRBuilder.getInstance().addInstr(loadInstr);
                //icmp
                String icmpName = IRBuilder.getInstance().genLocalVarName();
                Value operand2 = new Value(LLVMType.INT32,"0");
                IcmpInstruction icmpInstr = new IcmpInstruction(LLVMType.INSTR,icmpName,basicBlock,operand,operand2, IcmpOp.EQ);
                IRBuilder.getInstance().addInstr(icmpInstr);
                ZextInstruction zextInstr = new ZextInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),basicBlock,icmpInstr,LLVMType.INT1,LLVMType.INT32);
                IRBuilder.getInstance().addInstr(zextInstr);
                return zextInstr;
            }
        }
        //Ident '('[FuncRParams]')'
        else {
            String funcName = "@f_" + ((TokenNode)getChildren().get(0)).getToken().getContent();
            FuncSymbol funcSymbol = (FuncSymbol) IRBuilder.getInstance().getTableStack().getSymbol(funcName);
            Function function = IRBuilder.getInstance().getFuncByName(funcName);
            //gen call IR
            LLVMType llvmType = LLVMType.INSTR;
            String name = IRBuilder.getInstance().genLocalVarName();
            BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
            ArrayList<Value> paramArrayList = new ArrayList<>();
            CallInstruction callInstruction;
            //无参数
            if (getChildren().size() == 3) {
                callInstruction = new CallInstruction(llvmType,name,basicBlock,function,paramArrayList);
                IRBuilder.getInstance().addInstr(callInstruction);
                return callInstruction;
            }
            //有参数
            else {
                //FuncRParams → Exp { ',' Exp }
                ArrayList<Node> paramNodes = getChildren().get(2).getChildren();
                for (Node node : paramNodes) {
                    if (node instanceof Exp) {
                        paramArrayList.add(node.genIR());
                    }
                }
                callInstruction = new CallInstruction(llvmType,name,basicBlock,function,paramArrayList);
                IRBuilder.getInstance().addInstr(callInstruction);
            }
            return callInstruction;
        }
    }
}

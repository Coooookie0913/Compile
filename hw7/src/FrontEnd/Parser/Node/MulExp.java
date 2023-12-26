package FrontEnd.Parser.Node;

import FrontEnd.Lexer.TokenType;
import LLVM.*;
import LLVM.Instruction.MulInstruction;
import LLVM.Instruction.SdivInstruction;
import LLVM.Instruction.SremInstr;

import java.util.ArrayList;

//MulExp -> UnaryExp {'*' UnaryExp | '/' UnaryExp | '%' UnaryExp}
public class MulExp extends Node{
    public MulExp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public int calculate() {
        int ans = getChildren().get(0).calculate();
        int i;
        int op = 0; // 1:*   2:/   3:%
        for (i = 1; i < getChildren().size(); i++) {
            if (getChildren().get(i) instanceof TokenNode) {
                if (((TokenNode)getChildren().get(i)).getToken().getType() == TokenType.MULT) {
                    op = 1;
                } else if (((TokenNode)getChildren().get(i)).getToken().getType() == TokenType.DIV) {
                    op = 2;
                } else {
                    op = 3;
                }
            }
            if (getChildren().get(i) instanceof UnaryExp) {
                if (op == 1) {
                    ans *= getChildren().get(i).calculate();
                } else if (op == 2) {
                    ans /= getChildren().get(i).calculate();
                } else {
                    ans %= getChildren().get(i).calculate();
                }
            }
        }
        return ans;
    }
    
    @Override
    public Value genIR() {
        Value operand1 = ((UnaryExp)getChildren().get(0)).genIR();
        Value operand2 = null;
        TokenType op = null;
        LLVMType llvmType = LLVMType.INSTR;
        int i;
        for (i = 1; i < getChildren().size(); i++) {
            if (getChildren().get(i) instanceof TokenNode) {
                op = ((TokenNode)getChildren().get(i)).getToken().getType();
            }
            if (getChildren().get(i) instanceof UnaryExp) {
                String name = IRBuilder.getInstance().genLocalVarName();
                BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
                Instr instr;
                operand2 = ((UnaryExp)getChildren().get(i)).genIR();
                //生成指令
                if (op == TokenType.MULT) {
                    instr = new MulInstruction(llvmType,name,basicBlock,operand1,operand2);
                } else if (op == TokenType.DIV) {
                    instr = new SdivInstruction(llvmType,name,basicBlock,operand1,operand2);
                } else {
                    //取模
                    instr = new SremInstr(llvmType,name,basicBlock,operand1,operand2);
                }
                //加入IRBuilder
                IRBuilder.getInstance().addInstr(instr);
                //更新 operand1
                operand1 = instr;
            }
        }
        return operand1;
    }
}

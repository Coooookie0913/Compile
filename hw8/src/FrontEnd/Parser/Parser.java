package FrontEnd.Parser;

import FrontEnd.Lexer.Token;
import FrontEnd.Lexer.TokenStream;
import FrontEnd.Lexer.TokenType;
import FrontEnd.Parser.Node.*;
import FrontEnd.Parser.Node.Number;
import FrontEnd.Symbol.*;
import tools.Printer;

import java.util.ArrayList;
import java.util.Objects;

public class Parser {
    private TokenStream tokenStream;
    private Token curToken;
    private Printer printer;
    private TableStack tableStack;
    //最近定义/调用的一个函数的参数数量与参数维数
    int paramNum;
    ArrayList<Integer> paramDims;
    //错误g的检查开关 是否是函数的block
    Boolean funcReturnCheck;
    //针对printf语句
    int formatNum;
    int ExpNum;
    //当前是否在解析for块
    //Boolean forFlag;
    int forDepth = 0;
    
    public Parser(TokenStream tokenStream,Printer printer,TableStack tableStack) {
        this.tokenStream = tokenStream;
        this.printer = printer;
        this.tableStack = tableStack;
        paramNum = 0;
        paramDims = new ArrayList<>();
        funcReturnCheck = false;
        formatNum = 0;
        ExpNum = 0;
        forDepth = 0;
        peek();
    }
    
    //tool func
    //向后读一位，并返回当前token
    private void read() {
        tokenStream.read();
        curToken = tokenStream.peek();
    }
    
    //返回当前token
    private void peek() {
        curToken = tokenStream.peek();
    }
    
    //check and read ';'
    private void checkAndReadSemicn(ArrayList<Node> children) {
        if (curToken.getType() == TokenType.SEMICN) {
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(), SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        } else {
            ErrorMessage errorMessage = new ErrorMessage(tokenStream.watch(-1).getLine(), ErrorType.i);
            printer.addErrorMessage(errorMessage);
        }
    }
    
    //check and read ')'
    private void checkAndReadRparent(ArrayList<Node> children) {
        if (curToken.getType() == TokenType.RPARENT) {
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        } else {
            ErrorMessage errorMessage = new ErrorMessage(tokenStream.watch(-1).getLine(), ErrorType.j);
            printer.addErrorMessage(errorMessage);
        }
    }
    
    //check and read ']'
    private void checkAndReadRbrack(ArrayList<Node> children) {
        if (curToken.getType() == TokenType.RBRACK) {
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        } else {
            ErrorMessage errorMessage = new ErrorMessage(tokenStream.watch(-1).getLine(), ErrorType.k);
            printer.addErrorMessage(errorMessage);
        }
    }
    
    private void checkFuncRParam(Node node) {
        //Exp -> AddExp -> MulExp -> UnaryExp -> PrimaryExp -> LVal -> Ident{'['Exp']'}
        //UnaryExp -> Ident '(' [FuncRParams] ')'
        //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        //PrimaryExp → '(' Exp ')' | LVal | Number
        int flag1 = 1;//1 LVal
        int flag2 = 1;//1 Ident()
        int i;
        Node tempNode1 = node;
        Node tempNode2 = node;
        //如果是第二种情况一定会在i = 3 时break
        for (i = 0; i < 5; i++) {
            if (tempNode1.getChildren().size() != 1) {
                flag1 = 0;
                break;
            }
            tempNode1 = tempNode1.getChildren().get(0);
            //primaryExp -> number
            if (i == 4 && tempNode1.getSyntaxType() == SyntaxType.NUMBER) {
                flag1 = 0;
                break;
            }
        }
        for (i = 0; i < 3;i++) {
            if (tempNode2.getChildren().size() != 1) {
                flag2 = 0;
                break;
            }
            tempNode2 = tempNode2.getChildren().get(0);
        }
        if(tempNode2.getChildren().size() > 1 && tempNode2.getChildren().get(1) instanceof TokenNode && flag2 == 1) {
            if (((TokenNode)tempNode2.getChildren().get(1)).getToken().getType() == TokenType.LPARENT
                    && ((TokenNode)tempNode2.getChildren().get(0)).getToken().getType() == TokenType.IDENFR) {
                flag2 = 1;
            } else {
                flag2 = 0;
            }
        } else {
            flag2 = 0;
        }
        
        //tempNode is LVal && c 类错误会在parseLVal中判定(没有c类错误才开始check e)
        if (flag1 == 1) {
            String name = ((TokenNode)tempNode1.getChildren().get(0)).getToken().getContent();
            //没有c类错误
            if (tableStack.checkSymbol(name)) {
                Symbol tableSymbol = tableStack.getSymbol(name);
                int tableDim;
                if (tableSymbol instanceof VarSymbol) {
                    tableDim = ((VarSymbol)tableSymbol).getDim();
                } else {
                    tableDim = ((ConstSymbol) tableSymbol).getDim();
                }
                //数LVal中有几个'['']'
                int brackNum = 0;
                for (i = 0; i < tempNode1.getChildren().size(); i++) {
                    if (tempNode1.getChildren().get(i) instanceof TokenNode) {
                        if (Objects.equals(((TokenNode) tempNode1.getChildren().get(i)).getToken().getContent(), "[")) {
                            brackNum++;
                        }
                    }
                }
                int curDim = tableDim - brackNum;
                paramDims.add(curDim);
            }
        }
        //tempNode is UnaryExp
        else if (flag2 == 1) {
            String name = ((TokenNode)tempNode2.getChildren().get(0)).getToken().getContent();
            //没有c类错误
            if (tableStack.checkSymbol(name)) {
                Symbol tableSymbol = tableStack.getSymbol(name);
                int curDim;
                String returnType = ((FuncSymbol)tableSymbol).getReturnType();
                if (Objects.equals(returnType, "void")) {
                    curDim = Integer.MIN_VALUE;
                } else {
                    curDim = 0;
                }
                paramDims.add(curDim);
            }
        }
        else {
            paramDims.add(0);
        }
    }
    
    //默认读取到当前node的第一个token
    //调用parse得到的node已经读完
    //token生成的node需要read 手动读完当前token
    //endLine 不能用 curToken 否则会出现 null pointer
    public Node parseCompUnit() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        tableStack.enterBlock();
        while(curToken != null) {
            Node node = null;
            //保证顺序（此处适用于分析顺序正确的程序）
            //MainFuncDef
            if (tokenStream.watch(1).getType() == TokenType.MAINTK) {
                node = parseMainFuncDef();
            }
            //FuncDef
            //FuncType ident '('
            else if (tokenStream.watch(2).getType() == TokenType.LPARENT) {
                node = parseFuncDef();
            }
            //ConstDecl
            //const int ident
            else if (curToken.getType() == TokenType.CONSTTK) {
                node = parseConstDecl();
            }
            //varDecl
            //int ident
            else if (curToken.getType() == TokenType.INTTK) {
                node = parseVarDecl();
            }
            else break;
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        tableStack.leaveBlock();
        return new CompUnit(startLine,endLine,SyntaxType.COMP_UNIT,children);
    }
    
    //const int <ConstDef> {,<ConstDef>};
    //curToken = 'const'
    public Node parseConstDecl() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        int i;
        //'const' 'int'
        for (i = 0; i < 2; i++) {
            TokenNode tokenNode = new TokenNode(startLine,startLine,SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        }
        //curToken -> ConstDecl
        Node node = parseConstDef();
        children.add(node);
        //curToken = ',' or ';'
        while (curToken.getType() == TokenType.COMMA) {
            //parse ','
            TokenNode commaNode = new TokenNode(curToken.getLine(), curToken.getLine(), SyntaxType.TOKEN,null,curToken);
            children.add(commaNode);
            read();
            //parse constDef
            node = parseConstDef();
            children.add(node);
        }
        //parse semicn
        checkAndReadSemicn(children);
        int endLine = tokenStream.watch(-1).getLine();
        return new ConstDecl(startLine,endLine,SyntaxType.CONST_DECL,children);
    }
    
    //int varDef {,varDef};
    public Node parseVarDecl() {
        int starLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse int
        TokenNode intNode = new TokenNode(starLine,starLine,SyntaxType.TOKEN,null,curToken);
        children.add(intNode);
        read();
        //parse varDef
        Node node = parseVarDef();
        children.add(node);
        //curToken = ',' or ';'
        while(curToken.getType() == TokenType.COMMA) {
            //parse comma
            TokenNode commaNode = new TokenNode(curToken.getLine(), curToken.getLine(), SyntaxType.TOKEN,null,curToken);
            children.add(commaNode);
            read();
            //parse varDef
            node = parseVarDef();
            children.add(node);
        }
        //parse semicn
        checkAndReadSemicn(children);
        int endLine = tokenStream.watch(-1).getLine();
        return new VarDecl(starLine,endLine,SyntaxType.VAR_DECL,children);
    }
    
    //ConstDef -> Ident {'[' ConstExp ']'} '=' ConstInitVal
    public Node parseConstDef() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        int dim = 0;
        //parse ident
        TokenNode identNode = new TokenNode(startLine,startLine,SyntaxType.TOKEN,null,curToken);
        String name = curToken.getContent();
        children.add(identNode);
        read();
        //curToken = '['
        //parse '[' ConstExp ']'
        while (curToken.getType() == TokenType.LBRACK) {
            dim++;
            //parse '['
            TokenNode lbrackNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lbrackNode);
            read();
            //parse ConstExp
            Node node = parseConstExp();
            children.add(node);
            //parse ']'
            checkAndReadRbrack(children);
        }
        //parse '='
        TokenNode assignNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(assignNode);
        read();
        //parse ConstInitVal
        Node node = parseConstInitVal();
        children.add(node);
        //check error
        ConstSymbol symbol = new ConstSymbol(name,SymbolType.con,dim);
        SymbolTable curTable = tableStack.peek();
        if (curTable.check(name)) {
            ErrorMessage errorMessage = new ErrorMessage(startLine,ErrorType.b);
            printer.addErrorMessage(errorMessage);
        } else {
            curTable.addSymbol(symbol);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new ConstDef(startLine,endLine,SyntaxType.CONST_DEF,children);
    }
    
    //VarDef -> Ident{'['ConstExp']'} | Ident{'['ConstExp']'} '=' InitVal
    public Node parseVarDef() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        int dim = 0;
        //parse ident
        TokenNode identNode = new TokenNode(startLine,startLine,SyntaxType.TOKEN,null,curToken);
        children.add(identNode);
        String name = curToken.getContent();
        read();
        //curToken = '['
        //parse '[' ConstExp ']'
        while (curToken.getType() == TokenType.LBRACK) {
            dim++;
            //parse '['
            TokenNode lbrackNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lbrackNode);
            read();
            //parse ConstExp
            Node node = parseConstExp();
            children.add(node);
            //parse ']'
            checkAndReadRbrack(children);
        }
        if (curToken.getType() == TokenType.ASSIGN) {
            //parse '='
            TokenNode assignNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(assignNode);
            read();
            //parse initVal
            Node node = parseInitVal();
            children.add(node);
        }
        //check error
        VarSymbol symbol = new VarSymbol(name,SymbolType.var,dim);
        SymbolTable curTable = tableStack.peek();
        if (curTable.check(name)) {
            ErrorMessage errorMessage = new ErrorMessage(startLine,ErrorType.b);
            printer.addErrorMessage(errorMessage);
        } else {
            curTable.addSymbol(symbol);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new VarDef(startLine,endLine,SyntaxType.VAR_DEF,children);
    }
    
    //FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
    //FuncType -> 'void'|'int'
    //有形参 or 无形参
    public Node parseFuncDef() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse FuncType
        String returnType = curToken.getContent();//'void' or 'int'
        Node node = parseFuncType();
        children.add(node);
        //parse ident '('
        int i;
        String name = curToken.getContent();
        SymbolTable oldTable = tableStack.peek();
        int identLine = curToken.getLine();
        paramNum = 0;
        paramDims = new ArrayList<>();
        for (i = 0; i < 2; i++) {
            TokenNode identNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(identNode);
            read();
        }
        //建立新的符号表
        tableStack.enterBlock();
        //FuncFParams -> FuncParam,FuncParam...
        //FuncParam -> BType Ident... -> 'int' Ident...
        if (curToken.getType() == TokenType.INTTK) {
            node = parseFuncFParams();
            children.add(node);
        }
        //parse ')'
        checkAndReadRparent(children);
        //check func error b
        FuncSymbol symbol = new FuncSymbol(name,SymbolType.func,returnType,paramNum,paramDims);
        tableStack.setCurFunc(symbol);
        //oldTable中找函数是否重定义
        if (oldTable.check(name)) {
            ErrorMessage errorMessage = new ErrorMessage(identLine,ErrorType.b);
            printer.addErrorMessage(errorMessage);
        } else {
            oldTable.addSymbol(symbol);
        }
        //parse block 可以returnTypeCheck
        funcReturnCheck = true;
        node = parseBlock();
        funcReturnCheck = false;
        //解析完函数定义
        tableStack.leaveBlock();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new FuncDef(startLine,endLine,SyntaxType.FUNC_DEF,children);
    }
    
    //'int' 'main' '(' ')' Block
    public Node parseMainFuncDef() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //进入
        tableStack.enterBlock();
        FuncSymbol symbol = new FuncSymbol("main",SymbolType.func,"integer",0,new ArrayList<>());
        tableStack.setCurFunc(symbol);
        //parse int
        TokenNode intNode = new TokenNode(startLine,startLine,SyntaxType.TOKEN,null,curToken);
        children.add(intNode);
        read();
        //parse main
        TokenNode mainNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(mainNode);
        read();
        //parse '('
        TokenNode lparentNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(lparentNode);
        read();
        //parse ')'
        TokenNode rparentNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(rparentNode);
        read();
        //parse Block 可以 returnTypeCheck
        funcReturnCheck = true;
        Node node = parseBlock();
        funcReturnCheck = false;
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        //离开
        tableStack.leaveBlock();
        return new MainFuncDef(startLine,endLine,SyntaxType.MAIN_FUNC_DEF,children);
    }
    
    //Exp -> AddExp
    public Node parseExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseAddExp();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new Exp(startLine,endLine,SyntaxType.EXP,children);
    }
    
    //AddExp -> MulExp {'+' MulExp | '-' MulExp}
    public Node parseAddExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse MulExp
        Node node = parseMulExp();
        children.add(node);
        //parse '+' or '-'
        while (curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU) {
            //防止文法改变导致语法树输出改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.ADD_EXP,null);
            children.add(node);
            //parse '+' or '-'
            TokenNode pmNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(pmNode);
            read();
            //parse MulExp
            node = parseMulExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new AddExp(startLine,endLine,SyntaxType.ADD_EXP,children);
    }
    
    //LOrExp -> LAndExp {'||' LAndExp}
    public Node parseLOrExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse LAndExp
        Node node = parseLAndExp();
        children.add(node);
        while (curToken.getType() == TokenType.OR) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.LOR_EXP,null);
            children.add(node);
            //parse '||'
            TokenNode orNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(orNode);
            read();
            //parse LAndExp
            node = parseLAndExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new LOrExp(startLine,endLine,SyntaxType.LOR_EXP,children);
    }
    
    //PrimaryExp -> '('Exp')' | LVal | Number
    public Node parsePrimaryExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse (Exp)
        if (curToken.getType() == TokenType.LPARENT) {
            //parse '('
            TokenNode lparentNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lparentNode);
            read();
            //parse Exp
            Node node = parseExp();
            children.add(node);
            //parse ')'
            TokenNode rparentNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(rparentNode);
            read();
        }
        //parse Number
        else if (curToken.getType() == TokenType.INTCON) {
            Node node = parseNumber();
            children.add(node);
        }
        //parse LVal
        else {
            Node node = parseLVal();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new PrimaryExp(startLine,endLine,SyntaxType.PRIMARY_EXP,children);
    }
    
    //UnaryExp -> PrimaryExp | Ident '('[FuncRParams]')' | UnaryOp UnaryExp
    public Node parseUnaryExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse ident ([FuncRParams])
        if (curToken.getType() == TokenType.IDENFR && tokenStream.watch(1).getType() == TokenType.LPARENT) {
            //parse ident
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            String name = curToken.getContent();
            children.add(tokenNode);
            read();
            //check error c (not defined func)
            if (!tableStack.checkSymbol(name)) {
                ErrorMessage errorMessage = new ErrorMessage(startLine,ErrorType.c);
                printer.addErrorMessage(errorMessage);
            }
            //parse '('
            tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //ident '('    FuncRParams
            //FuncRParams -> Exp,Exp...
            //FIRST(Exp) = {'(',ident,intConst,'+','-'}
            if (curToken.getType() == TokenType.RPARENT) {
                paramNum = 0;
                if (tableStack.checkSymbol(name)) {
                    Symbol tableSymbol = tableStack.getSymbol(name);
                    int tableParamNum = ((FuncSymbol)tableSymbol).getParamNum();
                    if (tableParamNum != paramNum) {
                        ErrorMessage errorMessage = new ErrorMessage(startLine,ErrorType.d);
                        printer.addErrorMessage(errorMessage);
                    }
                }
            }
            if (curToken.getType() == TokenType.LPARENT || curToken.getType() == TokenType.IDENFR
                    || curToken.getType() == TokenType.INTCON || curToken.getType() == TokenType.PLUS
                    || curToken.getType() == TokenType.MINU) {
                //FuncRParams
                paramNum = 0;
                paramDims = new ArrayList<>();
                Node node = parseFuncRParams();
                children.add(node);
                //check error (d) 没有c错误才能check d
                //SymbolTable curTable = tableStack.peek();
                if (tableStack.checkFuncSymbol(name)) {
                    //Symbol tableSymbol = curTable.getSymbol(name);
                    Symbol tableSymbol = tableStack.getSymbol(name);
                    int tableParamNum = ((FuncSymbol)tableSymbol).getParamNum();
                    if (tableParamNum != paramNum) {
                        ErrorMessage errorMessage = new ErrorMessage(startLine,ErrorType.d);
                        printer.addErrorMessage(errorMessage);
                    }
                    //没有 d 才能 check e
                    else {
                        //check error (e)
                        ArrayList<Integer> tableDims = ((FuncSymbol)tableSymbol).getParamDims();
                        if (tableDims.size() != paramDims.size()) {
                            ErrorMessage errorMessage = new ErrorMessage(startLine,ErrorType.e);
                            printer.addErrorMessage(errorMessage);
                        } else {
                            int j;
                            for (j = 0; j < tableDims.size(); j++) {
                                if (tableDims.get(j) != paramDims.get(j)) {
                                    ErrorMessage errorMessage = new ErrorMessage(startLine,ErrorType.e);
                                    printer.addErrorMessage(errorMessage);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            //')'
            checkAndReadRparent(children);
        }
        //parse UnaryOp UnaryExp
        else if (curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU || curToken.getType() == TokenType.NOT) {
            Node node = parseUnaryOp();
            children.add(node);
            node = parseUnaryExp();
            children.add(node);
        }
        //parse PrimaryExp
        else {
            Node node = parsePrimaryExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new UnaryExp(startLine,endLine,SyntaxType.UNARY_EXP,children);
    }
    
    //MulExp -> UnaryExp {'*' UnaryExp | '/' UnaryExp | '%' UnaryExp}
    public Node parseMulExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse UnaryExp
        Node node = parseUnaryExp();
        children.add(node);
        while (curToken.getType() == TokenType.MULT || curToken.getType() == TokenType.DIV || curToken.getType() == TokenType.MOD) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.MUL_EXP,null);
            children.add(node);
            //parse '*' or '/' or '%'
            TokenNode mdmNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(mdmNode);
            read();
            //parse UnaryExp
            node = parseUnaryExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new MulExp(startLine,endLine,SyntaxType.MUL_EXP,children);
    }
    
    //RelExp -> AddExp {'<' AddExp | '>' AddExp | '<=' AddExp | '>=' AddExp}
    public Node parseRelExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse AddExp
        Node node = parseAddExp();
        children.add(node);
        while (curToken.getType() == TokenType.LSS || curToken.getType() == TokenType.LEQ
                || curToken.getType() == TokenType.GRE || curToken.getType() == TokenType.GEQ) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.REL_EXP,null);
            children.add(node);
            //parse > < <= >=
            TokenNode lgNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lgNode);
            read();
            //parse AddExp
            node = parseAddExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new RelExp(startLine,endLine,SyntaxType.REL_EXP,children);
    }
    
    //EqExp -> RelExp {'==' RelExp | '!=' RelExp}
    public Node parseEqExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseRelExp();
        children.add(node);
        while (curToken.getType() == TokenType.EQL || curToken.getType() == TokenType.NEQ) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.EQ_EXP,null);
            children.add(node);
            TokenNode enNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(enNode);
            read();
            node = parseRelExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new EqExp(startLine,endLine,SyntaxType.EQ_EXP,children);
    }
    
    //LAndExp -> EqExp {'&&' EqExp}
    public Node parseLAndExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseEqExp();
        children.add(node);
        while (curToken.getType() == TokenType.AND) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.LAND_EXP,null);
            children.add(node);
            TokenNode andNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(andNode);
            read();
            node = parseEqExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new LAndExp(startLine,endLine,SyntaxType.LAND_EXP,children);
    }
    
    //ConstExp -> AddExp
    public Node parseConstExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseAddExp();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new ConstExp(startLine,endLine,SyntaxType.CONST_EXP,children);
    }
    
    /// Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    ///| [Exp] ';' //有无Exp两种情况
    ///| Block
    ///| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    ///| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    ///| 'break' ';' | 'continue' ';'
    ///| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    ///| LVal '=' 'getint''('')'';'
    ///| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public Node parseStmt() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //block
        //这种block是不必进行returnType检查的(g)
        //但是要新建符号表！！！
        if (curToken.getType() == TokenType.LBRACE) {
            //TODO 用栈实现
            boolean temp = funcReturnCheck;
            funcReturnCheck = false;
            //新建符号表
            tableStack.enterBlock();
            Node node = parseBlock();
            children.add(node);
            //离开block
            tableStack.leaveBlock();
            //恢复原先权限
            funcReturnCheck = temp;
        }
        //if
        //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
        else if (curToken.getType() == TokenType.IFTK) {
            //if
            TokenNode ifNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(ifNode);
            read();
            //'('
            TokenNode lparentNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lparentNode);
            read();
            //Cond
            Node node = parseCond();
            children.add(node);
            //')'
            checkAndReadRparent(children);
            //stmt
            node = parseStmt();
            children.add(node);
            //有无else
            if (curToken.getType() == TokenType.ELSETK) {
                TokenNode elseNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(elseNode);
                read();
                //stmt
                node = parseStmt();
                children.add(node);
            }
        }
        //for
        //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
        else if (curToken.getType() == TokenType.FORTK) {
            //当前处于for循环
            //TODO 嵌套 for 循环
            //forFlag = true;
            forDepth++;
            //'for' '('
            int i;
            for (i = 0; i < 2; i++) {
                TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(tokenNode);
                read();
            }
            if (curToken.getType() != TokenType.SEMICN) {
                Node node = parseForStmt();
                children.add(node);
            }
            //';'
            checkAndReadSemicn(children);
            //Cond
            if (curToken.getType() != TokenType.SEMICN) {
                Node node = parseCond();
                children.add(node);
            }
            //';'
            checkAndReadSemicn(children);
            //ForStmt
            if (curToken.getType() != TokenType.RPARENT) {
                Node node = parseForStmt();
                children.add(node);
            }
            //')'
            checkAndReadRparent(children);
            //stmt
            Node node = parseStmt();
            children.add(node);
            //退出for循环
            //forFlag = false;
            forDepth--;
        }
        //| 'break' ';' | 'continue' ';'
        else if (curToken.getType() == TokenType.BREAKTK) {
            if (forDepth <= 0) {
                ErrorMessage errorMessage = new ErrorMessage(curToken.getLine(),ErrorType.m);
                printer.addErrorMessage(errorMessage);
            }
            TokenNode breakNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(breakNode);
            read();
            //parse ';'
            checkAndReadSemicn(children);
        } else if (curToken.getType() == TokenType.CONTINUETK) {
            if (forDepth <= 0) {
                ErrorMessage errorMessage = new ErrorMessage(curToken.getLine(),ErrorType.m);
                printer.addErrorMessage(errorMessage);
            }
            TokenNode continueNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(continueNode);
            read();
            //parse ';'
            checkAndReadSemicn(children);
        }
        //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
        else if (curToken.getType() == TokenType.RETURNTK) {
            String returnType = tableStack.getCurFunc().getReturnType();
            //return
            TokenNode returnNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(returnNode);
            int returnLine = curToken.getLine();
            read();
            if (curToken.getType() != TokenType.SEMICN) {
                //<Exp>
                Node node = parseExp();
                children.add(node);
                //check error
                if (returnType.equals("void")) {
                    ErrorMessage errorMessage = new ErrorMessage(returnLine,ErrorType.f);
                    printer.addErrorMessage(errorMessage);
                }
            }
            checkAndReadSemicn(children);
        }
        //| 'printf' '(' FormatString { ',' Exp} ')' ';' // 1.有Exp 2.无Exp
        else if (curToken.getType() == TokenType.PRINTFTK) {
            //FormatString已经在lexer完成错误处理
            //<FormatString> -> ' " ' {<Char>} ' " '
            //<Char> -> <FormatChar> | <NormalChar>
            //<FormatChar> -> %d
            //<NormalChar> → 十进制编码为32,33,40-126的ASCII字符，'\'（编码92）出现当且仅当为'\n'
            //'printf' '(' FormatString
            int printLine = curToken.getLine();
            for (int i = 0; i < 3; i++) {
                TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(tokenNode);
                // FormatString
                if (i == 2) {
                    String formatString = curToken.getContent();
                    String[] strings = formatString.split("%d");
                    formatNum = strings.length - 1;
                }
                read();
            }
            ExpNum = 0;
            while (curToken.getType() == TokenType.COMMA) {
                //','
                TokenNode printfNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(printfNode);
                read();
                //Exp
                Node node = parseExp();
                children.add(node);
                ExpNum++;
            }
            //check error l
            if (formatNum != ExpNum) {
                ErrorMessage errorMessage = new ErrorMessage(printLine,ErrorType.l);
                printer.addErrorMessage(errorMessage);
            }
            //')'
            checkAndReadRparent(children);
            //';'
            checkAndReadSemicn(children);
        }
        //LVal '=' Exp ';'
        //LVal '=' 'getint''('')'';'
        //| [Exp] ';' //有无Exp两种情况
        else {
            // ';'
            if (curToken.getType() == TokenType.SEMICN) {
                //';'
                TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(tokenNode);
                read();
            } else {
                //LVal or Exp
                Node temp = parseExp();
                //Exp   ';'
                if (curToken.getType() == TokenType.SEMICN) {
                    children.add(temp);
                    //';'
                    TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(), SyntaxType.TOKEN, null,curToken);
                    children.add(tokenNode);
                    read();
                }
                //LVal   '=' Exp ';'
                //LVal   '=' 'getint''('')'';'
                else if (curToken.getType() == TokenType.ASSIGN) {
                    //Exp -> AddExp -> MulExp -> UnaryExp -> PrimaryExp -> LVal
                    Node node = temp.getChildren().get(0).getChildren().get(0).getChildren().get(0)
                            .getChildren().get(0).getChildren().get(0);
                    children.add(node);
                    int i;
                    //check error h
                    String name = ((TokenNode)node.getChildren().get(0)).getToken().getContent();
                    Symbol tableSymbol = tableStack.getSymbol(name);
                    if (tableSymbol instanceof ConstSymbol) {
                        ErrorMessage errorMessage = new ErrorMessage(tokenStream.watch(-1).getLine(),ErrorType.h);
                        printer.addErrorMessage(errorMessage);
                    }
                    //'='
                    TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                    children.add(tokenNode);
                    read();
                    //parse 'getint' '(' ')'
                    if (curToken.getType() == TokenType.GETINTTK) {
                        //'getint' '('
                        for (i = 0; i < 2; i++) {
                            tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                            children.add(tokenNode);
                            read();
                        }
                        //')'
                        checkAndReadRparent(children);
                    } else {
                        //parse Exp
                        node = parseExp();
                        children.add(node);
                    }
                    //parse ';'
                    checkAndReadSemicn(children);
                }
                //Exp 缺少';'
                else {
                    ErrorMessage errorMessage = new ErrorMessage(tokenStream.watch(-1).getLine(),ErrorType.i);
                    printer.addErrorMessage(errorMessage);
                }
            }
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Stmt(startLine,endLine,SyntaxType.STMT,children);
    }
    
    //ForStmt -> LVal '=' Exp
    public Node parseForStmt() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //LVal
        Node node = parseLVal();
        children.add(node);
        //'='
        TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        //Exp
        node = parseExp();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new ForStmt(startLine,endLine,SyntaxType.FOR_STMT,children);
    }
    
    //Cond -> LOrExp
    public Node parseCond() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseLOrExp();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new Cond(startLine,endLine,SyntaxType.COND,children);
    }
    
    //LVal -> Ident{'[' Exp ']'} 1.普通变量 2.一维数组 3.二维数组
    public Node parseLVal() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //ident
        TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,children,curToken);
        children.add(tokenNode);
        String name = curToken.getContent();
        read();
        //'[' Exp ']'
        while (curToken.getType() == TokenType.LBRACK) {
            //'['
            tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //Exp
            Node node = parseExp();
            children.add(node);
            //']'
            checkAndReadRbrack(children);
        }
        //check error (not defined)
        if (!tableStack.checkSymbol(name)) {
            ErrorMessage errorMessage = new ErrorMessage(startLine,ErrorType.c);
            printer.addErrorMessage(errorMessage);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new LVal(startLine,endLine,SyntaxType.LVAL,children);
    }
    
    //Number -> IntConst
    public Node parseNumber() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new Number(startLine,endLine,SyntaxType.NUMBER,children);
    }
    
    //UnaryOp -> '+' | '-' | '!'
    public Node parseUnaryOp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new UnaryOp(startLine,endLine,SyntaxType.UNARY_OP,children);
    }
    
    //FuncRParams -> Exp {',' Exp}
    //实参变量
    public Node parseFuncRParams() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        int temp = paramNum;
        ArrayList<Integer> tempDims = paramDims;
        paramNum = 0;
        paramDims = new ArrayList<>();
        Node node = parseExp();
        paramNum = temp;
        paramDims = tempDims;
        paramNum++;
        children.add(node);
        checkFuncRParam(node);
        //',' Exp
        while (curToken.getType() == TokenType.COMMA) {
            //','
            TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //Exp
            temp = paramNum;
            tempDims = paramDims;
            paramNum = 0;
            paramDims = new ArrayList<>();
            node = parseExp();
            paramNum = temp;
            paramDims = tempDims;
            paramNum++;
            children.add(node);
            checkFuncRParam(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new FuncRParams(startLine,endLine,SyntaxType.FUNCR_PARAMS,children);
    }
    
    //FuncFParams -> FuncFParam {',' FuncFParam}
    public Node parseFuncFParams() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseFuncFParam();
        paramNum++;
        children.add(node);
        while (curToken.getType() == TokenType.COMMA) {
            //','
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //FuncFParam
            node = parseFuncFParam();
            paramNum++;
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new FuncFParams(startLine,endLine,SyntaxType.FUNCF_PARAMS,children);
    }
    
    //FuncParam -> BType Ident ['[' ']' {'[' ConstExp ']'}] 1.普通变量2.一维数组变量 3.二维数组变量
    public Node parseFuncFParam() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        int dim = 0;
        String name = null;
        //BTyte Ident
        for (int i = 0; i < 2; i++) {
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            if (i == 1) {
                name = curToken.getContent();
            }
            read();
        }
        //'[' ']'
        if (curToken.getType() == TokenType.LBRACK) {
            dim++;
            //'['
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //']'
            checkAndReadRbrack(children);
        }
        while (curToken.getType() == TokenType.LBRACK) {
            dim++;
            //'['
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //ConstExp
            Node node = parseConstExp();
            children.add(node);
            //']'
            checkAndReadRbrack(children);
        }
        paramDims.add(dim);
        //check error
        VarSymbol symbol = new VarSymbol(name,SymbolType.var,dim);
        SymbolTable curTable = tableStack.peek();
        if (curTable.check(name)) {
            ErrorMessage errorMessage = new ErrorMessage(startLine,ErrorType.b);
            printer.addErrorMessage(errorMessage);
        } else {
            curTable.addSymbol(symbol);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new FuncFParam(startLine,endLine,SyntaxType.FUNCF_PARAM,children);
    }
    
    //ConstInitVal → ConstExp | '{'  [ ConstInitVal {  ','  ConstInitVal } ]  '}' // 1.常表达式初值 2.一维数组初值 3.二维数组初值
    public Node parseConstInitVal() {
        int starLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //ConstExp
        if (curToken.getType() != TokenType.LBRACE) {
            Node node = parseConstExp();
            children.add(node);
        }
        //'{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        else {
            //'{'
            TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //ConstInitVal { ',' ConstInitVal}
            if (curToken.getType() != TokenType.RBRACE) {
                Node node = parseConstInitVal();
                children.add(node);
                while (curToken.getType() == TokenType.COMMA) {
                    //','
                    tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                    children.add(tokenNode);
                    read();
                    //ConstInitVal
                    node = parseConstInitVal();
                    children.add(node);
                }
            }
            //'}'
            tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new ConstInitVal(starLine,endLine,SyntaxType.CONST_INITVAL,children);
    }
    
    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public Node parseInitVal() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //Exp
        if (curToken.getType() != TokenType.LBRACE) {
            Node node = parseExp();
            children.add(node);
        }
        //'{' [ InitVal { ',' InitVal } ] '}'
        else {
            //'{'
            TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //InitVal { ',' InitVal}
            if (curToken.getType() != TokenType.RBRACE) {
                Node node = parseInitVal();
                children.add(node);
                while (curToken.getType() == TokenType.COMMA) {
                    //','
                    tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                    children.add(tokenNode);
                    read();
                    //InitVal
                    node = parseInitVal();
                    children.add(node);
                }
            }
            //'}'
            tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new InitVal(startLine,endLine,SyntaxType.INITVAL,children);
    }
    
    // FuncType → 'void' | 'int'
    public Node parseFuncType() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new FuncType(startLine,endLine,SyntaxType.FUNC_TYPE,children);
    }
    
    //Block → '{' { BlockItem } '}'
    //BlockItem → Decl | Stmt
    //Decl → ConstDecl | VarDecl
    public Node parseBlock() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        int returnFlag = 0;
        //'{'
        TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        //{ BlockItem }
        while (curToken.getType() != TokenType.RBRACE) {
            //ConstDecl
            if (curToken.getType() == TokenType.CONSTTK) {
                Node node = parseConstDecl();
                children.add(node);
            }
            //VarDecl
            else if (curToken.getType() == TokenType.INTTK) {
                Node node = parseVarDecl();
                children.add(node);
            }
            //Stmt
            else {
                if (curToken.getType() == TokenType.RETURNTK) {
                    returnFlag = 1;
                }
                Node node = parseStmt();
                children.add(node);
            }
        }
        //check error g
        String returnType = tableStack.getCurFunc().getReturnType();
        if (Objects.equals(returnType, "integer") && returnFlag == 0 && funcReturnCheck) {
            ErrorMessage errorMessage = new ErrorMessage(curToken.getLine(),ErrorType.g);
            printer.addErrorMessage(errorMessage);
        }
        //'}'
        tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new Block(startLine,endLine,SyntaxType.BLOCK,children);
    }
}


package FrontEnd.Lexer;

import FrontEnd.Symbol.ErrorMessage;
import FrontEnd.Symbol.ErrorType;
import tools.Printer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Lexer {
    private InputStream source;
    private int curPos;
    private char curChar;
    private Token curToken;//content type line
    private int curLine;
    private ArrayList<Token> tokenArrayList;
    private Printer printer;
    public Lexer(InputStream source,Printer printer) throws IOException {
        this.source = source;
        this.curPos = 0;
        this.curChar = (char) source.read();
        this.curLine = 1;
        this.tokenArrayList = new ArrayList<>();
        curToken = getCurToken();
        this.printer = printer;
    }
    
    //tool function
    private void read() throws IOException {
        curChar = (char) source.read();
    }
    
    private boolean isBlank() throws IOException {
        if (curChar == ' ' || curChar == '\t' || isNewLine()) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean isNewLine() throws IOException {
        // 考虑"\r\n"换行
        if (curChar == '\r') {
            read();
        }
        if (curChar == '\n') {
            return true;
        }
        return false;
    }
    
    private boolean isEOF() {
        if (curChar == '\uFFFF') {
            return true;
        } else {
            return false;
        }
    }
    
    private TokenType getIdentType(String ident) {
        switch (ident) {
            case "main":
                return TokenType.MAINTK;
            case "const":
                return TokenType.CONSTTK;
            case "int":
                return TokenType.INTTK;
            case "break":
                return TokenType.BREAKTK;
            case "continue":
                return TokenType.CONTINUETK;
            case "if":
                return TokenType.IFTK;
            case "else":
                return TokenType.ELSETK;
            case "for":
                return TokenType.FORTK;
            case "getint":
                return TokenType.GETINTTK;
            case "printf":
                return TokenType.PRINTFTK;
            case "return":
                return TokenType.RETURNTK;
            case "void":
                return TokenType.VOIDTK;
            default:
                return TokenType.IDENFR;
        }
    }
    
    //注释
    private int FSM(int state) throws IOException {
        switch (state) {
            case 5:
                if (curChar == '*') {
                    read();
                    return 6;
                } else {
                    if (isNewLine()) {
                        curLine++;
                    }
                    read();
                    return 5;
                }
            case 6:
                if (curChar == '/') {
                    read();
                    return 7;
                } else if (curChar == '*') {
                    read();
                    return 6;
                } else {
                    if (isNewLine()) {
                        curLine++;
                    }
                    read();
                    return 5;
                }
            case 7:
                read();
                return 7;
            default:
                System.out.println("error in /**/");
                return -1;
        }
    }
    
    //get result
    public ArrayList<Token> getTokenArrayList() throws IOException {
        //already build
        if (tokenArrayList.size() > 0) {
            return tokenArrayList;
            //to build
        } else {
            while (curToken.getType() != TokenType.EOF) {
                tokenArrayList.add(curToken);
                curToken = getCurToken();
            }
        }
        return tokenArrayList;
    }
    
    public Token getCurToken() throws IOException {
        StringBuilder sb = new StringBuilder();
        //skip blank
        while (isBlank()) {// ' ' '\t' '\n' '\r\n'
            if (isNewLine()) {
                curLine++;
            }
            read();
        }
        //EOF
        if (isEOF()) {
            return new Token(TokenType.EOF,"EOF",curLine);
        }
        //symbols
        else if (curChar == '!') {
            sb.append(curChar);
            read();
            if (curChar == '=') {
                sb.append(curChar);
                read();
                return new Token(TokenType.NEQ,"!=",curLine);
            } else {
                return new Token(TokenType.NOT,"!",curLine);
            }
        } else if (curChar == '&') {
            sb.append(curChar);
            read();
            if (curChar != '&') {
                return new Token(TokenType.ERROR,"&& ERROR",curLine);
            } else {
                sb.append(curChar);
                read();
                return new Token(TokenType.AND,"&&",curLine);
            }
        } else if (curChar == '|') {
            sb.append(curChar);
            read();
            if (curChar != '|') {
                return new Token(TokenType.ERROR,"|| ERROR",curLine);
            } else {
                sb.append(curChar);
                read();
                return new Token(TokenType.OR,"||",curLine);
            }
        } else if (curChar == '+') {
            sb.append(curChar);
            read();
            return new Token(TokenType.PLUS,"+",curLine);
        } else if (curChar == '-') {
            sb.append(curChar);
            read();
            return new Token(TokenType.MINU,"-",curLine);
        } else if (curChar == '*') {
            sb.append(curChar);
            read();
            return new Token(TokenType.MULT,"*",curLine);
        } else if (curChar == '%') {
            sb.append(curChar);
            read();
            return new Token(TokenType.MOD,"%",curLine);
        } else if (curChar == '<') {
            sb.append(curChar);
            read();
            if (curChar == '=') {
                sb.append(curChar);
                read();
                return new Token(TokenType.LEQ,"<=",curLine);
            } else {
                return new Token(TokenType.LSS,"<",curLine);
            }
        } else if (curChar == '>') {
            sb.append(curChar);
            read();
            if (curChar == '=') {
                sb.append(curChar);
                read();
                return new Token(TokenType.GEQ,">=",curLine);
            } else {
                return new Token(TokenType.GRE,">",curLine);
            }
        } else if (curChar == '=') {
            sb.append(curChar);
            read();
            if (curChar == '=') {
                sb.append(curChar);
                read();
                return new Token(TokenType.EQL,"==",curLine);
            } else {
                return new Token(TokenType.ASSIGN,"=",curLine);
            }
        } else if (curChar == ';') {
            sb.append(curChar);
            read();
            return new Token(TokenType.SEMICN,";",curLine);
        } else if (curChar == ',') {
            sb.append(curChar);
            read();
            return new Token(TokenType.COMMA,",",curLine);
        } else if (curChar == '(') {
            sb.append(curChar);
            read();
            return new Token(TokenType.LPARENT,"(",curLine);
        } else if (curChar == ')') {
            sb.append(curChar);
            read();
            return new Token(TokenType.RPARENT,")",curLine);
        } else if (curChar == '[') {
            sb.append(curChar);
            read();
            return new Token(TokenType.LBRACK,"[",curLine);
        } else if (curChar == ']') {
            sb.append(curChar);
            read();
            return new Token(TokenType.RBRACK,"]",curLine);
        } else if (curChar == '{') {
            sb.append(curChar);
            read();
            return new Token(TokenType.LBRACE,"{",curLine);
        } else if (curChar == '}') {
            sb.append(curChar);
            read();
            return new Token(TokenType.RBRACE,"}",curLine);
        }
        //digit
        else if (Character.isDigit(curChar)) {
            sb.append(curChar);
            read();
            while(Character.isDigit(curChar)) {
                sb.append(curChar);
                read();
            }
            return new Token(TokenType.INTCON,sb.toString(),curLine);
        }
        //string
        //需要进行错误处理（词法）
        else if (curChar == '"') {
            sb.append(curChar);
            read();
            while(curChar != '"') {
                if (curChar == 32 || curChar == 33 || (curChar >= 40 && curChar <= 91) || (curChar >= 93 && curChar <= 126)) {
                    sb.append(curChar);
                    read();
                }
                //'\'(92)
                else if (curChar == '\\') {
                    sb.append(curChar);
                    read();
                    if (curChar != 'n') {
                        ErrorMessage errorMessage = new ErrorMessage(curLine, ErrorType.a);
                        printer.addErrorMessage(errorMessage);
                        //把错误的字符读掉保存
                        //sb.append(curChar);
                        //read();
                        if (curChar != '"') {
                            sb.append(curChar);
                            read();
                        }
                    }
                }
                //'%'(37)
                else if (curChar == '%') {
                    sb.append(curChar);
                    read();
                    if (curChar != 'd') {
                        ErrorMessage errorMessage = new ErrorMessage(curLine,ErrorType.a);
                        printer.addErrorMessage(errorMessage);
                        //sb.append(curChar);
                        //read();
                        if (curChar != '"') {
                            sb.append(curChar);
                            read();
                        }
                    }
                }
                //其他不合法字符 read 但不保存
                else {
                    ErrorMessage errorMessage = new ErrorMessage(curLine,ErrorType.a);
                    printer.addErrorMessage(errorMessage);
                    sb.append(curChar);
                    read();
                }
            }
            //' " '
            sb.append(curChar);
            read();
            return new Token(TokenType.STRCON, sb.toString(),curLine);
        }
        //ident
        else if (curChar == '_' || Character.isLetter(curChar)) {
            sb.append(curChar);
            read();
            while (curChar == '_' || Character.isLetter(curChar) || Character.isDigit(curChar)) {
                sb.append(curChar);
                read();
            }
            String ident = sb.toString();
            return new Token(getIdentType(ident),ident,curLine);
        }
        // DIV & note
        else if (curChar == '/') {
            sb.append(curChar);
            read();
            //div (q4)
            if (curChar != '/' && curChar != '*') {
                return new Token(TokenType.DIV,"/",curLine);
            }
            //only one line note (q2)
            else if (curChar == '/') {
                read();
                while (!isNewLine()) {
                    read();
                }
                //q3
                read();
                curLine++;
                return getCurToken();
            }
            //multi lines note (q5)
            //FSM
            else {
                read();
                int state = 5;
                while (state == 5 || state == 6) {
                    state = FSM(state);
                }
                if (state == 7) {
                    return getCurToken();
                } else {
                    return new Token(TokenType.ERROR,"/**/",curLine);
                }
            }
        }
        sb.append(curChar);
        read();
        return new Token(TokenType.ERROR, sb.toString(), curLine);
    }
}

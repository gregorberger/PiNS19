/**
 * @author sliva
 */
package compiler.phases.lexan;

import java.io.*;

import compiler.common.report.*;
import compiler.data.symbol.*;
import compiler.phases.*;

/**
 * Lexical analysis.
 *
 * @author sliva
 */
public class LexAn extends Phase {

    /**
     * The name of the source file.
     */
    private final String srcFileName;

    /**
     * The source file reader.
     */
    private final BufferedReader srcFile;

    /**
     * Trenutna vrstica
     */
    private int currLine;

    /**
     * Trenutni stolpec
     */
    private int currCol;

    /**
     * Trenutni znak
     */
    private int currChar;


    /**
     * Constructs a new phase of lexical analysis.
     */
    public LexAn() {
        super("lexan");
        srcFileName = compiler.Main.cmdLineArgValue("--src-file-name");
        try {
            srcFile = new BufferedReader(new FileReader(srcFileName));
            currLine = 1;
            currCol = -1;
            nextChar();
        } catch (IOException ___) {
            throw new Report.Error("Cannot open source file '" + srcFileName + "'.");
        }
    }

    @Override
    public void close() {
        try {
            srcFile.close();
        } catch (IOException ___) {
            Report.warning("Cannot close source file '" + this.srcFileName + "'.");
        }
        super.close();
    }

    /**
     * The lexer.
     * <p>
     * This method returns the next symbol from the source file. To perform the
     * lexical analysis of the entire source file, this method must be called until
     * it returns EOF. This method calls {@link #lexify()}, logs its result if
     * requested, and returns it.
     *
     * @return The next symbol from the source file or EOF if no symbol is available
     * any more.
     */
    public Symbol lexer() {
        Symbol symb = lexify();
        if (symb.token != Symbol.Term.EOF)
            //System.out.println(symb.location() + " " + symb.token);
            symb.log(logger);
        return symb;
    }

    /**
     * Performs the lexical analysis of the source file.
     * <p>
     * This method returns the next symbol from the source file. To perform the
     * lexical analysis of the entire source file, this method must be called until
     * it returns EOF.
     *
     * @return The next symbol from the source file or EOF if no symbol is available
     * any more.
     */
    private Symbol lexify() {

        StringBuilder besedilo = new StringBuilder();

        while (true) {
            if (this.currChar >= 128) {
                throw new Report.Error(new Location(currLine, currCol + 1, currLine, currCol + 1), "Invalid character.");
            }
            // Konec datoteke
            if (this.currChar == -1) {
                return new Symbol(Symbol.Term.EOF, "EOF", new Location(currLine, currCol, currLine, currCol));
            }

            // Pregled simbolovy
            if (this.currChar == (int) '+') {
                nextChar();
                return new Symbol(Symbol.Term.ADD, "+", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '=') {
                nextChar();
                if (this.currChar == '=') {
                    nextChar();
                    return new Symbol(Symbol.Term.EQU, "==", new Location(currLine, currCol - 2, currLine, currCol));
                }
                return new Symbol(Symbol.Term.ASSIGN, "=", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '!') {
                nextChar();
                if (this.currChar == (int) '=') {
                    nextChar();
                    return new Symbol(Symbol.Term.NEQ, "!=", new Location(currLine, currCol - 2, currLine, currCol));
                }
            }

            if (this.currChar == (int) '<') {
                nextChar();
                if (this.currChar == (int) '=') {
                    nextChar();
                    return new Symbol(Symbol.Term.LEQ, "<=", new Location(currLine, currCol - 2, currLine, currCol));
                }
                return new Symbol(Symbol.Term.LTH, "<", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '>') {
                nextChar();
                if (this.currChar == (int) '=') {
                    nextChar();
                    return new Symbol(Symbol.Term.GEQ, ">=", new Location(currLine, currCol - 2, currLine, currCol));
                }
                return new Symbol(Symbol.Term.GTH, ">", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '-') {
                nextChar();
                return new Symbol(Symbol.Term.SUB, "-", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '*') {
                nextChar();
                return new Symbol(Symbol.Term.MUL, "*", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '/') {
                nextChar();
                return new Symbol(Symbol.Term.DIV, "/", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '%') {
                nextChar();
                return new Symbol(Symbol.Term.MOD, "%", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '$') {
                nextChar();
                return new Symbol(Symbol.Term.ADDR, "$", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '@') {
                nextChar();
                return new Symbol(Symbol.Term.DATA, "@", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) ',') {
                nextChar();
                return new Symbol(Symbol.Term.COMMA, ",", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) ':') {
                nextChar();
                return new Symbol(Symbol.Term.COLON, ":", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) ';') {
                nextChar();
                return new Symbol(Symbol.Term.SEMIC, ";", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '[') {
                nextChar();
                return new Symbol(Symbol.Term.LBRACKET, "[", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) ']') {
                nextChar();
                return new Symbol(Symbol.Term.RBRACKET, "]", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '(') {
                nextChar();
                return new Symbol(Symbol.Term.LPARENTHESIS, "(", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) ')') {
                nextChar();
                return new Symbol(Symbol.Term.RPARENTHESIS, ")", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '{') {
                nextChar();
                return new Symbol(Symbol.Term.LBRACE, "{", new Location(currLine, currCol - 1, currLine, currCol));
            }

            if (this.currChar == (int) '}') {
                nextChar();
                return new Symbol(Symbol.Term.RBRACE, "}", new Location(currLine, currCol - 1, currLine, currCol));
            }

            // Belo besedilo
            // Presledek ali tab
            if (this.currChar == 32 || this.currChar == 9) {
                nextChar();
                continue;
            }

            // Nova vrstica \n
            if (this.currChar == 10) {
                nextChar();
                this.currLine++;
                this.currCol = 0;
                continue;
            }

            // Konec vrstice \r
            if (this.currChar == 13) {
                nextChar();
                // Konec vrstice \r\n
                if (this.currChar == 10) {
                    this.currLine++;
                    nextChar();
                }

                this.currCol = 0;
                continue;
            }

            // Komentar
            if (this.currChar == (int) '#') {
                while (true) {
                    if (this.currChar == 10 || this.currChar == 13) {
                        nextChar();
                        this.currLine++;
                        this.currCol = 0;
                        break;
                    } else if (this.currChar == -1) {
                        break;
                    }
                    nextChar();
                }
                continue;
            }

            // Kljucne besede
            if ((this.currChar >= (int) 'a' && this.currChar <= (int) 'z') ||
                    (this.currChar >= (int) 'A' && this.currChar <= (int) 'Z')) {

                while (true) {
                    if ((this.currChar >= (int) 'a' && this.currChar <= (int) 'z') ||
                            (this.currChar >= (int) 'A' && this.currChar <= (int) 'Z') ||
                            this.currChar == (int) '_' ||
                            (this.currChar >= (int) '0' && this.currChar <= (int) '9')) {
                        besedilo.append((char) this.currChar);
                        nextChar();
                    } else if (this.currChar >= 128) {
                        throw new Report.Error(new Location(currLine, currCol - 1, currLine, currCol - 1), "Invalid character.");
                    } else {
                        if (besedilo.toString().equals("del")) {
                            return new Symbol(Symbol.Term.DEL, besedilo.toString(), new Location(currLine, currCol - 2, currLine, currCol));
                        } else if (besedilo.toString().equals("arr")) {
                            return new Symbol(Symbol.Term.ARR, besedilo.toString(), new Location(currLine, currCol - 2, currLine, currCol));
                        } else if (besedilo.toString().equals("bool")) {
                            return new Symbol(Symbol.Term.BOOL, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("char")) {
                            return new Symbol(Symbol.Term.CHAR, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("do")) {
                            return new Symbol(Symbol.Term.DO, besedilo.toString(), new Location(currLine, currCol - 1, currLine, currCol));
                        } else if (besedilo.toString().equals("else")) {
                            return new Symbol(Symbol.Term.ELSE, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("end")) {
                            return new Symbol(Symbol.Term.END, besedilo.toString(), new Location(currLine, currCol - 2, currLine, currCol));
                        } else if (besedilo.toString().equals("fun")) {
                            return new Symbol(Symbol.Term.FUN, besedilo.toString(), new Location(currLine, currCol - 2, currLine, currCol));
                        } else if (besedilo.toString().equals("if")) {
                            return new Symbol(Symbol.Term.IF, besedilo.toString(), new Location(currLine, currCol - 1, currLine, currCol));
                        } else if (besedilo.toString().equals("int")) {
                            return new Symbol(Symbol.Term.INT, besedilo.toString(), new Location(currLine, currCol - 2, currLine, currCol));
                        } else if (besedilo.toString().equals("new")) {
                            return new Symbol(Symbol.Term.NEW, besedilo.toString(), new Location(currLine, currCol - 2, currLine, currCol));
                        } else if (besedilo.toString().equals("ptr")) {
                            return new Symbol(Symbol.Term.PTR, besedilo.toString(), new Location(currLine, currCol - 2, currLine, currCol));
                        } else if (besedilo.toString().equals("then")) {
                            return new Symbol(Symbol.Term.THEN, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("typ")) {
                            return new Symbol(Symbol.Term.TYP, besedilo.toString(), new Location(currLine, currCol - 2, currLine, currCol));
                        } else if (besedilo.toString().equals("var")) {
                            return new Symbol(Symbol.Term.VAR, besedilo.toString(), new Location(currLine, currCol - 2, currLine, currCol));
                        } else if (besedilo.toString().equals("void")) {
                            return new Symbol(Symbol.Term.VOID, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("where")) {
                            return new Symbol(Symbol.Term.WHERE, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("while")) {
                            return new Symbol(Symbol.Term.WHILE, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("none")) {
                            return new Symbol(Symbol.Term.VOIDCONST, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("true")) {
                            return new Symbol(Symbol.Term.BOOLCONST, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("false")) {
                            return new Symbol(Symbol.Term.BOOLCONST, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else if (besedilo.toString().equals("null")) {
                            return new Symbol(Symbol.Term.PTRCONST, besedilo.toString(), new Location(currLine, currCol - 3, currLine, currCol));
                        } else {
                            return new Symbol(Symbol.Term.IDENTIFIER, besedilo.toString(), new Location(currLine, currCol - besedilo.length() + 1, currLine, currCol));
                        }

                    }
                }
            }
            // Char konstante
            if (this.currChar == (int) '\'') {
                nextChar();
                if (this.currChar >= 32 && this.currChar <= 126) {
                    besedilo.append((char) this.currChar);
                    nextChar();
                    if (this.currChar == (int) '\'') {
                        nextChar();
                        return new Symbol(Symbol.Term.CHARCONST, '\'' + besedilo.toString() + '\'', new Location(currLine, currCol - 1, currLine, currCol - 1));
                    } else if (this.currChar >= 32 && this.currChar <= 126) {
                        throw new Report.Error(new Location(currLine, currCol - 1, currLine, currCol - 1), "Char to long.");
                    } else {
                        throw new Report.Error(new Location(currLine, currCol - 1, currLine, currCol - 1), "Char not closed.");
                    }
                }
            }

            // Int konstante
            if (this.currChar >= (int) '0' && this.currChar <= (int) '9') {
                besedilo.append((char) this.currChar);
                nextChar();
                while (true) {
                    if (this.currChar >= (int) '0' && this.currChar <= (int) '9') {
                        besedilo.append((char) this.currChar);
                        nextChar();
                    } else {
                        return new Symbol(Symbol.Term.INTCONST, besedilo.toString(), new Location(currLine, currCol - besedilo.length() + 1, currLine, currCol));
                    }
                }
            }

        }

    }

    /**
     * Preberi naslednji znak
     */
    private void nextChar() {
        try {
            this.currChar = this.srcFile.read();
            this.currCol++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

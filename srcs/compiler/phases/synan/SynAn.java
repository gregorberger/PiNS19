/**
 * @author sliva
 */
package compiler.phases.synan;

import compiler.common.report.*;
import compiler.data.symbol.*;
import compiler.data.dertree.*;
import compiler.phases.*;
import compiler.phases.lexan.*;

import java.util.Arrays;

/**
 * Syntax analysis.
 *
 * @author sliva
 */
public class SynAn extends Phase {

    /**
     * The derivation tree of the program being compiled.
     */
    public static DerTree derTree = null;

    /**
     * The lexical analyzer used by this syntax analyzer.
     */
    private final LexAn lexAn;

    /**
     * Constructs a new phase of syntax analysis.
     */
    public SynAn() {
        super("synan");
        lexAn = new LexAn();
    }

    @Override
    public void close() {
        lexAn.close();
        super.close();
    }

    /**
     * The parser.
     * <p>
     * This method constructs a derivation tree of the program in the source file.
     * It calls method {@link #parseSource()} that starts a recursive descent parser
     * implementation of an LL(1) parsing algorithm.
     */
    public void parser() {
        currSymb = lexAn.lexer();
        derTree = parseSource();
        if (currSymb.token != Symbol.Term.EOF)
            error();
    }

    /**
     * The lookahead buffer (of length 1).
     */
    private Symbol currSymb = null;

    /**
     * Appends the current symbol in the lookahead buffer to a derivation tree node
     * (typically the node of the derivation tree that is currently being expanded
     * by the parser) and replaces the current symbol (just added) with the next
     * input symbol.
     *
     * @param node The node of the derivation tree currently being expanded by the
     *             parser.
     */
    private void add(DerNode node) {
        if (currSymb == null)
            throw new Report.InternalError();
        node.add(new DerLeaf(currSymb));
        currSymb = lexAn.lexer();
    }

    /**
     * If the current symbol is the expected terminal, appends the current symbol in
     * the lookahead buffer to a derivation tree node (typically the node of the
     * derivation tree that is currently being expanded by the parser) and replaces
     * the current symbol (just added) with the next input symbol. Otherwise,
     * produces the error message.
     *
     * @param node     The node of the derivation tree currently being expanded by
     *                 the parser.
     * @param token    The expected terminal.
     * @param errorMsg The error message.
     */
    private void add(DerNode node, Symbol.Term token, String errorMsg) {
        if (currSymb == null)
            throw new Report.InternalError();
        if (currSymb.token == token) {
            node.add(new DerLeaf(currSymb));
            currSymb = lexAn.lexer();
        } else
            throw new Report.Error(currSymb, errorMsg);
    }

    // Source -> Decls .
    private DerNode parseSource() {
        DerNode node = new DerNode(DerNode.Nont.Source);
        switch (currSymb.token) {
            case TYP:
            case VAR:
            case FUN:
                node.add(parseDecls());
                break;
            default:
                error();
        }

        return node;
    }

    // Decls -> Decl DeclsRst .
    private DerTree parseDecls() {
        DerNode node = new DerNode(DerNode.Nont.Decls);
        switch (currSymb.token) {
            case TYP:
            case VAR:
            case FUN:
                node.add(parseDecl());
                node.add(parseDeclsRst());
                break;
            default:
                error();
        }

        return node;
    }

    /*
    Decl -> typ identifier : Type  .
    Decl -> var identifier : Type  .
    Decl -> fun identifier ( ParDeclsEps ) : Type BodyEps  .
     */
    private DerNode parseDecl() {
        DerNode node = new DerNode(DerNode.Nont.Decl);
        switch (currSymb.token) {
            case TYP:
            case VAR:
                add(node);
                add(node, Symbol.Term.IDENTIFIER, "expected " + Symbol.Term.IDENTIFIER);
                add(node, Symbol.Term.COLON, "expectred " + Symbol.Term.COLON);
                node.add(parseType());
                break;
            case FUN:
                add(node);
                add(node, Symbol.Term.IDENTIFIER, "expected " + Symbol.Term.IDENTIFIER);
                add(node, Symbol.Term.LPARENTHESIS, "expected " + Symbol.Term.LPARENTHESIS);
                node.add(parseParDeclsEps());
                add(node, Symbol.Term.RPARENTHESIS, "expected " + Symbol.Term.RPARENTHESIS);

                add(node, Symbol.Term.COLON, "expected " + Symbol.Term.COLON);
                node.add(parseType());
                node.add(parseBodyEps());
                break;
            default:
                error();
        }
        return node;
    }

    /*
    DeclsRst -> .
    DeclsRst -> Decls .
     */
    private DerNode parseDeclsRst() {
        DerNode node = new DerNode(DerNode.Nont.DeclsRest);
        switch (currSymb.token) {
            case TYP:
            case VAR:
            case FUN:
                node.add(parseDecls());
                break;
            case RBRACE:
            case EOF:
                break;
            default:
                error();
        }

        return node;
    }


    /*
    ParDeclsEps -> .
    ParDeclsEps -> ParDecls .
     */
    private DerNode parseParDeclsEps() {
        DerNode node = new DerNode(DerNode.Nont.ParDeclsEps);
        switch (currSymb.token) {
            case IDENTIFIER:
                node.add(parseParDecls());
                break;
            case RPARENTHESIS:
                break;
            default:
                error();
        }

        return node;
    }

    // ParDecls -> ParDecl ParDeclsRest .
    private DerNode parseParDecls() {
        DerNode node = new DerNode(DerNode.Nont.ParDecls);
        switch (currSymb.token) {
            case IDENTIFIER:
                node.add(parseParDecl());
                node.add(parseParDeclsRest());
                break;
            default:
                error();
        }

        return node;
    }

    /*
    ParDeclsRest -> .
    ParDeclsRest -> , ParDecls .
     */
    private DerNode parseParDeclsRest() {
        DerNode node = new DerNode(DerNode.Nont.ParDeclsRest);
        switch (currSymb.token) {
            case RPARENTHESIS:
                break;
            case COMMA:
                add(node);
                node.add(parseParDecls());
                break;
            default:
                error();
        }

        return node;
    }

    // ParDecl -> identifier : Type .
    private DerNode parseParDecl() {
        DerNode node = new DerNode(DerNode.Nont.ParDecl);
        switch (currSymb.token) {
            case IDENTIFIER:
                add(node);
                add(node, Symbol.Term.COLON, "expectred " + Symbol.Term.COLON);
                node.add(parseType());
                break;
            default:
                error();
        }

        return node;
    }


    /*
    BodyEps -> .
    BodyEps ->  = Stmts : RelExpr WhereEps .
     */
    private DerNode parseBodyEps() {
        DerNode node = new DerNode(DerNode.Nont.BodyEps);
        switch (currSymb.token) {
            case TYP:
            case VAR:
            case FUN:
            case RBRACE:
            case EOF:
                break;
            case ASSIGN:
                add(node);
                node.add(parseStmts());
                add(node, Symbol.Term.COLON, "expectred " + Symbol.Term.COLON);
                node.add(parseRelExpr());
                node.add(parseWhereEps());
                break;
            default:
                error();
        }

        return node;
    }

    /*
    WhereEps -> { where Decls } .
    WhereEps -> .
     */
    private DerNode parseWhereEps() {
        DerNode node = new DerNode(DerNode.Nont.WhereEps);
        switch (currSymb.token) {
            case TYP:
            case VAR:
            case FUN:
            case RBRACE:
            case EOF:
                break;
            case LBRACE:
                add(node);
                add(node, Symbol.Term.WHERE, "expected " + Symbol.Term.WHERE);
                node.add(parseDecls());
                add(node, Symbol.Term.RBRACE, "expected " + Symbol.Term.RBRACE);
                break;
            default:
                error();
        }

        return node;
    }

    /*
    Type -> void | int | char | bool .
    Type -> arr [ RelExpr ] Type | ptr Type .
    Type -> identifier | ( Type ).
     */
    private DerNode parseType() {
        DerNode node = new DerNode(DerNode.Nont.Type);
        switch (currSymb.token) {
            case IDENTIFIER:
                add(node);
                break;
            case LPARENTHESIS:
                add(node);
                node.add(parseType());
                add(node, Symbol.Term.RPARENTHESIS, "expected " + Symbol.Term.RPARENTHESIS);
                break;
            case VOID:
                add(node);
                break;
            case INT:
                add(node);
                break;
            case CHAR:
                add(node);
                break;
            case BOOL:
                add(node);
                break;
            case ARR:
                add(node);
                add(node, Symbol.Term.LBRACKET, "expected " + Symbol.Term.LBRACKET);
                node.add(parseRelExpr());
                add(node, Symbol.Term.RBRACKET, "expected " + Symbol.Term.RBRACKET);
                node.add(parseType());
                break;
            case PTR:
                add(node);
                node.add(parseType());
                break;
            default:
                error();
        }

        return node;
    }

    // RelExpr -> AddExpr RelExprRest .
    private DerNode parseRelExpr() {
        DerNode node = new DerNode(DerNode.Nont.RelExpr);
        switch (currSymb.token) {
            case IDENTIFIER:
            case LPARENTHESIS:
            case ADD:
            case SUB:
            case DATA:
            case ADDR:
            case VOIDCONST:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case NEW:
            case DEL:
                node.add(parseAddExpr());
                node.add(parseRelExprRest());
                break;
            default:
                error();
        }

        return node;
    }

    /*
    RelExprRest -> .
    RelExprRest -> eql AddExpr | neq AddExpr | leq AddExpr .
    RelExprRest -> geq AddExpr | lth AddExpr | gth AddExpr .
     */
    private DerNode parseRelExprRest() {
        DerNode node = new DerNode(DerNode.Nont.RelExprRest);
        switch (currSymb.token) {
            case TYP:
            case COLON:
            case VAR:
            case FUN:
            case RPARENTHESIS:
            case COMMA:
            case ASSIGN:
            case RBRACKET:
            case DEL:
            case RBRACE:
            case THEN:
            case END:
            case DO:
            case SEMIC:
            case ELSE:
            case EOF:
            case LBRACE:
                break;
            case EQU:
                add(node);
                node.add(parseAddExpr());
                break;
            case NEQ:
                add(node);
                node.add(parseAddExpr());
                break;
            case LEQ:
                add(node);
                node.add(parseAddExpr());
                break;
            case GEQ:
                add(node);
                node.add(parseAddExpr());
                break;
            case LTH:
                add(node);
                node.add(parseAddExpr());
                break;
            case GTH:
                add(node);
                node.add(parseAddExpr());
                break;
            default:
                error();
        }


        return node;
    }

    // AddExpr -> MulExpr AddExprRest .
    private DerNode parseAddExpr() {
        DerNode node = new DerNode(DerNode.Nont.AddExpr);
        switch (currSymb.token) {
            case IDENTIFIER:
            case LPARENTHESIS:
            case ADD:
            case SUB:
            case DATA:
            case ADDR:
            case VOIDCONST:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case NEW:
            case DEL:
                node.add(parseMulExpr());
                node.add(parseAddExprRest());
                break;
            default:
                error();
        }

        return node;
    }

    // MulExpr -> PrefExpr MulExprRest.
    private DerNode parseMulExpr() {
        DerNode node = new DerNode(DerNode.Nont.MulExpr);
        switch (currSymb.token) {
            case IDENTIFIER:
            case LPARENTHESIS:
            case ADD:
            case SUB:
            case DATA:
            case ADDR:
            case VOIDCONST:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case NEW:
            case DEL:
                node.add(parsePrefExpr());
                node.add(parseMulExprRest());
                break;
            default:
                error();
        }

        return node;
    }

    /*
    PrefExpr -> PstfExpr | + PrefExpr | - PrefExpr .
    PrefExpr -> @ PrefExpr | $ PrefExpr .
     */
    private DerNode parsePrefExpr() {
        DerNode node = new DerNode(DerNode.Nont.PrefExpr);
        switch (currSymb.token) {
            case IDENTIFIER:
            case LPARENTHESIS:
            case VOIDCONST:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case NEW:
            case DEL:
                node.add(parsePstfExpr());
                break;
            case ADD:
                add(node);
                node.add(parsePrefExpr());
                break;
            case SUB:
                add(node);
                node.add(parsePrefExpr());
                break;
            case DATA:
                add(node);
                node.add(parsePrefExpr());
                break;
            case ADDR:
                add(node);
                node.add(parsePrefExpr());
                break;
            default:
                error();
        }

        return node;
    }

    // PstfExpr -> Expr PstfExprRest .
    private DerNode parsePstfExpr() {
        DerNode node = new DerNode(DerNode.Nont.PstfExpr);
        switch (currSymb.token) {
            case IDENTIFIER:
            case LPARENTHESIS:
            case VOIDCONST:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case NEW:
            case DEL:
                node.add(parseExpr());
                node.add(parsePstfExprRest());
                break;
            default:
                error();
        }

        return node;
    }

    // Expr -> AtomExpr | ( RelExpr CastEps ) .
    private DerNode parseExpr() {
        DerNode node = new DerNode(DerNode.Nont.Expr);
        switch (currSymb.token) {
            case IDENTIFIER:
            case VOIDCONST:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case NEW:
            case DEL:
                node.add(parseAtomExpr());
                break;
            case LPARENTHESIS:
                add(node);
                node.add(parseRelExpr());
                node.add(parseCastEps());
                add(node, Symbol.Term.RPARENTHESIS, "expected " + Symbol.Term.RPARENTHESIS);
                break;
            default:
                error();
        }

        return node;
    }

    /*
    CastEps -> .
    CastEps -> : Type .
     */
    private DerNode parseCastEps() {
        DerNode node = new DerNode(DerNode.Nont.CastEps);
        switch (currSymb.token) {
            case COLON:
                add(node);
                node.add(parseType());
                break;
            case RPARENTHESIS:
                break;
            default:
                error();
        }

        return node;
    }

    // AtomExpr -> voidconst | boolconst | charconst | intconst | ptrconst | new ( Type ) |del ( RelExpr ) | identifier CallEps .
    private DerNode parseAtomExpr() {
        DerNode node = new DerNode(DerNode.Nont.AtomExpr);
        switch (currSymb.token) {
            case IDENTIFIER:
                add(node);
                node.add(parseCallEps());
                break;
            case VOIDCONST:
                add(node);
                break;
            case BOOLCONST:
                add(node);
                break;
            case CHARCONST:
                add(node);
                break;
            case INTCONST:
                add(node);
                break;
            case PTRCONST:
                add(node);
                break;
            case NEW:
                add(node);
                add(node, Symbol.Term.LPARENTHESIS, "expected " + Symbol.Term.LPARENTHESIS);
                node.add(parseType());
                add(node, Symbol.Term.RPARENTHESIS, "expected " + Symbol.Term.RPARENTHESIS);
                break;
            case DEL:
                add(node);
                add(node, Symbol.Term.LPARENTHESIS, "expected " + Symbol.Term.LPARENTHESIS);
                node.add(parseRelExpr());
                add(node, Symbol.Term.RPARENTHESIS, "expected " + Symbol.Term.RPARENTHESIS);
                break;
            default:
                error();

        }

        return node;
    }

    /*
    CallEps -> .
    CallEps -> ( ArgsEps ) .
     */
    private DerNode parseCallEps() {
        DerNode node = new DerNode(DerNode.Nont.CallEps);
        switch (currSymb.token) {
            case TYP:
            case COLON:
            case VAR:
            case FUN:
            case RPARENTHESIS:
            case COMMA:
            case ASSIGN:
            case LBRACKET:
            case RBRACKET:
            case EQU:
            case NEQ:
            case LEQ:
            case GEQ:
            case LTH:
            case GTH:
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case LBRACE:
            case RBRACE:
            case THEN:
            case END:
            case DO:
            case SEMIC:
            case ELSE:
            case EOF:
                break;
            case LPARENTHESIS:
                add(node);
                node.add(parseArgsEps());
                add(node, Symbol.Term.RPARENTHESIS, "expected " + Symbol.Term.RPARENTHESIS);
                break;
            default:
                error();
        }
        return node;
    }

    /*
    ArgsEps -> .
    ArgsEps -> Args ArgsRest .
     */
    private DerNode parseArgsEps() {
        DerNode node = new DerNode(DerNode.Nont.ArgsEps);
        switch (currSymb.token) {
            case RPARENTHESIS:
                break;
            case IDENTIFIER:
            case LPARENTHESIS:
            case ADD:
            case SUB:
            case DATA:
            case ADDR:
            case VOIDCONST:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case NEW:
            case DEL:
                node.add(parseArgs());
                node.add(parseArgsRest());
                break;
            default:
                error();
        }
        return node;
    }

    // Args -> RelExpr .
    private DerNode parseArgs() {
        DerNode node = new DerNode(DerNode.Nont.Args);
        switch (currSymb.token) {
            case IDENTIFIER:
            case LPARENTHESIS:
            case ADD:
            case SUB:
            case DATA:
            case ADDR:
            case VOIDCONST:
            case INTCONST:
            case BOOLCONST:
            case CHARCONST:
            case PTRCONST:
            case NEW:
            case DEL:
                node.add(parseRelExpr());
                break;
            default:
                error();
        }

        return node;
    }

    /*
    ArgsRest -> , Args ArgsRest .
    ArgsRest -> .
     */
    private DerNode parseArgsRest() {
        DerNode node = new DerNode(DerNode.Nont.ArgsRest);
        switch (currSymb.token) {
            case RPARENTHESIS:
                break;
            case COMMA:
                add(node);
                node.add(parseArgs());
                node.add(parseArgsRest());
                break;
            default:
                error();
        }

        return node;
    }

    /*
    PstfExprRest -> .
    PstfExprRest -> [ RelExpr ] PstfExprRest .
     */
    private DerNode parsePstfExprRest() {
        DerNode node = new DerNode(DerNode.Nont.PstfExprRest);
        switch (currSymb.token) {
            case TYP:
            case COLON:
            case VAR:
            case FUN:
            case RPARENTHESIS:
            case COMMA:
            case ASSIGN:
            case RBRACKET:
            case EQU:
            case NEQ:
            case LEQ:
            case GEQ:
            case LTH:
            case GTH:
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case ADDR:
            case LBRACE:
            case RBRACE:
            case THEN:
            case END:
            case DO:
            case SEMIC:
            case ELSE:
            case EOF:
            case MOD:
                break;
            case LBRACKET:
                add(node);
                node.add(parseRelExpr());
                add(node, Symbol.Term.RBRACKET, "expected " + Symbol.Term.RBRACKET);
                node.add(parsePstfExprRest());
                break;
            default:
                error();
        }

        return node;
    }

    /*
    MulExprRest -> .
    MulExprRest -> * PrefExpr MulExprRest | / PrefExpr MulExprRest | % PrefExpr MulExprRest
.
     */
    private DerNode parseMulExprRest() {
        DerNode node = new DerNode(DerNode.Nont.MulExprRest);
        switch (currSymb.token) {
            case TYP:
            case COLON:
            case VAR:
            case FUN:
            case RPARENTHESIS:
            case COMMA:
            case ASSIGN:
            case RBRACKET:
            case EQU:
            case NEQ:
            case LEQ:
            case GEQ:
            case LTH:
            case GTH:
            case ADD:
            case SUB:
            case LBRACE:
            case RBRACE:
            case THEN:
            case END:
            case DO:
            case SEMIC:
            case ELSE:
            case EOF:
                break;
            case MUL:
            case DIV:
            case MOD:
                add(node);
                node.add(parsePrefExpr());
                node.add(parseMulExprRest());
                break;
            default:
                error();
        }
        return node;
    }


    /*
    AddExprRest -> .
    AddExprRest -> + MulExpr AddExprRest | - MulExpr AddExprRest.
    */
    private DerNode parseAddExprRest() {
        DerNode node = new DerNode(DerNode.Nont.AddExprRest);
        switch (currSymb.token) {
            case TYP:
            case COLON:
            case VAR:
            case FUN:
            case RPARENTHESIS:
            case COMMA:
            case ASSIGN:
            case RBRACKET:
            case EQU:
            case NEQ:
            case LEQ:
            case GEQ:
            case LTH:
            case GTH:
            case LBRACE:
            case RBRACE:
            case THEN:
            case END:
            case DO:
            case SEMIC:
            case ELSE:
            case EOF:
                break;
            case ADD:
            case SUB:
                add(node);
                node.add(parseMulExpr());
                node.add(parseAddExprRest());
                break;
            default:
                error();
        }
        return node;
    }

    // Stmts -> Stmt StmtsRest .
    private DerNode parseStmts() {
        DerNode node = new DerNode(DerNode.Nont.Stmts);
        switch (currSymb.token) {
            case IDENTIFIER:
            case LPARENTHESIS:
            case ADD:
            case SUB:
            case DATA:
            case ADDR:
            case VOIDCONST:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case NEW:
            case DEL:
            case IF:
            case WHILE:
                node.add(parseStmt());
                node.add(parseStmtsRest());
                break;
            default:
                error();
        }
        return node;
    }

    /*
    Stmt -> RelExpr AssignEps  .
    Stmt -> if RelExpr then Stmts ElseEps end  .
    Stmt -> while RelExpr do Stmts end  .
     */
    private DerNode parseStmt() {
        DerNode node = new DerNode(DerNode.Nont.Stmt);
        switch (currSymb.token) {
            case IDENTIFIER:
            case LPARENTHESIS:
            case ADD:
            case SUB:
            case DATA:
            case ADDR:
            case VOIDCONST:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case NEW:
            case DEL:
                node.add(parseRelExpr());
                node.add(parseAssignEps());
                break;
            case IF:
                add(node);
                node.add(parseRelExpr());
                add(node, Symbol.Term.THEN, "expected " + Symbol.Term.THEN);
                node.add(parseStmts());
                node.add(parseElseEps());
                add(node, Symbol.Term.END, "expected " + Symbol.Term.END);
                break;
            case WHILE:
                add(node);
                node.add(parseRelExpr());
                add(node, Symbol.Term.DO, "expected " + Symbol.Term.DO);
                node.add(parseStmts());
                add(node, Symbol.Term.END, "expected " + Symbol.Term.END);
                break;
            default:
                error();
        }
        return node;
    }

    /*
    StmtsRest -> .
    StmtsRest -> ; Stmts.
     */
    private DerNode parseStmtsRest() {
        DerNode node = new DerNode(DerNode.Nont.StmtsRest);
        switch (currSymb.token) {
            case COLON:
            case END:
            case ELSE:
                break;
            case SEMIC:
                add(node);
                node.add(parseStmts());
                break;
            default:
                error();
        }
        return node;
    }

    /*
    AssignEps -> .
    AssignEps -> = RelExpr .
     */
    private DerNode parseAssignEps() {
        DerNode node = new DerNode(DerNode.Nont.AssignEps);
        switch (currSymb.token) {
            case COLON:
            case END:
            case SEMIC:
            case ELSE:
                break;
            case ASSIGN:
                add(node);
                node.add(parseRelExpr());
                break;
            default:
                error();
        }
        return node;
    }

    /*
    ElseEps -> .
    ElseEps -> else Stmts .
     */
    private DerNode parseElseEps() {
        DerNode node = new DerNode(DerNode.Nont.ElseEps);
        switch (currSymb.token) {
            case END:
                break;
            case ELSE:
                add(node);
                node.add(parseStmts());
                break;
            default:
                error();
        }
        return node;
    }

    // throw error when needed
    private void error() {
        throw new Report.Error(currSymb, "Unexpected '" + currSymb + "' at the end of a program.");
    }
}

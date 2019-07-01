/**
 * @author sliva
 */
package compiler.phases.abstr;

import java.util.*;

import compiler.common.report.*;
import compiler.data.dertree.*;
import compiler.data.dertree.visitor.*;
import compiler.data.abstree.*;


/**
 * Transforms a derivation tree to an abstract syntax tree.
 *
 * @author sliva
 */
public class AbsTreeConstructor implements DerVisitor<AbsTree, AbsTree> {

    @Override
    public AbsTree visit(DerLeaf leaf, AbsTree visArg) {
        throw new Report.InternalError();
    }

    @Override
    public AbsTree visit(DerNode node, AbsTree visArg) {
        switch (node.label) {


            // Source -> Decls .
            case Source: {
                AbsDecls decls = (AbsDecls) node.subtree(0).accept(this, null);
                return new AbsSource(node.location(), decls);
            }


            /*
            Decls -> Decl DeclsRst .
             */
            case Decls: {
                Vector<AbsDecl> allDecls = new Vector<AbsDecl>();
                if (node.numSubtrees() == 0)
                    return null;
                AbsDecl decl = (AbsDecl) node.subtree(0).accept(this, null);
                allDecls.add(decl);
                AbsDecls decls = (AbsDecls) node.subtree(1).accept(this, null);
                if (decls != null)
                    allDecls.addAll(decls.decls());
                return new AbsDecls(node.location(), allDecls);
            }



            /*
            DeclsRst -> .
            DeclsRst -> Decls .
             */
            case DeclsRest: {
                if (node.numSubtrees() == 0)
                    return new AbsDecls(new Location(0, 0), new Vector<AbsDecl>());
//                AbsDecl decl = (AbsDecl) node.subtree(0).accept(this, null);
//                allDecls.add(decl);
                Vector<AbsDecl> allDecls;
                AbsDecls decls = (AbsDecls) node.subtree(0).accept(this, null);
                allDecls = new Vector<AbsDecl>(decls.decls());
                return new AbsDecls(node.location(), allDecls);
            }

            /*
            Decl -> typ identifier : Type  .
            Decl -> var identifier : Type  .
            Decl -> fun identifier ( ParDeclsEps ) : Type BodyEps  .
             */
            case Decl: {
                switch (((DerLeaf) node.subtree(0)).symb.token) {
                    case VAR:
                        Location locationVar = new Location(node, node);
                        String nameVar = ((DerLeaf) node.subtree(1)).symb.lexeme;
                        AbsType typeVar = ((AbsType) node.subtree(3).accept(this, null));
                        return new AbsVarDecl(locationVar, nameVar, typeVar);
                    case TYP:
                        Location locationTyp = new Location(node, node);
                        String nameTyp = ((DerLeaf) node.subtree(1)).symb.lexeme;
                        AbsType typeTyp = ((AbsType) node.subtree(3).accept(this, null));
                        return new AbsTypDecl(locationTyp, nameTyp, typeTyp);
                    case FUN:
                        Location locationFun = new Location(node, node);
                        String nameFun = ((DerLeaf) node.subtree(1)).symb.lexeme;
                        AbsParDecls absParDecls = ((AbsParDecls) node.subtree(3).accept(this, null));
                        AbsType typeFun = ((AbsType) node.subtree(6).accept(this, null));
                        if (((DerNode) node.subtree(7)).numSubtrees() == 0) {
                            return new AbsFunDecl(locationFun, nameFun, absParDecls, typeFun);
                        } else {
                            AbsExpr absExpr = ((AbsExpr) node.subtree(7).accept(this, null));
                            return new AbsFunDef(locationFun, nameFun, absParDecls, typeFun, absExpr);
                        }

                }
            }

            /*
            ParDeclsEps -> .
            ParDeclsEps -> ParDecls .
             */
            case ParDeclsEps: {
                if (node.numSubtrees() == 0)
                    return new AbsParDecls(new Location(0, 0), new Vector<AbsParDecl>());
                return node.subtree(0).accept(this, null);
            }

            // ParDecls -> ParDecl ParDeclsRest .
            case ParDecls: {
                Vector<AbsParDecl> allParDecls = new Vector<AbsParDecl>();

                AbsParDecl decl = ((AbsParDecl) node.subtree(0).accept(this, null));
                allParDecls.add(decl);

                AbsParDecls decls = ((AbsParDecls) node.subtree(1).accept(this, null));
                if (decls != null)
                    allParDecls.addAll(decls.parDecls());

                return new AbsParDecls(node.location(), allParDecls);
            }

            // ParDecl -> identifier : Type .
            case ParDecl: {
                String nameIDENTIFIER = ((DerLeaf) node.subtree(0)).symb.lexeme;
                AbsType type = ((AbsType) node.subtree(2).accept(this, null));
                return new AbsParDecl(node.location(), nameIDENTIFIER, type);
            }

            /*
            ParDeclsRest -> .
            ParDeclsRest -> , ParDecls .
             */
            case ParDeclsRest: {
                if (node.numSubtrees() == 0)
                    return new AbsParDecls(new Location(0, 0), new Vector<AbsParDecl>());
                return node.subtree(1).accept(this, null);
            }

            /*
            Type -> void | int | char | bool .
            Type -> arr [ RelExpr ] Type | ptr Type .
            Type -> identifier | ( Type ).
             */
            case Type:
                switch (((DerLeaf) node.subtree(0)).symb.token) {
                    case VOID: {
                        Location locationVOID = new Location(node, node);
                        return new AbsAtomType(locationVOID, AbsAtomType.Type.VOID);
                    }
                    case INT: {
                        Location locationINT = new Location(node, node);
                        return new AbsAtomType(locationINT, AbsAtomType.Type.INT);
                    }
                    case CHAR: {
                        Location locationChar = new Location(node, node);
                        return new AbsAtomType(locationChar, AbsAtomType.Type.CHAR);
                    }
                    case BOOL: {
                        Location locationBOOL = new Location(node, node);
                        return new AbsAtomType(locationBOOL, AbsAtomType.Type.BOOL);
                    }
                    case IDENTIFIER: {
                        Location locationIDENTIFIER = new Location(node, node);
                        String nameIDENTIFIER = ((DerLeaf) node.subtree(0)).symb.lexeme;
                        return new AbsTypName(locationIDENTIFIER, nameIDENTIFIER);
                    }
                    case PTR: {
                        Location locationPTR = new Location(node, node);
                        AbsType absType = ((AbsType) node.subtree(1).accept(this, null));
                        return new AbsPtrType(locationPTR, absType);
                    }
                    case ARR: {
                        Location locationARR = new Location(node, node);
                        AbsExpr absExpr = ((AbsExpr) node.subtree(2).accept(this, null));
                        AbsType absType = ((AbsType) node.subtree(4).accept(this, null));
                        return new AbsArrType(locationARR, absExpr, absType);
                    }
                    case LPARENTHESIS: {
                        return node.subtree(1).accept(this, null);

                    }

                }


                // Stmts -> Stmt StmtsRest .
            case Stmts: {
                Vector<AbsStmt> allStms = new Vector<AbsStmt>();
                AbsStmt absStmt = ((AbsStmt) node.subtree(0).accept(this, null));
                allStms.add(absStmt);
                AbsStmts absStmts = ((AbsStmts) node.subtree(1).accept(this, null));
                if (absStmts != null)
                    allStms.addAll(absStmts.stmts());
                return new AbsStmts(node.location(), allStms);
            }

            /*
            StmtsRest -> .
            StmtsRest -> ; Stmts.
             */
            case StmtsRest: {
                if (node.numSubtrees() == 0)
                    return new AbsStmts(new Location(0, 0), new Vector<AbsStmt>());
                return node.subtree(1).accept(this, null);
            }

            /*
            Stmt -> RelExpr AssignEps  .
            Stmt -> if RelExpr then Stmts ElseEps end  .
            Stmt -> while RelExpr do Stmts end  .
             */
            case Stmt: {
                if (node.numSubtrees() == 6) {
                    AbsExpr absExpr = ((AbsExpr) node.subtree(1).accept(this, null));
                    AbsStmts stmts = ((AbsStmts) node.subtree(3).accept(this, null));
                    AbsStmts elsestmts = ((AbsStmts) node.subtree(4).accept(this, null));
                    return new AbsIfStmt(node.location(), absExpr, stmts, elsestmts);
                } else if (node.numSubtrees() == 5) {
                    AbsExpr absExpr = ((AbsExpr) node.subtree(1).accept(this, null));
                    AbsStmts stmts = ((AbsStmts) node.subtree(3).accept(this, null));
                    return new AbsWhileStmt(node.location(), absExpr, stmts);
                } else if (node.numSubtrees() == 2) {
                    AbsExpr absExpr = ((AbsExpr) node.subtree(0).accept(this, null));
                    AbsExpr absExpr2 = ((AbsExpr) node.subtree(1).accept(this, null));
                    if (absExpr2 == null)
                        return new AbsExprStmt(node.location(), absExpr);
                    else
                        return new AbsAssignStmt(node.location(), absExpr, absExpr2);
                }
            }



            /*
            WhereEps -> { where Decls } .
            WhereEps -> .
             */
            case WhereEps: {
                if (node.numSubtrees() == 0)
                    return null;
                return node.subtree(2).accept(this, null);
            }

            /*
            AssignEps -> .
            AssignEps -> = RelExpr .
             */
            case AssignEps: {
                if (node.numSubtrees() == 0)
                    return visArg;
                return node.subtree(1).accept(this, null);
            }

            /*
            ElseEps -> .
            ElseEps -> else Stmts .
             */
            case ElseEps: {
                if (node.numSubtrees() == 0)
                    return new AbsStmts(new Location(0, 0), new Vector<AbsStmt>());
                return node.subtree(1).accept(this, null);
            }

            /*
            BodyEps -> .
            BodyEps ->  = Stmts : RelExpr WhereEps .
             */
            case BodyEps: {
                if (node.numSubtrees() == 0)
                    return new AbsDecls(new Location(0, 0), new Vector<AbsDecl>());

                AbsStmts absStmts = ((AbsStmts) node.subtree(1).accept(this, null));
                AbsExpr absExpr = ((AbsExpr) node.subtree(3).accept(this, null));
                AbsDecls absDecls = ((AbsDecls) node.subtree(4).accept(this, null));
                if (absDecls == null)
                    return new AbsBlockExpr(node.location(), new AbsDecls(node.location(), new Vector<AbsDecl>()), absStmts, absExpr);
                return new AbsBlockExpr(node.location(), absDecls, absStmts, absExpr);
            }

            // RelExpr -> AddExpr RelExprRest .
            case RelExpr: {
                AbsExpr expr = (AbsExpr) node.subtree(0).accept(this, null);
                return node.subtree(1).accept(this, expr);
            }

            /*
            RelExprRest -> .
            RelExprRest -> eql AddExpr | neq AddExpr | leq AddExpr .
            RelExprRest -> geq AddExpr | lth AddExpr | gth AddExpr .
             */
            case RelExprRest: {
                if (node.numSubtrees() == 0)
                    return visArg;
                AbsBinExpr.Oper oper = null;
                switch (((DerLeaf) node.subtree(0)).symb.lexeme) {
                    case "==":
                        oper = AbsBinExpr.Oper.EQU;
                        break;
                    case "!=":
                        oper = AbsBinExpr.Oper.NEQ;
                        break;
                    case "<=":
                        oper = AbsBinExpr.Oper.LEQ;
                        break;
                    case ">=":
                        oper = AbsBinExpr.Oper.GEQ;
                        break;
                    case "<":
                        oper = AbsBinExpr.Oper.LTH;
                        break;
                    case ">":
                        oper = AbsBinExpr.Oper.GTH;
                        break;

                }
                AbsExpr expr = (AbsExpr) node.subtree(1).accept(this, null);
                return new AbsBinExpr(node.location(), oper, (AbsExpr) visArg, expr);
                //return node.subtree(1).accept(this, binExpr);
            }

            // AddExpr -> MulExpr AddExprRest .
            case AddExpr: {
                AbsExpr expr = (AbsExpr) node.subtree(0).accept(this, null);
                return node.subtree(1).accept(this, expr);
            }

            /*
            AddExprRest -> .
            AddExprRest -> + MulExpr AddExprRest | - MulExpr AddExprRest.
             */
            case AddExprRest: {
                if (node.numSubtrees() == 0)
                    return visArg;
                AbsBinExpr.Oper oper = null;
                if (((DerLeaf) node.subtree(0)).symb.lexeme.equals("+"))
                    oper = AbsBinExpr.Oper.ADD;
                else if (((DerLeaf) node.subtree(0)).symb.lexeme.equals("-"))
                    oper = AbsBinExpr.Oper.SUB;

                AbsExpr expr = (AbsExpr) node.subtree(1).accept(this, null);
                AbsBinExpr binExpr = new AbsBinExpr(node.location(), oper, (AbsExpr) visArg, expr);
                return node.subtree(2).accept(this, binExpr);
                //return binExpr;
            }

            // MulExpr -> PrefExpr MulExprRest.
            case MulExpr: {
                AbsExpr expr = (AbsExpr) node.subtree(0).accept(this, null);
                return node.subtree(1).accept(this, expr);
            }

            /*
            MulExprRest -> .
            MulExprRest -> * PrefExpr MulExprRest | / PrefExpr MulExprRest | % PrefExpr MulExprRest .
             */
            case MulExprRest: {
                if (node.numSubtrees() == 0)
                    return visArg;
                AbsBinExpr.Oper oper = null;
                switch (((DerLeaf) node.subtree(0)).symb.lexeme) {
                    case "*":
                        oper = AbsBinExpr.Oper.MUL;
                        break;
                    case "/":
                        oper = AbsBinExpr.Oper.DIV;
                        break;
                    case "%":
                        oper = AbsBinExpr.Oper.MOD;
                        break;
                }
                AbsExpr expr = (AbsExpr) node.subtree(1).accept(this, null);
                AbsBinExpr binExpr = new AbsBinExpr(node.location(), oper, (AbsExpr) visArg, expr);
                //return binExpr;
                return node.subtree(2).accept(this, binExpr);
            }

            /*
            PrefExpr -> PstfExpr | + PrefExpr | - PrefExpr .
            PrefExpr -> @ PrefExpr | $ PrefExpr .
             */

            case PrefExpr: {
                if (node.numSubtrees() == 1) {
                    return node.subtree(0).accept(this, null);
                }
                if (node.numSubtrees() == 2) {
                    AbsUnExpr.Oper oper = null;
                    switch (((DerLeaf) node.subtree(0)).symb.token) {
                        case ADD:
                            oper = AbsUnExpr.Oper.ADD;
                            break;
                        case SUB:
                            oper = AbsUnExpr.Oper.SUB;
                            break;
                        case DATA:
                            oper = AbsUnExpr.Oper.DATA;
                            break;
                        case ADDR:
                            oper = AbsUnExpr.Oper.ADDR;
                            break;
                    }
                    AbsExpr expr = (AbsExpr) node.subtree(1).accept(this, null);
                    return new AbsUnExpr(node.location(), oper, expr);
                }
            }

            // PstfExpr -> Expr PstfExprRest .
            case PstfExpr: {
                AbsExpr expr = (AbsExpr) node.subtree(0).accept(this, null);
                return node.subtree(1).accept(this, expr);
            }

            /*
            PstfExprRest -> .
            PstfExprRest -> [ RelExpr ] PstfExprRest .
             */

            case PstfExprRest: {
                if (node.numSubtrees() == 0)
                    return visArg;
                AbsExpr expr = (AbsExpr) node.subtree(1).accept(this, null);
                return node.subtree(3).accept(this, new AbsArrExpr(node, (AbsExpr) visArg, expr));
//                AbsExpr absExpr = ((AbsExpr) node.subtree(1).accept(this, null));
//                AbsExpr absExpr2 = ((AbsExpr) node.subtree(3).accept(this, null));
//                if (absExpr2 == null)
//                    return new AbsArrExpr(node.location(), (AbsExpr) visArg, absExpr);
//                else {
//                    return node.subtree(3).accept(this, absExpr);
//                }
            }

            // Expr -> AtomExpr | ( RelExpr CastEps ) .
            case Expr: {
                if (node.numSubtrees() == 1) {
                    return node.subtree(0).accept(this, null);
                }
                AbsExpr absExpr = ((AbsExpr) node.subtree(1).accept(this, null));
                AbsExpr absExpr2 = ((AbsExpr) node.subtree(2).accept(this, null));
                if (absExpr2 == null)
                    //return new AbsExprStmt(node.location(), absExpr);
                    return node.subtree(1).accept(this, null);
                else {
                    return node.subtree(2).accept(this, absExpr);
                }
            }

            /*
            CastEps -> .
            CastEps -> : Type .
             */
            case CastEps: {
                if (node.numSubtrees() == 0)
                    return visArg;
                AbsType type = (AbsType) node.subtree(1).accept(this, null);
                return new AbsCastExpr(node.location(), (AbsExpr) visArg, type);
            }


            // AtomExpr -> voidconst | boolconst | charconst | intconst | ptrconst | new ( Type ) | del ( RelExpr ) | identifier CallEps .
            case AtomExpr: {
                switch (((DerLeaf) node.subtree(0)).symb.token) {
                    case VOIDCONST:
                        return new AbsAtomExpr(node.location(), AbsAtomExpr.Type.VOID, ((DerLeaf) node.subtree(0)).symb.lexeme);
                    case BOOLCONST:
                        return new AbsAtomExpr(node.location(), AbsAtomExpr.Type.BOOL, ((DerLeaf) node.subtree(0)).symb.lexeme);
                    case CHARCONST:
                        return new AbsAtomExpr(node.location(), AbsAtomExpr.Type.CHAR, ((DerLeaf) node.subtree(0)).symb.lexeme);
                    case INTCONST:
                        return new AbsAtomExpr(node.location(), AbsAtomExpr.Type.INT, ((DerLeaf) node.subtree(0)).symb.lexeme);
                    case PTRCONST:
                        return new AbsAtomExpr(node.location(), AbsAtomExpr.Type.PTR, ((DerLeaf) node.subtree(0)).symb.lexeme);
                    case NEW:
                        AbsType type = (AbsType) node.subtree(2).accept(this, null);
                        return new AbsNewExpr(node.location(), type);
                    case DEL:
                        AbsExpr expr = (AbsExpr) node.subtree(2).accept(this, null);
                        return new AbsDelExpr(node.location(), expr);
                    case IDENTIFIER:
                        String nameIDENTIFIER = ((DerLeaf) node.subtree(0)).symb.lexeme;
                        AbsArgs args = (AbsArgs) node.subtree(1).accept(this, null);
                        if (args == null)
                            return new AbsVarName(node.location(), nameIDENTIFIER);
                            //return new AbsFunName(node.location(), nameIDENTIFIER, new AbsArgs(new Location(0,0), new Vector<AbsExpr>()));
                        else
                            return new AbsFunName(node.location(), nameIDENTIFIER, args);

                }
            }

            /*
            CallEps -> .
            CallEps -> ( ArgsEps )
             */
            case CallEps: {
                if (node.numSubtrees() == 0)
                    return visArg;
                return node.subtree(1).accept(this, visArg);
            }

            /*
            ArgsEps -> .
            ArgsEps -> Args ArgsRest .
             */
            case ArgsEps: {
                if (node.numSubtrees() == 0)
                    return new AbsArgs(new Location(0, 0), new Vector<AbsExpr>());
                Vector<AbsExpr> allArgs = new Vector<AbsExpr>();
                AbsExpr absExpr = ((AbsExpr) node.subtree(0).accept(this, null));
                allArgs.add(absExpr);
                AbsArgs argsRest = (AbsArgs) node.subtree(1).accept(this, null);
                if (argsRest != null) {
                    allArgs.addAll(argsRest.args());
                }
                return new AbsArgs(node.location(), allArgs);
            }

            /*
            ArgsRest -> , Args ArgsRest .
            ArgsRest -> .
             */

            case ArgsRest: {
                if (node.numSubtrees() == 0)
                    return visArg;

                Vector<AbsExpr> allArgs = new Vector<AbsExpr>();
                AbsExpr absExpr = ((AbsExpr) node.subtree(1).accept(this, null));
                if (absExpr != null)
                    allArgs.add(absExpr);
                AbsArgs argsRest = (AbsArgs) node.subtree(2).accept(this, null);
                if (argsRest != null) {
                    allArgs.addAll(argsRest.args());
                }
                return new AbsArgs(node.location(), allArgs);

            }

            // Args -> RelExpr .
            case Args: {
                if (node.numSubtrees() == 0)
                    return visArg;
                return node.subtree(0).accept(this, null);
            }
        }
        return null;
    }
}

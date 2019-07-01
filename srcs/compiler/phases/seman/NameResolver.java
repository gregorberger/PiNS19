/**
 * @author sliva
 */
package compiler.phases.seman;

import compiler.common.report.*;
import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;

import java.util.Vector;

/**
 * Name resolving: the result is stored in {@link SemAn#declaredAt}.
 *
 * @author sliva
 */
public class NameResolver extends AbsFullVisitor<Object, Object> {

    /**
     * Symbol table.
     */
    private final SymbTable symbTable = new SymbTable();

    public Object visit(AbsSource source, Object visArg) {
        symbTable.newScope();
        source.decls.accept(this, visArg);
        symbTable.oldScope();
        return null;
    }


    public Object visit(AbsDecls decls, Object visArg) {
        for (AbsDecl decl : decls.decls()) {
            try {
                symbTable.ins(decl.name, decl);
            } catch (Exception e) {
                throw new Report.Error(decl.location(), "SemanticError: Name '" + decl.name + "' already declared");
            }
        }
        for (AbsDecl decl : decls.decls()) {
            symbTable.newScope();
            decl.accept(this, visArg);
            symbTable.oldScope();
        }
        return null;
    }

    public Object visit(AbsTypName typ, Object visArg) {
        try {
            symbTable.fnd(typ.name);
            SemAn.declaredAt.put(typ, symbTable.fnd(typ.name));
        } catch (Exception e) {
            throw new Report.Error(typ.location(), "SemanticError: Type '" + typ.name + "' is not declared.");
        }
        return null;
    }


    public Object visit(AbsFunName funName, Object visArg) {
        try {
            funName.args.accept(this, visArg);
            symbTable.fnd(funName.name);
            SemAn.declaredAt.put(funName, symbTable.fnd(funName.name));
        } catch (SymbTable.CannotFndNameException e) {
            throw new Report.Error(funName.location(), "SemanticError: Function '" + funName.name + "' is not declared.");
        }


        return null;
    }

    public Object visit(AbsVarName varName, Object visArg) {
        try {
            symbTable.fnd(varName.name);
            SemAn.declaredAt.put(varName, symbTable.fnd(varName.name));
        } catch (Exception e) {
            throw new Report.Error(varName.location(), "SemanticError: Name in function '" + varName.name + "' not declared");
        }

        return null;
    }


    public Object visit(AbsParDecls parDecls, Object visArg) {
        for (AbsParDecl parDecl : parDecls.parDecls()) {
            parDecl.accept(this, parDecl);
        }
        symbTable.newScope();
        for (AbsParDecl parDecl : parDecls.parDecls()) {
            try {
                symbTable.ins(parDecl.name, parDecl);
            } catch (Exception e) {
                throw new Report.Error(parDecl.location(), "SemanticError: Name in function '" + parDecl.name + "' already declared");
            }
        }
        symbTable.newScope();


        return null;
    }

    @Override
    public Object visit(AbsArrType arr, Object visArg){
        try {
                arr.elemType.accept(this, visArg);
                arr.len.accept(this, visArg);
        } catch (Exception e){
            throw new Report.InternalError();
        }
        return null;
    }


}

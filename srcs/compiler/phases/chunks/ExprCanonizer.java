/**
 * @author sliva
 */
package compiler.phases.chunks;

import java.util.*;
import compiler.data.layout.*;
import compiler.data.imcode.*;
import compiler.data.imcode.visitor.*;

/**
 * @author sliva
 */
public class ExprCanonizer implements ImcVisitor<ImcExpr, Vector<ImcStmt>> {
	
	public ImcExpr visit(ImcBINOP imcBinop, Vector<ImcStmt> stmts) {
		ImcExpr fstExpr = imcBinop.fstExpr.accept(this, stmts);
		Temp temp1 = new Temp();
		stmts.add(new ImcMOVE(new ImcTEMP(temp1), fstExpr));
		ImcExpr sndExpr = imcBinop.sndExpr.accept(this, stmts);
		Temp temp2 = new Temp();
		stmts.add(new ImcMOVE(new ImcTEMP(temp2), sndExpr));
		return new ImcBINOP(imcBinop.oper, new ImcTEMP(temp1), new ImcTEMP(temp2));
	}
	
	public ImcExpr visit(ImcCALL imcCall, Vector<ImcStmt> stmts) {
		Vector<ImcExpr> canonArgs = new Vector<ImcExpr>();
		for (ImcExpr arg: imcCall.args()) {
			ImcExpr canonArg = arg.accept(this, stmts);
			Temp temp = new Temp();
			stmts.add(new ImcMOVE(new ImcTEMP(temp), canonArg));
			canonArgs.add(new ImcTEMP(temp));
		}
		Temp temp = new Temp();
		stmts.add(new ImcMOVE(new ImcTEMP(temp), new ImcCALL(imcCall.label, canonArgs)));
		return new ImcTEMP(temp);
	}

	public ImcExpr visit(ImcCONST imcConst, Vector<ImcStmt> stmts) {
		return new ImcCONST(imcConst.value);
	}

	public ImcExpr visit(ImcMEM imcMem, Vector<ImcStmt> stmts) {
		ImcExpr addr = imcMem.addr.accept(this, stmts);
		return new ImcMEM(addr);
	}

	public ImcExpr visit(ImcNAME imcName, Vector<ImcStmt> stmts) {
		return new ImcNAME(imcName.label);
	}

	public ImcExpr visit(ImcSEXPR imcSExpr, Vector<ImcStmt> stmts) {
		stmts.addAll(imcSExpr.stmt.accept(new StmtCanonizer(), null));
		return imcSExpr.expr.accept(this, stmts);
	}
	
	public ImcExpr visit(ImcTEMP imcTemp, Vector<ImcStmt> stmts) {
		return new ImcTEMP(imcTemp.temp);
	}
	
	public ImcExpr visit(ImcUNOP imcUnop, Vector<ImcStmt> stmts) {
		ImcExpr subExpr = imcUnop.subExpr.accept(this, stmts);
		return new ImcUNOP(imcUnop.oper, subExpr);
	}

}

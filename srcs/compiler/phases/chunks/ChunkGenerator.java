/**
 * @author sliva
 */
package compiler.phases.chunks;

import java.util.*;
import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;
import compiler.data.layout.*;
import compiler.data.imcode.*;
import compiler.data.chunk.*;
import compiler.phases.frames.*;
import compiler.phases.imcgen.*;

/**
 * @author sliva
 *
 */
public class ChunkGenerator extends AbsFullVisitor<Object, Object> {

	public Object visit(AbsFunDef funDef, Object visArg) {
		funDef.value.accept(this, null);
		Frame frame = Frames.frames.get(funDef);

		Label entryLabel = new Label();
		Label exitLabel = new Label();
		Vector<ImcStmt> canonStmts = new Vector<ImcStmt>();

		canonStmts.add(new ImcLABEL(entryLabel));
		ImcExpr bodyExpr = ImcGen.exprImCode.get(funDef.value);
		ImcStmt bodyStmt = new ImcMOVE(new ImcTEMP(frame.RV), bodyExpr);
		canonStmts.addAll(bodyStmt.accept(new StmtCanonizer(), null));
		canonStmts.add(new ImcJUMP(exitLabel));
		Vector<ImcStmt> linearStmts = linearize(canonStmts);
		Chunks.codeChunks.add(new CodeChunk(frame, linearStmts, entryLabel, exitLabel));
		return null;
	}

	public Object visit(AbsVarDecl varDecl, Object visArg) {
		Access access = Frames.accesses.get(varDecl);
		if (access instanceof AbsAccess) {
			Chunks.dataChunks.add(new DataChunk((AbsAccess) access));
		}
		return null;
	}

	//

	private Vector<ImcStmt> linearize(Vector<ImcStmt> stmts) {
		Vector<ImcStmt> linearStmts = new Vector<ImcStmt>();
		for (int s = 0; s < stmts.size(); s++) {
			ImcStmt stmt = stmts.get(s);
			if (stmt instanceof ImcCJUMP) {
				ImcCJUMP imcCJump = (ImcCJUMP)stmt;
				Label negLabel = new Label();
				linearStmts.add(new ImcCJUMP(imcCJump.cond, imcCJump.posLabel, negLabel));
				linearStmts.add(new ImcLABEL(negLabel));
				linearStmts.add(new ImcJUMP(imcCJump.negLabel));
			}
			else
				linearStmts.add(stmt);
		}
		return linearStmts;
	}

}

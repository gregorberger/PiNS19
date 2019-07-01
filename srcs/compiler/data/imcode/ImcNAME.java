/**
 * @author sliva
 */
package compiler.data.imcode;

import compiler.data.layout.*;
import compiler.data.imcode.visitor.*;

public class ImcNAME extends ImcExpr {

	public final Label label;

	public ImcNAME(Label label) {
		this.label = label;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

}

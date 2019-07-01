/**
 * @author sliva
 */
package compiler.data.layout;

import compiler.common.logger.*;

/**
 * An access to a variable at a fixed address.
 * 
 * @author sliva
 */
public class AbsAccess extends Access {

	/** Label denoting a fixed address. */
	public final Label label;

	/**
	 * Constructs a new absolute access.
	 * 
	 * @param size  The size of a variable.
	 * @param label Offset of a variable at an absolute address.
	 */
	public AbsAccess(long size, Label label) {
		super(size);
		this.label = label;
	}

	@Override
	public void log(Logger logger) {
		if (logger == null)
			return;
		logger.begElement("access");
		logger.addAttribute("size", Long.toString(size));
		logger.addAttribute("label", label.name);
		logger.endElement();
	}

}

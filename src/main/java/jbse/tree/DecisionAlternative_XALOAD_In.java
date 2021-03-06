package jbse.tree;

import jbse.val.Expression;
import jbse.val.Reference;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * was inbounds.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternative_XALOAD_In 
extends DecisionAlternative_XALOAD implements DecisionAlternative_XYLOAD_GETX_Loads {
	private final boolean fresh;
	private final Reference arrayToWriteBack;
	
	protected DecisionAlternative_XALOAD_In(String branchId, Expression arrayAccessExpression, boolean fresh, Reference arrayToWriteBack, int branchNumber) {
		super(branchId, arrayAccessExpression, branchNumber);
		this.fresh = fresh;
		this.arrayToWriteBack = arrayToWriteBack;
	}
	
	public final boolean isValueFresh() {
		return this.fresh;
	}
	
	public final Reference getArrayToWriteBack() {
		return this.arrayToWriteBack;
	}
}

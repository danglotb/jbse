package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.className;
import static jbse.common.Type.INT;
import static jbse.common.Type.NULLREF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isPrimitiveOpStack;
import static jbse.common.Type.isReference;
import static jbse.common.Type.splitReturnValueDescriptor;

import java.util.function.Supplier;

import jbse.bc.exc.BadClassFileException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} for all the "return from method" bytecodes
 * ([i/l/f/d/a]return).
 * 
 * @author Pietro Braione
 *
 */
final class Algo_XRETURN extends Algorithm<
BytecodeData_0, 
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>,
StrategyRefine<DecisionAlternative_NONE>,
StrategyUpdate<DecisionAlternative_NONE>> {
    
    private final char returnType; //set by constructor
    private Value valueToReturn; //set by cooker
    private int pcReturn; //set by updater

    /**
     * Constructor.
     * 
     * @param returnType the type of the value that
     *        must be returned.
     */
    public Algo_XRETURN(char returnType) {
        this.returnType = returnType;
    }

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected Supplier<BytecodeData_0> bytecodeData() {
        return BytecodeData_0::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            this.valueToReturn = this.data.operand(0);
            final char valueType = this.valueToReturn.getType();
            if ((valueType != this.returnType) && !(valueType == NULLREF && this.returnType == REFERENCE)) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
            //TODO this code is duplicated in Algo_PUTX: refactor! 
            try {
                //checks/converts the type of the value to be returned
                final String destinationType = splitReturnValueDescriptor(state.getCurrentMethodSignature().getDescriptor());
                if (isPrimitive(destinationType)) {
                    final char destinationTypePrimitive = destinationType.charAt(0);
                    if (isPrimitiveOpStack(destinationTypePrimitive)) {
                        if (valueType != destinationTypePrimitive) {
                            throwVerifyError(state);
                            exitFromAlgorithm();
                        }
                    } else if (valueType == INT) {
                        try {
                            this.valueToReturn = ((Primitive) this.valueToReturn).narrow(destinationTypePrimitive);
                        } catch (InvalidTypeException e) {
                            //this should never happen
                            failExecution(e);
                        }
                    } else {
                        throwVerifyError(state);
                        exitFromAlgorithm();
                    }
                } else if (isReference(valueType)) {
                    final Reference refToReturn = (Reference) this.valueToReturn;
                    if (!state.isNull(refToReturn)) {
                        final String valueObjectType = state.getObject(refToReturn).getType();
                        if (!state.getClassHierarchy().isAssignmentCompatible(valueObjectType, className(destinationType))) {
                            throwVerifyError(state);
                            exitFromAlgorithm();
                        }
                    }
                } else if (valueType == NULLREF) {
                    //nothing to do
                } else { //field has reference type, value has primitive type
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }
            } catch (BadClassFileException | ThreadStackEmptyException e) {
                //this should not happen
                //TODO really?
                failExecution(e);
            }
            
            //since the value to return goes on the operand stack, it 
            //must be widened to int if it is a boolean, byte, char or short
            if (isPrimitive(this.valueToReturn.getType()) && !isPrimitiveOpStack(this.valueToReturn.getType())) {
                try {
                    this.valueToReturn = ((Primitive) this.valueToReturn).widen(INT);
                } catch (InvalidTypeException e) {
                    //this should never happen
                    failExecution(e);
                }
            }
        };        
    }

    @Override
    protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.popCurrentFrame();
            if (state.getStackSize() == 0) {
                state.setStuckReturn(this.valueToReturn);
            } else {
                state.pushOperand(this.valueToReturn);
                this.pcReturn = state.getReturnPC();
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> false;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> this.pcReturn;
    }
}

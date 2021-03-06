package jbse.algo;

import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ABSTRACT_METHOD_ERROR;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.exc.InvalidTypeException;

/**
 * Algorithm for completing the semantics of the 
 * invoke* bytecodes (invoke[interface/special/static/virtual]).
 *  
 * @author Pietro Braione
 */
final class Algo_INVOKEX_Completion extends Algo_INVOKEX_Abstract {
    public Algo_INVOKEX_Completion(boolean isInterface, boolean isSpecial, boolean isStatic) {
        super(isInterface, isSpecial, isStatic);
    }

    private boolean shouldFindImplementation; //set by methods

    public void setImplementation(ClassFile classFileMethodImpl, Signature methodSignatureImpl) {
        this.shouldFindImplementation = false;
        this.classFileMethodImpl = classFileMethodImpl;
        this.methodSignatureImpl = methodSignatureImpl;
        this.isSignaturePolymorphic = false;
    }

    public void shouldFindImplementation() {
        this.shouldFindImplementation = true;
    }

    private int pcOffsetReturn; //set by methods
    
    public void setProgramCounterOffset(int pcOffset) {
        this.pcOffsetReturn = pcOffset;
    }
    
    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            //performs method resolution again (this info is still necessary)
            try {
                resolveMethod(state);
            } catch (IncompatibleClassFileException | 
                     MethodNotFoundException | 
                     MethodNotAccessibleException | 
                     BadClassFileException e) {
                //this should never happen (Algo_INVOKEX already checked them)
                failExecution(e);
            }

            //since a method can be base-level overridden by a static method, 
            //we need to know whether the implementation is or is not static
            boolean isStaticImpl = false; //to keep the compiler happy
            
            if (this.shouldFindImplementation) {
                //if the method is looked up, the implementation is static 
                //if we are executing an invokestatic bytecode
                isStaticImpl = this.isStatic; 
                
                //looks for the method implementation with standard lookup
                try {
                    findImpl(state);
                } catch (MethodNotAccessibleException e) {
                    throwNew(state, ILLEGAL_ACCESS_ERROR);
                    exitFromAlgorithm();
                } catch (MethodAbstractException e) {
                    throwNew(state, ABSTRACT_METHOD_ERROR);
                    exitFromAlgorithm();
                } catch (IncompatibleClassFileException e) {
                    throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                    exitFromAlgorithm();
                } catch (BadClassFileException e) {
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }
            } else {
                //otherwise, the implementation's signature is already in this.methodSignatureImpl, 
                //and it could be static, so we need to check
                try {
                    isStaticImpl = this.classFileMethodImpl.isMethodStatic(this.methodSignatureImpl) ? true : this.isStatic;
                } catch (MethodNotFoundException e) {
                    //this should never happen 
                    failExecution(e);
                }
            }

            //checks that the method has an implementation
            try {
                if (this.classFileMethodImpl == null || this.classFileMethodImpl.isMethodAbstract(this.methodSignatureImpl)) {
                    //Algo_INVOKEX found a standard implementation, so this should never happen
                    failExecution("Unexpected missing method implementation");
                }
            } catch (MethodNotFoundException e) {
                //this should never happen after resolution 
                failExecution(e);
            }     

            //creates and initializes the class of the implementation method; this is necessary for
            //static base-level overriding methods; note that in the ordinary invokestatic case the
            //class of the method implementation is the class of the resolved method, so this just 
            //repeats what was already done in Algo_INVOKEX
            if (isStaticImpl) { 
                try {
                    ensureClassCreatedAndInitialized(state, this.methodSignatureImpl.getClassName(), this.ctx);
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (InvalidInputException | BadClassFileException e) {
                    //this should never happen after resolution 
                    failExecution(e);
                }
            }
            
            //if the method is native, fails (should have an 
            //overriding implementation)
            try {
                if (this.classFileMethodImpl.isMethodNative(this.methodSignatureImpl)) {
                    throw new NotYetImplementedException("method " + this.methodSignatureImpl + " is native and has no overriding implementation");
                }
            } catch (MethodNotFoundException e) {
                //this should never happen
                failExecution(e);
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
            try {
                state.pushFrame(this.methodSignatureImpl, false, this.pcOffsetReturn, this.data.operands());
            } catch (InvalidProgramCounterException | InvalidSlotException | InvalidTypeException e) {
                //TODO is it ok?
                throwVerifyError(state);
            } catch (NullMethodReceiverException | BadClassFileException | 
                     MethodNotFoundException | MethodCodeNotFoundException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> 0; //nothing to add to the program counter of the pushed frame
    }
}

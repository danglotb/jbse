package jbse.algo.meta;

import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#ensureClassInitialized(Class)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_ENSURECLASSINITIALIZED extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) 
    throws InterruptException, DecisionException, ClasspathException {
        final Reference ref = (Reference) this.data.operand(1);
        if (state.isNull(ref)) {
            throwNew(state, NULL_POINTER_EXCEPTION); //this is what Hotspot does
            exitFromAlgorithm();
        }
        final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(ref);
        if (clazz == null) {
            //this should never happen
            throw new UnexpectedInternalException("Unexpected unresolved symbolic reference as Class c parameter of sun.misc.Unsafe.ensureClassInitialized.");
        }
        final String className = clazz.representedClass();
        
        try {
            ensureClassCreatedAndInitialized(state, className, this.ctx);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (InvalidInputException | BadClassFileException e) {
            //this should never happen
            //TODO really?
            failExecution(e);
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //nothing to do
    }
}

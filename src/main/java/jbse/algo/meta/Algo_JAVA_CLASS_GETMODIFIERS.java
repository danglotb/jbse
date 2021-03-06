package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#isPrimitive()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETMODIFIERS extends Algo_INVOKEMETA_Nonbranching {
    private Simplex modifiers; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) throws InterruptException {
        try {
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject((Reference) this.data.operand(0));
            if (clazz == null) {
                //this should never happen
                failExecution("violated invariant (unexpected heap access with symbolic unresolved reference)");
            }
            final String className = clazz.representedClass();
            final ClassFile cf = (clazz.isPrimitive() ? 
                                  state.getClassHierarchy().getClassFilePrimitive(className) :
                                  state.getClassHierarchy().getClassFile(className));
            this.modifiers = state.getCalculator().valInt(cf.getModifiers());
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        state.pushOperand(this.modifiers);
    }
}

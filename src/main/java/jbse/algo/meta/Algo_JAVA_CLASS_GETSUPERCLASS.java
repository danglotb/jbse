package jbse.algo.meta;

import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link java.lang.Class#isPrimitive()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETSUPERCLASS extends Algo_INVOKEMETA_Nonbranching {
    private ReferenceConcrete refSuper; //set by cookMore

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
            final String superclassName = cf.getSuperclassName();
            if (superclassName == null) {
                this.refSuper = Null.getInstance();
            } else {
                ensureInstance_JAVA_CLASS(state, superclassName, superclassName, this.ctx);
                this.refSuper = state.referenceToInstance_JAVA_CLASS(superclassName);
            }
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException | ClassFileNotAccessibleException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        state.pushOperand(this.refSuper);
    }
}

package jbse.algo.meta;

import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_ACCESSIBLEOBJECT_OVERRIDE;
import static jbse.bc.Signatures.JAVA_FIELD;
import static jbse.bc.Signatures.JAVA_FIELD_ANNOTATIONS;
import static jbse.bc.Signatures.JAVA_FIELD_CLAZZ;
import static jbse.bc.Signatures.JAVA_FIELD_MODIFIERS;
import static jbse.bc.Signatures.JAVA_FIELD_NAME;
import static jbse.bc.Signatures.JAVA_FIELD_SIGNATURE;
import static jbse.bc.Signatures.JAVA_FIELD_SLOT;
import static jbse.bc.Signatures.JAVA_FIELD_TYPE;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.className;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.toPrimitiveCanonicalName;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.Class#getDeclaredFields0(boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETDECLAREDFIELDS0 extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile cf; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        try {           
            //gets the classfile represented by the "this" parameter
            final Reference classRef = (Reference) this.data.operand(0);
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(classRef); //TODO check that operand is concrete and not null
            final String className = clazz.representedClass();
            this.cf = (clazz.isPrimitive() ? 
                       state.getClassHierarchy().getClassFilePrimitive(className) :
                       state.getClassHierarchy().getClassFile(className));
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected void update(State state) 
    throws SymbolicValueNotAllowedException, ThreadStackEmptyException, InterruptException {
        //gets the signatures of the fields to emit; the position of the signature
        //in sigFields indicates its slot
        final boolean onlyPublic = ((Simplex) this.data.operand(1)).surelyTrue();
        final List<Signature> sigFields;
        try {
            sigFields = Arrays.stream(this.cf.getDeclaredFields())
            .map(sig -> {
                try {
                    if (onlyPublic && !this.cf.isFieldPublic(sig)) {
                        return null;
                    } else {
                        return sig;
                    }
                } catch (FieldNotFoundException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof FieldNotFoundException) {
                //this should never happen
                failExecution((Exception) e.getCause());
            }
            throw e;
        }

        final int numFields = sigFields.stream()
        .map(s -> (s == null ? 0 : 1))
        .reduce(0, (a, b) -> a + b);


        //builds the array to return
        ReferenceConcrete result = null; //to keep the compiler happy
        try {
            result = state.createArray(null, state.getCalculator().valInt(numFields), "" + ARRAYOF + REFERENCE + JAVA_FIELD + TYPEEND);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (InvalidTypeException e) {
            //this should never happen
            failExecution(e);
        }

        //constructs the java.lang.reflect.Field objects and fills the array
        final Reference classRef = (Reference) this.data.operand(0);
        final Array resultArray = (Array) state.getObject(result);
        final Calculator calc = state.getCalculator();
        int index = 0;
        int slot = 0;
        for (Signature sigField : sigFields) {
            if (sigField != null) {
                //creates an instance of java.lang.reflect.Field and 
                //puts it in the return array
                ReferenceConcrete fieldRef = null; //to keep the compiler happy
                try {
                    fieldRef = state.createInstance(JAVA_FIELD);
                    resultArray.setFast(calc.valInt(index) , fieldRef);
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (InvalidOperandException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
                    //this should never happen
                    failExecution(e);
                }

                //from here initializes the java.lang.reflect.Field instance
                final Instance field = (Instance) state.getObject(fieldRef);

                //sets clazz
                field.setFieldValue(JAVA_FIELD_CLAZZ, classRef);

                //sets name
                try {
                    state.ensureStringLiteral(sigField.getName());
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                }
                final ReferenceConcrete refSigName = state.referenceToStringLiteral(sigField.getName());
                field.setFieldValue(JAVA_FIELD_NAME, refSigName);

                //sets modifiers
                try {
                    field.setFieldValue(JAVA_FIELD_MODIFIERS, calc.valInt(this.cf.getFieldModifiers(sigField)));
                } catch (FieldNotFoundException e) {
                    //this should never happen
                    failExecution(e);
                }

                //sets signature
                try {
                    final String sigType = this.cf.getFieldGenericSignatureType(sigField);
                    final ReferenceConcrete refSigType;
                    if (sigType == null) {
                        refSigType = Null.getInstance();
                    } else {
                        state.ensureStringLiteral(sigType);
                        refSigType = state.referenceToStringLiteral(sigType);
                    }
                    field.setFieldValue(JAVA_FIELD_SIGNATURE, refSigType);
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (FieldNotFoundException e) {
                    //this should never happen
                    failExecution(e);
                }

                //sets slot
                field.setFieldValue(JAVA_FIELD_SLOT, calc.valInt(slot));

                //sets type
                final String fieldType = sigField.getDescriptor();
                ReferenceConcrete typeClassRef = null; //to keep the compiler happy
                if (isPrimitive(fieldType)) {
                    try {
                        final String fieldTypeNameCanonical = toPrimitiveCanonicalName(fieldType);
                        state.ensureInstance_JAVA_CLASS_primitive(fieldTypeNameCanonical);
                        typeClassRef = state.referenceToInstance_JAVA_CLASS_primitive(fieldTypeNameCanonical);
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    } catch (ClassFileNotFoundException e) {
                        //this should never happen
                        failExecution(e);
                    }
                } else {
                    final String fieldTypeClass = className(fieldType);
                    try {
                        ensureInstance_JAVA_CLASS(state, fieldTypeClass, fieldTypeClass, this.ctx);
                        typeClassRef = state.referenceToInstance_JAVA_CLASS(fieldTypeClass);
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    } catch (BadClassFileException e) {
                        //TODO is it ok?
                        throwVerifyError(state);
                        exitFromAlgorithm();
                    } catch (ClassFileNotAccessibleException e) {
                        //this should never happen
                        failExecution(e);
                    }
                }
                field.setFieldValue(JAVA_FIELD_TYPE, typeClassRef);

                //sets override
                field.setFieldValue(JAVA_ACCESSIBLEOBJECT_OVERRIDE, calc.valBoolean(false));

                //sets annotations
                try {
                    final byte[] annotations = this.cf.getFieldAnnotationsRaw(sigField);
                    final ReferenceConcrete annotationsRef = state.createArray(null, calc.valInt(annotations.length), "" + ARRAYOF + BYTE);
                    field.setFieldValue(JAVA_FIELD_ANNOTATIONS, annotationsRef);
                    final Array annotationsArray = (Array) state.getObject(annotationsRef);
                    for (int i = 0; i < annotations.length; ++i) {
                        annotationsArray.setFast(calc.valInt(i), calc.valByte(annotations[i]));
                    }
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (FieldNotFoundException | InvalidTypeException | 
                         InvalidOperandException | FastArrayAccessNotAllowedException e) {
                    //this should never happen
                    failExecution(e);
                }
                
                ++index;
            }
            ++slot;
        }


        //returns the array
        state.pushOperand(result);
    }
}

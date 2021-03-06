package jbse.bc;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodNotFoundException;

class ClassFileBoolean extends ClassFilePrimitive {	
    ClassFileBoolean() { super("boolean"); }	
}

class ClassFileByte extends ClassFilePrimitive {
    ClassFileByte() { super("byte"); }	
}

class ClassFileCharacter extends ClassFilePrimitive {	
    ClassFileCharacter() { super("char"); }	
}

class ClassFileShort extends ClassFilePrimitive {
    ClassFileShort() { super("short"); }	
}

class ClassFileInteger extends ClassFilePrimitive {
    ClassFileInteger() { super("int"); }	
}

class ClassFileLong extends ClassFilePrimitive {
    ClassFileLong() { super("long"); }	
}

class ClassFileFloat extends ClassFilePrimitive {
    ClassFileFloat() { super("float"); }	
}

class ClassFileDouble extends ClassFilePrimitive {
    ClassFileDouble() { super("double"); }	
}

class ClassFileVoid extends ClassFilePrimitive {
    ClassFileVoid() { super("void"); }	
}


/**
 * A {@link ClassFile} for the primitive classes.
 * 
 * @author Pietro Braione
 */
abstract class ClassFilePrimitive extends ClassFile {
    private static final String NO_CONSTANT_POOL = "Primitive classes have no constant pool.";

    private final String className;

    protected ClassFilePrimitive(String className) {
        this.className = className;
    }

    @Override
    byte[] getBinaryFileContent() {
        return null;
    }

    @Override
    public String getSourceFile() {
        return "";
    }

    @Override
    public String getPackageName() {
        return ""; //TODO is it ok?
    }

    @Override
    public String getClassName() {
        return this.className;
    }
    
    @Override
    public int getModifiers() {
        return getAccessFlags();
    }

    @Override
    public int getAccessFlags() {
        return Modifier.ABSTRACT | Modifier.FINAL | Modifier.PUBLIC; //see openjdk 8, hotspot source code, src/share/vm/prims/jvm.cpp function JVM_GetClassAccessFlags
    }

    @Override
    public boolean isArray() {
        return false;
    }
    
    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return true;
    }

    @Override
    public boolean isPublic() {
        return true;
    }
    
    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isPackage() {
        return false;
    }
    
    @Override
    public boolean isPrivate() {
        return false;
    }


    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isSuperInvoke() {
        return false; //no meaning since objects of primitive classes have no methods
    }

    @Override
    public boolean isLocal() {
        return false;
    }
    
    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public String getHostClass() {
        return null;
    }

    @Override
    public String classContainer() {
        return null;
    }

    @Override
    public Signature getEnclosingMethodOrConstructor() {
        return null;
    }
    
    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public int constantPoolSize() {
        return 1;  //empty constant pool
    }

    @Override
    public boolean hasMethodImplementation(Signature methodSignature) {
        return false;
    }

    @Override
    public boolean hasMethodDeclaration(Signature methodSignature) {
        return false;
    }

    @Override
    public boolean hasOneSignaturePolymorphicMethodDeclaration(String methodName) {
        return false;
    }

    @Override
    public boolean isMethodAbstract(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodStatic(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPublic(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodProtected(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPackage(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPrivate(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodNative(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodVarargs(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodSignaturePolymorphic(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public boolean isMethodCallerSensitive(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public String getMethodGenericSignatureType(Signature methodSignature) throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public int getMethodModifiers(Signature methodSignature) throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public byte[] getMethodAnnotationsRaw(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public Object[] getMethodAvailableAnnotations(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public String[] getMethodThrownExceptions(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public ExceptionTable getExceptionTable(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public LocalVariableTable getLocalVariableTable(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public LineNumberTable getLineNumberTable(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public ConstantPoolValue getValueFromConstantPool(int index)
    throws InvalidIndexException {
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }

    @Override
    public byte[] getMethodCodeBySignature(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean hasFieldDeclaration(Signature fieldSignature) {
        return false;
    }

    @Override
    public boolean isFieldFinal(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldPublic(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldProtected(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldPackage(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldPrivate(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldStatic(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean hasFieldConstantValue(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public int fieldConstantValueIndex(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public String getFieldGenericSignatureType(Signature fieldSignature) 
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public int getFieldModifiers(Signature fieldSignature) 
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public byte[] getFieldAnnotationsRaw(Signature fieldSignature) 
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public Signature[] getDeclaredFieldsNonStatic() {
        return new Signature[0];
    }

    @Override
    public Signature[] getDeclaredFieldsStatic() {
        return new Signature[0];
    }

    @Override
    public Signature[] getDeclaredFields() {
        return new Signature[0];
    }

    @Override
    public Signature getFieldSignature(int fieldRef)
    throws InvalidIndexException {
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }

    @Override
    public Signature[] getDeclaredConstructors() {
        return new Signature[0];
    }

    @Override
    public Signature[] getDeclaredMethods() {
        return new Signature[0];
    }

    @Override
    public Signature getMethodSignature(int methodRef)
    throws InvalidIndexException {
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }

    @Override
    public Signature getInterfaceMethodSignature(int methodRef)
    throws InvalidIndexException {
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }

    @Override
    public String getClassSignature(int classRef) 
    throws InvalidIndexException {
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }

    @Override
    public String getSuperclassName() {
        return null;
    }

    @Override
    public List<String> getSuperInterfaceNames() {
        return Collections.emptyList();
    }

    @Override
    public int getLocalVariableLength(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public int getCodeLength(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }
}

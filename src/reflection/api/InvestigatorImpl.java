package reflection.api;

import java.lang.reflect.*;
import java.util.*;

public class InvestigatorImpl implements Investigator {

    private Class objClass;
    private Object obj;

    public InvestigatorImpl(){}

    /**
     * loads in instance of a given class, which you will need to investigate through means of reflection
     * and supply information on...
     *
     * @param anInstanceOfSomething an instance of an unknown type...
     */
    @Override
    public void load(Object anInstanceOfSomething) {
        objClass = anInstanceOfSomething.getClass();
        obj = anInstanceOfSomething;
    }

    /**
     * returns the total number of the current class methods (not including the super class methods)
     * the total number of methods includes all sorts of modifiers: private, public, static, protected etc.
     *
     * @return total number of methods
     */
    @Override
    public int getTotalNumberOfMethods() {
        return objClass.getDeclaredMethods().length;
    }

    /**
     * returns the total number of constructors that this class has.
     * the total number of constructors includes all sorts of modifiers: private, public, protected, default etc.
     *
     * @return total number of constructors
     */
    @Override
    public int getTotalNumberOfConstructors() {
        return objClass.getDeclaredConstructors().length;
    }

    /**
     * returns the total number of fields that this class has.
     * the total number of fields DOES NOE include fields inherited from super class, if such exists
     * and includes all fields of any type (final, static etc.)
     *
     * @return the total number of fields
     */
    @Override
    public int getTotalNumberOfFields() {
        return objClass.getDeclaredFields().length;
    }

    private static String getClassBaseName(Class cls){
        String[] nameParts = cls.getName().split("\\.");
        return nameParts[nameParts.length -1];
    }

    /**
     * returns the names of all the interfaces that this class implements
     * Note that by name I mean simple, short Name that is only the interface name,
     * and not its fully qualified name including the package it lies within...
     *
     * @return set of interfaces names
     */
    @Override
    public Set<String> getAllImplementedInterfaces() {
        Set<String> interfacesNames = new HashSet<String>();
        Class[] intfs = objClass.getInterfaces();
        for (Class intf: intfs){
            interfacesNames.add(InvestigatorImpl.getClassBaseName(intf));
        }
        return interfacesNames;
    }

    /**
     * returns total number of constant fields (=final), of any type (private, public, static etc.)
     *
     * @return total number of constant fields
     */
    @Override
    public int getCountOfConstantFields() {
        int count =0;
        Field[] fields = objClass.getDeclaredFields();
        for (Field field: fields){
            if (Modifier.isFinal(field.getModifiers())){
                count++;
            }
        }
        return count;
    }

    /**
     * returns total number of static methods, of any type (private, public etc.)
     *
     * @return total number of static methods
     */
    @Override
    public int getCountOfStaticMethods() {
        int count =0;
        Method[] methods = objClass.getDeclaredMethods();
        for (Method method: methods){
            if (Modifier.isStatic(method.getModifiers())){
                count++;
            }
        }
        return count;
    }

    /**
     * checks if the given class extends a super class, that is not Object (which all extend by default, implicitly)
     *
     * @return true if the class extends, false otherwise
     */
    @Override
    public boolean isExtending() {
        return !objClass.getSuperclass().equals(Object.class);
    }

    /**
     * get the name of the parent class in case this class extends other class
     * the name of the class is the short, simple name and not the fully qualified name
     *
     * @return the name of the parent class. null if this class doesn't extend other class
     */
    @Override
    public String getParentClassSimpleName() {
        if (!isExtending()) return null;
        return InvestigatorImpl.getClassBaseName(objClass.getSuperclass());
    }

    /**
     * checks if the given class parent is of type abstract
     *
     * @return true if the parent class is abstract, false otherwise (also in case the given class does not extends any other class)
     */
    @Override
    public boolean isParentClassAbstract() {
        if (!isExtending()) return false;
        return Modifier.isAbstract(objClass.getSuperclass().getModifiers());
    }


    private static List<Field> getAllFields(Class cls) {
        List<Field> fields = new ArrayList<Field>();
        for (Class c = cls; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    /**
     * get all the names of all fields, including the ones coming from the inheritance chain
     * all fields of any type should exists: private, public, protected, static etc.
     *
     * @return set of fields names
     */
    @Override
    public Set<String> getNamesOfAllFieldsIncludingInheritanceChain() {
        Set<String> returnSet = new HashSet<String>();
        for (Field field: InvestigatorImpl.getAllFields(objClass)){
            returnSet.add(field.getName());
        }
        return returnSet;
    }

    /**
     * invokes a method that returns an int value, on the given instance.
     * the method to invoke will be given by its name only
     *
     * @param methodName the name of the method to invoke
     * @param args       the arguments to pass to the method, if such exists
     * @return the result returned from the method invocation
     */
    @Override
    public int invokeMethodThatReturnsInt(String methodName, Object... args) {
        ArrayList<Class> paramsClasses = new ArrayList<Class>();
        for (Object arg : args){
            paramsClasses.add(arg.getClass());
        }
        Object invokedReturnValue = null;
        Class[] parameterTypes = paramsClasses.toArray(new Class[paramsClasses.size()]);

        try {
            Method method = objClass.getDeclaredMethod(methodName, parameterTypes);
            invokedReturnValue = method.invoke(obj, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
        }
        return (int) invokedReturnValue;
    }

    /**
     * creates a new instance of the given class, using a specific constructor,
     * to be determined by the number of given arguments.
     * No worries: there will be no ambiguity, i.e. two constructors that gets in 2 arguments each on of them...
     * This method should act as a standalone and without any side effects, that is the newly created
     * instance should not replace the given instance.
     *
     * @param numberOfArgs number of arguments that a specific constructor has. can be 0.
     * @param args         arguments to pass to the constructor
     * @return the newly created instance
     */
    @Override
    public Object createInstance(int numberOfArgs, Object... args) {
        Constructor[] constructors = objClass.getConstructors();
        Constructor chosenConstructor = null;
        for(Constructor ctor: constructors){
            if (ctor.getParameterCount() == numberOfArgs){
                chosenConstructor = ctor;
            }
        }

        Object newInstance = null;
        try {
            newInstance = chosenConstructor.newInstance(args);
        } catch(InstantiationException | IllegalAccessException | InvocationTargetException e){
        }

        return newInstance;
    }

    /**
     * changes access control of a method and invokes it (== invokes a private method...)
     *
     * @param name            name of the method to change its access modifier
     * @param parametersTypes the types of parameters the method accepts
     * @param args            arguments to pass to the method invocation (once elevated)
     * @return the result from the method invocation
     */
    @Override
    public Object elevateMethodAndInvoke(String name, Class<?>[] parametersTypes, Object... args) {
        Object r = null;
        try{
            Method method = objClass.getDeclaredMethod(name, parametersTypes);
            method.setAccessible(true);
            r = method.invoke(obj, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
        }
        return r;
    }

    public static List<Class> getAllSuperClasses(Class cls) {
        List<Class> classes = new ArrayList<Class>();
        for (Class c = cls; c != null; c = c.getSuperclass()) {
            classes.add(c);
        }
        return classes;
    }


    /**
     * explores the inheritance chain of the object. returns a string representing the list of
     * parents this class has, starting from Object, and ending at the class name.
     * all classes names in the list should appear in their simple short name
     *
     * @param delimiter a string to put between each two classes in the chain (e.g. "->")
     * @return a string denotes the inheritance chain.
     * It can look like this (given a decimeter of "->"): Object-><class name>->...-><class name>
     */
    @Override
    public String getInheritanceChain(String delimiter) {
        List<Class> superClassesList = InvestigatorImpl.getAllSuperClasses(objClass);
        Collections.reverse(superClassesList);
        ArrayList<String> superClassesNamesList = new ArrayList<String>();
        for (Class cls: superClassesList){
            superClassesNamesList.add(InvestigatorImpl.getClassBaseName(cls));
        }
        return "".join(delimiter, superClassesNamesList);
    }
}

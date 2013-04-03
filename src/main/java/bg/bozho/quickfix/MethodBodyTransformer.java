package bg.bozho.quickfix;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class MethodBodyTransformer implements ClassFileTransformer {

    public static Map<String, Map<String, CtMethod>> replacementMethods = new HashMap<String, Map<String, CtMethod>>();

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return replaceMethod(className);
    }

    public static byte[] replaceMethod(String className) {
        try {
            String fqn = className.replace('/', '.');
            Map<String, CtMethod> methods = replacementMethods.get(fqn);
            if (methods == null) {
                return null;
            }
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.get(fqn);

            for (String methodName : methods.keySet()) {
                CtMethod m = cc.getDeclaredMethod(methodName);
                m.setBody(methods.get(methodName), null);
            }
            return cc.toBytecode();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setupMethodRedefinition(Class<?> clazz) {
        try {
            Method[] methods = clazz.getDeclaredMethods();
            ClassPool pool = ClassPool.getDefault();
            for (Method method : methods) {
                ReplaceMethod annotation = method.getAnnotation(ReplaceMethod.class);
                if (annotation == null) {
                    continue;
                }
                if (annotation.targetClassName().isEmpty() && annotation.targetClass() == Object.class) {
                    throw new IllegalStateException("Either targetClass or targetClassName must be defined for @ReplaceMethod");
                }

                String targetClassName = annotation.targetClassName();
                if (targetClassName.isEmpty()) {
                    targetClassName = annotation.targetClass().getName();
                }
                Map<String, CtMethod> methodMap = replacementMethods.get(targetClassName);
                if (methodMap == null) {
                    methodMap = new HashMap<String, CtMethod>();
                    replacementMethods.put(targetClassName, methodMap);
                }

                CtClass cc = pool.get(clazz.getName());
                CtMethod[] declaredMethods = cc.getDeclaredMethods();
                for (CtMethod declared : declaredMethods) {
                    if (declared.getName().equals(annotation.methodName())
                            && (annotation.argumentTypes().length == 0
                            || parameterTypesMatch(declared.getParameterTypes(), annotation.argumentTypes()))) {
                        methodMap.put(annotation.methodName(), declared);
                    }
                }
            }
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean parameterTypesMatch(CtClass[] parameterTypes, Class<?>[] argumentTypes) {
        if (parameterTypes.length != argumentTypes.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i ++) {
            if (!parameterTypes[i].getName().equals(argumentTypes[i].getName())) {
                return false;
            }
        }
        return true;
    }
}

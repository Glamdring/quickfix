package bg.bozho.quickfix;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReplaceMethod {
    String targetClassName() default "";
    Class<?> targetClass() default Object.class;
    String methodName();
    Class<?>[] argumentTypes() default {};
}

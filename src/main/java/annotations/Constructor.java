package annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface Constructor {
    String name() default "";
    String type() default "";

}

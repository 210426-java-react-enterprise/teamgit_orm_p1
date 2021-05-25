package annotations;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.annotation.*;

@Deprecated
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Connection {
    String url();
    String username();
    String password();
}

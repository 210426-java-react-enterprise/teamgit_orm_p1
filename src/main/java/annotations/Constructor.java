package annotations;

public @interface Constructor {
    String name() default "";
    String type() default "";

}

package net.william278.huskhomes.hook;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface PluginHook {

    @NotNull
    String name();

    @NotNull
    Register register();

    enum Register {
        ON_LOAD,
        ON_ENABLE
    }

}

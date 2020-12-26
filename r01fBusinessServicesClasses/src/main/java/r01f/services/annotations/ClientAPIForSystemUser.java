package r01f.services.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;



/**
 * see {@link r01f.model.security.context.SecurityContextProviderForSystemUserBase}
 */
@Qualifier //@BindingAnnotation						
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
public @interface ClientAPIForSystemUser {
	/* nothing to do */
}
 
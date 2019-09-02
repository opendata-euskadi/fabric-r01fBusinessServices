package r01f.services.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import r01f.securitycontext.SecurityContextProviderForMasterUserBase;



/**
 * see {@link SecurityContextProviderForMasterUserBase}
 */
@Qualifier //@BindingAnnotation						
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
public @interface ClientAPIForMasterUser {
	/* nothing to do */
}
 
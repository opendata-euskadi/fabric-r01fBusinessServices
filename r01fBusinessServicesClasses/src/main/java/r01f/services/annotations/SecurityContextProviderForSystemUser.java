package r01f.services.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import r01f.securitycontext.SecurityContextProviderForSystemUserBase;



/**
 * see {@link SecurityContextProviderForSystemUserBase}
 */
@Qualifier //@BindingAnnotation			
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
public @interface SecurityContextProviderForSystemUser {
	/* nothing to do */
}
 
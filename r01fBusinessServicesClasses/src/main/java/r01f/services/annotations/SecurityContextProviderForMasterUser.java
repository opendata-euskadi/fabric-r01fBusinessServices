package r01f.services.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * see {@link r01f.securitycontext.SecurityContextProviderForSystemUserBase}
 */
@Deprecated 	// use SecurityContextProviderForSystemUser
@Qualifier //@BindingAnnotation			
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
public @interface SecurityContextProviderForMasterUser {
	/* nothing to do */
}
 
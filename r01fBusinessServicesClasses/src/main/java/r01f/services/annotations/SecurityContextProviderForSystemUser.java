package r01f.services.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import r01f.securitycontext.SecurityContext;
import r01f.securitycontext.SecurityContextProviderForSystemUserBase;



/**
 * see {@link SecurityContextProviderForSystemUserBase}
 * 
 * Usually the {@link SecurityContext} is defined like (see base type for more details):
 * <pre class='brush:java'>
 *		@Provides @SecurityContextProviderForSystemUser
 *		SecurityContext _provideSystemUserContext() {
 *			SecurityContextAuthenticatedActor actor = SecurityContextAuthenticatedActor.forSystemUserLogin();
 *			return new SecurityContextBase(actor) {
 *						private static final long serialVersionUID = -3900360139907081976L;
 *		    	   };
 *		}
 * </pre>
 */
@Qualifier //@BindingAnnotation			
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
public @interface SecurityContextProviderForSystemUser {
	/* nothing to do */
}
 
package r01f.services.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;



/**
 * This annotation this annotation should not be used except in special cases,
 *  for example in spring when two providers are created ,BOTH NEEDED... one for system and ANOTHERone for user
 *  to  avoid  "org.springframework.beans.factory.NoUniqueBeanDefinitionException:
 *               No qualifying bean of type 'r01f.securitycontext.SecurityContext' available: expected single matching bean but found 2:
 *                 _provideSystemSecurityContext,_provideUserSecurityContext"
 * Use this way:
 *  @Bean @SecurityContextProviderForSystemUser
	@Inject
	public SecurityContext _provideSystemSecurityContext(final ServiceBootstrapSpringHandler servicesBootstrap) {
		SecurityContext outCtx = servicesBootstrap.getInjector()
												  .getProvider(Key.get(SecurityContext.class,
														  			   SecurityContextProviderForSystemUser.class))
												  .get();
		return outCtx;
	}
	@Bean @SecurityContextProviderForLoggedUser
	@Inject
	public SecurityContext _provideUserSecurityContext(final ServiceBootstrapSpringHandler servicesBootstrap) {
		SecurityContext outCtx = servicesBootstrap.getInjector()
												  .getProvider(SecurityContext.class)
												  .get();
		return outCtx;
	}
 +
 * </pre>
 */
@Qualifier //@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
public @interface SecurityContextProviderForLoggedUser {
	/* nothing to do */
}

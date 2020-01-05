package r01f.bootstrap;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.util.types.collections.CollectionUtils;

/**
 * JerseyServletGuiceModuleBase is the equivalent for: JerseyServletModule
 * Consider the need of use it ! : 
 *   - For REST Resources with JERSEY2 , it's  enougth to use: ResourceConfig  link @RESTResourceConfigBootstrapBase into Project : r01fBusinessRESTServicesCoreGuiceHK2Classes
 *   - For Servlets, it's  enougth to use directly guice-servlet from r01fBusinessWebServicesCoreGuiceClasses
 *   .. but Don't use GuiceServletContextListener to create Injector ( this was used to create Injector in Jersey 1) !! :
 *      ............ The injector must be created at ResourceConfig :
 *        @Inject
		  public GuiceResourceConfig(final ServiceLocator serviceLocator) {
			    register(UserServiceREST.class);
			    register(UserModelObjectResponseTypeMapper.class);
				register(new ContainerLifecycleListener() {		
			
				Injector injector = Guice.createInjector(new JerseyBootstrapModule());
				initGuiceIntoHK2Bridge(serviceLocator,injector);
		}
 *      
 *   .. see : https://stackoverflow.com/questions/59358816/rest-services-jersey-2-guice-hk-and-message-body-writers-and-readers
 */
@Slf4j
@RequiredArgsConstructor
@Deprecated
public abstract class JerseyServletGuiceModuleBase
	          extends ServletModule {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Class<? extends ResourceConfig> _resourceConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureServlets() {
		// Route all requests through GuiceContainer
		// IMPORTANT!
		//		If this property is NOT defined, GUICE will try to crate REST resource instances
		//		for every @Path annotated types defined at the injector
		Map<String,String> params = new HashMap<String,String>();
		params.put("javax.ws.rs.Application",
				   _resourceConfig.getCanonicalName());
//		params.put("jersey.config.disableMetainfServicesLookup","1");

		Map<String,String> moreParams = this.getJerseryInitParams();
		if (CollectionUtils.hasData(moreParams)) params.putAll(moreParams);;
        bind(ServletContainer.class).in(Scopes.SINGLETON);
		serve("/*").with(ServletContainer.class,
						 params);
		log.info(" Application: javax.ws.rs.Application={}",_resourceConfig.getName());
	}
	protected Map<String,String> getJerseryInitParams() {
		return null;
	}
	
	/* Sample:
	protected void configureServletsSample() {
		
		bind(UserDao.class).to(InMemoryUserDao.class);
		bind(InMemoryUserDatabase.class).in(Scopes.SINGLETON);
		
		bind(WelcomeTexter.class);
		bind(HelloWorldServlet.class).in(Scopes.SINGLETON);
		serve("/hello").with(HelloWorldServlet.class);
		
		Map<String, String> params = new HashMap<>();
		params.put("javax.ws.rs.Application", GuiceResourceConfig.class.getCanonicalName()); // REST Resources registeres inside
		
		bind(ServletContainer.class).in(Scopes.SINGLETON);
		serve("/app/*").with(ServletContainer.class,params);
	}*/
}

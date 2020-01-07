package r01f.bootstrap.rest;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import lombok.extern.slf4j.Slf4j;
import r01f.bootstrap.ResourceConfigBootstrap;
import r01f.bootstrap.ResourceConfigBootstrapGuiceHK2Delegate;
import r01f.bootstrap.services.ServicesBootstrapUtil;
import r01f.rest.RESTResourceConfigBase;


/**
 * Rest Resource referenced at {@link org.glassfish.jersey.server.ResourceConfig} (Jersey 2 )
 * ... implemented extending {@link RESTResourceConfigBase }                                  
 * in order to bootstrap dependencies from Guice to HK2  :
 * <pre> *   
 * 		   Jersey  1 vs Jersey 2
 *         ----------------------------------------------------------------------------------------------------
 *         Jersey 1 :
 *          	Container
					\-> GuiceFilter (Guice: initialize request scope)
					  \-> GuiceServletContextListener (Guice: initialize the Guice injector)
					    \-> JerseyServletModule (Jersey: bind Jersey Rest Resources types to Guice module)
					      \-> GuiceContainer (Jersey: redirect incoming requests to mapped resource classes)
            Jersey 2 :
	            	Container
						\-> ResourceConfig (r01f : see [@RESTResourceConfigBootstrapBase ) : 
									1. Register REST Resources,  TypeMappers, ExceptionMappers, etc..
									2. Initialize Guice Injector.
									3. Initialize Bridge
									4. Register a ContainerLifecycleListener() to start/stop  guice services (jpa, etc..)
						      \-> ServletContainer  (Jersey 2: redirect incoming requests to mapped resource classes)
		  Notes : 
		   		- ResourceConfig extending RESTResourceConfigBootstrapBase should be registered into web.xml: 
							
							<filter> 
						        <filter-name>JerseyApplication</filter-name>
						        <filter-class>org.glassfish.jersey.servlet.ServletContainer</filter-class>
						        <init-param>
						            <param-name>javax.ws.rs.Application</param-name>
						            <param-value>ResourceConfig Class extending RESTResourceConfigBootstrapBase</param-value>
						        </init-param>
						   </filter>							
							<filter-mapping>
						        <filter-name>JerseyApplication</filter-name>
						        <url-pattern>/*</url-pattern>
						   </filter-mapping>
						   
		   		- JerseyServletModule(and GuiceFilter) can be used [ see  Jersey2 link [ @, but don't use GuiceServletContextListener ( this was used to create Injector in Jersey 1)	   		
 * </pre>
 */
@Slf4j
@Singleton
public abstract class RESTResourceConfigBootstrapBase
      extends RESTResourceConfigBase 
    implements ResourceConfigBootstrap<ResourceConfig>, 
               RESTResourceConfigBootstrap {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
     private ResourceConfigBootstrapGuiceHK2Delegate  _resourceConfigBootstrapDelegate;  // Delegate Guice HK2<> Bridge
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR !! ServiceLocator must be Injected!!
/////////////////////////////////////////////////////////////////////////////////////////
    public RESTResourceConfigBootstrapBase(final ServiceLocator serviceLocator) {       
    	 super();
        _resourceConfigBootstrapDelegate = new ResourceConfigBootstrapGuiceHK2Delegate(serviceLocator,
        		                                                                       buildResourceConfigBootstrapData()); // bootstrapGuiceInjector //Inherited ResourceConfigBootstrap<ResourceConfig> 
          // After ResourceConfigBootstrapGuiceHK2Delegate bridge, create a ContainerLifecycleListener to Start Guice Services.Do this order.
         register( new  ContainerLifecycleListener() {
										@Override
										public void onStartup(Container container) {									
											// Init JPA's Persistence Service, Lucene indexes , Hazelcast , etc..and everything that has to be started
											// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
											 log.warn("################################################################"
										    		+ "######################  ContainerLifecycleListener onStartup (ServicesBootstrapUtil.startServices )");
											ServicesBootstrapUtil.startServices(_resourceConfigBootstrapDelegate.getInjector());
										}
							
										@Override
										public void onReload(Container container) {
											// On Reload.										
										}
							
										@Override
										public void onShutdown(Container container) {
										    log.warn("################################################################"
										    		+ "######################  ContainerLifecycleListener onShutdown (ServicesBootstrapUtil.stopServices ) ");
											ServicesBootstrapUtil.stopServices(_resourceConfigBootstrapDelegate.getInjector());
											
										}});
     
      
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS TO IMPLEMENT
/////////////////////////////////////////////////////////////////////////////////////////
}
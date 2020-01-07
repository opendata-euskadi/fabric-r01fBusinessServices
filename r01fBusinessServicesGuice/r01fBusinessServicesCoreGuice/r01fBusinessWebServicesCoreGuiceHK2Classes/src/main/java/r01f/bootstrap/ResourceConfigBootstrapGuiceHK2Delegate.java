package r01f.bootstrap;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.bootstrap.services.ServicesBootstrapUtil;


/**
 * ResourceConfigBootstrapGuiceHK2Delegate is invoked into {RESTResourceConfigBootstrapBase}
 * to provide a bridge from Guice to HK2 See {@link https://javaee.github.io/hk2/guice-bridge }
 */
@Accessors(prefix="_")
public class ResourceConfigBootstrapGuiceHK2Delegate {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
     @Getter protected final ServiceLocator _serviceLocator;
     @Getter protected final Injector _injector;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR 
/////////////////////////////////////////////////////////////////////////////////////////
    public ResourceConfigBootstrapGuiceHK2Delegate(final ServiceLocator serviceLocator, 
    		                                       final ResourceConfigBootstrapData bootstrapData) {  
        //1. HK2 Service Locator 
    	_serviceLocator = serviceLocator;
    	//2. Guice InJector
         _injector =  _createInjector(bootstrapData);
         //2. Init Guice Into HK2 Brige
        _initGuiceIntoHK2Bridge(serviceLocator, _injector);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE METHODS
/////////////////////////////////////////////////////////////////////////////////////////
    private void _initGuiceIntoHK2Bridge(final ServiceLocator serviceLocator, final Injector injector) {
    	GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(injector);
       
    } 
	protected Injector _createInjector(final ResourceConfigBootstrapData bootstrapData) {
		return Guice.createInjector(ServicesBootstrapUtil.getBootstrapGuiceModules(bootstrapData.getServicesBootstrapConfig())
			 					                          .withCommonEventsExecutor(bootstrapData.getCommonEventsConfig())
														  .withCommonBindingModules(bootstrapData.getCommonGuiceModules()));
		
	}
}

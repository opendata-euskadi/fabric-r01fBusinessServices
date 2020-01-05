package r01f.bootstrap;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import com.google.inject.Injector;
import lombok.Getter;
import lombok.experimental.Accessors;


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
//  CONSTRUCTOR !! ServiceLocator must be Injected!!
/////////////////////////////////////////////////////////////////////////////////////////
    public ResourceConfigBootstrapGuiceHK2Delegate(final ServiceLocator serviceLocator, final Injector injector) {  
         _serviceLocator = serviceLocator;
         _injector =  injector;
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
}

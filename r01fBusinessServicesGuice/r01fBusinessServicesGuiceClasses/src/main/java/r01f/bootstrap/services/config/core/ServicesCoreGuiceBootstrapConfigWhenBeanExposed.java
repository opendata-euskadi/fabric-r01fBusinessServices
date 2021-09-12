package r01f.bootstrap.services.config.core;

import java.util.Collection;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.bootstrap.services.config.ServicesBootstrapConfigBuilder;
import r01f.bootstrap.services.core.BeanImplementedServicesCoreBootstrapGuiceModuleBase;
import r01f.services.core.CoreService;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;

/**
 * @see ServicesBootstrapConfigBuilder
 */
@Accessors(prefix="_")
public class ServicesCoreGuiceBootstrapConfigWhenBeanExposed
	 extends ServicesCoreGuiceBootstrapConfigBase 
  implements ServicesCoreBootstrapConfigWhenBeanExposed {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The packages where the service interfaces BEAN implementantions can be found
	 * BEWARE!
	 * org.reflections is used to scan subtypes of CoreService. This library requires
	 * ALL the packages in the type hierarchy to be given to the scan methods:
	 * <pre class='brush:java'>
	 * 		CoreService
	 * 			|-- interface 1
	 * 					|--  interface 2
	 * 							|-- all the core service impl
	 * </pre> 
	 * The packages where CoreService, interface 1 and interface 2 resides MUST be handed 
	 * to the subtypeOfScan method of org.reflections
	 */
	@Getter private final Collection<Class<? extends CoreService>> _coreServiceImplBaseTypes;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesCoreGuiceBootstrapConfigWhenBeanExposed(final CoreAppCode coreAppCode,final CoreModule coreModule,
														   final Class<? extends BeanImplementedServicesCoreBootstrapGuiceModuleBase> coreBootstrapGuiceModule,
												   	       final Collection<ServicesCoreSubModuleBootstrapConfig<?>> subModulesCfgs,
												   	       final Collection<Class<? extends CoreService>> coreServicesBaseTypes,
												   	       final boolean isolate) {
		super(coreAppCode,coreModule,
			  coreBootstrapGuiceModule,
			  subModulesCfgs,
			  isolate);
		_coreServiceImplBaseTypes = coreServicesBaseTypes;
	}
	public ServicesCoreGuiceBootstrapConfigWhenBeanExposed(final CoreAppCode coreAppCode,final CoreModule coreModule,
														   final Class<? extends BeanImplementedServicesCoreBootstrapGuiceModuleBase> coreBootstrapGuiceModule,
												      	   final Collection<ServicesCoreSubModuleBootstrapConfig<?>> subModulesCfgs,
												      	   final Collection<Class<? extends CoreService>> coreServicesBaseTypes) {
		this(coreAppCode,coreModule,
			 coreBootstrapGuiceModule,
			 subModulesCfgs,
			 coreServicesBaseTypes,
			 true);		// usually bean core modules contains DB modules (jpa) that MUST be binded in a private module
	}
}

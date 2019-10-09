package r01f.bootstrap;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

import lombok.extern.slf4j.Slf4j;
import r01f.bootstrap.services.ServicesBootstrapUtil;
import r01f.bootstrap.services.config.ServicesBootstrapConfig;
import r01f.bootstrap.services.config.core.ServicesCoreModuleEventsConfig;
import r01f.model.annotations.ModelObjectsMarshaller;
import r01f.objectstreamer.Marshaller;
import r01f.reflection.ReflectionUtils;
import r01f.services.interfaces.ExposedServiceInterface;
import r01f.services.interfaces.ServiceInterface;
import r01f.servlet.spring.SpringRootConfigBootstrapBase;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;


@Slf4j
@Configuration
public abstract class SpringRootConfigBootstrapGuiceBase
              extends  SpringRootConfigBootstrapBase  {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Collection<ServicesBootstrapConfig> _servicesBootstrapConfig;
	private final Collection<Module> _commonGuiceModules;
	private final ServicesCoreModuleEventsConfig _commonEventsConfig;

/////////////////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected SpringRootConfigBootstrapGuiceBase(final ServicesBootstrapConfig... bootstrapCfgs) {
		this(Lists.newArrayList(bootstrapCfgs),
			 (Collection<Module>)null);		// no commong guice modules
	}
	protected SpringRootConfigBootstrapGuiceBase(final ServicesBootstrapConfig bootstrapCfg,
										         final Module... commonModules) {
		this(Lists.newArrayList(bootstrapCfg),
			 CollectionUtils.hasData(commonModules) ? Lists.<Module>newArrayList(commonModules) : Lists.<Module>newArrayList());
	}
	protected SpringRootConfigBootstrapGuiceBase(final Collection<ServicesBootstrapConfig> bootstrapCfgs,
										         final ServicesCoreModuleEventsConfig buildCommonModuleEventsConfig,
										         final Module... commonModules) {
		this(bootstrapCfgs,buildCommonModuleEventsConfig,
			 CollectionUtils.hasData(commonModules) ? Lists.<Module>newArrayList(commonModules) : Lists.<Module>newArrayList());
	}

	protected SpringRootConfigBootstrapGuiceBase(final Collection<ServicesBootstrapConfig> bootstrapCfgs,
										         final Module... commonModules) {
		this(bootstrapCfgs,
			 CollectionUtils.hasData(commonModules) ? Lists.<Module>newArrayList(commonModules) : Lists.<Module>newArrayList());
	}

	protected SpringRootConfigBootstrapGuiceBase(final Collection<ServicesBootstrapConfig> bootstrapCfg,
										         final Collection<Module> commonGuiceModules) {
		this(bootstrapCfg,null,commonGuiceModules);
	}

	protected SpringRootConfigBootstrapGuiceBase(final Collection<ServicesBootstrapConfig> bootstrapCfg,
											     final ServicesCoreModuleEventsConfig commonEventsConfig,
										         final Collection<Module> commonGuiceModules) {
		if (CollectionUtils.isNullOrEmpty(bootstrapCfg)) {
			throw new IllegalArgumentException();
		}
		_servicesBootstrapConfig = bootstrapCfg;
		_commonGuiceModules = commonGuiceModules;
		_commonEventsConfig = commonEventsConfig;
	}
////////////////////////////////////////////////////////////////////////////////////
// LIFECYCLE MANAGEMENT INTERFACE
///////////////////////////////////////////////////////////////////////////////////
	 public static interface ServiceBootstrapSpringHandler {
    	public void startServices();
    	public void stopServices();
    	public Injector getInjector();
    }

    @Bean (initMethod="startServices" , destroyMethod = "stopServices")
	public ServiceBootstrapSpringHandler bootstrapSpring() {
       return new ServiceBootstrapSpringHandler() {
	    	   			Injector  GUICE_INJECTOR =  createInjector();
	
						@Override
						public void startServices() {
							ServicesBootstrapUtil.startServices(GUICE_INJECTOR);
						}
	
						@Override
						public void stopServices() {
							ServicesBootstrapUtil.stopServices(GUICE_INJECTOR);
						}
						@Override
						public Injector getInjector() {
							return GUICE_INJECTOR;
						}
			 };
    }
//////////////////////////////////////////////////////////////////////////////////
// EXPOSED SERVICES
//////////////////////////////////////////////////////////////////////////////////
  
    @Inject  //  Inject ServiceBootstrapSpringHandler
    @Bean    //  Expose
    public  GuiceExposedServicesToBeanProcessor exposeGuiceServicesToBeans(final ServiceBootstrapSpringHandler servicesBootstrap) {
      return new GuiceExposedServicesToBeanProcessor(servicesBootstrap);
    }
    
    @Inject                         //Inject ServiceBootstrapSpringHandler
    @Bean @ModelObjectsMarshaller  // Expose as @ModelObjectsMarshaller
    public Marshaller marshaller(final ServiceBootstrapSpringHandler servicesBootstrap) {
    	return servicesBootstrap.getInjector()
    								.getInstance(Key.get(Marshaller.class,ModelObjectsMarshaller.class));
    }
 ////////////////////////////////////////////////////////////////////////////////////
//  INJECTOR CREATION
///////////////////////////////////////////////////////////////////////////////////
	protected Injector createInjector() {
	    Injector GUICE_INJECTOR = Guice.createInjector(ServicesBootstrapUtil.getBootstrapGuiceModules(_servicesBootstrapConfig)
											 					                          .withCommonEventsExecutor(_commonEventsConfig)
																						  .withCommonBindingModules(_commonGuiceModules));

		return GUICE_INJECTOR;
	}
	
	public class GuiceExposedServicesToBeanProcessor
   	  implements BeanDefinitionRegistryPostProcessor {

	    final ServiceBootstrapSpringHandler _servicesBootstrap;

      	public GuiceExposedServicesToBeanProcessor(final ServiceBootstrapSpringHandler servicesBootstrap) {
	    	  _servicesBootstrap = servicesBootstrap;
      	}
		@Override
		public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
	       List<Class<?>>  exposedClass = _getExposedServicesAsClass();
	       for (final Class<?> serviceClass : exposedClass  ){
	    	   BeanDefinitionBuilder  beanDefBuilder =  BeanDefinitionBuilder.genericBeanDefinition(serviceClass).setLazyInit(true);
	    	   registry.registerBeanDefinition( serviceClass.getName(),beanDefBuilder.getBeanDefinition());
	       }
	    }
		@Override
		public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
		    List<Class<?>>  exposedClass = _getExposedServicesAsClass();
	        for (final Class<?> serviceClass : exposedClass  ) {
	    	    log.warn(" >> Registred bean {}  ", serviceClass.getName());
	    	   	beanFactory.registerSingleton(serviceClass.getName(), _servicesBootstrap.getInjector()
	    	   			                                                                 .getInstance(serviceClass));
	       }
		}
		private List<Class<?>> _getExposedServicesAsClass() {
			List<Class<?>> servicesInterfaces = Lists.newArrayList();
			for (Binding<?> b : _servicesBootstrap.getInjector().getBindings().values()) {
    			 boolean isAsignable =
    					 ServiceInterface.class.isAssignableFrom(b.getKey().getTypeLiteral().getRawType()) ;
    			 if (isAsignable) {
    				 if (b.getKey().getTypeLiteral().getRawType().isAnnotationPresent(ExposedServiceInterface.class) ){
						servicesInterfaces.add(ReflectionUtils.typeFromClassName( b.getKey().getTypeLiteral().getRawType().getCanonicalName()));
    				 }
    			 }
    		}
			return servicesInterfaces;
		}
   }
}
package r01f.bootstrap;

import java.util.Collection;

import javax.servlet.ServletContextListener;

import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import r01f.bootstrap.services.ServicesBootstrapUtil;
import r01f.bootstrap.services.config.ServicesBootstrapConfig;
import r01f.bootstrap.services.config.core.ServicesCoreModuleEventsConfig;
import r01f.patterns.FactoryFrom;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;
import r01f.xmlproperties.XMLPropertiesForApp;

/**
 * Extends {@link GuiceServletContextListener} (that in turn extends {@link ServletContextListener})
 * to have the opportunity to:
 * <ul>
 * 	<li>When starting the web app: start JPA service</li>
 * 	<li>When closing the web app: stop JPA service and free lucene resources (the index writer)</li>
 * </ul>
 * If this is NOT done, an error is raised when re-deploying the application because lucene index
 * are still opened by lucene threads
 * This {@link ServletContextListener} MUST be configured at web.xml removing the default {@link ServletContextListener}
 * (if it exists)
 * <pre class='brush:xml'>
 *		<listener>
 *			<listener-class>r01e.rest.R01VRESTGuiceServletContextListener</listener-class>
 *		</listener>
 * </pre>
 */
public abstract class ServletContextListenerBase
	          extends GuiceServletContextListenerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Collection<ServicesBootstrapConfig> _servicesBootstrapConfig;
	private final Collection<Module> _commonGuiceModules;
	private final ServicesCoreModuleEventsConfig _commonEventsConfig;

/////////////////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected ServletContextListenerBase(final ServicesBootstrapConfig... bootstrapCfgs) {
		this(Lists.newArrayList(bootstrapCfgs),
			 (Collection<Module>)null);		// no commong guice modules
	}
	protected ServletContextListenerBase(final ServicesBootstrapConfig bootstrapCfg,
										 final Module... commonModules) {
		this(Lists.newArrayList(bootstrapCfg),
			 CollectionUtils.hasData(commonModules) ? Lists.<Module>newArrayList(commonModules) : Lists.<Module>newArrayList());
	}
	protected ServletContextListenerBase(final Collection<ServicesBootstrapConfig> bootstrapCfgs,
										 final ServicesCoreModuleEventsConfig buildCommonModuleEventsConfig,
										 final Module... commonModules) {
		this(bootstrapCfgs,buildCommonModuleEventsConfig,
			 CollectionUtils.hasData(commonModules) ? Lists.<Module>newArrayList(commonModules) : Lists.<Module>newArrayList());
	}

	protected ServletContextListenerBase(final Collection<ServicesBootstrapConfig> bootstrapCfgs,
										 final Module... commonModules) {
		this(bootstrapCfgs,
			 CollectionUtils.hasData(commonModules) ? Lists.<Module>newArrayList(commonModules) : Lists.<Module>newArrayList());
	}

	protected ServletContextListenerBase(final Collection<ServicesBootstrapConfig> bootstrapCfg,
										 final Collection<Module> commonGuiceModules) {
		this(bootstrapCfg,null,commonGuiceModules);
	}

	protected ServletContextListenerBase(final Collection<ServicesBootstrapConfig> bootstrapCfg,
										 final ServicesCoreModuleEventsConfig commonEventsConfig,
										 final Collection<Module> commonGuiceModules) {
		if (CollectionUtils.isNullOrEmpty(bootstrapCfg)) throw new IllegalArgumentException();
		_servicesBootstrapConfig = bootstrapCfg;
		_commonGuiceModules = commonGuiceModules;
		_commonEventsConfig = commonEventsConfig;
	}
	protected ServletContextListenerBase(final XMLPropertiesForApp xmlProps,
										 final FactoryFrom<XMLPropertiesForApp,Collection<ServicesBootstrapConfig>> bootstrapCfgFactory,
										 final FactoryFrom<XMLPropertiesForApp,Collection<Module>> commonGuiceModulesFactory) {
		this(xmlProps,
			 bootstrapCfgFactory,
			 null,
			 commonGuiceModulesFactory);
	}
	protected ServletContextListenerBase(final XMLPropertiesForApp xmlProps,
										 final FactoryFrom<XMLPropertiesForApp,Collection<ServicesBootstrapConfig>> bootstrapCfgFactory,
										 final FactoryFrom<XMLPropertiesForApp,ServicesCoreModuleEventsConfig> commonEventsConfigFactory,
										 final FactoryFrom<XMLPropertiesForApp,Collection<Module>> commonGuiceModulesFactory) {
		this(bootstrapCfgFactory != null ? bootstrapCfgFactory.from(xmlProps) : null,
			 commonEventsConfigFactory != null ? commonEventsConfigFactory.from(xmlProps) : null,
			 commonGuiceModulesFactory != null ? commonGuiceModulesFactory.from(xmlProps) : null);
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  Overridden methods of GuiceServletContextListener
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected Iterable<Module> _createGuiceModules() {
		return ServicesBootstrapUtil.getBootstrapGuiceModules(_servicesBootstrapConfig)
		                          .withCommonEventsExecutor(_commonEventsConfig)
								  .withCommonBindingModules(_commonGuiceModules);
	}
}

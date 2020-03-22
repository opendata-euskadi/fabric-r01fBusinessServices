package r01f.bootstrap;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.inject.Module;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.bootstrap.services.config.ServicesBootstrapConfig;
import r01f.bootstrap.services.config.core.ServicesCoreModuleEventsConfig;
import r01f.patterns.IsBuilder;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class ResourceConfigBootstrapDataBuilder
		   implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static ResourceConfigBootstrapDataBuilderCommonEventsConfigStep withServices(final Collection<ServicesBootstrapConfig> servicesBootstrapConfig) {
	
		return new ResourceConfigBootstrapDataBuilder() { /* nothing */ }
						.new ResourceConfigBootstrapDataBuilderCommonEventsConfigStep(servicesBootstrapConfig);
	}
	public static ResourceConfigBootstrapDataBuilderCommonEventsConfigStep withServices(final ServicesBootstrapConfig... servicesBootstrapConfig) {	
		return new ResourceConfigBootstrapDataBuilder() { /* nothing */ }
						.new ResourceConfigBootstrapDataBuilderCommonEventsConfigStep(Lists.newArrayList(servicesBootstrapConfig));
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ResourceConfigBootstrapDataBuilderCommonEventsConfigStep {
		final Collection<ServicesBootstrapConfig> _servicesBootstrapConfig;	

		public ResourceConfigBootstrapDataBuilderCommonGuiceModulesStep andCommonEventsConfig(final ServicesCoreModuleEventsConfig commonEventsConfig) {	
			return new ResourceConfigBootstrapDataBuilderCommonGuiceModulesStep(_servicesBootstrapConfig,commonEventsConfig);
		}
		public ResourceConfigBootstrapDataBuilderCommonGuiceModulesStep withoutCommonEventsConfig() {	
			return new ResourceConfigBootstrapDataBuilderCommonGuiceModulesStep(_servicesBootstrapConfig,null);
		}
	}
	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ResourceConfigBootstrapDataBuilderCommonGuiceModulesStep {
		
		final Collection<ServicesBootstrapConfig> _servicesBootstrapConfig;	
	    final ServicesCoreModuleEventsConfig _commonEventsConfig;
	    
	    public ResourceConfigBootstrapDataBuilderBuilderStep usingCommonBindingsModules(final Module... commonGuiceModules) {				
			return usingCommonBindingsModules(Lists.newArrayList(commonGuiceModules));
		}

		public ResourceConfigBootstrapDataBuilderBuilderStep usingCommonBindingsModules(final Collection<Module> commonGuiceModules) {				
			return new ResourceConfigBootstrapDataBuilderBuilderStep(_servicesBootstrapConfig,_commonEventsConfig,commonGuiceModules);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ResourceConfigBootstrapDataBuilderBuilderStep {
		final Collection<ServicesBootstrapConfig> _servicesBootstrapConfig;	
	    final ServicesCoreModuleEventsConfig _commonEventsConfig;
	    final Collection<Module> _commonGuiceModules;

		public ResourceConfigBootstrapData build() {
			return new ResourceConfigBootstrapData(_servicesBootstrapConfig,_commonEventsConfig,_commonGuiceModules);
		}
	}
}

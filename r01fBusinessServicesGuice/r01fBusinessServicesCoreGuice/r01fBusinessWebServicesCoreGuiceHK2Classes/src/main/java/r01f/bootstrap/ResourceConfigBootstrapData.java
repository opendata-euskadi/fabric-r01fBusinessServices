package r01f.bootstrap;

import java.util.Collection;

import com.google.inject.Module;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.bootstrap.services.config.ServicesBootstrapConfig;
import r01f.bootstrap.services.config.core.ServicesCoreModuleEventsConfig;
import r01f.util.types.collections.CollectionUtils;

@Accessors(prefix="_")
public class ResourceConfigBootstrapData {

/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private  final Collection<ServicesBootstrapConfig> _servicesBootstrapConfig;
	@Getter private  final Collection<Module> _commonGuiceModules;
	@Getter private  final ServicesCoreModuleEventsConfig _commonEventsConfig;
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ResourceConfigBootstrapData(final Collection<ServicesBootstrapConfig> bootstrapCfg,
									   final ServicesCoreModuleEventsConfig commonEventsConfig,
									   final Collection<Module> commonGuiceModules) {
		if (CollectionUtils.isNullOrEmpty(bootstrapCfg)) {
			throw new IllegalArgumentException();
		}
		_servicesBootstrapConfig = bootstrapCfg;
		_commonGuiceModules = commonGuiceModules;
		_commonEventsConfig = commonEventsConfig;
	}

}

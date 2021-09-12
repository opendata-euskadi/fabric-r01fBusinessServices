package r01f.bootstrap.services.config.client;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.inject.Module;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.bootstrap.services.client.ServicesClientAPIBootstrapGuiceModuleBase;
import r01f.patterns.IsBuilder;
import r01f.services.client.ClientAPI;
import r01f.services.ids.ServiceIDs.ClientApiAppCode;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.collections.CollectionUtils;


/**
 * Builder for ServicesConfig
 * Usage: 
 * <pre class='brush:java'>

 * </pre>
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class ServicesClientBootstrapConfigBuilder 
	       implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static ServicesClientBootstrapConfigBuilderApiTypeStep forClientApiAppCode(final ClientApiAppCode appCode) {
		return new ServicesClientBootstrapConfigBuilder() { /* nothing */ }
					.new ServicesClientBootstrapConfigBuilderApiTypeStep(appCode);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CLIENT API / SERVICE INTERFACES
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class ServicesClientBootstrapConfigBuilderApiTypeStep {
		protected final ClientApiAppCode _clientApiAppCode;
		
		public ServicesClientBootstrapConfigBuilderServiceInterfacesStep exposingApi(final Class<? extends ClientAPI> clientApiType) {
			return new ServicesClientBootstrapConfigBuilderServiceInterfacesStep(_clientApiAppCode,
																				 clientApiType);
		}
		public ServicesClientBootstrapConfigBuilderServiceInterfacesStep notExposingApi() {
			return new ServicesClientBootstrapConfigBuilderServiceInterfacesStep(_clientApiAppCode,
																				 null);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class ServicesClientBootstrapConfigBuilderServiceInterfacesStep {
		protected final ClientApiAppCode _clientApiAppCode;
		protected final Class<? extends ClientAPI> _clientApiType;

		public ServicesClientBootstrapConfigBuilderCoreConfigStep ofServiceInterfacesExtending(final Class<? extends ServiceInterface>... serviceInterfaceBaseTypes) {
			return this.ofServiceInterfacesExtending(Lists.newArrayList(serviceInterfaceBaseTypes));
		}
		public ServicesClientBootstrapConfigBuilderCoreConfigStep ofServiceInterfacesExtending(final Collection<Class<? extends ServiceInterface>> serviceInterfaceBaseTypes) {
			return new ServicesClientBootstrapConfigBuilderCoreConfigStep(_clientApiAppCode,
																		  _clientApiType,serviceInterfaceBaseTypes);
		}
		public ServicesClientBootstrapConfigBuilderCoreConfigStep doNotFindServiceInterfaces() {
			return new ServicesClientBootstrapConfigBuilderCoreConfigStep(_clientApiAppCode,
																		  _clientApiType,
																		  null);	// no service interfaces
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EXPOSITION
/////////////////////////////////////////////////////////////////////////////////////////	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class ServicesClientBootstrapConfigBuilderCoreConfigStep {
		protected final ClientApiAppCode _clientApiAppCode;
		protected final Class<? extends ClientAPI> _clientApiType;
		protected final Collection<Class<? extends ServiceInterface>> _serviceInterfacesBaseTypes;
		
		public ServicesClientBootstrapConfigBuilderBuildStep bootstrappedWith(final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> clientBootstrapGuiceModuleType,
																			  final Module... moreClientBootstrapGuiceModules) {
			return this.bootstrappedWith(clientBootstrapGuiceModuleType,
										 CollectionUtils.hasData(moreClientBootstrapGuiceModules) ? Lists.newArrayList(moreClientBootstrapGuiceModules)
												 												  : null);
		}
		public ServicesClientBootstrapConfigBuilderBuildStep bootstrappedWith(final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> clientBootstrapGuiceModuleType,
																			  final Collection<Module> moreClientBootrapGuiceModules) {
			ServicesClientConfigForCoreModule<ServicesCoreModuleExpositionAsBeans,
											  ServicesClientProxyForCoreBeanExposed> beanCoreModuleCfg = ServicesClientConfigForCoreModuleBuilder.of(null,null)
											  																		.forCoreExposedAsBeans();
			Collection<ServicesClientConfigForCoreModule<?,?>> coreModuleConfigs = Lists.<ServicesClientConfigForCoreModule<?,?>>newArrayList(beanCoreModuleCfg);
			return new ServicesClientBootstrapConfigBuilderBuildStep(_clientApiAppCode,
																	 _clientApiType,_serviceInterfacesBaseTypes,
																	 coreModuleConfigs,
																	 clientBootstrapGuiceModuleType,moreClientBootrapGuiceModules);
		}
		public ServicesClientBootstrapConfigBuilderGuiceModuleStep forCoreModules(final ServicesClientConfigForCoreModule<?,?>... clientCoreModuleCfgs) {
			if (CollectionUtils.isNullOrEmpty(clientCoreModuleCfgs)) throw new IllegalArgumentException("No client config for core modules!");
			return this.forCoreModules(Lists.newArrayList(clientCoreModuleCfgs));
		}
		public ServicesClientBootstrapConfigBuilderGuiceModuleStep forCoreModules(final Collection<ServicesClientConfigForCoreModule<?,?>> clientCoreModuleCfgs) {
			if (CollectionUtils.isNullOrEmpty(clientCoreModuleCfgs)) throw new IllegalArgumentException("No client config for core modules!");
			return new ServicesClientBootstrapConfigBuilderGuiceModuleStep(_clientApiAppCode,
																	 	   _clientApiType,_serviceInterfacesBaseTypes,
																	 	   clientCoreModuleCfgs);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BOOTSTRAP
/////////////////////////////////////////////////////////////////////////////////////////	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class ServicesClientBootstrapConfigBuilderGuiceModuleStep {
		protected final ClientApiAppCode _clientApiAppCode;
		protected final Class<? extends ClientAPI> _clientApiType;
		protected final Collection<Class<? extends ServiceInterface>> _serviceInterfacesBaseTypes;
		protected final Collection<ServicesClientConfigForCoreModule<?,?>> _coreModuleConfigs;
		
		public ServicesClientBootstrapConfigBuilderBuildStep bootstrappedWith(final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> clientBootstrapGuiceModuleType,
																			  final Module... moreClientBootstrapGuiceModules) {
			return this.bootstrappedWith(clientBootstrapGuiceModuleType,
										 CollectionUtils.hasData(moreClientBootstrapGuiceModules) ? Lists.newArrayList(moreClientBootstrapGuiceModules)
												 												  : null);
		}
		public ServicesClientBootstrapConfigBuilderBuildStep bootstrappedWith(final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> clientBootstrapGuiceModuleType,
																			  final Collection<Module> moreClientBootrapGuiceModules) {
			return new ServicesClientBootstrapConfigBuilderBuildStep(_clientApiAppCode,
																	 _clientApiType,_serviceInterfacesBaseTypes,
																	 _coreModuleConfigs,
																	 clientBootstrapGuiceModuleType,moreClientBootrapGuiceModules);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SUB-MODULES & BUILD
/////////////////////////////////////////////////////////////////////////////////////////	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class ServicesClientBootstrapConfigBuilderBuildStep {
		protected final ClientApiAppCode _clientApiAppCode;
		protected final Class<? extends ClientAPI> _clientApiType;
		protected final Collection<Class<? extends ServiceInterface>> _serviceInterfacesBaseTypes;
		protected final Collection<ServicesClientConfigForCoreModule<?,?>> _coreModuleConfigs;
		protected final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> _clientBootstrapGuiceModuleType;
		protected final Collection<Module> _moreClientBootrapGuiceModules;
		protected Collection<ServicesClientSubModuleBootstrapConfig<?>> _subModulesCfgs;
		
		public ServicesClientBootstrapConfigBuilderBuildStep withSubModulesConfigs(final ServicesClientSubModuleBootstrapConfig<?>... subModulesCfgs) {
			_subModulesCfgs = CollectionUtils.hasData(subModulesCfgs) ? Lists.newArrayList(subModulesCfgs) : null;
			return this;
		}
		public ServicesClientGuiceBootstrapConfig build() {
			return new ServicesClientGuiceBootstrapConfigImpl(_clientApiAppCode,
								  			 		          _clientApiType,_serviceInterfacesBaseTypes,
								  			 			      _clientBootstrapGuiceModuleType,_moreClientBootrapGuiceModules,
								  			 			      _coreModuleConfigs,
								  			 			      _subModulesCfgs);
		}
	}
}

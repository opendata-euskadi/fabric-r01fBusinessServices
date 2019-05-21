package r01f.services.client;

import java.util.Map;

import javax.inject.Provider;

import lombok.experimental.Accessors;
import r01f.objectstreamer.Marshaller;
import r01f.securitycontext.SecurityContext;
import r01f.services.interfaces.ServiceInterface;
import r01f.types.AppVersion;



/**
 * Base type for every API implementation 
 */
@Accessors(prefix="_")
public abstract class ClientAPIImplBase 
           implements ClientAPI {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR INJECTED
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The api version
	 */
	protected final AppVersion _version;
	/**
	 * Model objects marshaller
	 */
	protected final Marshaller _modelObjectsMarshaller;
	/**
	 * securityContext 
	 */
	protected final Provider<SecurityContext> _securityContextProvider;
	/**
	 * A guice's Map binder that provides a {@link ServiceInterface}'s core impl or proxy to the core impl
	 */
	@SuppressWarnings("rawtypes")
	protected final Map<Class,ServiceInterface> _srvcIfaceMappings;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("rawtypes") 
	public ClientAPIImplBase(final Provider<SecurityContext> securityContextProvider,
							 final Marshaller modelObjectsMarshaller,
							 final Map<Class,ServiceInterface> srvcIfaceMappings) {
		this(securityContextProvider,
			 modelObjectsMarshaller,
			 srvcIfaceMappings,
			 null);		// No version
	}
	@SuppressWarnings("rawtypes") 
	public ClientAPIImplBase(final Provider<SecurityContext> securityContextProvider,
							 final Marshaller modelObjectsMarshaller,
							 final Map<Class,ServiceInterface> srvcIfaceMappings,
							 final AppVersion version) {
		_securityContextProvider = securityContextProvider;
		_modelObjectsMarshaller = modelObjectsMarshaller;
		_srvcIfaceMappings = srvcIfaceMappings;
		_version = version != null ? version : AppVersion.from("unknown-api-version");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CAST
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <A extends ClientAPI> A as(final Class<A> type) {
		return (A)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MARSHALLER
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Marshaller getModelObjectsMarshaller() {
		return _modelObjectsMarshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	API VERSION                                                                          
/////////////////////////////////////////////////////////////////////////////////////////
	public AppVersion getApiVersion() {
		return _version;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SECURITY CONTEXT
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public Provider<SecurityContext> getSecurityContextProvider() {
		return _securityContextProvider;
	}
	@Override @SuppressWarnings("unchecked")
	public <U extends SecurityContext> U getSecurityContext() {
		return (U)_securityContextProvider.get();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SERVICE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("rawtypes")
	public Map<Class,ServiceInterface> getServiceInterfaceMappings() {
		return _srvcIfaceMappings;
	}
	@Override @SuppressWarnings("unchecked")
	public <S extends ServiceInterface> S getServiceInterfaceCoreImplOrProxy(final Class<S> serviceInterfaceType) {
		S outSrvcIfaceCoreImplOrProxy = _srvcIfaceMappings != null ? (S)_srvcIfaceMappings.get(serviceInterfaceType)
										  						   : null;
		return outSrvcIfaceCoreImplOrProxy;
	}
}
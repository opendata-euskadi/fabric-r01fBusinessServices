package r01f.bootstrap;


import com.google.inject.Binder;

@Deprecated	// use CoreServicesBootstrapGuiceModuleBindsEventListeners
public interface ServicesBootstrapGuiceModuleBindsCRUDEventListeners {
	/**
	 * Binds the CRUD events listeners
	 * @param binder
	 */
	public void bindCRUDEventListeners(final Binder binder);	
}

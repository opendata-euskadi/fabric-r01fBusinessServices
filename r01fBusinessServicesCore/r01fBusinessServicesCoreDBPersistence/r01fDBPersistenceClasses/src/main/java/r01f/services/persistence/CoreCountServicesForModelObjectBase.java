package r01f.services.persistence;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.persist.Transactional;

import lombok.experimental.Accessors;
import r01f.bootstrap.services.config.core.ServicesCoreBootstrapConfigWhenBeanExposed;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CountResult;
import r01f.objectstreamer.Marshaller;
import r01f.securitycontext.SecurityContext;
import r01f.services.interfaces.CountServicesForModelObject;


/**
 * Core service base for persistence services
 * 
 * INJECTED STATUS
 * ===============
 * 	The TRANSACTIONAL methods are located at the Services layer (this type); 
 * 	Some operations might span multiple @Transactional-annotated methods so it's very important
 *  	for the {@link EntityManager} to have Extended-scope
 * 		see http://piotrnowicki.com/2012/11/types-of-entitymanagers-application-managed-entitymanager/
 * 		 or http://www.javacodegeeks.com/2013/06/jpa-2-entitymanagers-transactions-and-everything-around-it.html 
 *    	
 * 	The {@link EntityManager} should be created at the services layer (this type) and handled to every delegated
 * 	type (crud, search, etc)
 * 
 *  	Beware that:
 *  	<ul>
 *  		<li>This type is (at the end) injected in a service-layer that usually is a {@link Singleton} instance</li>
 *  		<li>{@link EntityManager} is NOT usually thread safe and an {@link EntityManagerFactory} should be used if the type is NOT thread safe to create an {@link EntityManager}</li>
 *  		<li>Because this type is (at the end) injected in a {@link Singleton} and it's NOT thread-safe, the {@link EntityManagerFactory} should be used</li>
 *  		<li>When creating an {@link EntityManager} from a {@link EntityManagerFactory} the application is responsible for creation and
 *  			removal of the {@link EntityManager}... so it's an [Application-Managed {@link EntityManager}] and these types of
 *  			managers ALLWAYS have EXTENDED SCOPE (see http://piotrnowicki.com/2012/11/types-of-entitymanagers-application-managed-entitymanager/)</li>
 *  	</ul>
 *  	See
 *  	<ul> 
 *  		<li>http://www.javacodegeeks.com/2013/06/jpa-2-entitymanagers-transactions-and-everything-around-it.html</li>
 *  		<li>http://piotrnowicki.com/2012/11/types-of-entitymanagers-application-managed-entitymanager/</li>
 *  	</ul>
 *
 * @param <O>
 * @param <M>
 * @param <PD>
 */
@Accessors(prefix="_")
public abstract class CoreCountServicesForModelObjectBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
			  extends CorePersistenceServicesBase 
		   implements CountServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor
	 * @param cfg core config
	 * @param modelObjectsMarshaller annotated with @ModelObjectsMarshaller
	 * @param eventBus
	 * @param entityManagerProvider
	 */
	public CoreCountServicesForModelObjectBase(final ServicesCoreBootstrapConfigWhenBeanExposed cfg,
									   		   final Marshaller modelObjectsMarshaller,
									   		   final EventBus eventBus,
									   		   final Provider<EntityManager> entityManagerProvider) {
		super(cfg,
			  modelObjectsMarshaller,
			  eventBus,
			  entityManagerProvider);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  COUNT
/////////////////////////////////////////////////////////////////////////////////////////
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public CountResult<M> countAll(final SecurityContext securityContext) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(CountServicesForModelObject.class)
							.countAll(securityContext);
	}
}

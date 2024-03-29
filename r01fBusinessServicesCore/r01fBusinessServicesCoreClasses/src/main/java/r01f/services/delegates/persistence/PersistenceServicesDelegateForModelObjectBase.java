package r01f.services.delegates.persistence;

import com.google.common.eventbus.EventBus;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.bootstrap.services.config.core.ServicesCoreBootstrapConfigWhenBeanExposed;
import r01f.events.COREServiceMethodExecEvents.COREServiceMethodExecErrorEvent;
import r01f.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CRUDError;
import r01f.model.persistence.CRUDOK;
import r01f.model.persistence.CRUDResult;
import r01f.securitycontext.SecurityContext;
import r01f.services.callback.spec.COREServiceMethodCallbackSpec;
import r01f.services.interfaces.ServiceInterfaceForModelObject;

@Slf4j
@Accessors(prefix="_")
public abstract class PersistenceServicesDelegateForModelObjectBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
			  extends PersistenceServicesDelegateBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The {@link PersistableModelObject}'s type
	 */
	@Getter protected final Class<M> _modelObjectType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceServicesDelegateForModelObjectBase(final ServicesCoreBootstrapConfigWhenBeanExposed coreCfg,
														 final Class<M> modelObjectType,
														 final ServiceInterfaceForModelObject<O,M> serviceImpl,
														 final EventBus eventBus) {
		super(coreCfg,
			  serviceImpl,
			  eventBus);
		_modelObjectType = modelObjectType;
	}
	public PersistenceServicesDelegateForModelObjectBase(final ServicesCoreBootstrapConfigWhenBeanExposed coreCfg,
														 final Class<M> modelObjectType,
														 final ServiceInterfaceForModelObject<O,M> serviceImpl) {
		this(coreCfg,
			 modelObjectType,
			 serviceImpl,
			 null);		// no event bus
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	EVENT FIRING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Throws an {@link EventBus} event
	 * @param securityContext
	 * @param opResult
	 */
	protected void _fireEvent(final SecurityContext securityContext,
							  final CRUDResult<M> opResult) {
		_fireEvent(securityContext,
				   opResult,
				   null);		// no callback
	}
	/**
	 * Throws an {@link EventBus} event
	 * @param securityContext
	 * @param opResult
	 * @param callbackSpec
	 */
	protected void _fireEvent(final SecurityContext securityContext,
							  final CRUDResult<M> opResult,
							  final COREServiceMethodCallbackSpec callbackSpec) {
//		try {
		if (this.getEventBus() == null) {
			log.debug("NO event bus available; CRUD events will NOT be handled");
			return;
		}
		log.debug("Publishing an event of type: {}: ({}) success={}",
				  opResult.getClass(),
				  opResult.getCalledMethod(),
				  opResult.hasSucceeded());

		if (opResult.hasFailed()) {
			CRUDError<M> opNOK = opResult.asCRUDError();		// as(CRUDError.class)
			COREServiceMethodExecErrorEvent nokEvent = new COREServiceMethodExecErrorEvent(securityContext,
												 					         	 		   opNOK,
												 					         	 		   callbackSpec);
			this.getEventBus().post(nokEvent);

		} else if (opResult.hasSucceeded()) {
			CRUDOK<M> opOK = opResult.asCRUDOK();				// as(CRUDOK.class);
			COREServiceMethodExecOKEvent okEvent = new COREServiceMethodExecOKEvent(securityContext,
												 					      	  	    opOK,
												 					      	  	    callbackSpec);
			this.getEventBus().post(okEvent);
		}
//		} catch (Throwable th) {
//			log.error("ERROR while handling CORE EVENT: {}",
//					  th.getMessage(),th);
//		}
	}
}

package r01f.events.crud;

import org.slf4j.Logger;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.events.COREServiceMethodExecEventListeners.COREServiceMethodExecOKEventListener;
import r01f.events.COREServiceMethodExecEvents.COREServiceMethodExecEvent;
import r01f.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;
import r01f.model.ModelObject;
import r01f.model.persistence.CRUDOK;
import r01f.model.persistence.CRUDResult;
import r01f.reflection.ReflectionUtils;
import r01f.securitycontext.SecurityContext;
import r01f.services.callback.COREMethodCallback;
import r01f.services.callback.spec.COREServiceMethodBeanCallbackSpec;
import r01f.services.callback.spec.COREServiceMethodCallbackSpec;
import r01f.util.types.Strings;

/**
 * Listener to {@link COREServiceMethodExecOKEvent}s thrown by the persistence layer through the {@link EventBus}
 * @param <M>
 */
@Slf4j
@Accessors(prefix="_")
public abstract class CRUDOKEventListenerBase 
           implements COREServiceMethodExecOKEventListener {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The type is needed because guava's event bus does NOT supports generic event types
	 * 	- {@link CRUDOperationEvent} is a generic type parameterized with the persistable model object type, 
	 *  - Subscriptions to the event bus are done by event type, that's by {@link CRUDOperationEvent} type
	 *  - BUT since guava {@link EventBus} does NOT supports generics, the subscriptions are done to the raw {@link CRUDOperationEvent}
	 *  - ... so ALL listeners will be attached to the SAME event type: {@link CRUDOperationEvent}
	 *  - ... and ALL listeners will receive {@link CRUDOperationEvent} events
	 *  - ... but ONLY one should handle it.
	 * In order for the event handler (listener) to discriminate events to handle, the model object's type
	 * is used (see {@link #_hasToBeHandled(CRUDOperationEvent)} method)
	 */
	protected final Class<? extends ModelObject> _type;
	/**
	 * Filters the events using the afected model object
	 */
	protected final transient CRUDOKEventFilter _crudOperationOKEventFilter;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOKEventListenerBase(final Class<? extends ModelObject> type) {
		_type = type;
		_crudOperationOKEventFilter = new CRUDOKEventFilter() {
												@Override @SuppressWarnings("unchecked")
												public boolean hasTobeHandled(final COREServiceMethodExecOKEvent opEvent) {
													CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
																				    					  .as(CRUDResult.class);
													// the event refers to the same model object type THIS event handler handles;
													return ReflectionUtils.isSubClassOf(crudResult.as(CRUDOK.class).getObjectType(),
																						_type);
												}
									  };
	}
	public CRUDOKEventListenerBase(final Class<? extends ModelObject> type,
								   final CRUDOKEventFilter crudOperationOKEventFilter) {
		_type = type;
		_crudOperationOKEventFilter = crudOperationOKEventFilter;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEFAULT DEAD EVENT LISTENER
/////////////////////////////////////////////////////////////////////////////////////////	
	@Subscribe
	public void handleDeadEvent(final DeadEvent deadEvent) {
		log.warn("> Not handled event:  {}",
				 deadEvent.getEvent());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful create or update operation 
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulCreateUpdateOrDelete(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _crudOperationOKEventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;
		
		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);
		return (crudResult.hasSucceeded() 
				// it's a create, update or delete event
			 && (crudResult.asCRUDOK().hasBeenCreated() 
				 || 
				 crudResult.asCRUDOK().hasBeenUpdated() 
				 ||
				 crudResult.asCRUDOK().hasBeenDeleted()));	
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful create or update operation 
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulCreateOrUpdate(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _crudOperationOKEventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;
		
		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);
		return (crudResult.hasSucceeded() 
				// it's a create or update event
			 && (crudResult.asCRUDOK().hasBeenCreated() 
				 || 
				 crudResult.asCRUDOK().hasBeenUpdated()));	
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful create 
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulCreate(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _crudOperationOKEventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;
		
		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);
		return (crudResult.hasSucceeded() 
			 && (crudResult.asCRUDOK().hasBeenCreated()));												// it's a create event
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful update operation 
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulUpdate(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _crudOperationOKEventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;
		
		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);		
		return (crudResult.hasSucceeded()
			 && (crudResult.asCRUDOK().hasBeenUpdated()));								// it's an update event
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful delete operation 
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulDelete(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _crudOperationOKEventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;
		
		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);
		return (crudResult.hasSucceeded()
			 && (crudResult.asCRUDOK().hasBeenDeleted()));								// it's a delete event
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	CALLBACK SEND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sends a callback 
	 * @param securityContext
	 * @param event
	 */
	public void sendCallbackFor(final SecurityContext securityContext,
								final COREServiceMethodExecEvent event) {
		if (event.getCallbackSpec() != null) {			
			COREMethodCallback callback = _createCallbackInstance(event.getCallbackSpec());
			if (event.isForCOREMethodCallOK()) {
				callback.onCOREMethodCallOK(securityContext,
										    event.getCOREServiceMethodExecResult()
											     .asCOREServiceMethodExecOK());
			} 
			else if (event.isForCOREMethodCallError()) {
				callback.onCOREMethodCallError(securityContext,
											   event.getCOREServiceMethodExecResult()
												    .asCOREServiceMethodExecError());
			}
		} else {
			System.out.println("NO CALLBACK!!!!!!");
		}
	}
	private COREMethodCallback _createCallbackInstance(final COREServiceMethodCallbackSpec callbackSpec) {
		COREMethodCallback callback = null;
		if (callbackSpec instanceof COREServiceMethodBeanCallbackSpec) {
			COREServiceMethodBeanCallbackSpec beanCallbackSpec = (COREServiceMethodBeanCallbackSpec)callbackSpec;
			Class<? extends COREMethodCallback> callbackType = beanCallbackSpec.getImplType();
			callback = ReflectionUtils.createInstanceOf(callbackType);
		} 
		else {
			throw new UnsupportedOperationException();
		}
		return callback;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _debugEvent(final Logger log,
						  	   final COREServiceMethodExecOKEvent opOKEvent,final boolean hasToBeHandled) {
		if (log.isTraceEnabled()) {
			log.trace(_debugEvent(opOKEvent,
						  		  hasToBeHandled));
		} else if (log.isDebugEnabled() && hasToBeHandled) {
			log.debug(_debugEvent(opOKEvent,
						  		  hasToBeHandled));
		}
	}
	protected String _debugEvent(final COREServiceMethodExecOKEvent opOKEvent,
							   	 final boolean hasToBeHandled) {
		return Strings.customized("EventListener registered for events of [{}] entities\n" + 
						  		  "{}\n" + 
						  		  "Handle the event: {}",
					  			  _type,
							  	  opOKEvent.debugInfo(),
							  	  hasToBeHandled);
	}
}

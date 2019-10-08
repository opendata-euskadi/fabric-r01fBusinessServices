package r01f.events.crud;

import com.google.common.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;
import r01f.events.COREServiceMethodCallEventListeners.COREServiceMethodCallErrorEventListener;
import r01f.events.COREServiceMethodCallEvents.COREServiceMethodCallErrorEvent;
import r01f.model.services.COREServiceMethodExecError;

/**
 * Default {@link COREServiceMethodCallErrorEvent}s listener that simply logs the op NOK events
 * @param <R>
 */
@Slf4j
public class CRUDErrorEventListener
  implements COREServiceMethodCallErrorEventListener {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Subscribe	// subscribes this event listener at the EventBus
	@Override
	public void onPersistenceOperationError(final COREServiceMethodCallErrorEvent opErrorEvent) {
		// Persistence CRUD operation error
		COREServiceMethodExecError<?> opError = opErrorEvent.getAsCOREServiceMethodExecError();
		if (opError.wasBecauseAClientError()) {
			// do not polute log with client errors ;-)
			log.info("Client Error: {}",opError.getDetailedMessage());
		} else {
			log.error("======================= [{}] OperationNOK event=====================\n{}",
					  CRUDErrorEventListener.class,
					  opError.getDetailedMessage(),opError.getCOREServiceException());
		}
	}
}

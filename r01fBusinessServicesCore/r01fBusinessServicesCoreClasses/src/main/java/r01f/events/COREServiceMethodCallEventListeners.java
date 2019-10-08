package r01f.events;

import r01f.events.COREServiceMethodCallEvents.COREServiceMethodCallErrorEvent;
import r01f.events.COREServiceMethodCallEvents.COREServiceMethodCallOKEvent;



/**
 * Event listeners
 */
public class COREServiceMethodCallEventListeners {
/////////////////////////////////////////////////////////////////////////////////////////
//  Listens to OK PersistenceOperationOKEvent
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface COREServiceMethodCallOKEventListener
					extends COREEventBusEventListener {
		public void onPersistenceOperationOK(final COREServiceMethodCallOKEvent opOKEvent);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Listens to NOK PersistenceOperationErrorEvent
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface COREServiceMethodCallErrorEventListener
					extends COREEventBusEventListener {
		public void onPersistenceOperationError(final COREServiceMethodCallErrorEvent opErrorEvent);
	}
}

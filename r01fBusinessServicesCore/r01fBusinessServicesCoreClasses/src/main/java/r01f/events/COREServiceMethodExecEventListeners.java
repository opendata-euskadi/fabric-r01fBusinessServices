package r01f.events;

import r01f.events.COREServiceMethodExecEvents.COREServiceMethodExecErrorEvent;
import r01f.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;



/**
 * Event listeners
 */
public class COREServiceMethodExecEventListeners {
/////////////////////////////////////////////////////////////////////////////////////////
//  Listens to OK PersistenceOperationOKEvent
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface COREServiceMethodExecOKEventListener
					extends COREEventBusEventListener {
		public void onPersistenceOperationOK(final COREServiceMethodExecOKEvent opOKEvent);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Listens to NOK PersistenceOperationErrorEvent
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface COREServiceMethodExecErrorEventListener
					extends COREEventBusEventListener {
		public void onPersistenceOperationError(final COREServiceMethodExecErrorEvent opErrorEvent);
	}
}

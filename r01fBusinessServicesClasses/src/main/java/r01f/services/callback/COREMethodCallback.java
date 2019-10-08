package r01f.services.callback;

import r01f.model.services.COREServiceMethodExecError;
import r01f.model.services.COREServiceMethodExecOK;
import r01f.model.services.HasCOREServiceMethodExecResult;
import r01f.securitycontext.SecurityContext;

/**
 * CORE method callback interface
 * How callbacks works
 * ===================
 * The callback mechanism works as depicted in the following picture:
 * <pre>
 *     +--------+   +------+       +----+ +-----------+ +--------+ +------------+
 *     |Callback|   |Client|       |CORE| |Persistence| |EventBus| |EventHandler|
 *     |handler |   |      |       |    | |(ie: DB)   | |        | |(async)     |
 *     +---+----+   +--+---+       +--+-+ +-----+-----+ +----+---+ +-----+------+
 *         |           |              |         |            |           |
 *         |           |              |         |            |           |
 *         |           +------------> |         |            |           |
 *         |           |              |         |            |           |
 *         |           |              +-------> |            |           |
 *         |           |              +--------------------> |           |
 *         |           | <------------+         |            |           |
 *         |           |              |         |            +---------> |
 *         |                                                 |           |
 *         | <-----------------------------------------------------------+
 *         |                                                 |           |
 * </pre>
 * [1] - The [client] initiates a [persistence operation] calling a [core] function
 * [2] - The [core] executes it's works usually storing some data in the [DB]
 * [3] - If some [background] (async) work must be done in the [core], an [event]
 * 		 is sent to the [EventBus] to be executed later
 * [4] - The [persistence operation result] is returned to the client
 * 		 BEWARE that this result ONLY tells the client that PART OF THE WORK was done
 * 				(the DB persist)
 * [5] - An [Event Handler] (usually asynchronously) captures the [Event] and 
 * 		 executes some other work (usually heavy work)
 * [6] - The [client] can be informed back about the work performed by the [EventHandler]
 * 		 using the [callback handler]
 * 
 * BEWARE!!!
 * In order to disacouple the [client] logic from the [core], the client only sends 
 * an spec of how to call the [Callback handler]
 * 
 * [client] code:
 * <pre class='brush:java'>
 *		public static class MyCOREMethodCallback
 *			        extends COREMethodCallbackBase {
 *			@Override
 *			public void onCOREMethodCallOK(final SecurityContext securityContext,
 *										   final COREServiceMethodExecOK<?> opOK) {
 *				System.out.println("CALLBACK OK!!");
 *			}
 *			@Override
 *			public void onCOREMethodCallError(final SecurityContext securityContext,
 *											  final COREServiceMethodExecError<?> opError) {
 *				System.out.println("CALLBACK ERROR!!!");
 *			}
 *		}
 * </pre>
 * 
 * [core] code (usually after the [event handler] finishes it's work)
 * <pre class='brush:java'>
 *	private class MyOperationOKEventListener
 *		  extends CRUDOperationOKEventListenerBase {
 *		
 *		public MyOperationOKEventListener(final Class<M> modelObjType) {
 *			super(modelObjType,
 *				  new CRUDOperationOKEventFilter() {
 *							@Override
 *							public boolean hasTobeHandled(final PersistenceOperationOKEvent opEvent) {
 *								PersistenceOperationOK opResult = opEvent.getResultAsOperationOK();
 *								return (opResult instanceof CRUDOK) 
 *								    && (opResult.as(CRUDOK.class).hasBeenCreated() || opResult.as(CRUDOK.class).hasBeenUpdated() || opResult.as(CRUDOK.class).hasBeenDeleted())
 *								    && (opResult.as(CRUDOK.class).getObjectType().isAssignableFrom(modelObjType));
 *							}
 *				  });
 *		}
 *		@Subscribe	// subscribes this event listener at the EventBus
 *		@Override
 *		public void onPersistenceOperationOK(final PersistenceOperationOKEvent opOKEvent) {
 *			if (_crudOperationOKEventFilter.hasTobeHandled(opOKEvent)) {
 *				// DO some HARD WORK
 *				// ...
 *				// ... and tell the [client] about the result
 *				this.sendCallbackFor(opOKEvent);	<--- this is where 
 *			}
 *		}
 *	}
 * </pre>
 * 
 */
public interface COREMethodCallback
		 extends HasCOREServiceMethodExecResult {
/////////////////////////////////////////////////////////////////////////////////////////
// 	CALLBACK METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Callback method called when the persistence operation succeeds
	 * @param securityContext
	 * @param opOK
	 */
	public void onCOREMethodCallOK(final SecurityContext securityContext,
								   final COREServiceMethodExecOK<?> opOK);	
	/**
	 * Callback method called when the persistence operation fails
	 * @param securityContext
	 * @param opNOK
	 */
	public void onCOREMethodCallError(final SecurityContext securityContext,
									  final COREServiceMethodExecError<?> opError);
}

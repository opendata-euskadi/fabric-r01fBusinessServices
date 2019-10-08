package r01f.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.model.services.COREServiceMethodExecError;
import r01f.model.services.COREServiceMethodExecOK;
import r01f.model.services.COREServiceMethodExecResult;
import r01f.model.services.HasCOREServiceMethodExecResult;
import r01f.securitycontext.HasSecurityContext;
import r01f.securitycontext.SecurityContext;
import r01f.services.callback.spec.COREServiceMethodCallbackSpec;
import r01f.services.callback.spec.HasCOREServiceMethodCallbackSpec;

/**
 * CORE method call events
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class COREServiceMethodCallEvents {
/////////////////////////////////////////////////////////////////////////////////////////
//  Base type
/////////////////////////////////////////////////////////////////////////////////////////
	public interface COREServiceMethodCallEvent 
			 extends HasSecurityContext,
			   	     HasCOREServiceMethodExecResult,
			   	     HasCOREServiceMethodCallbackSpec,
			   		 Debuggable {
		public boolean isForCOREMethodCallOK();
		public boolean isForCOREMethodCallError();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	static abstract class COREServiceMethodCallEventBase
			   implements COREServiceMethodCallEvent {
		/**
		 * The user context
		 */
		@Getter private final SecurityContext _securityContext;
		/**
		 * The operation result 
		 */
		@Getter protected final COREServiceMethodExecResult<?> _COREServiceMethodExecResult;
		/**
		 * The async callback
		 */
		@Getter protected final COREServiceMethodCallbackSpec _callbackSpec;
		
		@Override
		public boolean hasSucceeded() {
			return _COREServiceMethodExecResult.hasSucceeded();
		}
		@Override
		public boolean hasFailed() {
			return _COREServiceMethodExecResult.hasFailed();
		}
		@Override
		public CharSequence debugInfo() {
			return _COREServiceMethodExecResult != null ? _COREServiceMethodExecResult.debugInfo()
												 : "no debug info: no method call exec result";
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation OK
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	public static class COREServiceMethodCallOKEvent 
		        extends COREServiceMethodCallEventBase {
		public COREServiceMethodCallOKEvent(final SecurityContext securityContext,
									 		final COREServiceMethodExecOK<?> opOK,
									 		final COREServiceMethodCallbackSpec callbackSpec) {
			super(securityContext,
				  opOK,
				  callbackSpec);
		}
		public COREServiceMethodExecOK<?> getAsCOREServiceMethodExecOK() {
			return (COREServiceMethodExecOK<?>)_COREServiceMethodExecResult;
		}
		@Override
		public boolean isForCOREMethodCallOK() {
			return true;
		}
		@Override
		public boolean isForCOREMethodCallError() {
			return false;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation NOK
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	public static class COREServiceMethodCallErrorEvent 
		 		extends COREServiceMethodCallEventBase {
		public COREServiceMethodCallErrorEvent(final SecurityContext securityContext,
									    	   final COREServiceMethodExecError<?> opNOK,
									    	   final COREServiceMethodCallbackSpec callbackSpec) {
			super(securityContext,
				  opNOK,
				  callbackSpec);
		}
		public COREServiceMethodExecError<?> getAsCOREServiceMethodExecError() {
			return (COREServiceMethodExecError<?>)_COREServiceMethodExecResult;
		}
		@Override
		public boolean isForCOREMethodCallOK() {
			return false;
		}
		@Override
		public boolean isForCOREMethodCallError() {
			return true;
		}
	}
}

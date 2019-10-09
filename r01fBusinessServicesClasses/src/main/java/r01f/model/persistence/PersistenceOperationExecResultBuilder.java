package r01f.model.persistence;

import com.google.common.annotations.GwtIncompatible;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.model.services.COREServiceErrorType;
import r01f.model.services.COREServiceErrorTypes;
import r01f.model.services.COREServiceMethod;
import r01f.patterns.IsBuilder;
import r01f.securitycontext.SecurityContext;
import r01f.util.types.Strings;

/**
 * Builder type for {@link PersistenceOperationExecResult}-implementing types:
 * <ul>
 * 		<li>A successful operation execution result: {@link PersistenceOperationExecOK}</li>
 * 		<li>An error on a FIND operation execution: {@link PersistenceOperationExecError}</li>
 * </ul>
 * If the operation execution was successful:
 * <pre class='brush:java'>
 * 		PersistenceOperationExecOK<MyReturnedObjType> opOK = PersistenceOperationExecResultBuilder.using(securityContext)
 * 																	   			   				  .executed("an operation")
 * 																								  .returning(myReturnedObjTypeInstance);
 * </pre>
 * If an error is raised while executing the persistence operation:
 * <pre class='brush:java'>
 * 		PersistenceOperationExecError<MyReturnedObjType> opError = PersistenceOperationExecResultBuilder.using(securityContext)
 *			  																			 		    	.notExecuted("an operation")
 *			  																								.because(error);
 * </pre>
 */
@GwtIncompatible
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class PersistenceOperationExecResultBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static PersistenceOperationExecResultBuilderResultStep using(final SecurityContext securityContext) {
		return new PersistenceOperationExecResultBuilder() {/* nothing */ }
						.new PersistenceOperationExecResultBuilderResultStep(securityContext);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class PersistenceOperationExecResultBuilderResultStep {
		private final SecurityContext _securityContext;
		
		public PersistenceOperationExecResultBuilderReturnedObjStep executed(final COREServiceMethod requestedMethod) {
			return new PersistenceOperationExecResultBuilderReturnedObjStep(_securityContext,
																			requestedMethod);
		}
		public PersistenceOperationExecResultBuilderErrorStep notExecuted(final COREServiceMethod requestedMethod) {
			return new PersistenceOperationExecResultBuilderErrorStep(_securityContext,
																	  requestedMethod);
		}
		public PersistenceOperationExecResultBuilderReturnedObjStep executed(final PersistenceRequestedOperation requestedMethod) {
			return new PersistenceOperationExecResultBuilderReturnedObjStep(_securityContext,
																			requestedMethod.getCOREServiceMethod());
		}
		public PersistenceOperationExecResultBuilderErrorStep notExecuted(final PersistenceRequestedOperation requestedMethod) {
			return new PersistenceOperationExecResultBuilderErrorStep(_securityContext,
																	  requestedMethod.getCOREServiceMethod());
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class PersistenceOperationExecResultBuilderReturnedObjStep {
		protected final SecurityContext _securityContext;
		protected final COREServiceMethod _requestedMethod;
		
		public <T> PersistenceOperationExecOK<T> returning(final T instance) {
			PersistenceOperationExecOK<T> outOpOK = new PersistenceOperationExecOK<T>();
			outOpOK.setMethodExecResult(instance);
			return outOpOK;
		}
		
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class PersistenceOperationExecResultBuilderErrorStep {
		protected final SecurityContext _securityContext;
		protected final COREServiceMethod _requestedMethod;
		
		public <T> PersistenceOperationExecError<T> because(final PersistenceOperationExecError<?> other) {
			return this.because(other.getError());
		}
		public <T> PersistenceOperationExecError<T> because(final Throwable th) {
			PersistenceOperationExecError<T> outError = new PersistenceOperationExecError<T>(_requestedMethod,
																							 th);
			return outError;
		}
		public <T> PersistenceOperationExecError<T> because(final String error,
															final COREServiceErrorType errType) {
			PersistenceOperationExecError<T> outError = new PersistenceOperationExecError<T>(_requestedMethod,
																							 errType,
																							 error);
			return outError;
		}
		public <T> PersistenceOperationExecError<T> becauseClientBadRequest(final String msg,final Object... vars) {
			PersistenceOperationExecError<T> outError = new PersistenceOperationExecError<T>(_requestedMethod,
																							 COREServiceErrorTypes.BAD_CLIENT_REQUEST,
																							 Strings.customized(msg,vars));		// the error message
			return outError;
		}
		public <T,M> PersistenceOperationResult<T> because(final CRUDError<M> crudError) {
			PersistenceOperationExecError<T> outError = new PersistenceOperationExecError<T>(_requestedMethod,
																							 crudError.getErrorType(),	
																							 crudError.getErrorMessage());
			return outError;
		}
	}
}

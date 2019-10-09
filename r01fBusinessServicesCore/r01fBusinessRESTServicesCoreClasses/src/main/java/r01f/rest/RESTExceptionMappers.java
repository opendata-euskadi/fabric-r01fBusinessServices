package r01f.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import lombok.RequiredArgsConstructor;
import r01f.exceptions.Throwables;
import r01f.model.persistence.PersistenceException;
import r01f.model.persistence.PersistenceRequestedOperation;
import r01f.model.persistence.PersistenceServiceErrorTypes;
import r01f.model.services.COREServiceErrorType;
import r01f.model.services.COREServiceErrorTypes;
import r01f.model.services.COREServiceException;

/**
 * {@link ExceptionMapper}(s) used to map {@link Exception}s to {@link Response}s
 * 
 * <pre>
 * IMPORTANT!	Do NOT forget to include this types at the getClasses() method of {@link {AppCode}RESTApp} type
 * </pre>
 */
public class RESTExceptionMappers {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor
	public abstract static class RESTPersistenceExceptionMapper 
	         		  implements ExceptionMapper<PersistenceException> {
		
		@Override
		public Response toResponse(final PersistenceException persistenceException) {			
			Response outResponse = _handleThrowable(persistenceException);
			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static abstract class RESTUncaughtExceptionMapper 
	         		  implements ExceptionMapper<Throwable> {
		@Override
		public Response toResponse(final Throwable th) {
			return _handleThrowable(th);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Maps an exception to an {@link HttpResponse}
	 * The exception is built back at client side type: r01f.services.client.servicesproxy.rest.RESTResponseToCRUDResultMapperForModelObject
	 * @param th
	 * @return
	 */
	private static Response _handleThrowable(final Throwable th) {
		// Print stack trace before any treatment (cause this could fail and mask the original Exception!!!!!) 
		th.printStackTrace();
		// serialize 
		Response outResponse = null;
		// Persistence exceptions
		if (th instanceof COREServiceException) {
			COREServiceException coreEx = (COREServiceException)th;
			// server errors
			if (coreEx.isServerError()) {			// Server Error
				// force exception stack trace print
				outResponse = Response.status(Status.INTERNAL_SERVER_ERROR)
									  .header("x-r01-errorCode",COREServiceErrorTypes.SERVER_ERROR.encodeAsString())
									  .header("x-r01-extErrorCode",coreEx.getExtendedCode())
									  .header("x-r01-errorMessage",coreEx.getMessage())
									  .header("x-r01-requestedOperation",coreEx.getCalledMethod().asString())
									  .header("x-r01-errorType",coreEx.getClass().getName())
									  .entity(Throwables.getStackTraceAsString(th))
									  .type(MediaType.TEXT_HTML)
									  .build();
			} 
			// client errors
			else if (coreEx.isClientError()) {	
				COREServiceErrorType coreErrorType = coreEx.getType();
				
				// record not found
				if (coreErrorType.is(PersistenceServiceErrorTypes.ENTITY_NOT_FOUND)) {		
					outResponse = Response.status(Status.NOT_FOUND)						
										  .header("x-r01-errorCode",coreErrorType.encodeAsString())
										  .header("x-r01-extErrorCode",coreEx.getExtendedCode())
										  .header("x-r01-errorMessage",coreEx.getMessage())
										  .header("x-r01-requestedOperation",coreEx.getCalledMethod().asString())
										  .header("x-r01-errorType",coreEx.getClass().getName())
										  .entity(Throwables.getStackTraceAsString(th))
										  .type(MediaType.TEXT_HTML)
										  .build();		
				} 
				// update requested but record existed OR the server version is different (optimistic locking)
				else if (coreEx.getCalledMethod().isIn(PersistenceRequestedOperation.UPDATE.getCOREServiceMethod(),
													   PersistenceRequestedOperation.CREATE.getCOREServiceMethod())
				      && coreErrorType.isIn(PersistenceServiceErrorTypes.ENTITY_ALREADY_EXISTS,
				    		  				PersistenceServiceErrorTypes.OPTIMISTIC_LOCKING_ERROR)) {
					outResponse = Response.status(Status.CONFLICT)
										  .header("x-r01-errorCode",coreErrorType.encodeAsString())
										  .header("x-r01-extErrorCode",coreEx.getExtendedCode())
										  .header("x-r01-errorMessage",coreEx.getMessage())
										  .header("x-r01-requestedOperation",coreEx.getCalledMethod().asString())
										  .header("x-r01-errorType",coreEx.getClass().getName())
										  .entity(Throwables.getStackTraceAsString(th))
										  .type(MediaType.TEXT_HTML)
										  .build();						
				}
				// another bad client request
				else {				
					outResponse = Response.status(Status.BAD_REQUEST)
										  .header("x-r01-errorCode",coreErrorType.encodeAsString())
										  .header("x-r01-extErrorCode",coreEx.getExtendedCode())
										  .header("x-r01-errorMessage",coreEx.getMessage())
										  .header("x-r01-requestedOperation",coreEx.getCalledMethod().asString())
										  .header("x-r01-errorType",coreEx.getClass().getName())
										  .entity(Throwables.getStackTraceAsString(th))
										  .type(MediaType.TEXT_HTML)
										  .build();
				}
			}
		}
		// Illegal argument exception
		else if (th instanceof IllegalArgumentException) {
			IllegalArgumentException illArgEx = (IllegalArgumentException)th;
			outResponse = Response.status(Status.BAD_REQUEST)
								  .header("x-r01-errorCode",COREServiceErrorTypes.BAD_CLIENT_REQUEST.encodeAsString())
								  .header("x-r01-errorMessage",illArgEx.getMessage())
								  .header("x-r01-errorType",illArgEx.getClass().getName())
								  .entity(Throwables.getStackTraceAsString(illArgEx))
								  .type(MediaType.TEXT_HTML)
								  .build();
		}
		// any other exception type
		else {
			//th.printStackTrace();
			outResponse = Response.status(Status.INTERNAL_SERVER_ERROR)
								  .header("x-r01-errorCode",COREServiceErrorTypes.SERVER_ERROR.encodeAsString())
								  .header("x-r01-errorMessage",th.getMessage())
								  .header("x-r01-errorType",th.getClass().getName())
								  .entity(Throwables.getStackTraceAsString(th))
								  .type(MediaType.TEXT_HTML)
								  .build();
		}
		return outResponse;
	}
}

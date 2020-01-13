package r01f.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RESTExceptionMappers {
/////////////////////////////////////////////////////////////////////////////////////////
//  RESTPersistenceExceptionMapper
/////////////////////////////////////////////////////////////////////////////////////////

	public abstract static class RESTPersistenceExceptionMapper 
			extends RESTExceptionMapperBase<PersistenceException>		
	     implements ExceptionMapper<PersistenceException> {
		
		public RESTPersistenceExceptionMapper() {
			super();
		}
		
		public RESTPersistenceExceptionMapper(final MediaType mediaType, final ExceptionToReponseEntity<PersistenceException> transformer) {
			super(mediaType,transformer);
		 }
	
		@Override
		public Response toResponse(final PersistenceException persistenceException) {			
			Response outResponse = _handleThrowable(persistenceException);
			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RESTUncaughtExceptionMapper
/////////////////////////////////////////////////////////////////////////////////////////
	public static abstract class RESTUncaughtExceptionMapper 
			extends RESTExceptionMapperBase<Throwable>
	       implements ExceptionMapper<Throwable> {
		
		public RESTUncaughtExceptionMapper() {
			super();
		}		
		public RESTUncaughtExceptionMapper(final MediaType mediaType, final ExceptionToReponseEntity<Throwable> transformer) {
			super(mediaType,transformer);
		 }
		
		@Override
		public Response toResponse(final Throwable th) {
			return _handleThrowable(th);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Exception to Response 
/////////////////////////////////////////////////////////////////////////////////////////
	@FunctionalInterface
	public static interface ExceptionToReponseEntity<T extends Throwable> {	
		Object from( int errorCode, final T th); // Entity of rest requires 'Object'
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  RESTExceptionMapperBase
/////////////////////////////////////////////////////////////////////////////////////////	
	 abstract static class RESTExceptionMapperBase<T extends Throwable>  {
		 
		 protected final MediaType _mediaType;
		 protected final ExceptionToReponseEntity<T> _transformer;

		 public RESTExceptionMapperBase() {// This has been the default behaviour for many years...
			_mediaType = MediaType.TEXT_HTML_TYPE;
			_transformer =  ( error, th )  -> { return  Throwables.getStackTraceAsString(th); };
		}		 
		 public RESTExceptionMapperBase(final MediaType mediaType, final ExceptionToReponseEntity<T> transformer) {
			_mediaType = mediaType;
			_transformer = transformer;
		} 		 
		/**
		 * Maps an exception to an {@link HttpResponse}
		 * The exception is built back at client side type: r01f.services.client.servicesproxy.rest.RESTResponseToCRUDResultMapperForModelObject
		 * @param th
		 * @return
		 */
		@SuppressWarnings("unchecked")
		protected  Response _handleThrowable(final Throwable th) {
			// Print stack trace before any treatment (cause this could fail and mask the original Exception!!!!!) 
			//th.printStackTrace();
			log.warn( " Error: {}", Throwables.getStackTraceAsString(th));
			// serialize 
			Response outResponse = null;
			// Persistence exceptions
			if (th instanceof COREServiceException) {				
				COREServiceException coreEx = (COREServiceException)th;
				COREServiceErrorType coreErrorType = coreEx.getType();
				 log.error(" COREServiceException, coreErrorType {} ", coreErrorType);
				// server errors
				if (coreEx.isServerError()) {
					// Server Error
					// force exception stack trace print
					log.error(" COREServiceException, server error");
					outResponse = Response.status(Status.INTERNAL_SERVER_ERROR)
											  .header("x-r01-errorCode",COREServiceErrorTypes.SERVER_ERROR.encodeAsString())
											  .header("x-r01-extErrorCode",coreEx.getExtendedCode())
											  .header("x-r01-errorMessage",coreEx.getMessage())
											  .header("x-r01-requestedOperation",coreEx.getCalledMethod().asString())
											  .header("x-r01-errorType",coreEx.getClass().getName())
											  .entity(_transformer.from(COREServiceErrorTypes.SERVER_ERROR.getCode(), (T) th))
											  .type(_mediaType)
										  .build();
				} 
				// client errors
				else if (coreEx.isClientError()) {	
					
					// record not found
					if (coreErrorType.is(PersistenceServiceErrorTypes.ENTITY_NOT_FOUND)) {		
						outResponse = Response.status(Status.NOT_FOUND)						
											  .header("x-r01-errorCode",coreErrorType.encodeAsString())
											  .header("x-r01-extErrorCode",coreEx.getExtendedCode())
											  .header("x-r01-errorMessage",coreEx.getMessage())
											  .header("x-r01-requestedOperation",coreEx.getCalledMethod().asString())
											  .header("x-r01-errorType",coreEx.getClass().getName())
											  .entity(_transformer.from(Status.NOT_FOUND.getStatusCode(), (T) th))
											  .type(_mediaType)
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
											  .entity(_transformer.from(Status.CONFLICT.getStatusCode(), (T) th))
											  .type(_mediaType)
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
											  .entity(_transformer)
											  .entity(_transformer.from(Status.BAD_REQUEST.getStatusCode(), (T) th))
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
									  .entity(_transformer.from(Status.BAD_REQUEST.getStatusCode(), (T) th))
									  .type(_mediaType)
									  .build();
			}
			// any other exception type
			else {
				//th.printStackTrace();
				log.error(" Error {}", th.getLocalizedMessage());;
				outResponse = Response.status(Status.INTERNAL_SERVER_ERROR)
									  .header("x-r01-errorCode",COREServiceErrorTypes.SERVER_ERROR.encodeAsString())
									  .header("x-r01-errorMessage",th.getMessage())
									  .header("x-r01-errorType",th.getClass().getName())
									  .entity(_transformer.from(Status.INTERNAL_SERVER_ERROR.getStatusCode(), (T) th))
									  .type(_mediaType)
									  .build();
			}
			return outResponse;
		}
	}
}

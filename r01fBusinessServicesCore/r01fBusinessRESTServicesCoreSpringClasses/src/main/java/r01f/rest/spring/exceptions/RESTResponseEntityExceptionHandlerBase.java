package r01f.rest.spring.exceptions;



import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import r01f.httpclient.HttpResponse;
import r01f.model.persistence.PersistenceException;
import r01f.model.persistence.PersistenceRequestedOperation;
import r01f.model.persistence.PersistenceServiceErrorTypes;
import r01f.model.services.COREServiceErrorType;
import r01f.model.services.COREServiceErrorTypes;


@ControllerAdvice
public class RESTResponseEntityExceptionHandlerBase
		 extends ResponseEntityExceptionHandler {

	 @ExceptionHandler(value= Throwable.class )
	 protected ResponseEntity<Throwable> handleConflict(final Throwable ex, final WebRequest request) {
	        return _handleThrowable(ex);
	 }

	 @ExceptionHandler(value= PersistenceException.class )
	 protected ResponseEntity<PersistenceException> handleConflictPersistenceException(final PersistenceException ex, final WebRequest request) {
	        return _handleConflictPersistenceException(ex);
	 }
//////////////////////////////////////////////////////////////////////////////////////////////
// SHOW ERROR
/////////////////////////////////////////////////////////////////////////////////////////////
	private static ResponseEntity<PersistenceException> _handleConflictPersistenceException(final PersistenceException persistEx) {
		ResponseEntity<PersistenceException> outResponse = null;
		if (persistEx.isServerError()) {			// Server Error
				// force exception stack trace print
				outResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
													  .contentType(MediaType.APPLICATION_JSON)
													  .header("x-r01-errorCode",COREServiceErrorTypes.SERVER_ERROR.encodeAsString())
													  .header("x-r01-extErrorCode",Integer.toString(persistEx.getExtendedCode()))
													  .header("x-r01-errorMessage",persistEx.getMessage())
													  .header("x-r01-requestedOperation",persistEx.getCalledMethod().asString())
													  .header("x-r01-errorType",persistEx.getClass().getName())
													  .body(persistEx);


			}
			// client errors
			else if (persistEx.isClientError()) {
				COREServiceErrorType coreErrorType = persistEx.getType();
				
				// record not found
				if (coreErrorType.is(PersistenceServiceErrorTypes.ENTITY_NOT_FOUND)) {
					outResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
													  .contentType(MediaType.APPLICATION_JSON)
													  .header("x-r01-errorCode",coreErrorType.encodeAsString())
													  .header("x-r01-extErrorCode",Integer.toString(persistEx.getExtendedCode()))
													  .header("x-r01-errorMessage",persistEx.getMessage())
													  .header("x-r01-requestedOperation",persistEx.getCalledMethod().asString())
													  .header("x-r01-errorType",persistEx.getClass().getName())
													  .body(persistEx);

				}
				// update requested but record existed OR the server version is different (optimistic locking)
				else if (persistEx.getCalledMethod().isIn(PersistenceRequestedOperation.UPDATE.getCOREServiceMethod(),
														  PersistenceRequestedOperation.CREATE.getCOREServiceMethod())
				      && coreErrorType.isIn(PersistenceServiceErrorTypes.ENTITY_ALREADY_EXISTS,
				    		  				PersistenceServiceErrorTypes.OPTIMISTIC_LOCKING_ERROR)) {
					outResponse = ResponseEntity.status(HttpStatus.CONFLICT)
												  .contentType(MediaType.APPLICATION_JSON)
												  .header("x-r01-errorCode",coreErrorType.encodeAsString())
												  .header("x-r01-extErrorCode",Integer.toString(persistEx.getExtendedCode()))
												  .header("x-r01-errorMessage",persistEx.getMessage())
												  .header("x-r01-requestedOperation",persistEx.getCalledMethod().asString())
												  .header("x-r01-errorType",persistEx.getClass().getName())
												  .body(persistEx);
				}
				// another bad client request
				else {
					outResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST)
										  .header("x-r01-errorCode",coreErrorType.encodeAsString())
										  .header("x-r01-extErrorCode",Integer.toString(persistEx.getExtendedCode()))
										  .header("x-r01-errorMessage",persistEx.getMessage())
										  .header("x-r01-requestedOperation",persistEx.getCalledMethod().asString())
										  .header("x-r01-errorType",persistEx.getClass().getName())
										  .body(persistEx);
			}
		}
		return outResponse;

	}


	/**
	 * Maps an exception to an {@link HttpResponse}
	 * The exception is built back at client side type: r01f.services.client.servicesproxy.rest.RESTResponseToCRUDResultMapperForModelObject
	 * @param th
	 * @return
	 */
	private static ResponseEntity<Throwable> _handleThrowable(final Throwable th) {
		// Print stack trace before any treatment (cause this could fail and mask the original Exception!!!!!)
		th.printStackTrace();
		// serialize
		ResponseEntity<Throwable> outResponse = null;
	    /*if (th instanceof IllegalArgumentException) {
			IllegalArgumentException illArgEx = (IllegalArgumentException)th;
			outResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST)
											  .header("x-r01-errorCode",PersistenceErrorType.BAD_REQUEST_DATA.name())
											  .header("x-r01-errorMessage",illArgEx.getMessage())
											  .header("x-r01-errorType",illArgEx.getClass().getName())
											  .body(illArgEx);
		}
		// any other exception type
		else {*/
			//th.printStackTrace();
			outResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
											  .header("x-r01-errorCode",COREServiceErrorTypes.SERVER_ERROR.encodeAsString())
											  .header("x-r01-errorMessage",th.getMessage())
											  .header("x-r01-errorType",th.getClass().getName())
											  .body(th);

		/*}*/

		return outResponse;
	}



}

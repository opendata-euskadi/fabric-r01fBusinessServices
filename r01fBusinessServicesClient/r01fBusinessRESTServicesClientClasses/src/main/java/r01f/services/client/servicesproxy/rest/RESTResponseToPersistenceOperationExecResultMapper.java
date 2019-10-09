package r01f.services.client.servicesproxy.rest;

import com.google.common.reflect.TypeToken;

import r01f.exceptions.Throwables;
import r01f.httpclient.HttpResponse;
import r01f.model.persistence.PersistenceException;
import r01f.model.persistence.PersistenceOperationExecResultBuilder;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.services.COREServiceErrorTypes;
import r01f.model.services.COREServiceMethod;
import r01f.objectstreamer.Marshaller;
import r01f.securitycontext.SecurityContext;
import r01f.services.COREServiceProxyException;
import r01f.types.url.Url;
import r01f.util.types.Strings;

public class RESTResponseToPersistenceOperationExecResultMapper {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Marshaller _marshaller;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTResponseToPersistenceOperationExecResultMapper(final Marshaller marshaller) {
		_marshaller = marshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Maps the entity contained in the {@link HttpResponse}
	 * @param securityContext
	 * @param restResourceUrl
	 * @param httpResponse
	 * @param Class<T> expectedType
	 * @return
	 * @throws PersistenceException
	 */
	public <T> PersistenceOperationResult<T> mapHttpResponse(final SecurityContext securityContext,
															 	 final Url restResourceUrl,final HttpResponse httpResponse) {
		PersistenceOperationResult<T> outResult = null;
		if (httpResponse.isSuccess()) {
			outResult = _mapHttpResponseForSuccess(securityContext,
												   restResourceUrl,httpResponse);
		} else {
			outResult = _mapHttpResponseForError(securityContext,
												 restResourceUrl,httpResponse);
		}
		return outResult;
	}
	@SuppressWarnings({ "unused","serial" })
	protected <T> PersistenceOperationResult<T> _mapHttpResponseForSuccess(final SecurityContext securityContext,
												   	   			  		   	   final Url restResourceUrl,final HttpResponse httpResponse) {
		PersistenceOperationResult<T> outOperationResult = null;
		
		// [0] - Load the response		
		String responseStr = httpResponse.loadAsString();		// DO not move!!
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));
		// [1] - Map the response
		outOperationResult = _marshaller.forReading()
										.fromXml(responseStr,
												 new TypeToken<PersistenceOperationResult<T>>() { /* nothing */ });
		
		// [2] - Return
		return outOperationResult;
	}
	protected static <T> PersistenceOperationResult<T> _mapHttpResponseForError(final SecurityContext securityContext,
																			    final Url restResourceUrl,final HttpResponse httpResponse) {
		PersistenceOperationResult<T> outOpError = null;
		
		// [0] - Load the http response text
		String responseStr = httpResponse.loadAsString();
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));
		String requestedOpStr = httpResponse.getSingleValuedHeaderAsString("x-r01-requestedOperation");
		COREServiceMethod reqIp = requestedOpStr != null ? COREServiceMethod.named(requestedOpStr)
													     : COREServiceMethod.UNKNOWN;
		
		// [1] - Server error (the request could NOT be processed)
		if (httpResponse.isServerError()) {
			outOpError = PersistenceOperationExecResultBuilder.using(securityContext)
															  .notExecuted(reqIp)
															  .because(responseStr,
																	   COREServiceErrorTypes.SERVER_ERROR);
		}
		// [2] - Error while request processing: the PersistenceOperationExecError comes INSIDE the response
		else {
			outOpError = PersistenceOperationExecResultBuilder.using(securityContext)
															  .notExecuted(reqIp)
															  .because(responseStr,
																	   COREServiceErrorTypes.BAD_CLIENT_REQUEST);
		}
		// [4] - Return the CRUDOperationResult
		return outOpError;
	}
}

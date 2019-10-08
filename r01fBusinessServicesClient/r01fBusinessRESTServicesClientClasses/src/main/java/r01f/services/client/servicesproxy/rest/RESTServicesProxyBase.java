package r01f.services.client.servicesproxy.rest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpResponse;
import r01f.httpclient.HttpResponseCode;
import r01f.model.services.COREServiceErrorTypes;
import r01f.model.services.COREServiceException;
import r01f.model.services.COREServiceMethod;
import r01f.objectstreamer.Marshaller;
import r01f.securitycontext.SecurityContext;
import r01f.services.client.servicesproxy.rest.RESTServiceResourceUrlPathBuilders.RESTServiceResourceUrlPathBuilder;
import r01f.services.interfaces.ProxyForRESTImplementedService;
import r01f.types.Path;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.types.url.UrlQueryString;
import r01f.types.url.Urls;
import r01f.util.types.Strings;


/**
 * Encapsulates all the HTTP stuff of REST calls
 */
@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class RESTServicesProxyBase 
		   implements ProxyForRESTImplementedService {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
			protected final Marshaller _marshaller;
	
	@Getter private final RESTServiceResourceUrlPathBuilder _servicesRESTResourceUrlPathBuilder;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  PATH BUILDING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a concrete subtype of {@link RESTServiceResourceUrlPathBuilder} used to compose the service's REST resource url {@link Path}
	 * @param type
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	protected <P extends RESTServiceResourceUrlPathBuilder> P getServicesRESTResourceUrlPathBuilderAs(final Class<P> type) {
		return (P)this.getServicesRESTResourceUrlPathBuilder();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Throws an error for an erroneous response
	 * @param securityContext
	 * @param restResourceUrl
	 * @param httpResponse
	 * @throws ServiceException
	 */
	protected static void _throwServiceExceptionFor(final SecurityContext securityContext,
												    final Url restResourceUrl,
												    final HttpResponse httpResponse) throws COREServiceException {
		HttpResponseCode responseCode = httpResponse.getCode();
		String errorCodeStr = httpResponse.getSingleValuedHeaderAsString("x-r01-errorCode");
		String extErrorCodeStr = httpResponse.getSingleValuedHeaderAsString("x-r01-extErrorCode");
		String errorMsg = httpResponse.getSingleValuedHeaderAsString("x-r01-errorMessage");
		String coreMethodStr = httpResponse.getSingleValuedHeaderAsString("x-r01-requestedOperation");
		String errorDetail = httpResponse.loadAsString();

		COREServiceMethod coreMethod = Strings.isNOTNullOrEmpty(coreMethodStr) ? COREServiceMethod.named(coreMethodStr)
																			   : COREServiceMethod.UNKNOWN;
		int extErrCode = Strings.isNOTNullOrEmpty(extErrorCodeStr) ? Integer.parseInt(extErrorCodeStr)
																   : -1;
		if (errorMsg == null) { 
			errorMsg = Strings.customized("The REST service at {} returned an unknown error",
										  restResourceUrl);
		} else {
			errorMsg = Strings.customized("The REST service at {} returned a {}/{} error: {}",
										  restResourceUrl,errorCodeStr,extErrorCodeStr,errorMsg);	
		}
		log.error(errorDetail);
		
		// Throw a client or server error
		if (responseCode == HttpResponseCode.BAD_REQUEST
		 || responseCode == HttpResponseCode.METHOD_NOT_ALLOWED 
		 || responseCode == HttpResponseCode.NOT_FOUND) {
			throw new COREServiceException(coreMethod,
										   COREServiceErrorTypes.BAD_CLIENT_REQUEST,extErrCode,
										   errorMsg);
		} 
		throw new COREServiceException(coreMethod,
									   COREServiceErrorTypes.SERVER_ERROR,extErrCode,
									   errorMsg);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Composes the complete REST endpoint URI for a path
	 * @param path
	 * @return
	 */
	protected Url composeURIFor(final UrlPath path) {
		RESTServiceResourceUrlPathBuilder pathBuilder = this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilder.class);
		return Urls.join(pathBuilder.getHost(),
						 pathBuilder.getEndPointBasePath().joinedWith(path));
	}
	/**
	 * Composes the complete REST endpoint URI for a path
	 * @param path
	 * @param qryString
	 * @return
	 */
	protected Url composeURIFor(final UrlPath path,
								final UrlQueryString qryString) {
		RESTServiceResourceUrlPathBuilder pathBuilder = this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilder.class);
		return Urls.join(pathBuilder.getHost(),
						 pathBuilder.getEndPointBasePath().joinedWith(path),
						 qryString);
	}
}

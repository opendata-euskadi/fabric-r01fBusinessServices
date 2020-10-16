package r01f.services.client.servicesproxy.rest;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpClient;
import r01f.httpclient.HttpRequestHeader;
import r01f.httpclient.HttpRequestPayload;
import r01f.httpclient.HttpResponse;
import r01f.mime.MimeType;
import r01f.mime.MimeTypes;
import r01f.objectstreamer.Marshaller;
import r01f.services.COREServiceProxyException;
import r01f.types.url.Url;
import r01f.util.types.Strings;

@Slf4j
public abstract class DelegateForRawREST {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Objects to xml/json marshaller
	 */
	protected final Marshaller _marshaller;
	/**
	 * Maps from the REST Response to the returned object
	 */
	protected final RESTResponseToResultMapper _responseToResultMapper;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DelegateForRawREST(final Marshaller marshaller) {
		_marshaller = marshaller;
		_responseToResultMapper = new RESTResponseToResultMapper(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected RESTResponseToResultMapper getResponseToResultMapper() {
		return _responseToResultMapper;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	HEAD
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpResponse HEAD(final Url restResourceUrl,
								   final String securityContextXml) {
		return HEAD(restResourceUrl, new HttpRequestHeader("securityContext",securityContextXml));
	}
	public static HttpResponse HEAD(final Url restResourceUrl,
								   final HttpRequestHeader securityHeader,
								   final HttpRequestHeader... otherHeaders) {
		log.warn("\t\tHEAD resource: {}",restResourceUrl);
			
		HttpResponse outHttpResponse = null;
		try {
			outHttpResponse = HttpClient.forUrl(restResourceUrl)
									    // Security Header
										.withHeader(securityHeader.getName(),securityHeader.getValue())
										.withHeaders(otherHeaders)		// any additional header
									    .HEAD()
									  	.getResponse()
									  		.directNoAuthConnected();
		} catch (final IOException ioEx) {
			ioEx.getLocalizedMessage();
			log.error("Error while HEADing {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}
		return outHttpResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GET
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpResponse GET(final Url restResourceUrl,
								   final String securityContextXml) {
		return GET(restResourceUrl, new HttpRequestHeader("securityContext",securityContextXml));
	}
	public static HttpResponse GET(final Url restResourceUrl,
								   final HttpRequestHeader securityHeader,
								   final HttpRequestHeader... otherHeaders) {
		log.warn("\t\tGET resource: {}",restResourceUrl);
			
		HttpResponse outHttpResponse = null;
		try {
			outHttpResponse = HttpClient.forUrl(restResourceUrl)
									    // Security Header
										 .withHeader(securityHeader.getName(),securityHeader.getValue())
										 .withHeaders(otherHeaders)		// any additional header
									     .GET()
									  	 .getResponse()
									  		.directNoAuthConnected();
		} catch (final IOException ioEx) {
			ioEx.getLocalizedMessage();
			log.error("Error while GETing {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}
		return outHttpResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	POST
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpResponse POST(final Url restResourceUrl,
									final String securityContextXml,
							     	final String entityXml,
							     	final HttpRequestHeader... headers) {
		return POST(restResourceUrl,
				    MimeTypes.APPLICATION_XML, 
				    new HttpRequestHeader("securityContext",securityContextXml),
				    entityXml,
				    headers);
	
	}
	public static HttpResponse POST(final Url restResourceUrl,
			                        final MimeType mimeType,
									final HttpRequestHeader securityHeader,
							     	final String entityAsString,							     	
							     	final HttpRequestHeader... otherHeaders) {
		log.warn("\t\tPOST resource: {}",restResourceUrl);
		HttpResponse outHttpResponse = null;
		try {
			if (Strings.isNOTNullOrEmpty(entityAsString)) {
				outHttpResponse = HttpClient.forUrl(restResourceUrl)		
											// Security Header
										    .withHeader(securityHeader.getName(),securityHeader.getValue())
											.withHeaders(otherHeaders)		// any additional header
										    .POST()
									      		.withPayload(HttpRequestPayload.wrap(entityAsString)
																			   .mimeType(mimeType))
										    .getResponse()
										    	.directNoAuthConnected();
			} else {
				outHttpResponse = HttpClient.forUrl(restResourceUrl)		
											// Security Header
								   	        .withHeader(securityHeader.getName(),securityHeader.getValue())
											.withHeaders(otherHeaders)		// any additional header
										    .POST()
										 		.withoutPayload(mimeType)
										    .getResponse()
										    	.directNoAuthConnected();
			}
		} catch (final IOException ioEx) {
			ioEx.getLocalizedMessage();
			log.error("Error while POSTing to {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}				
		return outHttpResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	PUT
/////////////////////////////////////////////////////////////////////////////////////////	
	public static HttpResponse PUT(final Url restResourceUrl,
								   final String securityContextXml,
				 			  	   final String entityXml,
				 			  	   final HttpRequestHeader... headers) {		
		return PUT(restResourceUrl,
				    MimeTypes.APPLICATION_XML, 
				    new HttpRequestHeader("securityContext",securityContextXml),
				    entityXml,
				    headers);
		
	}	
	public static HttpResponse PUT(final Url restResourceUrl,
								   final MimeType mimeType,
								   final HttpRequestHeader securityHeader,
				 			  	   final String entityAsString,
				 			  	   final HttpRequestHeader... otherHeaders) {
		log.trace("\t\tPUT resource: {}",restResourceUrl);
		HttpResponse outHttpResponse = null;
		try {
			if (entityAsString != null) {
				outHttpResponse = HttpClient.forUrl(restResourceUrl)		
											// Security Header
										    .withHeader(securityHeader.getName(),securityHeader.getValue())
										    //Additional Headers
											.withHeaders(otherHeaders)		
											.PUT()
	
											.withPayload(HttpRequestPayload.wrap(entityAsString)
																			   .mimeType(mimeType))
											.getResponse()
												.directNoAuthConnected();
			} else {
				outHttpResponse = HttpClient.forUrl(restResourceUrl)
											// Security Header
											.withHeader(securityHeader.getName(),securityHeader.getValue())
											 //Additional Headers
											.withHeaders(otherHeaders)			
											.PUT()
												.withoutPayload(mimeType)
											.getResponse()
												.directNoAuthConnected();
			}
		} catch (final IOException ioEx) {
			log.error("Error while PUTing to {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}		
		return outHttpResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DELETE
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpResponse DELETE(final Url restResourceUrl,
									  final String securityContextXml) {	
		return DELETE(restResourceUrl, new HttpRequestHeader("securityContext",securityContextXml));	
	}
	public static HttpResponse DELETE(final Url restResourceUrl, 
									  final HttpRequestHeader securityHeader,
									  final HttpRequestHeader... otherHeaders) {
		log.warn("\t\tDELETE resource: {}",restResourceUrl);
		HttpResponse outHttpResponse = null;
		try {
			outHttpResponse = HttpClient.forUrl(restResourceUrl)
										// Security Header
										  .withHeader(securityHeader.getName(),securityHeader.getValue())
										  .withHeaders(otherHeaders)		// any additional header
										.DELETE()
										.getResponse()	
											.directNoAuthConnected();
		} catch (final IOException ioEx) {
			log.error("Error while DELETEing {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}
		return outHttpResponse;	
	}
}

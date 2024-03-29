package r01f.services.client.servicesproxy.rest;

import java.lang.reflect.Type;
import java.util.Iterator;

import com.google.common.reflect.TypeToken;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.generics.ParameterizedTypeImpl;
import r01f.guids.AnyOID;
import r01f.guids.OID;
import r01f.guids.PersistableObjectOID;
import r01f.httpclient.HttpResponse;
import r01f.mime.MimeType;
import r01f.mime.MimeTypes;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CRUDError;
import r01f.model.persistence.CRUDOK;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.CRUDResultBuilder;
import r01f.model.persistence.PersistenceException;
import r01f.model.persistence.PersistenceRequestedOperation;
import r01f.model.services.COREServiceErrorType;
import r01f.objectstreamer.Marshaller;
import r01f.reflection.ReflectionUtils;
import r01f.securitycontext.SecurityContext;
import r01f.services.COREServiceProxyException;
import r01f.types.url.Url;
import r01f.util.types.Numbers;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@Accessors(prefix="_")
public class RESTResponseToCRUDResultMapperForModelObject<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
	implements RESTResponseToCRUDResultMapper<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
			protected final Marshaller _marshaller;
	@Getter protected final Class<M> _modelObjectType;
	@Getter protected final MimeType _mimeType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTResponseToCRUDResultMapperForModelObject(final Marshaller marshaller,
							    						final Class<M> modelObjectType) {
		this(marshaller,modelObjectType,MimeTypes.APPLICATION_XML);
	}
	public RESTResponseToCRUDResultMapperForModelObject(final Marshaller marshaller,
							    						final Class<M> modelObjectType,
							    						final MimeType mimeType) {
		_marshaller = marshaller;
		_modelObjectType = modelObjectType;
		_mimeType = mimeType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceCRUDResultIdOnErrorStep mapHttpResponseForEntity(final SecurityContext securityContext,
															   	 	   final PersistenceRequestedOperation requestedOp,
															   	 	   final Url restResourceUrl,final HttpResponse httpResponse) {
		return new PersistenceCRUDResultIdOnErrorStep(securityContext,
													  requestedOp,
													  restResourceUrl,httpResponse);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class PersistenceCRUDResultIdOnErrorStep {
		private final SecurityContext securityContext;
	   	private final PersistenceRequestedOperation requestedOp;
	   	private final Url restResourceUrl;
	   	private final HttpResponse httpResponse;

	   	public CRUDResult<M> identifiedOnErrorBy(final OID... oids) {
	   		CRUDResult<M> outCRUDResult = null;
	   		if (oids.length == 1) {
	   			outCRUDResult = _identifiedOnErrorBy(oids[0]);
	   		} else {
		   		StringBuilder oidStr = new StringBuilder(oids.length * 32);
		   		for (Iterator<OID> oidIt = CollectionUtils.of(oids).asCollection().iterator(); oidIt.hasNext(); ) {
		   			OID oid = oidIt.next();
		   			oidStr.append(oid.asString());
		   			if (oidIt.hasNext()) oidStr.append("/");
		   		}
		   		outCRUDResult = _identifiedOnErrorBy(AnyOID.forId(oidStr.toString()));
	   		}
	   		return outCRUDResult;
	   	}
	   	public CRUDResult<M> identifiedOnErrorBy(final String any) {
	   		return _identifiedOnErrorBy(AnyOID.forId(any));
	   	}
		private CRUDResult<M> _identifiedOnErrorBy(final OID oid) {
			CRUDResult<M> outOperationResult = null;

			System.out.println(" http response code " + httpResponse.isSuccess());
			if (httpResponse.isSuccess()) {
				outOperationResult = _mapHttpResponseForSuccess(securityContext,
																requestedOp,
																restResourceUrl,httpResponse);
			} else {
				outOperationResult = _mapHttpResponseForError(securityContext,
															  requestedOp,
															  oid,
															  restResourceUrl,httpResponse);
			}
			return outOperationResult;
		}
	}
	@Override
	public CRUDResult<M> mapHttpResponseForEntity(final SecurityContext securityContext,
												  final PersistenceRequestedOperation requestedOp,
												  final M targetEntity,
												  final Url restResourceUrl,final HttpResponse httpResponse) {
		return this.mapHttpResponseForEntity(securityContext,
											 requestedOp,
											 targetEntity.getOid(),
											 restResourceUrl,httpResponse);
	}
	@Override
	public CRUDResult<M> mapHttpResponseForEntity(final SecurityContext securityContext,
												  final PersistenceRequestedOperation requestedOp,
												  final OID targetEntityOid,
												  final Url restResourceUrl,final HttpResponse httpResponse) {
		CRUDResult<M> outOperationResult = null;

		if (httpResponse.isSuccess()) {
			outOperationResult = _mapHttpResponseForSuccess(securityContext,
															requestedOp,
															restResourceUrl,httpResponse);
		} else {
			outOperationResult = _mapHttpResponseForError(securityContext,
														  requestedOp,
														  targetEntityOid,
														  restResourceUrl,httpResponse);
		}
		return outOperationResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SUCCESS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected CRUDOK<M> _mapHttpResponseForSuccess(final SecurityContext securityContext,
												   final PersistenceRequestedOperation requestedOp,
												   final Url restResourceUrl,final HttpResponse httpResponse) {
		CRUDOK<M> outOperationResult = null;

		// [0] - Load the response
		String responseStr = httpResponse.loadAsString();		// DO not move!!
		if (Strings.isNullOrEmpty(responseStr)) {
			throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE."
																	+ " This is a developer mistake! It MUST return the target entity data into CRUDResult",  restResourceUrl));
		}
		/*	 Marshaller tries to marshall "M" just a PersistableObject Interface and NOT as the concrete class... so crashes...
			   outOperationResult = _marshaller.forReading().fromJson(responseStr,
														  new TypeToken<CRUDOK<M>>() { /* nothing */
		/*	... temporary use this dirty trick to get concrete class to marhaller , so the CRUD will JUST for modelTypes
		 *  		 (TypeToken<CRUDOK<M>>) TypeToken.of( new ParameterizedTypeImpl(CRUDOK.class, modelTypes, null));
		 */
		Type[] modelTypes = { _modelObjectType };
		TypeToken<CRUDOK<M>> typeToken =  (TypeToken<CRUDOK<M>>)TypeToken.of(new ParameterizedTypeImpl(CRUDOK.class,
																									   modelTypes,
																									   null));			// owner type


		// [1] - Map the response
		if (_mimeType.is(MimeTypes.APPLICATION_XML)) {
			outOperationResult = _marshaller.forReading().fromXml(responseStr,typeToken);

		} else if (_mimeType.is(MimeTypes.APPLICATION_JSON)) {
			outOperationResult = _marshaller.forReading().fromJson(responseStr,typeToken);


		} else {
			throw new IllegalArgumentException(Strings.customized("{} mimeType not suported",
																  _mimeType)) ;
		}
		if (outOperationResult == null) {
			throw new COREServiceProxyException(Throwables.message("The REST service {}"
																	+ " worked BUT RESPONSE:  {}"
																	+ "  ....CANNOT BE PARSED to a  CRUDResult object for {}" ,
																			restResourceUrl, responseStr , _modelObjectType));
		}
		// [2] - Return
		return outOperationResult;
	}
/////////////////////////////////////////////////////////////////////////////////
//  ERROR
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * Builds back a {@link CRUDError} object from the {@link HttpResponse} object
	 * The exception was mapped to the {@link HttpResponse} at server type r01f.rest.RESTExceptionMappers object
	 * @param securityContext
	 * @param requestedOp
	 * @param requestedOid
	 * @param restResourceUrl
	 * @param httpResponse
	 * @return
	 */
	protected CRUDError<M> _mapHttpResponseForError(final SecurityContext securityContext,
												    final PersistenceRequestedOperation requestedOp,
												    final OID requestedOid,
												    final Url restResourceUrl,final HttpResponse httpResponse) {
		CRUDError<M> outOpError = null;
		log.error("\n Http Response Code:  {}", httpResponse.getCode());
		// [0] - Load the http response text
		String responseStr = httpResponse.loadAsString();
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   	   restResourceUrl));

		// [1] - Server error (the request could NOT be processed)
		if (httpResponse.isServerError()) {
			outOpError = CRUDResultBuilder.using(securityContext)
										  .on(_modelObjectType)
										  .not(requestedOp)
										  .becauseServerError(responseStr)	// the rest endpoint response is the error as TEXT
										 		.about(requestedOid).build();
		}
		// [2] - Client error (the client sent an unprocessable entity)
		if (httpResponse.isClientError()) {
			if (httpResponse.isNotFound()) {
				// Not found
				outOpError = CRUDResultBuilder.using(securityContext)
											  .on(_modelObjectType)
											  .not(requestedOp)
											  .becauseClientRequestedEntityWasNOTFound()
											 		 .about(requestedOid).build();
			} else {
				// other client errors: entity update conflict, illegal argument, etc
				String errorCodeHeader = httpResponse.getSingleValuedHeaderAsString("x-r01-errorCode");
				String extErrorCodeHeader = httpResponse.getSingleValuedHeaderAsString("x-r01-extErrorCode");
				String errorMessageHeader = httpResponse.getSingleValuedHeaderAsString("x-r01-errorMessage");
				//String requestedOperationHeader = httpResponse.getSingleValuedHeaderAsString("x-r01-requestedOperation");
				String errorJavaTypeHeader = httpResponse.getSingleValuedHeaderAsString("x-r01-errorType");

				Class<? extends Throwable> errorJavaType = null;
				if (Strings.isNOTNullOrEmpty(errorJavaTypeHeader)) errorJavaType = ReflectionUtils.typeFromClassName(errorJavaTypeHeader);

				if (errorJavaType != null && ReflectionUtils.isSubClassOf(errorJavaType,PersistenceException.class)) {
					COREServiceErrorType persistErrorType = Strings.isNOTNullOrEmpty(errorCodeHeader)
																		? COREServiceErrorType.fromEncodedString(errorCodeHeader)
																		: null;
					int extErrorCode = Strings.isNOTNullOrEmpty(extErrorCodeHeader) ? Numbers.toInt(extErrorCodeHeader) : -1;

					if (persistErrorType != null) {
						outOpError = CRUDResultBuilder.using(securityContext)
													  .on(_modelObjectType)
													  .not(requestedOp)
													  .becauseClientError(persistErrorType,errorMessageHeader)
													  		.about(requestedOid)
													  		.buildWithExtendedErrorCode(extErrorCode);
					}
				}
				// The exception type is unknown... but it's sure it's a client bad request
				if (outOpError == null) {
					outOpError = CRUDResultBuilder.using(securityContext)
												  .on(_modelObjectType)
												  .not(requestedOp)
												  .becauseClientBadRequest(errorMessageHeader)
												  		.about(requestedOid).build();
				}
			}
		}
		// [3] - Unknown error
		else {
			outOpError = CRUDResultBuilder.using(securityContext)
										  .on(_modelObjectType)
										  .not(requestedOp)
										  .becauseServerError(responseStr)	// the rest endpoint response is the error as TEXT
										 		.about(requestedOid).build();
		}
		// [4] - Return the CRUDOperationResult
		return outOpError;
	}
	protected CRUDError<M> _mapHttpResponseForError(final SecurityContext securityContext,
												    final PersistenceRequestedOperation requestedOp,
												    final M requestedEntity,
												    final Url restResourceUrl,final HttpResponse httpResponse) {
		CRUDError<M> outOpError = null;

		// [0] - Load the http response text
		String responseStr = httpResponse.loadAsString();
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   	   restResourceUrl));

		// [1] - Server error (the request could NOT be processed)
		if (httpResponse.isServerError()) {
			outOpError = CRUDResultBuilder.using(securityContext)
										  .on(_modelObjectType)
										  .not(requestedOp)
										  .becauseServerError(responseStr)	// the rest endpoint response is the error as TEXT
										 		.about(requestedEntity).build();
		}
		// [2] - Client error (the client sent a not processable entity)
		else if (httpResponse.isClientError()) {
			if (httpResponse.isNotFound()) {
				// Not found
				outOpError = CRUDResultBuilder.using(securityContext)
											  .on(_modelObjectType)
											  .not(requestedOp)
											  .becauseClientCannotConnectToServer(restResourceUrl)
											 		.about(requestedEntity.getOid()).build();
			} else {
				// other client errors: entity update conflict, illegal argument, etc
				//String errorCode = httpResponse.getSingleValuedHeaderAsString("x-r01-errorCode");
				String errorMessage = httpResponse.getSingleValuedHeaderAsString("x-r01-errorMessage");
				outOpError = CRUDResultBuilder.using(securityContext)
											  .on(_modelObjectType)
											  .not(requestedOp)
											  .becauseClientBadRequest(errorMessage)
											  		.about(requestedEntity).build();
			}
		}
		// [3] - Unknown error
		else {
			outOpError = CRUDResultBuilder.using(securityContext)
										  .on(_modelObjectType)
										  .not(requestedOp)
										  .becauseServerError(responseStr)	// the rest endpoint response is the error as TEXT
										 		.about(requestedEntity).build();
		}
		// [4] - Return the CRUDOperationResult
		return outOpError;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

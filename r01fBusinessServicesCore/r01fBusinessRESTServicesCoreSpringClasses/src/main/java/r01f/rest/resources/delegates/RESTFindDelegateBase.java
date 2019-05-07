package r01f.rest.resources.delegates;

import java.net.URI;
import java.util.Date;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.securitycontext.SecurityContext;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.types.Range;

/**
 * Base type for REST services that encapsulates the common CRUD ops>
 */
@Accessors(prefix="_")
public abstract class RESTFindDelegateBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
	          extends RESTDelegateForModelObjectBase<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final FindServicesForModelObject<O,M> _findServices;

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <F extends FindServicesForModelObject<O,M>> F getFindServicesAs(@SuppressWarnings("unused") final Class<F> type) {
		return (F)_findServices;
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTFindDelegateBase(final Class<M> modelObjectType,
								final FindServicesForModelObject<O,M> findServices) {
		super(modelObjectType);
		_findServices = findServices;

	}

/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds all persisted model object oids
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public ResponseEntity<FindOIDsResult<O>> findAll(final SecurityContext securityContext,final String resourcePath,final MediaType mediaType) {
		FindOIDsResult<O> findResult = _findServices.findAll(securityContext);
		ResponseEntity<FindOIDsResult<O>> outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																				    	.at(URI.create(resourcePath))
																				    	.withContentType(mediaType)
																				    .build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids which create date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param createDate
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public ResponseEntity<FindOIDsResult<O>> findByCreateDate(final SecurityContext securityContext,final String resourcePath,final MediaType mediaType,
									 						   final Range<Date> createDate) {
		FindOIDsResult<O> findResult = _findServices.findByCreateDate(securityContext,
																	  createDate);
		ResponseEntity<FindOIDsResult<O>>  outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																						.at(URI.create(resourcePath))
																						.withContentType(mediaType)
																					.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids which last update date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param lastUpdateDate
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public ResponseEntity<FindOIDsResult<O>> findByLastUpdateDate(final SecurityContext securityContext,final String resourcePath,final MediaType mediaType,
																  final Range<Date> lastUpdateDate) {
		FindOIDsResult<O> findResult = _findServices.findByLastUpdateDate(securityContext,
																		 lastUpdateDate);
		ResponseEntity<FindOIDsResult<O>> outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.withContentType(mediaType)
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids created by the provided user
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param creatorUserCode
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public ResponseEntity<FindOIDsResult<O>> findByCreator(final SecurityContext securityContext,final String resourcePath,final MediaType mediaType,
														   final UserCode creatorUserCode) {
		FindOIDsResult<O> findResult = _findServices.findByCreator(securityContext,
																   creatorUserCode);
		ResponseEntity<FindOIDsResult<O>> outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
															                          .at(URI.create(resourcePath))
															                          .withContentType(mediaType)
															                         .build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids last updated by the provided user
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param lastUpdtorUserCode
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public ResponseEntity<FindOIDsResult<O>>  findByLastUpdator(final SecurityContext securityContext,final String resourcePath,final MediaType mediaType,
																final UserCode lastUpdtorUserCode) {
		FindOIDsResult<O> findResult = _findServices.findByLastUpdator(securityContext,
																	   lastUpdtorUserCode);

		ResponseEntity<FindOIDsResult<O>> outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																						  .at(URI.create(resourcePath))
																						  .withContentType(mediaType)
																					.build(findResult);
		return outResponse;
	}
}

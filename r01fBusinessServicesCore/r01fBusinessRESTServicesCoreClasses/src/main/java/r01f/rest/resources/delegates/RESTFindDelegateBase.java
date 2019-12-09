package r01f.rest.resources.delegates;

import java.net.URI;
import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable;
import r01f.model.persistence.FindOIDsResult;
import r01f.rest.RESTOperationsResponseBuilder;
import r01f.rest.RESTOperationsResponseBuilder.PersistenceOperationOnObjectResulCollectionToReponseEntity;
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
	protected final MediaType _mediaType;	
	protected final PersistenceOperationOnObjectResulCollectionToReponseEntity<M> _transformer;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <F extends FindServicesForModelObject<O,M>> F getFindServicesAs(final Class<F> type) {
		return (F)_findServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTFindDelegateBase(final Class<M> modelObjectType,
							    final FindServicesForModelObject<O,M> persistenceServices) {
		this(modelObjectType, persistenceServices, null, null);
	}	
	public RESTFindDelegateBase(final Class<M> modelObjectType,
							    final FindServicesForModelObject<O,M> persistenceServices,
							    final MediaType mediaType ) {
		this(modelObjectType,persistenceServices,mediaType, null );		
	}	
	public RESTFindDelegateBase(final Class<M> modelObjectType,
							    final FindServicesForModelObject<O,M> findServices,
							    final MediaType mediaType,
							    final PersistenceOperationOnObjectResulCollectionToReponseEntity<M> transformer) {
		super(modelObjectType);
		_findServices = findServices;
		_mediaType =    ( mediaType   != null )?  mediaType :
			                                      MediaType.APPLICATION_XML_TYPE;
		_transformer =  ( transformer != null )?  transformer :        //Default transformer received via Constructor or created in-situ with a default behaviour (returning CRUDResult)
			                                      c -> {return c ; }; 
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds all persisted model object oids
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findAll(final SecurityContext securityContext,final String resourcePath) {
		FindOIDsResult<O> findResult = _findServices.findAll(securityContext);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
															    .at(URI.create(resourcePath))
															    .mediaType(_mediaType)
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids which create date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param createDate
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByCreateDate(final SecurityContext securityContext,final String resourcePath,
									 final Range<Date> createDate) {
		FindOIDsResult<O> findResult = _findServices.findByCreateDate(securityContext,
																	  createDate);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.mediaType(_mediaType)
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids which last update date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param lastUpdateDate
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByLastUpdateDate(final SecurityContext securityContext,final String resourcePath,
										 final Range<Date> lastUpdateDate) {
		FindOIDsResult<O> findResult = _findServices.findByLastUpdateDate(securityContext,
																		 lastUpdateDate);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.mediaType(_mediaType)
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids created by the provided user
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param creatorUserCode
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByCreator(final SecurityContext securityContext,final String resourcePath,
								  final UserCode creatorUserCode) {
		FindOIDsResult<O> findResult = _findServices.findByCreator(securityContext,
																   creatorUserCode);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.mediaType(_mediaType)
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids last updated by the provided user
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param lastUpdtorUserCode
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByLastUpdator(final SecurityContext securityContext,final String resourcePath,
									  final UserCode lastUpdtorUserCode) {
		FindOIDsResult<O> findResult = _findServices.findByLastUpdator(securityContext,
																	   lastUpdtorUserCode);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.mediaType(_mediaType)
															.build(findResult);
		return outResponse;
	}
}

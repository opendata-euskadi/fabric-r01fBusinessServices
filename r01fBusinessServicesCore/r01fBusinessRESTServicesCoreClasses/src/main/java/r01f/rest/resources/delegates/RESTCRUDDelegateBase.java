package r01f.rest.resources.delegates;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.experimental.Accessors;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.PersistenceException;
import r01f.rest.RESTOperationsResponseBuilder;
import r01f.rest.RESTOperationsResponseBuilder.PersistenceOperationOnObjectResulToReponseEntity;
import r01f.securitycontext.SecurityContext;
import r01f.services.interfaces.CRUDServicesForModelObject;

/**
 * Base type for REST services that encapsulates the common CRUD ops
 */
@Accessors(prefix="_")
public abstract class RESTCRUDDelegateBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
	          extends RESTDelegateForModelObjectBase<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final CRUDServicesForModelObject<O,M> _persistenceServices;
	protected final MediaType _mediaType;
	protected final PersistenceOperationOnObjectResulToReponseEntity<M> _transformer;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <C extends CRUDServicesForModelObject<O,M>> C getCRUDServicesAs(final Class<C> type) {
		return (C)_persistenceServices;
	}
	@SuppressWarnings("unchecked")
	public <R extends RESTCRUDDelegateBase<O,M>> R getRESTCRUDDelegateAs(final Class<R> type) {
		return (R)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTCRUDDelegateBase(final Class<M> modelObjectType,
							    final CRUDServicesForModelObject<O,M> persistenceServices) {
		this(modelObjectType, persistenceServices, null, null);
	}
	public RESTCRUDDelegateBase(final Class<M> modelObjectType,
							    final CRUDServicesForModelObject<O,M> persistenceServices,
							    final MediaType mediaType ) {
		this(modelObjectType,persistenceServices,mediaType, null );
	}

	public RESTCRUDDelegateBase(final Class<M> modelObjectType,
							    final CRUDServicesForModelObject<O,M> persistenceServices,
							    final MediaType mediaType,
							    final PersistenceOperationOnObjectResulToReponseEntity<M> transformer) {
		super(modelObjectType);
		_persistenceServices = persistenceServices;
		_mediaType =    ( mediaType   != null )?  mediaType :
			                                      MediaType.APPLICATION_XML_TYPE;
		_transformer =  ( transformer != null )?  transformer :        //Default transformer received via Constructor or created in-situ with a default behaviour (returning CRUDResult)
			                                      c -> {return c ; };
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PERSISTENCE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param oid
	 * @return
	 * @throws PersistenceException
	 */
	public Response load(final SecurityContext securityContext,final String resourcePath,
						 final O oid) throws PersistenceException {
		return this.load(securityContext,
						 resourcePath,
						 _transformer,
						 oid);
	}
	/**
	 * Loads a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param transformer if needed to transform persistence result to response entity.
	 * @param oid
	 * @return
	 * @throws PersistenceException
	 */
	public Response load(final SecurityContext securityContext,final String resourcePath,
						 final PersistenceOperationOnObjectResulToReponseEntity<M> transformer,
						 final O oid) throws PersistenceException {
		CRUDResult<M> loadResult = _persistenceServices.load(securityContext,
									  					     oid);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
															    .at(URI.create(resourcePath))
															    .mediaType(_mediaType)
															    .withPersistenceOperationTransformer(transformer)
															.build(loadResult);
		return outResponse;
	}

    /**
     * Creates a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param modelObject
	 * @return
	 * @throws PersistenceException
	 */
	public Response create(final SecurityContext securityContext,final String resourcePath,
						   final M modelObject) throws PersistenceException {
		 return create(securityContext,resourcePath,_transformer,modelObject);
	}
	/**
	 * Creates a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param transformer if needed to transform persistence result to response entity.
	 * @param modelObject
	 * @return
	 * @throws PersistenceException
	 */
	public Response create(final SecurityContext securityContext,final String resourcePath,
						   final PersistenceOperationOnObjectResulToReponseEntity<M> transformer,
						   final M modelObject) throws PersistenceException {
		CRUDResult<M> createResult = _persistenceServices.create(securityContext,
										   	   					 modelObject);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
															    .at(URI.create(resourcePath))
															    .mediaType(_mediaType)
															    .withPersistenceOperationTransformer(transformer)
														    .build(createResult);
		return outResponse;
	}

	/**
	 * Updates a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param modelObject
	 * @return
	 * @throws PersistenceException
	 */
	public Response update(final SecurityContext securityContext,final String resourcePath,
						   final M modelObject) {
		return update(securityContext,resourcePath,_transformer,modelObject);
	}
	/**
	 * Updates a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param transformer if needed to transform persistence result to response entity.
	 * @param modelObject
	 * @return
	 * @throws PersistenceException
	 */
	public Response update(final SecurityContext securityContext,final String resourcePath,
						   final PersistenceOperationOnObjectResulToReponseEntity<M> transformer,
						   final M modelObject) throws PersistenceException {
		CRUDResult<M> updateResult = _persistenceServices.update(securityContext,
										   	      				 modelObject);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.mediaType(_mediaType)
																.withPersistenceOperationTransformer(transformer)
															   .build(updateResult);
		return outResponse;
	}

	/**
	 * Removes a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param transformer if needed to transform persistence result to response entity.
	 * @param oid
	 * @return
	 * @throws PersistenceException
	 */
	public Response delete(final SecurityContext securityContext,final String resourcePath,
						   final O oid) throws PersistenceException {
		CRUDResult<M> deleteResult = _persistenceServices.delete(securityContext,
																 oid);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
															     .at(URI.create(resourcePath))
															     .mediaType(_mediaType)
															     .withPersistenceOperationTransformer(_transformer)
															.build(deleteResult);
		return outResponse;
	}

	/**
	 * Removes a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param transformer if needed to transform persistence result to response entity.
	 * @param oid
	 * @return
	 * @throws PersistenceException
	 */
	public Response delete(final SecurityContext securityContext,final String resourcePath,
						   final PersistenceOperationOnObjectResulToReponseEntity<M> transformer,
						   final O oid) throws PersistenceException {
		CRUDResult<M> deleteResult = _persistenceServices.delete(securityContext,
																 oid);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
															     .at(URI.create(resourcePath))
															     .mediaType(_mediaType)
															     .withPersistenceOperationTransformer(transformer)
															.build(deleteResult);
		return outResponse;
	}
}

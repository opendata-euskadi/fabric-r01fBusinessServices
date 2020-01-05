package r01f.rest;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.exceptions.Throwables;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CRUDError;
import r01f.model.persistence.CRUDOK;
import r01f.model.persistence.CRUDOnMultipleResult;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.FindOIDsOK;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.persistence.FindOK;
import r01f.model.persistence.FindResult;
import r01f.model.persistence.FindSummariesOK;
import r01f.model.persistence.FindSummariesResult;
import r01f.model.persistence.PersistenceException;
import r01f.model.persistence.PersistenceOperationExecOK;
import r01f.model.persistence.PersistenceOperationOnObjectResult;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.search.SearchResults;
import r01f.patterns.IsBuilder;
import r01f.types.jobs.EnqueuedJob;
import r01f.util.types.collections.CollectionUtils;


/**
 * See {@link RESTServicesProxyBase}
 * Usage:
 * <pre class='brush:java'>
 * 
 * </pre>
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class RESTOperationsResponseBuilder 
  implements IsBuilder {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  A Funtional Interfaces to trasnform a Persistence Operation Result to ...
//       1. ...  CRUDResult  ( ...default behaviour... )
//       2. .... Model object included into CRUDResult.	
//       3. .... Whatever you want...
/////////////////////////////////////////////////////////////////////////////////////////
	@FunctionalInterface
	public static interface PersistenceOperationOnObjectResulToReponseEntity<T> {	
		
		Object from(final PersistenceOperationOnObjectResult<T>  persistenceOperationResult); // Entity of rest requires 'Object'
		
		default Object defaultFrom(final PersistenceOperationResult<T> crudResult) { return crudResult; }
	}
	
	@FunctionalInterface
	public static interface PersistenceOperationOnObjectResulCollectionToReponseEntity<T>
	               extends  PersistenceOperationOnObjectResulToReponseEntity<Collection<T>> {
		//
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <M> RESTCRUDOperationResponseBuilderForModelObjectURIStep<M> crudOn(final Class<M> modelObjectType) {
		return new RESTOperationsResponseBuilder() { /* nothing */ }
						.new RESTCRUDOperationResponseBuilderForModelObjectURIStep<M>(modelObjectType);
	}
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> RESTFindOperationResponseBuilderForModelObjectURIStep<O,M> findOn(final Class<M> modelObjectType) {
		return new RESTOperationsResponseBuilder() { /* nothing */ }
						.new RESTFindOperationResponseBuilderForModelObjectURIStep<O,M>(modelObjectType);
	}
	public static RESTExecOperationResponseBuilderForModelObjectURIStep executed() {
		return new RESTOperationsResponseBuilder() { /* nothing */ }
						.new RESTExecOperationResponseBuilderForModelObjectURIStep();
	}
	public static RESTSearchIndexOperationResponseBuilderForModelObjectURIStep searchIndex() {
		return new RESTOperationsResponseBuilder() { /* nothing */ }
						.new RESTSearchIndexOperationResponseBuilderForModelObjectURIStep();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTCRUDOperationResponseBuilderForModelObjectURIStep<M> {
		private final Class<M> _modelObjectType;
		
		public RESTCRUDOperationResponseBuilderForModelObjectMediaTypeStep<M> at(final URI resourceURI) {
			return new RESTCRUDOperationResponseBuilderForModelObjectMediaTypeStep<M>(_modelObjectType,
																  				      resourceURI);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTFindOperationResponseBuilderForModelObjectURIStep<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;
		
		public RESTFindOperationResponseBuilderForModelObjectMediaTypeStep<O,M> at(final URI resourceURI) {
			return new RESTFindOperationResponseBuilderForModelObjectMediaTypeStep<O,M>(_modelObjectType,
																  				        resourceURI);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTExecOperationResponseBuilderForModelObjectURIStep {
		
		public RESTEXECOperationResponseBuilderForModelObjectMediaTypeStep at(final URI resourceURI) {
			return new RESTEXECOperationResponseBuilderForModelObjectMediaTypeStep(resourceURI);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTSearchIndexOperationResponseBuilderForModelObjectURIStep {
		public RESTSearchIndexOperationResponseBuilderForModelObjectMediaTypeStep at(final URI resourceURI) {
			return new RESTSearchIndexOperationResponseBuilderForModelObjectMediaTypeStep(resourceURI);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
    @RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTCRUDOperationResponseBuilderForModelObjectMediaTypeStep<M> {
		private final Class<M> _modelObjectType;
		private final URI _resourceURI;
		
		public RESTCRUDOperationResponseBuilderForModelObjectEntityTransformerOrResultStep<M> mediaType(final MediaType mediaType) {
			return new RESTCRUDOperationResponseBuilderForModelObjectEntityTransformerOrResultStep<M>(_modelObjectType,
																  				                      _resourceURI,
																  				                       mediaType);
		}
	}
    
    @RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTCRUDOperationResponseBuilderForModelObjectEntityTransformerOrResultStep<M> {
		private final Class<M> _modelObjectType;
		private final URI _resourceURI;
		private final MediaType _mediaType;		
		// Build & Finish
		public Response build(final CRUDResult<M> crudResult) {	
				return new RESTCRUDOperationResponseBuilderForModelObjectResultStep<M>(_modelObjectType,
																					   _resourceURI,
																	  				   _mediaType,																	  				 
																	  				   c -> { return c;} )//By default return crud result																	  				   
					                                                                  .build(crudResult);
		}		
		// Build & Finish
		public Response build(final CRUDOnMultipleResult<M> crudResultOnMultiple) {			
			return new RESTCRUDOperationResponseBuilderForModelObjectResultStep<M>(_modelObjectType,
																  				   _resourceURI,
																  				   _mediaType,	
																  				    c -> { return c;}) //By default return crud result
																				  .build(crudResultOnMultiple);
		}		
		// With Persistence Operation Transformer.
		public RESTCRUDOperationResponseBuilderForModelObjectResultStep<M> withPersistenceOperationTransformer(final PersistenceOperationOnObjectResulToReponseEntity<M> transformer) {					
			return new RESTCRUDOperationResponseBuilderForModelObjectResultStep<M>(_modelObjectType,
																  				   _resourceURI,
																  				   _mediaType,	
																  				   transformer);
		}
	}
    
    @RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTFindOperationResponseBuilderForModelObjectMediaTypeStep<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;
		private final URI _resourceURI;
		
		public RESTFindOperationResponseBuilderForModelObjectEntityTransformerOrResultStep<O,M> mediaType(final MediaType mediaType) {
			return new RESTFindOperationResponseBuilderForModelObjectEntityTransformerOrResultStep<O,M>(_modelObjectType,
																									    _resourceURI,
																  				                         mediaType);
		}
	}
    
    
    @RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTFindOperationResponseBuilderForModelObjectEntityTransformerOrResultStep<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;
		private final URI _resourceURI;
		private final MediaType _mediaType;		
		// Build & Finish
		public Response build(final FindResult<M> findResult) {	
				return new RESTFindOperationResponseBuilderForModelObjectResultStep<O,M>(_modelObjectType,
																					     _resourceURI,
																	  				      _mediaType,																	  				 
																	  				      f -> { return f;} )//By default return find result																	  				   
					                                                                   .build(findResult);
		}		
		// Build & Finish
		public Response build(final FindOIDsResult<O> findOidsResult) {			
			return new RESTFindOperationResponseBuilderForModelObjectResultStep<O,M>(_modelObjectType,
																  				    _resourceURI,
																  				    _mediaType,	
																  				    c -> { return c;}) //By default return find result
																				  .build(findOidsResult);
		}	
	// Build & Finish
		public Response build(final FindSummariesResult<M> findSummResult) {			
			return new RESTFindOperationResponseBuilderForModelObjectResultStep<O,M>(_modelObjectType,
																  				    _resourceURI,
																  				    _mediaType,	
																  				     c -> { return c;}) //By default return find result
																				    .build(findSummResult);
		}	
		
		// With Persistence Operation Transformer.
		public RESTFindOperationResponseBuilderForModelObjectResultStep<O,M> withPersistenceOperationTransformer(final PersistenceOperationOnObjectResulCollectionToReponseEntity<?> transformer) {					
			return new RESTFindOperationResponseBuilderForModelObjectResultStep<O,M>(_modelObjectType,
																  				     _resourceURI,
																  				     _mediaType,	
																  				      transformer);
		}
	}
        
    @RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTSearchIndexOperationResponseBuilderForModelObjectMediaTypeStep {	
		private final URI _resourceURI;		
		
		public RESTSearchIndexOperationResponseBuilderResultStep mediaType(final MediaType mediaType) {
			return new RESTSearchIndexOperationResponseBuilderResultStep(mediaType,
					                                                    _resourceURI);
		}
	}    
    @RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTEXECOperationResponseBuilderForModelObjectMediaTypeStep {	
		private final URI _resourceURI;		
		
		public RESTEXECOperationResponseBuilderResultStep mediaType(final MediaType mediaType) {
			return new RESTEXECOperationResponseBuilderResultStep(mediaType,
					                                              _resourceURI);
		}
	}
    
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTCRUDOperationResponseBuilderForModelObjectResultStep<M> {
		private final Class<M> _modelObjectType;
		private final URI _resourceURI;
		private final MediaType _mediaType;
		private final PersistenceOperationOnObjectResulToReponseEntity<M> _transformer;		
		
		/**
		 * Returns a REST {@link Response} for a CRUD operation
		 * @param crudResult 
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final CRUDResult<M> crudResult) throws PersistenceException {				
			Response outResponse = null;			
			// Failed operation
			if (crudResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				crudResult.asCRUDError()		
						  .throwAsPersistenceException();	
				
			}
			// Successful operation
			else if (crudResult.hasSucceeded()) {
				CRUDOK<M> persistCRUDOK = crudResult.asCRUDOK();		//as(CRUDOK.class);
				
				if (persistCRUDOK.hasBeenLoaded()) {
					outResponse = Response.ok()
										  .header("x-r01-modelObjType",_modelObjectType.getName())
									  	  .entity(_transformer.from(crudResult))
									  	  .type(_mediaType)
									  	  .build();
				} else if (persistCRUDOK.hasBeenDeleted()) {
					outResponse = Response.ok()
										  .contentLocation(_resourceURI)
										  .header("x-r01-modelObjType",_modelObjectType.getName())
									  	  .entity(_transformer.from(crudResult))
									  	  .type(_mediaType)
									  	  .build();
				} else if (persistCRUDOK.hasBeenCreated()) {
					outResponse = Response.created(_resourceURI)
										  .header("x-r01-modelObjType",_modelObjectType.getName())
									  	   .entity(_transformer.from(crudResult))
									  	  .type(_mediaType)
									  	  .build();
				} else if (persistCRUDOK.hasBeenUpdated()) {
					outResponse = Response.ok()
									  	  .contentLocation(_resourceURI)
										  .header("x-r01-modelObjType",_modelObjectType.getName())
									  	   .entity(_transformer.from(crudResult))
									  	  .type(_mediaType)
									  	  .build();
				} else if (persistCRUDOK.hasNotBeenModified()) {
					outResponse = Response.notModified()	
										  .contentLocation(_resourceURI)
										  .header("x-r01-modelObjType",_modelObjectType.getName())
										  .entity(_transformer.from(crudResult))	
										  .type(_mediaType)
									  	  .build();
				} else {
					throw new UnsupportedOperationException(Throwables.message("{} is NOT a supported operation",persistCRUDOK.getRequestedOperation()));
				}
			}
			return outResponse;
		}
		/**
		 * Returns a REST {@link Response} for a CRUD operation
		 * @param multipleCRUDResult 
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final CRUDOnMultipleResult<M> multipleCRUDResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (multipleCRUDResult.haveAllFailed() 
			 || multipleCRUDResult.haveSomeFailed()) {
				Collection<CRUDError<M>> opsNOK = multipleCRUDResult.getOperationsNOK();
				// Throw the exception for the first error... it'll be mapped by the RESTExceptionMappers REST type mapper
				CRUDError<M> anError = CollectionUtils.pickOneElement(opsNOK);
				anError.throwAsPersistenceException();
			}
			// Successful operation
			else if (multipleCRUDResult.haveAllSucceeded()) {
				outResponse = Response.ok()
									  .contentLocation(_resourceURI)
									  .header("x-r01-modelObjType",_modelObjectType.getName())
									  .entity(multipleCRUDResult)
									  .type(_mediaType)
									  .build();
			}
			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTFindOperationResponseBuilderForModelObjectResultStep<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;
		private final URI _resourceURI;
		private final MediaType _mediaType;		
		@SuppressWarnings("rawtypes")
		private final PersistenceOperationOnObjectResulCollectionToReponseEntity _transformer;		
		/**
		 * Returns a REST {@link Response} for a FIND operation
		 * @param findOIDsResult 
		 * @return the response
		 * @throws PersistenceException
		 */
		@SuppressWarnings("unchecked")
		public Response build(final FindOIDsResult<O> findOIDsResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (findOIDsResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				findOIDsResult.asFindOIDsError()		
							  .throwAsPersistenceException();	
				
			}
			// Successful operation
			else {
				FindOIDsOK<O> findOK = findOIDsResult.asFindOIDsOK();		//as(FindOIDsOK.class);			
				outResponse = Response.ok()
									  .contentLocation(_resourceURI)
									  .header("x-r01-modelObjType",_modelObjectType.getName())
									  .entity(_transformer.from(findOK))
									  .type(_mediaType)
									  .build();
			}
			return outResponse;
		}
		@SuppressWarnings("unchecked")
		public Response build(final FindResult<M> persistenceOpResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asFindError()		//as(PersistenceOperationError.class)
								   .throwAsPersistenceException();	// throw an exception
				
			}
			// Successful operation
			else {			
				FindOK<M> findOK = persistenceOpResult.asFindOK();		// as(FindOK.class);
				outResponse = Response.ok()
									  .contentLocation(_resourceURI)
									  .header("x-r01-modelObjType",_modelObjectType.getName())
									  .entity(_transformer.from(findOK))
									  .type(_mediaType)
									  .build();
			}
			return outResponse;
		}
	
		
		@SuppressWarnings("unchecked")
		public Response build(final FindSummariesResult<M> findSummResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (findSummResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				findSummResult.asFindSummariesError()
							  .throwAsPersistenceException();
				
			}
			// Successful operation
			else {
				FindSummariesOK<M> findOK = findSummResult.asFindSummariesOK();		
				outResponse = Response.ok()
									  .contentLocation(_resourceURI)
									  .header("x-r01-modelObjType",_modelObjectType.getName())
									  .entity(_transformer.from(findOK))
									  .type(_mediaType)
									  .build();
			}
			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EXEC
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTEXECOperationResponseBuilderResultStep {
		private final MediaType _mediaType;
		private final URI _resourceURI;
		/**
		 * Returns a REST {@link Response} for a core-layer executed persistence operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final PersistenceOperationResult<?> persistenceOpResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asPersistenceOperationError()
								   .throwAsPersistenceException();	// throw an exception
				
			}
			// Successful operation
			else if (persistenceOpResult.hasSucceeded()) {
				PersistenceOperationExecOK<?> execOK = persistenceOpResult.asPersistenceOperationOK();
				outResponse = Response.ok()
									  .contentLocation(_resourceURI)
									  .entity(execOK)
									  .type(_mediaType)
									  .build();
			}
			return outResponse;
		}
		/**
		 * Returns a REST {@link Response} for a core-layer returned object
		 * @param obj
		 * @return
		 */
		public Response build(final Object obj) {
			Response outResponse = null;
			outResponse = Response.ok()
								  .contentLocation(_resourceURI)
								  .entity(obj)
								  .type(MediaType.APPLICATION_XML_TYPE)
								  .build();
			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTSearchIndexOperationResponseBuilderResultStep {
		private final MediaType _mediaType;
		private final URI _resourceURI;
		
		/**
		 * Returns a REST {@link Response} for a search operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final SearchResults<?,?> searchResults) {
			Response outResponse = Response.ok()
										   .contentLocation(_resourceURI)
										   .entity(searchResults)
										   .type(_mediaType)
										   .build();
			return outResponse;
		}
		/**
		 * Returns a REST {@link Response} for a search operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final EnqueuedJob job) {
			Response outResponse = Response.ok()
										   .contentLocation(_resourceURI)
										   .entity(job)
										   .type(_mediaType)
										   .build();
			return outResponse;
		}
		
	}
}

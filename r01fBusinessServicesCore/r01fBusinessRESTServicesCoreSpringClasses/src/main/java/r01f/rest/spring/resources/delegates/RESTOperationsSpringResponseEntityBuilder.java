package r01f.rest.spring.resources.delegates;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.Response;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.search.SearchResults;
import r01f.patterns.IsBuilder;
import r01f.types.jobs.EnqueuedJob;
import r01f.util.types.collections.CollectionUtils;


/**
 * See {@link RESTServicesProxyBase}
 * Usage:
 * <pre class='brush:java'>O
 *
 * </pre>
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class RESTOperationsSpringResponseEntityBuilder
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static <M> RESTCRUDOperationResponseBuilderForModelObjectURIStep<M> crudOn(final Class<M> modelObjectType) {
		return new RESTOperationsSpringResponseEntityBuilder() { /* nothing */ }
						.new RESTCRUDOperationResponseBuilderForModelObjectURIStep<M>(modelObjectType);
	}
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> RESTFindOperationResponseBuilderForModelObjectURIStep<O,M> findOn(final Class<M> modelObjectType) {
		return new RESTOperationsSpringResponseEntityBuilder() { /* nothing */ }
						.new RESTFindOperationResponseBuilderForModelObjectURIStep<O,M>(modelObjectType);
	}
	public static RESTExecOperationResponseBuilderForModelObjectURIStep executed() {
		return new RESTOperationsSpringResponseEntityBuilder() { /* nothing */ }
						.new RESTExecOperationResponseBuilderForModelObjectURIStep();
	}
	public static RESTSearchIndexOperationResponseBuilderForModelObjectURIStep searchIndex() {
		return new RESTOperationsSpringResponseEntityBuilder() { /* nothing */ }
						.new RESTSearchIndexOperationResponseBuilderForModelObjectURIStep();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTCRUDOperationResponseBuilderForModelObjectURIStep<M> {
		private final Class<M> _modelObjectType;

		public RESTCRUDOperationResponseBuilderForModelObjectContentTypeStep<M> at(final URI resourceURI) {
			return new RESTCRUDOperationResponseBuilderForModelObjectContentTypeStep<M>(_modelObjectType,
																  				        resourceURI);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTFindOperationResponseBuilderForModelObjectURIStep<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;

		public RESTFindOperationResponseBuilderForModelObjectContentTypeStep<O,M> at(final URI resourceURI) {
			return new RESTFindOperationResponseBuilderForModelObjectContentTypeStep<O,M>(_modelObjectType,
																  				     resourceURI);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTExecOperationResponseBuilderForModelObjectURIStep {
		@SuppressWarnings("static-method")
		public RESTEXECOperationResponseBuilderResultStep at(final URI resourceURI) {
			return new RESTEXECOperationResponseBuilderResultStep(resourceURI);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTSearchIndexOperationResponseBuilderForModelObjectURIStep {
		@SuppressWarnings("static-method")
		public RESTSearchIndexOperationResponseBuilderResultStep at(final URI resourceURI) {
			return new RESTSearchIndexOperationResponseBuilderResultStep(resourceURI);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
// CONTENT TYPE STEP
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTCRUDOperationResponseBuilderForModelObjectContentTypeStep<M> {
		private final Class<M> _modelObjectType;
        private final URI _resourceURI;
		public RESTCRUDOperationResponseBuilderForModelObjectResultStep<M> withContentType(final MediaType mediaType) {
			return new RESTCRUDOperationResponseBuilderForModelObjectResultStep<M>(_modelObjectType,
																  				   _resourceURI,
																  				    mediaType);
		}
	}

	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTFindOperationResponseBuilderForModelObjectContentTypeStep<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;
        private final URI _resourceURI;
		public RESTFindOperationResponseBuilderForModelObjectResultStep<O,M> withContentType(final MediaType mediaType) {
			return new RESTFindOperationResponseBuilderForModelObjectResultStep<O,M>(_modelObjectType,
																  				   _resourceURI,
																  				    mediaType);
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
		/**
		 * Returns a REST {@link Response} for a CRUD operation
		 * @param crudResult
		 * @return the response
		 * @throws PersistenceException
		 */
		@SuppressWarnings("unchecked")
		public ResponseEntity<M> build(final CRUDResult<M> crudResult) throws PersistenceException {
			ResponseEntity<M>  outResponse = null;

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
					outResponse = (ResponseEntity<M>) ResponseEntity.ok()
																	.header("x-r01-modelObjType",_modelObjectType.getName())
																	.contentType(_mediaType)
																	.body(persistCRUDOK);
				} else if (persistCRUDOK.hasBeenDeleted()) {
					outResponse = (ResponseEntity<M>) ResponseEntity.ok()
																	.location(_resourceURI)
																	.header("x-r01-modelObjType",_modelObjectType.getName())
																	.contentType(_mediaType)
																	.body(persistCRUDOK);

				} else if (persistCRUDOK.hasBeenCreated()) {
					outResponse = (ResponseEntity<M>) ResponseEntity.created(_resourceURI)
																	.header("x-r01-modelObjType",_modelObjectType.getName())
																	.contentType(_mediaType)
																	.body(persistCRUDOK);


				} else if (persistCRUDOK.hasBeenUpdated()) {
					outResponse = (ResponseEntity<M>) ResponseEntity.ok()
																	.location(_resourceURI)
																	.header("x-r01-modelObjType",_modelObjectType.getName())
																	.contentType(_mediaType)
																	.body(persistCRUDOK);

				} else if (persistCRUDOK.hasNotBeenModified()) {

					outResponse = (ResponseEntity<M>) ResponseEntity.ok()
																	.location(_resourceURI)
																	.header("x-r01-modelObjType",_modelObjectType.getName())
																	.contentType(_mediaType)
																	.body(persistCRUDOK);
				} else {
					throw new UnsupportedOperationException(Throwables.message("{} is NOT a supported operation",persistCRUDOK.getRequestedOperation()));
				}
			}
			return outResponse;
		}
		/**
		 * Returns a REST {@link Response} for a CRUD operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		@SuppressWarnings("unchecked")
		public ResponseEntity<M> build(final CRUDOnMultipleResult<M> persistenceOpResult) throws PersistenceException {
			ResponseEntity<M> outResponse = null;

			// Failed operation
			if (persistenceOpResult.haveAllFailed() || persistenceOpResult.haveSomeFailed()) {
				Collection<CRUDError<M>> opsNOK = persistenceOpResult.getOperationsNOK();
				// Throw the exception for the first error... it'll be mapped by the RESTExceptionMappers REST type mapper
				CRUDError<M> anError = CollectionUtils.pickOneElement(opsNOK);
				anError.throwAsPersistenceException();
			}
			// Successful operation
			else if (persistenceOpResult.haveAllSucceeded()) {
				outResponse = (ResponseEntity<M>) ResponseEntity.ok()
																	.location(_resourceURI)
																	.header("x-r01-modelObjType",_modelObjectType.getName())
																	.contentType(MediaType.APPLICATION_XML)
																	.body(persistenceOpResult);
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
		/**
		 * TODO Change FindOIDsOK for FindOIDsResult
		 * Returns a REST {@link Response} for a FIND operation
		 * @param findOIDsResult
		 * @return the response
		 * @throws PersistenceException
		 */
		public ResponseEntity<FindOIDsOK<O>> build(final FindOIDsResult<O> findOIDsResult) throws PersistenceException {
			ResponseEntity<FindOIDsOK<O>> outResponse = null;

			// Failed operation
			if (findOIDsResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				findOIDsResult.asFindOIDsError()
							  .throwAsPersistenceException();

			}
			// Successful operation
			else {
				FindOIDsOK<O> findOK = findOIDsResult.asFindOIDsOK();
				outResponse = ResponseEntity.ok()
																	.location(_resourceURI)
																	.header("x-r01-modelObjType",_modelObjectType.getName())
																	.contentType(_mediaType)
																	.body(findOK);
			}
			return outResponse;
		}
		
		// TODO Change FindOK for FindResult
		public ResponseEntity<FindOK<M>> build(final FindResult<M> persistenceOpResult) throws PersistenceException {
			ResponseEntity<FindOK<M>> outResponse = null;

			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asFindError()		//as(PersistenceOperationError.class)
								   .throwAsPersistenceException();	// throw an exception

			}
			// Successful operation
			else {
				FindOK<M> findOK = persistenceOpResult.asFindOK();			// as(FindOK.class);
				outResponse =  ResponseEntity.ok()
											   .location(_resourceURI)
											   .header("x-r01-modelObjType",_modelObjectType.getName())
											   .contentType(_mediaType)
											   .body(findOK);
			}
			return outResponse;
		}

		// TODO Change FindSummariesOK for FindSummariesResult
		public ResponseEntity<FindSummariesOK<M>> build(final FindSummariesResult<M> findSumResult) throws PersistenceException {
			ResponseEntity<FindSummariesOK<M> > outResponse = null;

			// Failed operation
			if (findSumResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				findSumResult.asFindSummariesError()
							 .throwAsPersistenceException();	

			}
			// Successful operation
			else {
				FindSummariesOK<M>  findOK = findSumResult.asFindSummariesOK();
				outResponse = ResponseEntity.ok()
											.location(_resourceURI)
											.header("x-r01-modelObjType",_modelObjectType.getName())
											.contentType(_mediaType)
											.body(findOK);
			}
			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EXEC
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTEXECOperationResponseBuilderResultStep {
		private final URI _resourceURI;
		/**
		 * Returns a REST {@link Response} for a core-layer executed persistence operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		@SuppressWarnings("unchecked")
		public ResponseEntity<PersistenceOperationResult<?>> build(final PersistenceOperationResult<?> persistenceOpResult) throws PersistenceException {
			ResponseEntity<?> outResponse = null;

			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asPersistenceOperationError()
								   .throwAsPersistenceException();	// throw an exception

			}
			// Successful operation
			else if (persistenceOpResult.hasSucceeded()) {
				PersistenceOperationResult<?> result = persistenceOpResult;
				outResponse =   ResponseEntity.ok()
											  .location(_resourceURI)
											  .contentType(MediaType.APPLICATION_XML)
											   .body(persistenceOpResult);
			}
			return (ResponseEntity<PersistenceOperationResult<?>>) outResponse;
		}
		/**
		 * Returns a REST {@link Response} for a core-layer returned object
		 * @param obj
		 * @return
		 */
		public ResponseEntity<Object> build(final Object obj) {
			ResponseEntity<Object>  outResponse = null;
			outResponse = ResponseEntity.ok()
									    .location(_resourceURI)
									    .contentType(MediaType.APPLICATION_XML)
									    .body(obj) ;

			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class RESTSearchIndexOperationResponseBuilderResultStep {
		private final URI _resourceURI;

		/**
		 * Returns a REST {@link Response} for a search operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		@SuppressWarnings("unchecked")
		public ResponseEntity<SearchResults<?,?>> build(final SearchResults<?,?> searchResults) {

			ResponseEntity<?> outResponse = ResponseEntity.ok()
						  								   .location(_resourceURI)
						  						    	   .body(searchResults);
			return (ResponseEntity<SearchResults<?, ?>>) outResponse;
		}
		/**
		 * Returns a REST {@link Response} for a search operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		public ResponseEntity<EnqueuedJob> build(final EnqueuedJob job) {
			ResponseEntity<EnqueuedJob> outResponse = ResponseEntity.ok()
						  											.location(_resourceURI)
						  											.body(job);


			return outResponse;
		}
	}
}

package r01f.services.delegates.persistence;


import java.util.Date;

import com.google.common.eventbus.EventBus;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.bootstrap.services.config.core.ServicesCoreBootstrapConfigWhenBeanExposed;
import r01f.guids.OID;
import r01f.guids.OIDIsGenerated;
import r01f.guids.OIDs;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.CRUDResultBuilder;
import r01f.model.persistence.PersistenceOperationExecError;
import r01f.model.persistence.PersistenceOperationExecOK;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.persistence.PersistenceRequestedOperation;
import r01f.model.services.COREServiceMethod;
import r01f.reflection.ReflectionUtils;
import r01f.securitycontext.SecurityContext;
import r01f.services.callback.spec.COREServiceMethodCallbackSpec;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.util.types.Strings;
import r01f.validation.ObjectValidationResult;
import r01f.validation.SelfValidates;
import r01f.validation.Validates;

/**
 * Service layer delegated type for CRUD (Create/Read/Update/Delete) operations
 */
@Slf4j
@Accessors(prefix="_")
public abstract class CRUDServicesForModelObjectDelegateBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
		      extends PersistenceServicesDelegateForModelObjectBase<O,M>
		   implements CRUDServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * true if read-only operations are the only ones available
	 */
	@Getter protected final boolean _readOnly;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDServicesForModelObjectDelegateBase(final ServicesCoreBootstrapConfigWhenBeanExposed coreCfg,
												  final Class<M> modelObjectType,
											 	  final CRUDServicesForModelObject<O,M> crud,
											 	  final EventBus eventBus) {
		super(coreCfg,
			  modelObjectType,
			  crud,
			  eventBus);
		_readOnly = false;
	}
	public CRUDServicesForModelObjectDelegateBase(final ServicesCoreBootstrapConfigWhenBeanExposed coreCfg,
												  final Class<M> modelObjectType,
											 	  final CRUDServicesForModelObject<O,M> crud) {
		this(coreCfg,
			 modelObjectType,
		     crud,
		     null);		// no event bus
	}
////////////////////////////////////////////////////////////////////////////////////////
//  LOAD | EXISTS
////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public PersistenceOperationResult<Boolean> exists(final SecurityContext securityContext,
						  							  final O oid) {
		PersistenceOperationResult<Boolean> outExists = null;

		// [0] - check the oid
		if (oid == null) {
			return new PersistenceOperationExecError<Boolean>(COREServiceMethod.named("exists"),
														      Strings.customized("The {} entity's oid cannot be null in order to check it's existence",
																   			  	 _modelObjectType));
		}
		// [1] - Load
		outExists = this.getServiceImplAs(CRUDServicesForModelObject.class)
					    .exists(securityContext,
							    oid);
		// [2] - Return
		return outExists;
	}
	@Override @SuppressWarnings("unchecked")
	public PersistenceOperationResult<Date> getLastUpdateDate(final SecurityContext securityContext,
														   	  final O oid) {
		PersistenceOperationResult<Date> outDate = null;

		// [0] - check the oid
		if (oid == null) {
			return new PersistenceOperationExecError<Date>(COREServiceMethod.named("lastUpdateDate"),
														   Strings.customized("The {} entity's oid cannot be null in order to get the last update date",
																   			  _modelObjectType));
		}
		// [1] - Load
		outDate = this.getServiceImplAs(CRUDServicesForModelObject.class)
					  .getLastUpdateDate(securityContext,
										 oid);
		// [2] - Return
		return outDate;
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> load(final SecurityContext securityContext,
				  			  final O oid) {
		CRUDResult<M> outEntityLoadResult = null;

		// [0] - check the oid
		if (oid == null) {
			return CRUDResultBuilder.using(securityContext)
								    .on(_modelObjectType)
								    .notLoaded()
								    .becauseClientBadRequest("The {} entity's oid cannot be null in order to be loaded",_modelObjectType)
								   		.build();
		}
		// [1] - Load
		outEntityLoadResult = this.getServiceImplAs(CRUDServicesForModelObject.class)
										.load(securityContext,
										 	  oid);
		// [2] - Throw CRUD event
//		_fireEvent(securityContext,
//				   outEntityLoadResult);
		// [3] - Return
		return outEntityLoadResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CREATE OR UPDATE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> create(final SecurityContext securityContext,
								final M modelObj) {
		return this.create(securityContext,
						   modelObj,
						   null);		// no callback
	}
	@Override
	public CRUDResult<M> create(final SecurityContext securityContext,
								final M modelObj,
								final COREServiceMethodCallbackSpec callbackSpec) {
		return this.doUpdateOrCreate(securityContext,
						  		 	 modelObj,
						  		 	 PersistenceRequestedOperation.CREATE,
						  		 	 callbackSpec);
	}
	@Override
	public CRUDResult<M> update(final SecurityContext securityContext,
								final M modelObj) {
		return this.update(securityContext,
						   modelObj,
				  		   null);		// no callback
	}
	@Override
	public CRUDResult<M> update(final SecurityContext securityContext,
								final M modelObj,
								final COREServiceMethodCallbackSpec callbackSpec) {
		return this.doUpdateOrCreate(securityContext,
						  		 	 modelObj,
						  		 	 PersistenceRequestedOperation.UPDATE,
						  		 	 callbackSpec);
	}
	protected CRUDResult<M> doUpdateOrCreate(final SecurityContext securityContext,
											 final M modelObj,
											 final PersistenceRequestedOperation requestedOperation) {
		return this.doUpdateOrCreate(securityContext,
									 modelObj,
									 requestedOperation,
									 null);		// no call-back
	}
	@SuppressWarnings({ "unchecked" })
	protected CRUDResult<M> doUpdateOrCreate(final SecurityContext securityContext,
											 final M modelObj,
											 final PersistenceRequestedOperation requestedOperation,
											 final COREServiceMethodCallbackSpec callbackSpec) {
		// [0] - check the entity
		if (modelObj == null) {
			return CRUDResultBuilder.using(securityContext)
							        .on(_modelObjectType)
							        .badClientRequest(PersistenceRequestedOperation.CREATE,
							    		 			  "The {} entity cannot be null in order to be {}ed",_modelObjectType,requestedOperation.name().toLowerCase())
							        .build();
		}
		// [1] check that it's NOT in read only status
		CRUDResult<M> outOpResult = this.errorIfReadOnlyOrNull(securityContext,
														  	   requestedOperation,
														  	   modelObj);
		if (outOpResult != null) return outOpResult;

		// [2] ensure that the new object has an oid
		if (requestedOperation == PersistenceRequestedOperation.CREATE
		 && modelObj.getOid() == null) {
			// ensure that the entity has an oid if the OID is NOT generated at DB-level
			Class<? extends OID> oidType = OIDs.oidTypeFor(modelObj.getClass());	// _modelObjectType
			if (!ReflectionUtils.isImplementing(oidType,OIDIsGenerated.class)) {
				O oid = (O)OIDs.supplyOid(oidType);
				modelObj.setOid(oid);
				log.debug("The entity to be created does NOT have the oid set so a new one is generated: {}",oid);
			}
		} else if (requestedOperation == PersistenceRequestedOperation.UPDATE
				&& modelObj.getOid() == null) {
			return (CRUDResult<M>)CRUDResultBuilder.using(securityContext)
												   .on(modelObj.getClass())		// _modelObjectType
												   .badClientRequest(PersistenceRequestedOperation.UPDATE,
												    		 		 "The {} entity to be updated does NOT have an OID, is it maybe a create operation",_modelObjectType)
												   .build();
		}

		// [3] complete the object
		M theModelObjToPersist = modelObj;
		if (this instanceof CompletesModelObjectBeforeCreateOrUpdate) {
			CompletesModelObjectBeforeCreateOrUpdate<M> completes = (CompletesModelObjectBeforeCreateOrUpdate<M>)this;
			theModelObjToPersist = completes.completeModelObjBeforeCreateOrUpdate(securityContext,
																	  			  requestedOperation,
																	  			  modelObj);
		}
		// [4] validate
		outOpResult = this.errorIfNOTValidOrNull(securityContext,
											 	 requestedOperation,
											 	 theModelObjToPersist);
		if (outOpResult != null) return outOpResult;

		// [5] Execute
		// 5.1) create
		if (requestedOperation == PersistenceRequestedOperation.CREATE) {
			outOpResult = this.getServiceImplAs(CRUDServicesForModelObject.class)
									.create(securityContext,
									   		theModelObjToPersist);
		}
		// 5.2) update
		else if (requestedOperation == PersistenceRequestedOperation.UPDATE) {
			outOpResult = this.getServiceImplAs(CRUDServicesForModelObject.class)
									.update(securityContext,
									   		theModelObjToPersist);
		}
		else {
			throw new IllegalArgumentException("doUpdateOrCreate() just can be called if the operation is CREATE or UPDATE, NOT " + requestedOperation);
		}
		// [6] throw CRUD event
		_fireEvent(securityContext,
				   outOpResult,
				   callbackSpec);
		// [7] return
		return outOpResult;
	}
	@Override @SuppressWarnings("unchecked")
	public PersistenceOperationResult<Boolean> touch(final SecurityContext securityContext,
												  	 final O oid,final Date date) {
		if (date == null) return new PersistenceOperationExecOK<>(COREServiceMethod.named("touch"),
																  false);
		return this.getServiceImplAs(CRUDServicesForModelObject.class)
				   .touch(securityContext,
						  oid,date);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DELETE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> delete(final SecurityContext securityContext,
								final O oid) {
		return this.delete(securityContext,
						   oid,
						   null);		// no async callback
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> delete(final SecurityContext securityContext,
								final O oid,
								final COREServiceMethodCallbackSpec callbackSpec) {
		// [0] - check the entity
		if (oid == null) {
			return CRUDResultBuilder.using(securityContext)
									   .on(_modelObjectType)
									   .badClientRequest(PersistenceRequestedOperation.DELETE,
									    		 		 "The {} entity's oid cannot be null in order to be deleted",_modelObjectType)
									    		.build();
		}
		// [1] check that it's NOT in read only status
		CRUDResult<M> outOpResult = this.errorIfReadOnlyOrNull(securityContext,
														  	   PersistenceRequestedOperation.DELETE,
														  	   oid);
		if (outOpResult != null) return outOpResult;

		// [2] delete
		outOpResult = this.getServiceImplAs(CRUDServicesForModelObject.class)
								.delete(securityContext,
							  	   		oid);
		// [3] throw CRUD event
		_fireEvent(securityContext,
				   outOpResult,
				   callbackSpec);

		// [4] return
		return outOpResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected CRUDResult<M> errorIfReadOnlyOrNull(final SecurityContext securityContext,
												  final PersistenceRequestedOperation reqOp,
												  final M modelObject) {
		CRUDResult<M> outError = null;
		if (_readOnly) {
			if (reqOp.is(PersistenceRequestedOperation.CREATE)) {
				outError = CRUDResultBuilder.using(securityContext)
											   .on(_modelObjectType)
											   .notCreated()
											   .becauseClientBadRequest("The CRUD services object is in READ-ONLY status!")
											   		.about(modelObject).build();
			} else if (reqOp.is(PersistenceRequestedOperation.UPDATE)) {
				outError = CRUDResultBuilder.using(securityContext)
											   .on(_modelObjectType)
											   .notUpdated()
											   .becauseClientBadRequest("The CRUD services object is in READ-ONLY status!")
											   		.about(modelObject).build();
			}
		}
		return outError;
	}
	protected CRUDResult<M> errorIfReadOnlyOrNull(final SecurityContext securityContext,
												  final PersistenceRequestedOperation reqOp,
												  final O oid) {
		CRUDResult<M> outError = null;
		if (_readOnly) {
			if (reqOp.is(PersistenceRequestedOperation.UPDATE)) {
				outError = CRUDResultBuilder.using(securityContext)
													   .on(_modelObjectType)
													   .notUpdated()
													   .becauseClientBadRequest("The CRUD services object is in READ-ONLY status!")
													   		.about(oid).build();
			} else if (reqOp.is(PersistenceRequestedOperation.DELETE)) {
				outError = CRUDResultBuilder.using(securityContext)
													   .on(_modelObjectType)
													   .notDeleted()
													   .becauseClientBadRequest("The CRUD services object is in READ-ONLY status!")
													   		.about(oid).build();
			}
		}
		return outError;
	}
	/**
	 * Invokes the _validateModelObjectBeforeCreateOrUpdate and return a {@link PersistenceOperationError} if it returns a NOT VALID result
	 * @param securityContext
	 * @param reqOp
	 * @param modelObj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected CRUDResult<M> errorIfNOTValidOrNull(final SecurityContext securityContext,
												  final PersistenceRequestedOperation reqOp,
												  final M modelObj) {
		ObjectValidationResult<M> valid = null;

		// model object self validation
		if (modelObj instanceof Validates) {
			valid = ((Validates<M>)modelObj).validate(modelObj);
		}
		if (valid == null
		 && modelObj instanceof SelfValidates) {
			valid = ((SelfValidates<M>)modelObj).validate();
		}
		// service logic validation
		if (valid != null && valid.isValid()
		 || valid == null) {
			if (this instanceof ValidatesModelObjectBeforeCreateOrUpdate) {
				// Validation is being used
				ValidatesModelObjectBeforeCreateOrUpdate<M> validates = (ValidatesModelObjectBeforeCreateOrUpdate<M>)this;
				valid = validates.validateModelObjBeforeCreateOrUpdate(securityContext,
																	   reqOp,
																	   modelObj);
			}
		}

		// return
		CRUDResult<M> outError = null;
		if (valid != null && valid.isNOTValid()) {
			if (reqOp.is(PersistenceRequestedOperation.CREATE)) {
				outError = CRUDResultBuilder.using(securityContext)
											   .on(_modelObjectType)
											   .notCreated()
											   .becauseClientSentEntityValidationErrors(valid.asNOKValidationResult())
											   		.about(modelObj).build();
			} else if (reqOp.is(PersistenceRequestedOperation.UPDATE)) {
				outError = CRUDResultBuilder.using(securityContext)
											   .on(_modelObjectType)
											   .notUpdated()
											   .becauseClientSentEntityValidationErrors(valid.asNOKValidationResult())
											   		.about(modelObj).build();
			}
		}
		return outError;
	}
}

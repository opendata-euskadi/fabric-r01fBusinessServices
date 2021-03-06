package r01f.persistence.db;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.PersistenceOperationExecError;
import r01f.model.persistence.PersistenceOperationExecResultBuilder;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.persistence.PersistenceRequestedOperation;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.db.config.DBModuleConfig;
import r01f.persistence.db.entities.DBEntityForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObjectImpl;
import r01f.reflection.ReflectionUtils;
import r01f.securitycontext.SecurityContext;

@Slf4j
@Accessors(prefix="_")
public abstract class DBCRUDForMultiLangFullTextSearch<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
				      					   			   PK extends DBPrimaryKeyForModelObject,DB extends DBEntityForModelObject<PK>> 
			  extends DBBase 
		   implements TransfersModelObjectStateToDBEntity<M,DB> {	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The model object's type
	 */
	@Getter protected final Class<M> _modelObjectType;
	/**
	 * db entity java type
	 */
	@Getter protected final Class<DB> _DBEntityType;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public DBCRUDForMultiLangFullTextSearch(final Class<M> modelObjectType,final Class<DB> dbEntityType,
											final DBModuleConfig dbCfg,
											final EntityManager entityManager,
											final Marshaller marshaller) {
		super(dbCfg,
			  entityManager,
			  marshaller);
		_modelObjectType = modelObjectType;
		_DBEntityType = dbEntityType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Builds the primary key from the model object
	 * @param entity
	 * @return
	 */
	protected PK dbEntityPrimaryKeyFor(final M entity) {
		// the key is an unique column primary key
		O oid = entity.getOid();
		PK outKey = this.dbEntityPrimaryKeyFor(oid);
		return outKey;
	}
	/**
	 * Builds the primary key for the given oid
	 * @param oid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected PK dbEntityPrimaryKeyFor(final O oid) {
		return (PK)DBPrimaryKeyForModelObjectImpl.from(oid);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Model object to DBEntity transform (WRITING)
/////////////////////////////////////////////////////////////////////////////////////////
	protected DB createDBEntityInstanceFor(final M modelObj) {
		DB outEntity = ReflectionUtils.<DB>createInstanceOf(_DBEntityType);
		return outEntity;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceOperationResult<Boolean> create(final SecurityContext securityContext,
													  final M modelObj) {
		return this.doCreateOrUpdateEntity(securityContext,
									  	   modelObj,
									  	   PersistenceRequestedOperation.CREATE);		// it's a creation
	}
	public PersistenceOperationResult<Boolean> update(final SecurityContext securityContext,
													  final M entity) {
		return this.doCreateOrUpdateEntity(securityContext,
									  	   entity,
									  	   PersistenceRequestedOperation.UPDATE);		// it's an update
	}
	public PersistenceOperationResult<Boolean> delete(final SecurityContext securityContext,
													  final O oid) {
		return this.doDelete(securityContext,
				  		 	 oid);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates or updates the entity by checking the entity existence in the first place
	 * and then creating or updating it
	 * @param securityContext
	 * @param modelObj
	 * @param requestedOp
	 * @return
	 */
	protected PersistenceOperationResult<Boolean> doCreateOrUpdateEntity(final SecurityContext securityContext,
												   						 final M modelObj,
												   						 final PersistenceRequestedOperation requestedOp) {
		// [0]: Try to find a previously existing entity
		PK pk = null;
		DB dbEntityToPersist = null;
		if (modelObj.getOid() != null) {
			// Create the pk from the oid
			pk = this.dbEntityPrimaryKeyFor(modelObj);
			if (pk == null) throw new IllegalStateException("A null [db primary key] is NOT allowed: the [db primary key] returned by " + this.getClass().getName() + "#dbEntityPrimaryKeyFor(modelObj) is null!!");
			
			// Find the entity
			dbEntityToPersist = this.getEntityManager().find(_DBEntityType,
													  		 pk);
			// If the entity exists BUT it's supposed to be new... it's an error
			if (dbEntityToPersist != null
			 && requestedOp.is(PersistenceRequestedOperation.CREATE)) {
				return PersistenceOperationExecResultBuilder.using(securityContext)
															.notExecuted(requestedOp)
															.becauseClientBadRequest("The entry with oid={} already exists; it cannot be {}",modelObj.getOid(),requestedOp);
			}
		} else if (requestedOp == PersistenceRequestedOperation.UPDATE) {
			throw new IllegalStateException("An UPDATE operation cannot be performed if the object has oid=null");
		}
		// [1]: Depending on the existence of the entity create or update
		if (dbEntityToPersist != null && pk != null) {
			// Update
			log.debug("> updating a {} entity with pk={} and entityVersion={}",_DBEntityType,
					  pk.asString(),modelObj.getEntityVersion());
		}
		else {
			// Create
			log.debug("> creating a {} entity with pk={}",_DBEntityType,
					  (pk != null ? pk.asString() : "<should be generated at db>"));

			dbEntityToPersist = this.createDBEntityInstanceFor(modelObj);
		}

		// [3]: Set the db entity fields from the model object
		this.setDBEntityFieldsFromModelObject(securityContext,
							   			  	  modelObj,dbEntityToPersist);

		// [5]: Persist
		try {
			return this.persistDBEntity(securityContext,
								   		dbEntityToPersist,
								   		requestedOp);
		} catch (PersistenceException persistEx) {
			log.error("Error while persisting a db entity of type {}: {}",
					  _DBEntityType,
					  persistEx.getMessage(),persistEx);
			// the previous existence of the entity was checked at the beginning of this method
			// BUT there's an edge case where the two threads concurrently try to create the same entity at the same moment
			// ... if both threads check the entity existence at the same time there's a remote situation in which both gets
			// a false result (the entity does not previously exists) BUT one thread create the entity and the other fails
			// because the entity already existed
			return _buildCRUDResultErrorIfEntityExistsOnConcurrencyOrThrow(securityContext,
																		   modelObj,
																		   requestedOp,
																		   persistEx);
		}
	}
	protected PersistenceOperationResult<Boolean> persistDBEntity(final SecurityContext securityContext,
										    					  final DB dbEntityToPersist,
										    					  final PersistenceRequestedOperation requestedOp) {
		// [1] - Persist using manual transaction management
		this.getEntityManager()
			.persist(dbEntityToPersist);
		
		// [99] - Build the CRUD result
		PersistenceOperationResult<Boolean> outResult = PersistenceOperationExecResultBuilder.using(securityContext)
												   											 .executed(requestedOp)
												   											 .returning(true);
		return outResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected PersistenceOperationResult<Boolean> doDelete(final SecurityContext securityContext,
									 					   final O oid) {
		// get the pk
		PK pk = this.dbEntityPrimaryKeyFor(oid);
		if (pk == null) throw new IllegalStateException("A null [db primary key] is NOT allowed: the [db primary key] returned by " + this.getClass().getName() + "#dbEntityPrimaryKeyFor(modelObj) is null!!");

		log.debug("> deleting a {} entity with pk={}",_DBEntityType,pk.asString());

		PersistenceOperationResult<Boolean> outResult = null;

		// [1]: Check the existence of the entity to be deleted
		DB dbEntity = this.getEntityManager()
						  .find(_DBEntityType,
							    pk);
		// [2]: Delete the entity
		if (dbEntity != null) {
			this.getEntityManager()
				.refresh(dbEntity);		// refresh the dbentity since it could be modified (ie by setting a bi-directional relation)
			this.getEntityManager()
				.remove(dbEntity);
			this.getEntityManager()
			 	.flush();
		} 
		outResult = PersistenceOperationExecResultBuilder.using(securityContext)
											   			 .executed(PersistenceRequestedOperation.DELETE)
											   			 .returning(dbEntity != null);	// true if deleted (the entity exists)
		return outResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Build CRUD Result Error If Entity Exists On Concurrency Or Throw Exception :
	 * 	The previous existence of the entity is checked at the beginning of this method
	 * BUT there's an edge case where the two threads concurrently try to create the same entity at the same moment
	 * ... if both threads check the entity existence at the same time there's a remote situation in which both gets
	 * a false result (the entity does not previously exists) BUT one thread create the entity and the other fails
	 * because the entity already existed
	 * @param securityContext
	 * @param modelObj
	 * @param requestedOp
	 * @param persistEx
	 * @return
	 */
	protected PersistenceOperationExecError<Boolean> _buildCRUDResultErrorIfEntityExistsOnConcurrencyOrThrow(final SecurityContext securityContext,
												   				               	   							 final M modelObj,
												   				               	   							 final PersistenceRequestedOperation requestedOp,
												   				               	   							 final PersistenceException persistEx) {
		log.warn(">>>_buildCRUDResultErrorIfEntityExistsOnConcurrencyOrThrow....");

		if (persistEx.getCause() instanceof org.eclipse.persistence.exceptions.DatabaseException
		 && (persistEx.getCause().getCause() instanceof SQLException || persistEx.getCause().getCause() instanceof SQLIntegrityConstraintViolationException)) {

			SQLException sqlEx = (SQLException)persistEx.getCause().getCause();
			
			if (1 == sqlEx.getErrorCode()
			 || 1062 == sqlEx.getErrorCode()) {
				log.warn(">> CRUD Error ORA-00001/MySQL-01062 because Entity {} already exists! ",
						 modelObj.getOid().asString());
				return PersistenceOperationExecResultBuilder.using(securityContext)
															.notExecuted(requestedOp)
															.becauseClientBadRequest("Cannot insert an entity with oid={}: it already exists",modelObj.getOid());
			} else if (1400 == sqlEx.getErrorCode()) {
				log.warn(">> CRUD Error ORA-01400: CANNOT MAKE A NULL INSERT!",
						 modelObj.getOid().asString());
				return PersistenceOperationExecResultBuilder.using(securityContext)
															.notExecuted(requestedOp)
															.becauseClientBadRequest("Cannot insert a null entity");
			} else if ("23000".equals(sqlEx.getSQLState())) { // Check if this code number is in some Constants Class.
			    log.warn(">> CRUD SQL State Error with Entity {}. Check logs!",
			    		 modelObj.getOid().asString());
				return PersistenceOperationExecResultBuilder.using(securityContext)
															.notExecuted(requestedOp)
															.because(sqlEx);
			} else {
				// another type of exception
				log.warn(">> exception {} will be throw {}",persistEx.getLocalizedMessage());
				throw persistEx;
			}
		}
		// another type of exception
		log.warn(">> exception {} will be throw {}",persistEx.getLocalizedMessage());
		throw persistEx;
	}
}

package r01f.persistence.db;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import com.google.common.base.Function;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.guids.PersistableObjectOID;
import r01f.model.HasTrackingInfo;
import r01f.model.PersistableModelObject;
import r01f.model.facets.HasEntityVersion;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.CRUDResultBuilder;
import r01f.model.persistence.PersistenceOperationExecError;
import r01f.model.persistence.PersistenceOperationExecOK;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.services.COREServiceMethod;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.db.config.DBModuleConfig;
import r01f.persistence.db.entities.DBEntityForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObjectImpl;
import r01f.securitycontext.SecurityContext;
import r01f.util.types.Dates;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Base type for every persistence layer type
 * @param <O>
 * @param <M>
 * @param <PK>
 * @param <DB>
 */
@Accessors(prefix="_")
@Slf4j
public abstract class DBBaseForModelObject<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
				      					   PK extends DBPrimaryKeyForModelObject,DB extends DBEntityForModelObject<PK>>
			  extends DBBase
		   implements TransformsDBEntityIntoModelObject<DB,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The model object's type
	 */
	@Getter protected final Class<M> _modelObjectType;
	/**
	 * db entity java type
	 */
	@Getter protected final Class<DB> _DBEntityType;
	/**
	 * Transforms a db entity into a model object
	 */
	@Getter protected final TransformsDBEntityIntoModelObject<DB,M> _dbEntityIntoModelObjectTransformer;

	@Deprecated 	// use dbEntity -> _dbEntityIntoModelObjectTransformer.dbEntityToModelObject(securityContext,
					//																			 dbEntity);
	protected final Function<DB,M> _dbEntityToModelObjTransformUsingDescriptor = new Function<DB,M>() {
																						@Override
																						public M apply(final DB dbEntity) {
																							return _dbEntityIntoModelObjectTransformer.dbEntityToModelObject(null,	// WTF!
																																	 	 					 dbEntity);
																						}
																				 };

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Deprecated
	public DBBaseForModelObject(final DBModuleConfig dbCfg,
								final Class<M> modelObjectType,final Class<DB> dbEntityType,
								final EntityManager entityManager,
								final Marshaller marshaller) {
		this(modelObjectType,dbEntityType,
			 dbCfg,
			 entityManager,
			 marshaller);

	}
	@Deprecated
	public DBBaseForModelObject(final DBModuleConfig dbCfg,
								final Class<M> modelObjectType,final Class<DB> dbEntityType,
								final TransformsDBEntityIntoModelObject<DB,M> dbEntityIntoModelObjectTransformer,
								final EntityManager entityManager,
								final Marshaller marshaller) {
		this(modelObjectType,dbEntityType,
			 dbEntityIntoModelObjectTransformer,
			 dbCfg,
			 entityManager,
			 marshaller);

	}
	public DBBaseForModelObject(final Class<M> modelObjectType,final Class<DB> dbEntityType,
								final DBModuleConfig dbCfg,
								final EntityManager entityManager,
								final Marshaller marshaller) {
		this(modelObjectType,dbEntityType,
			 // create a default transformer using the marshaller
			 DBBase.createTransformsDBEntityIntoModelObjectUsing(marshaller,		
																 modelObjectType),
			  dbCfg,
			  entityManager,
			  marshaller);
	}
	public DBBaseForModelObject(final Class<M> modelObjectType,final Class<DB> dbEntityType,
								final TransformsDBEntityIntoModelObject<DB,M> dbEntityIntoModelObjectTransformer,
								final DBModuleConfig dbCfg,
								final EntityManager entityManager,
								final Marshaller marshaller) {
		super(dbCfg,
			  entityManager,
			  marshaller);
		_modelObjectType = modelObjectType;
		_DBEntityType = dbEntityType;
		_dbEntityIntoModelObjectTransformer = dbEntityIntoModelObjectTransformer;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONVERTERS
/////////////////////////////////////////////////////////////////////////////////////////
	@Deprecated
	protected M _wrapDBEntityToModelObject(final SecurityContext securityContext,
										   final DB dbEntity) {
		return _transformDBEntityToModelObject(securityContext,
											   dbEntity);
	}
	protected M _transformDBEntityToModelObject(final SecurityContext securityContext,
								           		final DB dbEntity) {
		M out = this.dbEntityToModelObject(securityContext,
										   dbEntity);
		// ensure the tracking info ant entity version are set
		if (dbEntity instanceof HasTrackingInfo) {
			out.setTrackingInfo(((HasTrackingInfo)dbEntity).getTrackingInfo());
		}
		if (dbEntity instanceof HasEntityVersion) {
			out.setEntityVersion(((HasEntityVersion)dbEntity).getEntityVersion());
		}
		return out;
	}
	@Override
	public M dbEntityToModelObject(final SecurityContext securityContext,
								   final DB dbEntity) {
		M out = _dbEntityIntoModelObjectTransformer.dbEntityToModelObject(securityContext,
																		  dbEntity);
		if (out == null) throw new IllegalStateException("The model object returned by the db entity to model object transformer is null!");
		return out;
	}
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
//  LOAD
/////////////////////////////////////////////////////////////////////////////////////////
	protected PersistenceOperationResult<Boolean> doCheckExistence(final SecurityContext securityContext,
								   								   final O oid) {
		// Load the [last update date]
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
		Root<? extends DB> root = criteriaQuery.from(_DBEntityType);

		criteriaQuery.multiselect(root.get("_oid"));
		Predicate oidPredicate = criteriaBuilder.equal(root.<String>get("_oid"),
									 				   oid.asString());
		criteriaQuery.where(oidPredicate);
		List<Tuple> tupleResult = _entityManager.createQuery(criteriaQuery)
													.setHint(QueryHints.READ_ONLY,HintValues.TRUE)
											    .getResultList();

		// Compose the PersistenceOperationResult object
		PersistenceOperationResult<Boolean> outResult = null;
		if (CollectionUtils.isNullOrEmpty(tupleResult)) {
			outResult = new PersistenceOperationExecOK<Boolean>(COREServiceMethod.named("exists"),
																false);	// does NOT exists
			log.warn(outResult.getDetailedMessage());
		} else if (tupleResult.size() == 1) {
			outResult = new PersistenceOperationExecOK<Boolean>(COREServiceMethod.named("exists"),
																true);
		} else if (tupleResult.size() > 1) {
			outResult = new PersistenceOperationExecError<Boolean>(COREServiceMethod.named("exists"),
																   Strings.customized("There exists more than a single {} entities with oid={} when trying check it's existence",
																					   _modelObjectType,oid));
			log.warn(outResult.getDetailedMessage());
		}
		return outResult;
	}
	protected PersistenceOperationResult<Date> doGetLastUpdateDate(final SecurityContext securityContext,
								   								   final O oid) {
		// Load the [last update date]
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
		Root<? extends DB> root = criteriaQuery.from(_DBEntityType);

		criteriaQuery.multiselect(root.get("_lastUpdateDate"));
		Predicate oidPredicate = criteriaBuilder.equal(root.<String>get("_oid"),
									 					oid.asString());
		criteriaQuery.where(oidPredicate);
		List<Tuple> tupleResult = _entityManager.createQuery(criteriaQuery)
													.setHint(QueryHints.READ_ONLY,HintValues.TRUE)
											    .getResultList();

		// Compose the PersistenceOperationResult object
		PersistenceOperationResult<Date> outResult = null;
		if (CollectionUtils.isNullOrEmpty(tupleResult)) {
			outResult = new PersistenceOperationExecError<Date>(COREServiceMethod.named("lastUpdateDate"),
																Strings.customized("Could NOT find a {} entity with oid={} when trying to get the last update date",
																				   _modelObjectType,oid));
			log.warn(outResult.getDetailedMessage());
		} else if (tupleResult.size() > 1) {
			outResult = new PersistenceOperationExecError<Date>(COREServiceMethod.named("lastUpdateDate"),
																Strings.customized("There exists more than a single {} entities with oid={} when trying to get the last update date",
																				   _modelObjectType,oid));
			log.warn(outResult.getDetailedMessage());
		} else {
			Tuple tuple = CollectionUtils.pickOneAndOnlyElement(tupleResult);
			GregorianCalendar lastUpdateDate = (GregorianCalendar)tuple.get(0);
			outResult = new PersistenceOperationExecOK<Date>(COREServiceMethod.named("lastUpdateDate"),
															 lastUpdateDate != null ? lastUpdateDate.getTime() : new Date());
		}
		return outResult;
	}
	protected PersistenceOperationResult<Boolean> doTouch(final SecurityContext securityContext,
								   						  final O oid,final Date date) {
		// Load the [last update date]
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaUpdate<DB> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(_DBEntityType);
		Root<? extends DB> root = criteriaUpdate.from(_DBEntityType);
		criteriaUpdate.set(root.get("_lastUpdateDate"),Dates.asCalendar(date));
		Predicate oidPredicate = criteriaBuilder.equal(root.<String>get("_oid"),
									 				   oid.asString());
		criteriaUpdate.where(oidPredicate);
		int updatedCount = _entityManager.createQuery(criteriaUpdate)
					  					 .executeUpdate();

		// Compose the PersistenceOperationResult object
		PersistenceOperationResult<Boolean> outResult = null;
		if (updatedCount == 0) {
			outResult = new PersistenceOperationExecError<Boolean>(COREServiceMethod.named("touch"),
																   Strings.customized("Could NOT find a {} entity with oid={} when trying to get the last update date",
																				   	  _modelObjectType,oid));
			log.warn(outResult.getDetailedMessage());
		} else {
			outResult = new PersistenceOperationExecOK<Boolean>(COREServiceMethod.named("touch"),
															 	true);
		}
		return outResult;
	}
	protected CRUDResult<M> doLoad(final SecurityContext securityContext,
								   final O oid,final PK pk) {
		// check the oid
		if (pk == null) return CRUDResultBuilder.using(securityContext)
											    .on(_modelObjectType)
										  	    .notLoaded()
										  	    .becauseClientBadRequest("The {} entity's oid cannot be null in order to be loaded",_modelObjectType)
										  	   			.about(oid).build();
		// Load the entity
		DB dbEntity = this.doLoadDBEntity(securityContext,
									 	  pk);

		// Compose the PersistenceOperationResult object
		CRUDResult<M> outEntityLoadResult = null;
		if (dbEntity != null) {
			M modelObj = _transformDBEntityToModelObject(securityContext,
											    		 dbEntity);
			outEntityLoadResult = CRUDResultBuilder.using(securityContext)
										  .on(_modelObjectType)
										  .loaded()
										  .entity(modelObj);
		} else {
			outEntityLoadResult = CRUDResultBuilder.using(securityContext)
										  .on(_modelObjectType)
										  .notLoaded()
										  .becauseClientRequestedEntityWasNOTFound()
										  		.about(oid).build();
			log.warn(outEntityLoadResult.getDetailedMessage());
		}
		return outEntityLoadResult;
	}
	/**
	 * Loads the db entity using the oid
	 * @param securityContext
	 * @param oid
	 * @return
	 */
	protected DB doLoadDBEntity(final SecurityContext securityContext,
							    final O oid) {
		PK pk = this.dbEntityPrimaryKeyFor(oid);
		if (pk == null) return null;
		return doLoadDBEntity(securityContext,
							  pk);
	}
	/**
	 * Loads the db entity using the pk
	 * @param securityContext
	 * @param pk
	 * @return
	 */
	protected DB doLoadDBEntity(final SecurityContext securityContext,
							    final PK pk) {
		log.debug("> loading a {} entity with pk={}",
				  _DBEntityType,pk.asString());

		DB dbEntity = this.getEntityManager()
						  .find(_DBEntityType,
							    pk);
		return dbEntity;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Deprecated
	protected CRUDResult<M> _crudResultForSingleEntity(final SecurityContext securityContext,
													   final OID id,
													   final Collection<DB> dbEntities) {
		return _crudResultForSingleEntity(securityContext,
										  dbEntities,
										  id);
	}
	/**
	 * Composes a {@link CRUDResult} for a load operation where there should be
	 * only one result
	 * @param securityContext
	 * @param id
	 * @param dbEntities
	 * @return
	 */
	@Deprecated
	protected CRUDResult<M> _crudResultForSingleEntity(final SecurityContext securityContext,
													   final Collection<DB> dbEntities,
													   final OID id) {
		if (CollectionUtils.hasData(dbEntities)
		 && dbEntities.size() > 1) return CRUDResultBuilder.using(securityContext)
											 .on(_modelObjectType)
											 .notLoaded()
											 	.becauseServerError("The DB is in an illegal status: there MUST exist a single db entity of {} with id {} but {} exists",
											 						_DBEntityType,id,dbEntities.size())
											 	.about(id).build();
		return _crudResultForSingleEntity(securityContext,
										  dbEntities);
	}
	protected CRUDResult<M> _crudResultForSingleEntity(final SecurityContext securityContext,
													   final Collection<DB> dbEntities) {
		return _crudResultForSingleEntity(securityContext,
										  dbEntities,
										  null,null);	// no meta & value
	}
	protected CRUDResult<M> _crudResultForSingleEntity(final SecurityContext securityContext,
													   final Collection<DB> dbEntities,
													   final String meta,final String value) {
		if (CollectionUtils.hasData(dbEntities)
		 && dbEntities.size() > 1) return CRUDResultBuilder.using(securityContext)
											 .on(_modelObjectType)
											 .notLoaded()
											 	.becauseServerError("The DB is in an illegal status: there MUST exist a single db entity of {} but {} exists",
											 				    	_DBEntityType,dbEntities.size())
											 	.about(meta,value)
											 	.build();
		// Return
		CRUDResult<M> outResult = null;
		if (CollectionUtils.hasData(dbEntities)) {
			// normal
			DB dbEntity = CollectionUtils.of(dbEntities)
										 .pickOneAndOnlyElement();	// this is now safe
			outResult = CRUDResultBuilder.using(securityContext)
										 .on(_modelObjectType)
										 .loaded()
											.dbEntity(dbEntity)
											.transformedToModelObjectUsing(_dbEntityIntoModelObjectTransformer);
		} else {
			// no results
			outResult = CRUDResultBuilder.using(securityContext)
										 .on(_modelObjectType)
										 .notLoaded()
										 	.becauseClientRequestedEntityWasNOTFound()
										 	.about(meta,value)
										 	.build();
		}
		return outResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected Date _lastEntityUpdateDate() {
		String jpql = Strings.customized("SELECT MAX(e._lastUpdateDate) " +
										   "FROM {} e " +
										  "GROUP BY e._lastUpdateDate",
										 _DBEntityType.getSimpleName());
		Tuple tuple = _entityManager.createQuery(jpql,
												 Tuple.class)
								    .getSingleResult();
		Calendar cal = tuple != null ? tuple.get(0,Calendar.class)
									 : null;
		return cal != null ? Dates.fromCalendar(cal)
						   : new Date();
	}
}

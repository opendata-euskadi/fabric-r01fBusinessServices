package r01f.persistence.db;


import javax.inject.Provider;
import javax.persistence.EntityManager;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.model.HasTrackingInfo;
import r01f.model.ModelObject;
import r01f.model.TrackableModelObject.HasTrackableFacet;
import r01f.model.facets.HasEntityVersion;
import r01f.objectstreamer.HasMarshaller;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.db.config.DBModuleConfig;
import r01f.persistence.db.config.DBSpec;
import r01f.securitycontext.SecurityContext;


/**
 * Base type for every persistence layer type
 */
@Accessors(prefix="_")
@Slf4j
public abstract class DBBase
	       implements HasEntityManager,
	       			  HasMarshaller {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The entity manager obtained from the {@link EntityManager} {@link Provider}
	 */
	@Getter protected final EntityManager _entityManager;
	/**
	 * The db config
	 */
	@Getter protected final DBModuleConfig _dbConfig;
	/**
	 * The db spec
	 */
	@Getter protected final DBSpec _dbSpec;
	/**
	 * Marshaller
	 */
	@Getter protected final Marshaller _modelObjectsMarshaller;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBBase(final DBModuleConfig dbCfg,
				  final EntityManager entityManager,
				  final Marshaller marshaller) {
		_entityManager = entityManager;
		_dbConfig = dbCfg;
		_dbSpec = DBSpec.usedAt(_entityManager);	// maybe ca be get directly from dbconfig??
		_modelObjectsMarshaller = marshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method that creates a transformer that uses the XML descriptor at the [db entity] to 
	 * build a [model object]
	 * @param <DB>
	 * @param <M>
	 * @param marshaller
	 * @param modelObjectType
	 * @return
	 */
	public static <DB extends DBEntity,
				   M extends ModelObject> TransformsDBEntityIntoModelObject<DB,M> createTransFromsDBEntityIntoModelObjectUsing(final Marshaller marshaller,
							  																								   final Class<M> modelObjectType) {
		return new TransformsDBEntityIntoModelObject<DB,M>() {
						@Override
						public M dbEntityToModelObject(final SecurityContext securityContext,
													   final DB dbEntity) {
								M outObj = null;
								// use the descriptor to build the model object
								if (dbEntity instanceof DBEntityHasModelObjectDescriptor) {
									DBEntityHasModelObjectDescriptor hasDescriptor = (DBEntityHasModelObjectDescriptor)dbEntity;
									outObj = marshaller.forReading()
													   .fromXml(hasDescriptor.getDescriptor(),
																modelObjectType);
								} else {
									log.warn("The db entity of type {} does NOT implements {} so the db entity MUST be manually translated bo model object",
											 dbEntity.getClass().getSimpleName(),DBEntityHasModelObjectDescriptor.class.getSimpleName());
								}
								// copy some info from the dbEntity
								if (outObj != null) {
									if (dbEntity instanceof HasTrackingInfo
									 && outObj instanceof HasTrackableFacet) {
										HasTrackingInfo hasTrackingInfoDBEntity = (HasTrackingInfo)dbEntity;
										HasTrackableFacet hasTrackableFacetModelObj = (HasTrackableFacet)outObj;
										hasTrackableFacetModelObj.setTrackingInfo(hasTrackingInfoDBEntity.getTrackingInfo());
									} else {
										log.trace("either {} or {} has NOT tracking info",
												  dbEntity.getClass().getSimpleName(),outObj.getClass().getSimpleName());
									}
									if (dbEntity instanceof HasEntityVersion
									 && outObj instanceof HasEntityVersion) {
										HasEntityVersion hasEntityVersionDBEntity = (HasEntityVersion)dbEntity;
										HasEntityVersion hasEntityVersionModelObj = (HasEntityVersion)outObj;
										hasEntityVersionModelObj.setEntityVersion(hasEntityVersionDBEntity.getEntityVersion());
									} else {
										log.trace("either {} or {} has NOT entity version info",
												  dbEntity.getClass().getSimpleName(),outObj.getClass().getSimpleName());
									}
								}
								return outObj;
						}
				};
	}
}

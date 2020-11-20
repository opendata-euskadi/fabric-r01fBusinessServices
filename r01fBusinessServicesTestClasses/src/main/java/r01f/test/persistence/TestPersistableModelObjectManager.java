package r01f.test.persistence;

import java.util.Collection;
import java.util.Date;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.Threads;
import r01f.guids.PersistableObjectOID;
import r01f.model.ModelObjectTracking;
import r01f.model.PersistableModelObject;
import r01f.patterns.Factory;
import r01f.reflection.ReflectionUtils;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.test.api.TestAPIBase;
import r01f.types.dirtytrack.DirtyTrackAdapter;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@Accessors(prefix="_")
public class TestPersistableModelObjectManager<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
	 extends TestPersistableModelObjectManagerBase<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS TO USE WHEN THERE'RE BACKGROUND JOBS (ie indexing)
/////////////////////////////////////////////////////////////////////////////////////////
	public TestPersistableModelObjectManager(final Class<M> modelObjType,final Factory<? extends M> mockObjectsFactory,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
											 final long milisToWaitForBackgroundJobs) {
		super(modelObjType,
			  mockObjectsFactory,
			  crudAPI,
			  milisToWaitForBackgroundJobs);
	}
	public TestPersistableModelObjectManager(final M modelObj,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
											 final long milisToWaitForBackgroundJobs) {
		super(modelObj,
			  crudAPI,
			  milisToWaitForBackgroundJobs);
	}
	public TestPersistableModelObjectManager(final Class<M> modelObjType,final Factory<M> mockObjectsFactory,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		super(modelObjType,
			  mockObjectsFactory,
			  crudAPI);
	}
	public TestPersistableModelObjectManager(final M modelObj,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		super(modelObj,
			  crudAPI);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> TestPersistableModelObjectManager<O,M> create(final Class<M> modelObjType,final Factory<? extends M> mockObjectsFactory,
																																	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
																																	 final long milisToWaitForBackgroundJobs) {
		return new TestPersistableModelObjectManager<O,M>(modelObjType,mockObjectsFactory,
														  crudAPI,
														  milisToWaitForBackgroundJobs);
	}
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> TestPersistableModelObjectManager<O,M> create(final Class<M> modelObjType,final Factory<M> mockObjectsFactory,
																																	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		return new TestPersistableModelObjectManager<O,M>(modelObjType,mockObjectsFactory,
														  crudAPI,
														  0L);		// no need to wait for crud-associated background jobs
	}
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> TestPersistableModelObjectManager<O,M> create(final M modelObj,
																																	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
																																	 final long milisToWaitForBackgroundJobs) {
		return new TestPersistableModelObjectManager<O,M>(modelObj,
														  crudAPI,
														  milisToWaitForBackgroundJobs);
	}
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> TestPersistableModelObjectManager<O,M> create(final M modelObj,
																																	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		return new TestPersistableModelObjectManager<O,M>(modelObj,
														  crudAPI);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Collection<M> setUpMockObjs(final int numOfObjsToCreate) {
		final Collection<M> outCreatedModelObjs = Lists.newArrayListWithExpectedSize(numOfObjsToCreate);
		// create test model objects
		final Collection<O> createdModelObjs = Sets.newLinkedHashSetWithExpectedSize(numOfObjsToCreate);
		for (int i=0; i < numOfObjsToCreate; i++) {
			final M objectToBeCreated = _mockObjectsFactory.create();

			if (ReflectionUtils.isImplementing(objectToBeCreated.getClass(),
											   DirtyTrackAdapter.class)) {
				DirtyTrackAdapter.adapt(objectToBeCreated).setNew();	// ensure it's a new object!
			}
			objectToBeCreated.setEntityVersion(0);						// ensure it have a zero entity numeric id

			objectToBeCreated.setTrackingInfo(new ModelObjectTracking(TestAPIBase.TEST_USER_OID,TestAPIBase.TEST_USER,
																	  new Date()));			// Ensure tracking info

			final M createdModelObj = _CRUDApi.save(objectToBeCreated);
			if (createdModelObj.getOid() == null) throw new IllegalStateException("The created model object of type " + _modelObjType + " does NOT have oid value!");
			createdModelObjs.add(createdModelObj.getOid());
			log.info("... Created {} mock object with oid={}",_modelObjType.getSimpleName(),createdModelObj.getOid());
			outCreatedModelObjs.add(createdModelObj);
		}
		// add the oids to the created model obj oids
		if (CollectionUtils.isNullOrEmpty(_createdMockObjsOids)) _createdMockObjsOids = Lists.newArrayListWithExpectedSize(numOfObjsToCreate);
		_createdMockObjsOids.addAll(createdModelObjs);

		return outCreatedModelObjs;
	}
	@Override
	public void tearDownCreatedMockObjs() {
		if (CollectionUtils.isNullOrEmpty(_createdMockObjsOids)) return;

		// wait for background jobs to finish
		// an error in the background job will raise if the DB records are deleted before background jobs finish (ie lucene indexing or notification tasks)
		final long milisToWaitForBackgroundJobs = _createdMockObjsOids.size() * _milisToWaitForBackgroundJobs;
		if (milisToWaitForBackgroundJobs > 0) {
			log.info(".... give {} milis for background jobs (ie lucene index or notifications) to complete before deleting created DB records (lucene indexing or notifications will fail if the DB record is deleted)",
					 milisToWaitForBackgroundJobs);
			Threads.safeSleep(milisToWaitForBackgroundJobs);
		}

		// delete all DB records
		for (final O oid : _createdMockObjsOids) {
			_CRUDApi.delete(oid);
			log.info("... Deleted {} mock object with oid={}",_modelObjType.getSimpleName(),oid);
		}
		this.reset();
	}
}

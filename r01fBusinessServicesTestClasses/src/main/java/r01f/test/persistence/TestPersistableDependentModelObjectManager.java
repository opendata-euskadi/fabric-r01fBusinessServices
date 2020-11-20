package r01f.test.persistence;

import java.util.Collection;
import java.util.Date;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.experimental.Accessors;
import r01f.concurrent.Threads;
import r01f.guids.PersistableObjectOID;
import r01f.model.ModelObjectRef;
import r01f.model.ModelObjectReferenciable;
import r01f.model.ModelObjectTracking;
import r01f.model.PersistableModelObject;
import r01f.patterns.Factory;
import r01f.services.client.api.delegates.ClientAPIDelegateForDependentModelObjectCRUDServices;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.test.api.TestAPIBase;
import r01f.util.types.collections.CollectionUtils;

/**
 * A factory of dependent model objects
 * @param <O>
 * @param <M>
 * @param <PR>
 */
@Accessors(prefix="_")
public class TestPersistableDependentModelObjectManager<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
														PO extends PersistableObjectOID,P extends PersistableModelObject<PO>>
	 extends TestPersistableModelObjectManagerBase<O,M>
  implements ManagesTestMockDependentModelObjsLifeCycle<O,M,P> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final ClientAPIDelegateForDependentModelObjectCRUDServices<O,M,PO,P> _clientApiDelegateForDependentObjsCRUD;
	private final ManagesTestMockModelObjsLifeCycle<PO,P> _parentObjsManager;
	private P _parentObject;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS TO USE WHEN THERE'RE BACKGROUND JOBS (ie indexing)
/////////////////////////////////////////////////////////////////////////////////////////
	public TestPersistableDependentModelObjectManager(final ManagesTestMockModelObjsLifeCycle<PO,P> parentObjsMgr,
													  final Class<M> modelObjType,final Factory<? extends M> mockObjectsFactory,
											 		  final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,final ClientAPIDelegateForDependentModelObjectCRUDServices<O,M,PO,P> clientApiDelegateForDependentObjsCRUD,
											 		  final long milisToWaitForBackgroundJobs) {
		super(modelObjType,
			  mockObjectsFactory,
			  crudAPI,
			  milisToWaitForBackgroundJobs);
		_parentObjsManager = parentObjsMgr;
		_clientApiDelegateForDependentObjsCRUD = clientApiDelegateForDependentObjsCRUD;
	}
	public TestPersistableDependentModelObjectManager(final ManagesTestMockModelObjsLifeCycle<PO,P> parentObjsMgr,
													  final Class<M> modelObjType,final Factory<? extends M> mockObjectsFactory,
											 		  final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,final ClientAPIDelegateForDependentModelObjectCRUDServices<O,M,PO,P> clientApiDelegateForDependentObjsCRUD) {
		super(modelObjType,
			  mockObjectsFactory,
			  crudAPI,
			  0l);
		_parentObjsManager = parentObjsMgr;
		_clientApiDelegateForDependentObjsCRUD = clientApiDelegateForDependentObjsCRUD;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	public static <PO extends PersistableObjectOID,P extends PersistableModelObject<PO>,
				   O extends PersistableObjectOID,M extends PersistableModelObject<O>>
				  TestPersistableDependentModelObjectManager<O,M,PO,P> create(final ManagesTestMockModelObjsLifeCycle<PO,P> parentModelObjsMgr,
																  		 	  final Class<M> modelObjType,final Factory<? extends M> mockObjectsFactory,
																  		 	  final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,final ClientAPIDelegateForDependentModelObjectCRUDServices<O,M,PO,P> clientApiDelegateForDependentObjsCRUD,
																  		 	  final long milisToWaitForBackgroundJobs) {
		return new TestPersistableDependentModelObjectManager<O,M,PO,P>(parentModelObjsMgr,
																	 	modelObjType,mockObjectsFactory,
																	 	crudAPI,clientApiDelegateForDependentObjsCRUD,
																	 	milisToWaitForBackgroundJobs);
	}
	public static <PO extends PersistableObjectOID,P extends PersistableModelObject<PO>,
				   O extends PersistableObjectOID,M extends PersistableModelObject<O>>
				  TestPersistableDependentModelObjectManager<O,M,PO,P> create(final ManagesTestMockModelObjsLifeCycle<PO,P> parentModelObjsMgr,
																  			  final Class<M> modelObjType,final Factory<M> mockObjectsFactory,
																  			  final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,final ClientAPIDelegateForDependentModelObjectCRUDServices<O,M,PO,P> clientApiDelegateForDependentObjsCRUD) {
		return new TestPersistableDependentModelObjectManager<O,M,PO,P>(parentModelObjsMgr,
																		modelObjType,mockObjectsFactory,
																		crudAPI,clientApiDelegateForDependentObjsCRUD,
																		0l);		// no need to wait for crud-associated background jobs
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Collection<M> setUpMockObjs(final int numOfObjsToCreate) {
		// create a parent model obj if it was NOT received
		if (_parentObject == null) _parentObject = _parentObjsManager.setUpSingleMockObj();

		// create the child model objs
		Collection<M> outCreatedModelObjs = Lists.newArrayListWithExpectedSize(numOfObjsToCreate);

		// create test model objects
		_createdMockObjsOids = Sets.newLinkedHashSetWithExpectedSize(numOfObjsToCreate);
		for (int i=0; i < numOfObjsToCreate; i++) {
			M modelObjectToBeCreated = _mockObjectsFactory.create();
			modelObjectToBeCreated.setTrackingInfo(new ModelObjectTracking(TestAPIBase.TEST_USER_OID,TestAPIBase.TEST_USER,new Date()));			// Ensure tracking info

			M createdModelObj = _clientApiDelegateForDependentObjsCRUD.create(this.getParentModelObjectRef(),
												  							  modelObjectToBeCreated);
			_createdMockObjsOids.add(createdModelObj.getOid());
			System.out.println("... Created " + _modelObjType.getSimpleName() + " mock object with oid=" + createdModelObj.getOid());
			outCreatedModelObjs.add(createdModelObj);
		}
		return outCreatedModelObjs;
	}
	@Override
	public void tearDownCreatedMockObjs() {
		if (CollectionUtils.hasData(_createdMockObjsOids)) {
			// wait for background jobs to finish
			// an error in the background job will raise if the DB records are deleted before background jobs finish (ie lucene indexing or notification tasks)
			long milisToWaitForBackgroundJobs = _createdMockObjsOids.size() * _milisToWaitForBackgroundJobs;
			if (milisToWaitForBackgroundJobs > 0) {
				System.out.println(".... give " + milisToWaitForBackgroundJobs + " milis for background jobs (ie lucene index or notifications) to complete before deleting created DB records (lucene indexing or notifications will fail if the DB record is deleted)");
				Threads.safeSleep(milisToWaitForBackgroundJobs);
			}

			// delete all child DB records
			for (O oid : _createdMockObjsOids) {
				_CRUDApi.delete(oid);
				System.out.println("... Deleted " + _modelObjType.getSimpleName() + " mock object with oid=" + oid);
			}
			this.reset();
		}

		// delete the parent DB record
		System.out.println("Deleting parent " + _parentObjsManager.getModelObjType().getSimpleName() + " object: " + _parentObjsManager.getCreatedMockObjsOids());
		_parentObjsManager.tearDownCreatedMockObjs();
		_parentObjsManager.reset();

		_parentObject = null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public P getParentModelObject() {
		return _parentObjsManager.getAnyCreatedMockObj();
	}
	@Override @SuppressWarnings("unchecked")
	public <PR extends ModelObjectRef<P>> PR getParentModelObjectRef() {
		PR parentModelObjRef = ((ModelObjectReferenciable<PR>)_parentObject).getRef();
		return parentModelObjRef;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		StringBuilder out = new StringBuilder(super.debugInfo());
		out.append("\n\tParent object: ").append(_parentObject != null);
		out.append("\n\tParent objects manager: ").append(_parentObjsManager.debugInfo());
		return out;
	}
}

package r01f.test.persistence;

import java.util.Collection;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.patterns.Factory;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.util.types.collections.CollectionUtils;

@Accessors(prefix="_")
abstract class TestPersistableModelObjectManagerBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
    implements ManagesTestMockModelObjsLifeCycle<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final Class<M> _modelObjType;
	@Getter protected final Factory<? extends M> _mockObjectsFactory;
	@Getter protected final ClientAPIDelegateForModelObjectCRUDServices<O,M> _CRUDApi;
	@Getter protected final long _milisToWaitForBackgroundJobs;

	@Getter protected Collection<O> _createdMockObjsOids;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS TO USE WHEN THERE'RE BACKGROUND JOBS (ie indexing)
/////////////////////////////////////////////////////////////////////////////////////////
	public TestPersistableModelObjectManagerBase(final Class<M> modelObjType,
											 	 final Factory<? extends M> mockObjectsFactory,
											 	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
											 	 final long milisToWaitForBackgroundJobs) {
		_modelObjType = modelObjType;
		_mockObjectsFactory = mockObjectsFactory;
		_CRUDApi = crudAPI;
		_milisToWaitForBackgroundJobs = milisToWaitForBackgroundJobs;
	}
	@SuppressWarnings("unchecked")
	public TestPersistableModelObjectManagerBase(final M modelObj,
											 	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
											 	 final long milisToWaitForBackgroundJobs) {
		_modelObjType = (Class<M>)modelObj.getClass();
		_mockObjectsFactory = new Factory<M>() {
										@Override
										public M create() {
											return modelObj;
										}
				  			  };
		// remember to add the given obj
		_createdMockObjsOids = Lists.newArrayList();
		_createdMockObjsOids.add(modelObj.getOid());

		_CRUDApi = crudAPI;
		_milisToWaitForBackgroundJobs = milisToWaitForBackgroundJobs;
	}
	public TestPersistableModelObjectManagerBase(final Class<M> modelObjType,
											 	 final Factory<M> mockObjectsFactory,
											 	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		this(modelObjType,mockObjectsFactory,
			 crudAPI,
			 0l);
	}
	public TestPersistableModelObjectManagerBase(final M modelObj,
											 	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		this(modelObj,
			 crudAPI,
			 0l);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void reset() {
		_createdMockObjsOids = null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UTILS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public M setUpSingleMockObj() {
		Collection<M> createdObj = this.setUpMockObjs(1);
		return CollectionUtils.<M>pickOneAndOnlyElement(createdObj);
	}
	@Override
	public O getAnyCreatedMockObjOid() {
		if (CollectionUtils.isNullOrEmpty(_createdMockObjsOids)) throw new IllegalStateException("There's NO created model object available at the factory");
		return CollectionUtils.of(_createdMockObjsOids).pickOneElement();
	}
	@Override
	public O getAnyCreatedMockObjOidOrCreateNew() {
		if (CollectionUtils.isNullOrEmpty(_createdMockObjsOids)) this.setUpSingleMockObj();

		return CollectionUtils.of(_createdMockObjsOids).pickOneElement();
	}
	@Override
	public M getAnyCreatedMockObj() {
		O oid = this.getAnyCreatedMockObjOid();
		M outModelObj = _CRUDApi.load(oid);
		return outModelObj;
	}
	@Override
	public M getAnyCreatedMockObjOrCreateNew() {
		O oid = this.getAnyCreatedMockObjOidOrCreateNew();
		M outModelObj = _CRUDApi.load(oid);
		return outModelObj;
	}
	@Override
	public Collection<M> getCreatedMockObjs() {
		if (_createdMockObjsOids == null) return null;
		Collection<M> outModelObjs = Lists.newArrayListWithExpectedSize(_createdMockObjsOids.size());
		for (O oid : _createdMockObjsOids) {
			outModelObjs.add(_CRUDApi.load(oid));
		}
		return outModelObjs;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		StringBuilder sb = new StringBuilder(_modelObjType.getName());
		sb.append(" debug objects manager: ");
		if (CollectionUtils.hasData(_createdMockObjsOids)) {
			sb.append(_createdMockObjsOids.size()).append(" > ").append(_createdMockObjsOids);
		}
		else {
			sb.append(" NO objects");
		}
		return sb;
	}
}

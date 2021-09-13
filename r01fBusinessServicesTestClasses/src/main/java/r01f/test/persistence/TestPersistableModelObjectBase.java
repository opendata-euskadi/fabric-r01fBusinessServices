package r01f.test.persistence;

import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.Threads;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.patterns.CommandOn;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectFindServices;
import r01f.types.TimeLapse;

/**
 * JVM arguments:
 * -javaagent:D:/develop/local_libs/aspectj/lib/aspectjweaver.jar -Daj.weaving.verbose=true
 */
@Slf4j
public abstract class TestPersistableModelObjectBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final ClientAPIDelegateForModelObjectCRUDServices<O,M> _crudApi;
	protected final ClientAPIDelegateForModelObjectFindServices<O,M> _findApi;
	protected final ManagesTestMockModelObjsLifeCycle<O,M> _managesTestMockObjects;

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	protected TestPersistableModelObjectBase(final ManagesTestMockModelObjsLifeCycle<O,M> managesTestMockObjects,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudApi,final ClientAPIDelegateForModelObjectFindServices<O,M> findApi) {
		_managesTestMockObjects = managesTestMockObjects;
		_crudApi = crudApi;
		_findApi = findApi;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public ManagesTestMockModelObjsLifeCycle<O,M> getTestMockObjsLifeCycleManager() {
		return _managesTestMockObjects;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public void doTest() {
		this.doTest(true);	// test delete
	}
	public void doTest(final boolean deleteCreatedObjs) {
		log.warn("===========================================================");
		log.warn("TEST: {}",_managesTestMockObjects.getModelObjType().getSimpleName());
		log.warn("===========================================================");

		// [1]: Test Persistence (create, update, load and delete)
		this.doCRUDTest(deleteCreatedObjs);

		// [2]: Test Find
		this.doFindTest(deleteCreatedObjs);

		// [3]: Test other methods
		this.testOtherMethods();
		
		// [4]: wait for background jobs to complete
		Threads.safeSleep(TimeLapse.of("5s"));

		// [99]: Ensure created records are removed
		if (deleteCreatedObjs) this.tearDownCreatedMockObjs();
	}
	protected abstract void testOtherCRUDMethods();
	protected abstract void testOtherFindMethods();
	protected abstract void testOtherMethods();
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public void tearDownCreatedMockObjs() {
		_managesTestMockObjects.tearDownCreatedMockObjs();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	public void doCRUDTest() {
		this.doCRUDTest(true);	// test delete
	}
	public void doCRUDTest(final boolean testDelete) {
		// [1]: Basic persistence tests
		TestPersistableModelObjectCRUD<O,M> crudTest = TestPersistableModelObjectCRUD.create(// crud api
																							 _crudApi,
																							 // model objects factory
																							 _managesTestMockObjects);
		crudTest.testPersistence(_modelObjectStateUpdateCommand(),
								 testDelete);

		// [2]: Test other CRUD methods
		this.testOtherCRUDMethods();
	}
	/**
	 * @return a {@link CommandOn} that changes the model object's state (simulate a user update action)
	 */
	protected abstract CommandOn<M> _modelObjectStateUpdateCommand();

/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	public void doFindTest() {
		this.doFindTest(true);	// delete created objs
	}
	public void doFindTest(final boolean deleteCreatedObjs) {
		// [1]: Basic find tests
		TestPersistableModelObjectFind<O,M> findTest = TestPersistableModelObjectFind.create(// find api
																							 _findApi,
																							 // mock objects factory
																							 _managesTestMockObjects);
		findTest.testFind(deleteCreatedObjs);

		// [2]: Test extended methods
		System.out.println("[Test other FIND methods]");
		this.testOtherFindMethods();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unchecked" })
	protected <A extends ClientAPIDelegateForModelObjectCRUDServices<O,M>> A getClientCRUDApiAs(final Class<A> apiType) {
		return (A)_crudApi;
	}
	@SuppressWarnings({ "unchecked" })
	protected <A extends ClientAPIDelegateForModelObjectFindServices<O,M>> A getClientFindApiAs(final Class<A> apiType) {
		return (A)_findApi;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
}

package r01f.test.persistence;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.junit.Assert;

import com.google.common.base.Stopwatch;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.securitycontext.SecurityIDS.LoginID;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectFindServices;
import r01f.services.client.api.delegates.ClientAPIHasDelegateForDependentModelObjectFind;
import r01f.test.api.TestAPIBase;
import r01f.types.Range;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class TestPersistableModelObjectFind<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	private final ClientAPIDelegateForModelObjectFindServices<O,M> _findAPI;
	private final ManagesTestMockModelObjsLifeCycle<O,M> _managesTestMockObjs;

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> TestPersistableModelObjectFind<O,M> create(final ClientAPIDelegateForModelObjectFindServices<O,M> findAPI,
																												 				  final ManagesTestMockModelObjsLifeCycle<O,M> modelObjFactory) {
		return new TestPersistableModelObjectFind<O,M>(findAPI,
													   modelObjFactory);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public void testFind() {
		this.testFind(true);	// delete created objs
	}
	public void testFind(final boolean deleteCreatedObjs) {
		log.warn("[init][TEST BASIC FIND {}]-----------------------------------------------------------------------",
				 _managesTestMockObjs.getModelObjType());

		Stopwatch stopWatch = Stopwatch.createStarted();

		// [0]: SetUp: create some test objects
		_managesTestMockObjs.setUpMockObjs(5);

		// [1] - All entities
		log.warn("\tFIND ALL ENTITY's OIDS");
		Collection<O> allOids = _findAPI.findAll();
		log.warn("\t\t>> {}",allOids);
		Assert.assertTrue(CollectionUtils.hasData(allOids));

		// [2] - By create / last update date
		Range<Date> dateRange = Range.open(DateTime.now().minusDays(1).toDate(),
										   DateTime.now().plusDays(1).toDate());

		log.warn("\tFIND ENTITY's OIDs BY CREATE DATE: {}",dateRange.asString());
		Collection<O> oidsByCreateDate = _findAPI.findByCreateDate(dateRange);
		log.warn("\t\t>> {}",oidsByCreateDate);
		Assert.assertTrue(CollectionUtils.hasData(oidsByCreateDate));

		log.warn("\tFIND ENTITY's OIDs BY LAST UPDATE DATE: {}",dateRange.asString());
		Collection<O> oidsByLastUpdatedDate = _findAPI.findByCreateDate(dateRange);
		log.warn("\t\t>> {}",oidsByLastUpdatedDate);
		Assert.assertTrue(CollectionUtils.hasData(oidsByLastUpdatedDate));

		// [3] - By creator
		LoginID user = TestAPIBase.TEST_USER;

		log.warn("\tFIND ENTITY's OIDs BY CREATOR: {}",user);
		Collection<O> oidsByCreator = _findAPI.findByCreator(user);
		log.warn("\t\t>> {}",oidsByCreator);
		Assert.assertTrue(CollectionUtils.hasData(oidsByCreator));

		// [4] - By last updator (the objects haven't been updated so it must return 0)
		log.warn("\tFIND ENTITY's OIDs BY LAST UPDATOR: {}",user);
		Collection<O> oidsByLastUpdator = _findAPI.findByLastUpdator(user);
		log.warn("\t\t>> {}",oidsByLastUpdator);
		Assert.assertTrue(CollectionUtils.isNullOrEmpty(oidsByLastUpdator));

		log.warn("[end ][TEST BASIC FIND {}] (elapsed time: {} milis)-------------------------",
				 _managesTestMockObjs.getModelObjType(),NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)));

		// [99]: Delete previously created test objects to restore DB state
		if (deleteCreatedObjs) _managesTestMockObjs.tearDownCreatedMockObjs();
	}
	protected void testDependentObjectsFind() {
		// [5] - If it's a dependent model object, test the specific methods
		if (_findAPI instanceof ClientAPIHasDelegateForDependentModelObjectFind) {
			log.warn("\tFIND DEPENDENT ENTITIES");

			TestPersistableDependentModelObjectFind<O,M,?,?> depTest = TestPersistableDependentModelObjectFind.create(_findAPI,
																												   	  _managesTestMockObjs);
			depTest.testFindDependent();
		}
	}
}

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
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedModelObject;
import r01f.services.client.api.delegates.ClientAPIDelegateForDependentModelObjectFindServices;
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
	/**
	 * Tests the CRUD API (creates an entity, updates it, loads it and finally deletes it)
	 * @param modelObject
	 */
	@SuppressWarnings("rawtypes")
	public void testFind() {
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
		UserCode user = TestAPIBase.TEST_USER;

		log.warn("\tFIND ENTITY's OIDs BY CREATOR: {}",user);
		Collection<O> oidsByCreator = _findAPI.findByCreator(user);
		log.warn("\t\t>> {}",oidsByCreator);
		Assert.assertTrue(CollectionUtils.hasData(oidsByCreator));

		// [4] - By last updator (the objects haven't been updated so it must return 0)
		log.warn("\tFIND ENTITY's OIDs BY LAST UPDATOR: {}",user);
		Collection<O> oidsByLastUpdator = _findAPI.findByLastUpdator(user);
		log.warn("\t\t>> {}",oidsByLastUpdator);
		Assert.assertTrue(CollectionUtils.isNullOrEmpty(oidsByLastUpdator));

		// [5] - If it's a dependent model object, test the specific methods
		if (_findAPI instanceof ClientAPIHasDelegateForDependentModelObjectFind) {
			log.warn("\tFIND DEPENDENT ENTITIES");

			ClientAPIDelegateForDependentModelObjectFindServices<O,M,?> depFindAPI = (ClientAPIDelegateForDependentModelObjectFindServices<O,M,?>)((ClientAPIHasDelegateForDependentModelObjectFind<?>)_findAPI).getClientApiForDependentDelegate();
			ManagesTestMockDependentModelObjsLifeCycle<O,M,?> depObjFactory = (ManagesTestMockDependentModelObjsLifeCycle<O,M,?>)_managesTestMockObjs;

			// find child oids
			Collection<O> depOids = depFindAPI.findOidsOfDependentsOf(depObjFactory.getParentModelObject().getOid());
			Assert.assertTrue(CollectionUtils.hasData(depOids));
			log.warn("\t\t>> {} dependent objects of {}",depOids.size(),depObjFactory.getParentModelObject().getClass());
			for (O depOid : depOids) {
				log.warn("\t\t\t> {}",depOid);
			}
			// find child summaries
			Object depsSummarizedAsObject = depFindAPI.findSummariesOfDependentsOf(depObjFactory.getParentModelObject().getOid());
			Collection<? extends SummarizedModelObject<?>> depsSummarized = (Collection<? extends SummarizedModelObject<?>>)depsSummarizedAsObject;//depFindAPI.findSummariesOfDependentsOf(depObjFactory.getParentModelObject().getOid());
			Assert.assertTrue(CollectionUtils.hasData(depsSummarized) && depsSummarized.size() == depOids.size());

			// find chils
			Collection<M> deps = depFindAPI.findDependentsOf(depObjFactory.getParentModelObject().getOid());
			Assert.assertTrue(CollectionUtils.hasData(deps) && deps.size() == depOids.size());
		}

		log.warn("[end ][TEST BASIC FIND {}] (elapsed time: {} milis)-------------------------",
				 _managesTestMockObjs.getModelObjType(),NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)));

		// [99]: Delete previously created test objects to restore DB state
		_managesTestMockObjs.tearDownCreatedMockObjs();
	}
}

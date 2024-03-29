package r01f.test.persistence;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import com.google.common.base.Stopwatch;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.PersistableObjectOID;
import r01f.locale.Language;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedModelObject;
import r01f.services.client.api.delegates.ClientAPIDelegateForDependentModelObjectFindServices;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectFindServices;
import r01f.services.client.api.delegates.ClientAPIHasDelegateForDependentModelObjectFind;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class TestPersistableDependentModelObjectFind<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
													 PO extends PersistableObjectOID,P extends PersistableModelObject<PO>> {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	private final ClientAPIDelegateForModelObjectFindServices<O,M> _findAPI;
	private final ManagesTestMockModelObjsLifeCycle<O,M> _managesTestMockObjs;

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>,
				   PO extends PersistableObjectOID,P extends PersistableModelObject<PO>>
		  		 TestPersistableDependentModelObjectFind<O,M,PO,P> create(final ClientAPIDelegateForModelObjectFindServices<O,M> findAPI,
																		  final ManagesTestMockModelObjsLifeCycle<O,M> modelObjFactory) {
		return new TestPersistableDependentModelObjectFind<O,M,PO,P>(findAPI,
													   				 modelObjFactory);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tests the CRUD API (creates an entity, updates it, loads it and finally deletes it)
	 * @param modelObject
	 */
	@SuppressWarnings({ "rawtypes","unchecked" })
	public void testFindDependent() {
		if (!(_findAPI instanceof ClientAPIHasDelegateForDependentModelObjectFind)) throw new IllegalArgumentException();

		log.warn("[init][TEST BASIC FIND {}]-----------------------------------------------------------------------",
				 _managesTestMockObjs.getModelObjType());

		Stopwatch stopWatch = Stopwatch.createStarted();

		// [0]: SetUp: create some test objects
		_managesTestMockObjs.setUpMockObjs(5);


		// [1] - If it's a dependent model object, test the specific methods
		log.warn("\tFIND DEPENDENT ENTITIES");

		ClientAPIDelegateForDependentModelObjectFindServices<O,M,PO> depFindAPI = (ClientAPIDelegateForDependentModelObjectFindServices<O,M,PO>)((ClientAPIHasDelegateForDependentModelObjectFind<?>)_findAPI).getClientApiForDependentDelegate();
		ManagesTestMockDependentModelObjsLifeCycle<O,M,P> depObjFactory = (ManagesTestMockDependentModelObjsLifeCycle<O,M,P>)_managesTestMockObjs;

		// find child oids
		Collection<O> depOids = depFindAPI.findOidsOfDependentsOf(depObjFactory.getParentModelObject().getOid());
		Assert.assertTrue(CollectionUtils.hasData(depOids));
		log.warn("\t\t>> {} dependent objects of {}",depOids.size(),depObjFactory.getParentModelObject().getClass());
		for (O depOid : depOids) {
			log.warn("\t\t\t> {}",depOid);
		}
		// find child summaries
		Object depsSummarizedAsObject = depFindAPI.findSummariesOfDependentsOf(depObjFactory.getParentModelObject().getOid(),
																			   Language.DEFAULT);
		Collection<? extends SummarizedModelObject<?>> depsSummarized = (Collection<? extends SummarizedModelObject<?>>)depsSummarizedAsObject;//depFindAPI.findSummariesOfDependentsOf(depObjFactory.getParentModelObject().getOid());
		Assert.assertTrue(CollectionUtils.hasData(depsSummarized) && depsSummarized.size() == depOids.size());

		// find chils
		Collection<M> deps = depFindAPI.findDependentsOf(depObjFactory.getParentModelObject().getOid());
		Assert.assertTrue(CollectionUtils.hasData(deps) && deps.size() == depOids.size());

		log.warn("[end ][TEST BASIC FIND {}] (elapsed time: {} milis)-------------------------",
				 _managesTestMockObjs.getModelObjType(),NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)));

		// [99]: Delete previously created test objects to restore DB state
		_managesTestMockObjs.tearDownCreatedMockObjs();
	}
}

package r01f.test.persistence;

/**
 * Used when a requires the existence of any other model objects
 * just extend {@link TestPersistableModelObjectBase} as usual and implement this interface
 * <pre class='brush:java'>
 * 		public class MyTest 
 * 			 extends TestPersistableModelObjectBase
 * 		  implements ManagesTestRequiredModelObjects {
 * 				private MyRequiredObjType _myRequiredObj;
 * 
 * 				private ManagesTestMockModelObjsLifeCycle<MyRequiredObjOID,MyRequiredObj> _managesMyRequiredObj;
 * 
 * 				public MyTest(final MyClientAPI api) {
 * 					// this object
 *					super(TestPersistableModelObjectManager.create(MyObj.class,new MockMyObjFactory(),
 *													   			   api.myObjAPI().getForCRUD(),
 *													   			   1000l),		// sleep 1000 milis before deleting DB records to give time to background jobs (notifications) to complete
 *			  			  api.myObjAPI().getForCRUD(),api.myObjAPI().getForFind());
 *
 *					// the required object
 *					_managesMyRequiredObj = TestPersistableModelObjectManager.create(MyRequiredObj.class,new MockMyRequiredObjFactory(),
 *													   			   		 			 api.myRequiredObjAPI().getForCRUD(),
 *													   			         			 1000l);
 * 				}
 * 				public void setUpRequiredObjects() {
 * 					// create any required object
 *					_myRequiredObj = _managesMyRequiredObj.setUpSingleModelObj();
 * 				}
 * 				public void tearDownRequiredObjects() {
 * 					// tear down any previously created object
 * 					_managesMyRequiredObj.tearDownCreatedMockModelObjs();
 * 				}
 * 		}
 * </pre> 
 */
public interface ManagesTestRequiredModelObjects {
	public void setUpRequiredObjects();
	public void tearDownRequiredObjects();
}

package r01f.test.api;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.bootstrap.services.ServicesBootstrapUtil;
import r01f.bootstrap.services.config.ServicesBootstrapConfig;
import r01f.bootstrap.services.config.core.ServicesCoreModuleEventsConfig;
import r01f.concurrent.Threads;
import r01f.securitycontext.SecurityIDS.LoginID;
import r01f.securitycontext.SecurityOIDs.UserOID;
import r01f.services.annotations.ClientAPIForSystemUser;
import r01f.services.client.ClientAPI;
import r01f.types.TimeLapse;
import r01f.util.types.Strings;

/**
 * JVM arguments:
 * -javaagent:d:/develop/local_libs/aspectj/lib/aspectjweaver.jar -Daj.weaving.verbose=true
 */
@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class TestAPIBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static final UserOID TEST_USER_OID = UserOID.supply();
	public static final LoginID TEST_USER = LoginID.forId("testUser");
/////////////////////////////////////////////////////////////////////////////////////////
//  STATIC FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected static Collection<ServicesBootstrapConfig> SERVICES_BOOTSTRAP_CONFIG;
	protected static Injector GUICE_INJECTOR;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static Injector getGuiceInjector() {
		return GUICE_INJECTOR;
	}
	public static <A extends ClientAPI> A getClientApi(final Class<A> apiType) {
		return GUICE_INJECTOR.getInstance(apiType);
	}
	/**
	 * Get a client api binded as @ClientAPIForSystemUser
     * ..... so (IMPORTANT !) requires a ClientAPIForSystemUser Provider at client api bootstrap guice module , fe
     * 	    @Provides					// provides a client api
			@ClientAPIForSystemUser		// for SYSTEM system usage
			@Singleton					// BEWARE!!!
			@SuppressWarnings({ "rawtypes","static-method" })
			private MT01GeoClientAPI _provideMasterClientAPI(@SecurityContextProviderForSystemUser final Provider<SecurityContext> securityContextProvider,
													    	   			   @ModelObjectsMarshaller final Marshaller modelObjectsMarshaller,
													    	  @Named(MT01AppCodes.API_APPCODE_STR) final Map<Class,ServiceInterface> srvcIfaceMappings
	 * @param <A>
	 * @param apiType
	 * @return
	 */
	public static <A extends ClientAPI> A getClientApiForSystemUser(final Class<A> apiType) {
		A api = GUICE_INJECTOR.getProvider(Key.get(apiType,
				                          ClientAPIForSystemUser.class)).get();
		if (api == null) {
			throw new IllegalStateException( Strings.customized( "There is NO instance binded as @ClientAPIForSystemUser for api {} "
																	+ " .Check at client api level (XXXClientBootstrapGuiceModule) "
																	+ "   ..if exists a client api provider for System User ", apiType) );
		}
		return api;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RUN EXACTLY ONCE AT THE VERY BEGINNING OF THE TEST AS A WHOLE
//  (in fact they're run even before the type is constructed -that's why they're static)
/////////////////////////////////////////////////////////////////////////////////////////
	protected static void _setUpBeforeClass(final ServicesBootstrapConfig... srvcBootstrap) {
		if (srvcBootstrap == null) throw new IllegalArgumentException();
		_setUpBeforeClass(Lists.newArrayList(srvcBootstrap));
	}
	protected static void _setUpBeforeClass(final ServicesBootstrapConfig srvcBootstrap,
											final Module... commonClientModules) {
		if (srvcBootstrap == null) throw new IllegalArgumentException();
		_setUpBeforeClass(Lists.newArrayList(srvcBootstrap),
											 commonClientModules);
	}
	protected static void _setUpBeforeClass(final Collection<ServicesBootstrapConfig> servicesBootstrapConfig,
											final Module... commonClientModules) {
		_setUpBeforeClass(servicesBootstrapConfig,
						  null,
						  commonClientModules);
	}
	protected static void _setUpBeforeClass(final Collection<ServicesBootstrapConfig> servicesBootstrapConfig,
										    final ServicesCoreModuleEventsConfig coreEventsCfg,
											final Module... commonClientModules) {
		SERVICES_BOOTSTRAP_CONFIG = servicesBootstrapConfig;

		GUICE_INJECTOR = Guice.createInjector(ServicesBootstrapUtil.getBootstrapGuiceModules(SERVICES_BOOTSTRAP_CONFIG)
																		.withCommonEventsExecutor(coreEventsCfg)
																		.withCommonBindingModules(commonClientModules));

		// If stand-alone (no app-server is used), init the JPA service or any service that needs to be started
		// like the search engine index
		// 		If the core is available at client classpath, start it
		// 		This is the case where there's no app-server
		// 		(usually the JPA's ServiceHandler is binded at the Guice module extending DBGuiceModuleBase at core side)
		ServicesBootstrapUtil.startServices(GUICE_INJECTOR);
	}
	protected static void _tearDownAfterClass() {
		// Close JPA's Persistence Service, Lucene indexes and everything that has to be closed
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		ServicesBootstrapUtil.stopServices(GUICE_INJECTOR);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected void runTest(final int iterationNum) {
		try {
			Stopwatch stopWatch = Stopwatch.createStarted();

			for (int i=0; i < iterationNum; i++) {
				Stopwatch itStopWatch = Stopwatch.createStarted();
				System.out.println("\n\n\n\nSTART =========== Iteration " + i + " ===================\n\n\n\n");

				_doTest();		// Iteration test

				System.out.println("\n\n\n\nEND =========== Iteration " + i + " > " + itStopWatch.elapsed(TimeUnit.SECONDS) + "seconds ===================\n\n\n\n");
			}

			System.out.println("\n\n\n\n******* ELAPSED TIME: " + NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.SECONDS)) + " seconds");
			stopWatch.stop();
		} catch (Throwable th) {
			th.printStackTrace(System.out);

		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("static-method")
	protected void _doTest() {
		log.warn("MUST implement this!");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected static void _giveTimeForBackgroundJobsToFinish(final long milis) {
		_giveTimeForBackgroundJobsToFinish(milis,
										   null);	// default msg
	}
	protected static void _giveTimeForBackgroundJobsToFinish(final TimeLapse lapse) {
		_giveTimeForBackgroundJobsToFinish(lapse.asMilis(),
										   null);	// default msg
	}
	protected static void _giveTimeForBackgroundJobsToFinish(final long milis,
															 final String msg,final Object... msgParams) {
		// wait for background jobs to complete (if there's any background job that depends on DB data -like lucene indexing-
		// 										 if the DB data is deleted BEFORE the background job finish, it'll fail)
		if (Strings.isNullOrEmpty(msg)) {
			log.warn("... give {} milis for background jobs (ie lucene index) to complete before deleting created DB records (ie lucene indexing will fail if the DB record is deleted)",milis);
		} else {
			log.warn("... give {} milis for {}",milis,Strings.customized(msg,msgParams));
		}
		Threads.safeSleep(milis);
	}
	protected static void _giveTimeForBackgroundJobsToFinish(final TimeLapse lapse,
															 final String msg,final Object... msgParams) {
		_giveTimeForBackgroundJobsToFinish(lapse.asMilis(),
										   msg,msgParams);
	}

}

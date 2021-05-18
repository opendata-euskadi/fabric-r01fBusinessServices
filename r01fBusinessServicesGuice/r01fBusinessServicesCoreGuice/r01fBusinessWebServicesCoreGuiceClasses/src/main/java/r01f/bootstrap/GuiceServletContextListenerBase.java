package r01f.bootstrap;

import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import lombok.extern.slf4j.Slf4j;
import r01f.bootstrap.services.ServicesBootstrapUtil;

@Slf4j
public abstract class GuiceServletContextListenerBase 
 			  extends GuiceServletContextListener {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	protected Injector _injector;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  Overridden methods of GuiceServletContextListener
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected Injector getInjector() {
		if (_injector == null) {
			Iterable<Module> guiceModules = _createGuiceModules();
			_injector = Guice.createInjector(guiceModules);
		} else {
			log.warn("The Guice Injector is already created!!!");
		}
		return _injector;
	}
	protected abstract Iterable<Module> _createGuiceModules();
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		log.warn("=============================================");
		log.warn("Loading {} Servlet Context with {}...",
				 servletContextEvent.getServletContext().getContextPath(),
				 this.getClass().getSimpleName());
		log.warn("=============================================");

		super.contextInitialized(servletContextEvent);

		// Init JPA's Persistence Service, Lucene indexes and everything that has to be started
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		ServicesBootstrapUtil.startServices(_injector);
	}
	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		log.warn("=============================================");
		log.warn("DESTROYING {} Servlet Context with {} > closing search engine indexes if they are in use, release background jobs threads and so on...",
				 servletContextEvent.getServletContext().getContextPath(),
				 this.getClass().getSimpleName());
		log.warn("=============================================");

		// Close JPA's Persistence Service, Lucene indexes and everything that has to be closed
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		ServicesBootstrapUtil.stopServices(_injector);

		// finalize
		super.contextDestroyed(servletContextEvent);

		log.warn("=============================================");
		log.warn("{} Servlet Context DESTROYED!!...",
				 servletContextEvent.getServletContext().getContextPath());
		log.warn("=============================================");
	}
}

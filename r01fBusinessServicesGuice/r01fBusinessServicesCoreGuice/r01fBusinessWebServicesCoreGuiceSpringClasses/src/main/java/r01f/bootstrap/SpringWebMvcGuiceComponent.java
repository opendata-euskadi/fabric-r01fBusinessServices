package r01f.bootstrap;

import java.util.List;

import javax.inject.Inject;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.google.inject.Key;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.bootstrap.SpringRootConfigBootstrapGuiceBase.ServiceBootstrapSpringHandler;
import r01f.model.annotations.ModelObjectsMarshaller;
import r01f.objectstreamer.Marshaller;
import r01f.servlet.spring.SpringWebMvcComponent;

@Component
@EnableWebMvc
@Accessors(prefix="_")
public abstract class SpringWebMvcGuiceComponent
	 extends SpringWebMvcComponent {
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Members
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Getter final protected Marshaller _marshaller;
	@Getter final protected ServiceBootstrapSpringHandler _servicesBootstrap;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public SpringWebMvcGuiceComponent( final ServiceBootstrapSpringHandler servicesBootstrap ){
		_marshaller = servicesBootstrap.getInjector()
									   .getInstance(Key.get(Marshaller.class,ModelObjectsMarshaller.class));
		_servicesBootstrap = servicesBootstrap;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// METHODS
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public abstract void configureMessageConverters(List<HttpMessageConverter<?>> converters) ;
	/*@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		if ( converters == null) {
			converters = Lists.newArrayList();
		}

		converters.add( new ModelObjectConverterBase<ModelObject> (_marshaller) {});
		converters.add( new PersistenceOperationConverter(_marshaller));
		converters.add( new PersistenceExceptionConverter());
		converters.add( new ThrowableExceptionConverter());

	 }*/

}
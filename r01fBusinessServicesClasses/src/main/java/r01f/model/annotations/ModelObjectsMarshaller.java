package r01f.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Annotation that tells guice to inject a {@link javax.xml.bind.Marshaller}
 * used to map XML<->Java the R01M model object
 * <pre class='brush:java'>
 * 		public class MyService {
 * 			@Inject @ModelObjectsMarshaller
 * 			Marshaller _marsaller;
 * 		}
 * </pre>
 */
@Qualifier //@BindingAnnotation 
@Target({ ElementType.FIELD,ElementType.PARAMETER,ElementType.METHOD }) 
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelObjectsMarshaller {
	/* nothing to do */
}

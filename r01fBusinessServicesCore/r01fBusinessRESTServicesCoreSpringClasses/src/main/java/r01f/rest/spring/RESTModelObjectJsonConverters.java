package r01f.rest.spring;

import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;

import lombok.experimental.Accessors;
import r01f.model.ModelObject;
import r01f.model.search.SearchModelObject;
import r01f.model.services.COREServiceMethodExecResult;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.index.IndexManagementCommand;
import r01f.types.jobs.EnqueuedJob;

/**
 * Types in charge of convert the {@link Response} of a REST method form the business type returned from the method (ie {@link R01MStructureLabel})
 * to the bytes returned by the servlet in the {@link OutputStream}
 * ie: if inside a REST module exists a method like
 * <pre class='brush:java'>
 * 		@DELETE @Path("record/{id}")
 *		@Produces(application/xml)
 *		public Record deleteRecord(@PathParam("id") final String id)  {
 *			....
 *		}
 * </pre>
 * In order to return in the OutputStream an instanceof Record a serialization to bytes of this java object must be done
 * This kind of serialization is done at the type-mappers which implements the {@link MessageBodyWriter} or {@link MessageBodyReader}
 * interfaces, whether it:
 * <ul>
 * 		<li>serializes the method return type TO the {@link Response} {@link OutputStream}</li>
 * 		<li>... or serializes a method param FROM the {@link Request} {@link InputStream}</li>
 * </ul>
 * Spring scans the classpath searching the types that must be published as REST resources and also scans searching
 * types implementing {@link GenericHttpMessageConverter}
 * <pre>
 * NOTE:	As an alternative of JAX-RS scanning the classpath for the types, these can be issued at the
 * 			getClasses() method of the REST {@link Application} instance
 * </pre>
 * 			<pre class='brush:java'>
 * 					@Override
 *					public Set<Class<?>> getClasses() {
 *						Set<Class<?>> s = new HashSet<Class<?>>();
 *						s.add(LongResponseTypeMapper.class);
 *						...
 *						return s;
 *					}
 * 			</pre>
 *
 * For example, the {@link MessageBodyWriter} interface has three methods:
 * 	<table>
 * 		<tr>
 * 			<td>isWriteable</td>
 * 			<td>
 * 				<p>In this method a decision is made about the possibility of serialization of a received type using this {@link MessageBodyWriter} instance</p>
 * 				<p>Every {@link MessageBodyWriter} types are iterated one after another calling it's isWriteable method until one returning true is found</p>
 * 				<p>In order to make a decision to serialize or not some type, some methods can be used_</p>
 * 				<ul>
 * 					<li>Using the type of the object to serialize; this can be useful if the {@link MessageBodyWriter} instance is used for a concrete type</li>
 * 					<li>Using the MIME-TYPE: The method is annotated with @Produces(SOME-MIME-TYPE) and this MIME-TYPE is used to make the decision</li>
 * 					<li>Using some annotation:
 * 						<ul>
 * 							<li>if a REST module method return type is to be serialized, the method can be annotated with a custom annotation.</li>
 * 							<li>If a REST module method param is to be serialized, the param can be annotated with a custom annotation</li>
 *						</ul>
 *					</li>
 * 				</ul>
 * 			</td>
 * 		</tr>
 * 		<tr>
 * 			<td>getSize</td>
 * 			<td>
 * 				It the response size in bytes is known, this size must be returned; otherwise, return -1
 * 			</td>
 * 		</tr>
 * 		<tr>
 * 			<td>writeTo</td>
 * 			<td>
 * 				Performs the java object serialization to bytes written in the {@link Response} {@link OutputStream}
 * 			</td>
 * 		</tr>
 * 	</table>
 *
 * (see http://stackoverflow.com/questions/8194408/how-to-access-parameters-in-a-restful-post-method)
 *
 */

public class RESTModelObjectJsonConverters {

	@Accessors(prefix="_")
	public static abstract class ObjectMessageJsonConverter<T>
	   extends RESTMessageConverterBase<T>
	   implements GenericHttpMessageConverter<T> {

		public ObjectMessageJsonConverter(final Class<?> mappedType, final Marshaller modelObjectsMarshaller) {
			super(mappedType, MediaType.APPLICATION_JSON, modelObjectsMarshaller);
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////
//	ModelObject
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * GenericHttpMessageConverter for all {@link ModelObject}s
	 */
	@Accessors(prefix="_")
	public static abstract class ModelObjectConverterBase<M extends ModelObject>
		        		 extends ObjectMessageJsonConverter<M> {
		public ModelObjectConverterBase(final Marshaller marshaller) {
			super(ModelObject.class,
				  marshaller);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SearchModelObject
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * GenericHttpMessageConverter for all {@link SearchModelObject}s
	 */
	@Accessors(prefix="_")
	public static abstract class SearchModelObjectConverter
		     			 extends ObjectMessageJsonConverter<SearchModelObject> {

		public SearchModelObjectConverter(final Marshaller marshaller) {
			super(SearchModelObject.class,
				  marshaller);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RecordPersistenceOperationResult
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * GenericHttpMessageConverter for all {@link PersistenceOperationOK}
	 */
	public static class COREServiceMethodExecResultConverter
		                 extends ObjectMessageJsonConverter<COREServiceMethodExecResult<?>> {
		public COREServiceMethodExecResultConverter(final Marshaller modelObjectsMarshaller) {
			super(COREServiceMethodExecResult.class,
				  modelObjectsMarshaller);
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////
//	IndexManagementCommand & EnqueuedJob
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * GenericHttpMessageConverter for all {@link IndexManagementCommand}s
	 */
	@Accessors(prefix="_")
	public static abstract class IndexManagementCommandConverter
		     			 extends ObjectMessageJsonConverter<IndexManagementCommand> {

		public IndexManagementCommandConverter(final Marshaller marshaller) {
			super(IndexManagementCommand.class,
				  marshaller);
		}
	}
	/**
	 * GenericHttpMessageConverter for all {@link EnqueuedJob}s
	 */
	@Accessors(prefix="_")
	public static abstract class EnqueuedJobConverter
		     			 extends ObjectMessageJsonConverter<EnqueuedJob> {

		public EnqueuedJobConverter(final Marshaller marshaller) {
			super(EnqueuedJob.class,
				  marshaller);
		}
	}

}

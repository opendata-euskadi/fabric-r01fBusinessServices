package r01f.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.guids.OIDs;
import r01f.io.util.StringPersistenceUtils;
import r01f.model.ModelObject;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.index.IndexManagementCommand;
import r01f.reflection.ReflectionUtils;
import r01f.rest.RESTRequestTypeMappersForBasicTypes.MarshalledObjectRequestTypeMapper;
import r01f.types.jobs.EnqueuedJob;
import r01f.util.types.Strings;

/**
 * Type mappers for user types received as POST payload
 */
@Slf4j
public class RESTRequestTypeMappersForModelObjects {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	public static abstract class ModelObjectRequestTypeMapperBase<M extends ModelObject>
		  	    		 extends MarshalledObjectRequestTypeMapper<M> {

		@Getter private final Marshaller _objectsMarshaller;

		public ModelObjectRequestTypeMapperBase(final Marshaller marshaller) {
			this(marshaller,
				 MediaType.APPLICATION_XML_TYPE,MediaType.APPLICATION_JSON_TYPE);
		}

		public ModelObjectRequestTypeMapperBase(final Marshaller marshaller,
												final MediaType... mediaType) {
			super(ModelObject.class,
				  mediaType);
			_objectsMarshaller = marshaller;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	OID
/////////////////////////////////////////////////////////////////////////////////////////
	public static abstract class OIDRequestTypeMapperBase<O extends OID>
		  	 		  implements MessageBodyReader<O> {

		private final Marshaller _marshaller;

		@Inject
		public OIDRequestTypeMapperBase(final Marshaller marshaller) {
			_marshaller = marshaller;
		}

		@Override
		public boolean isReadable(final Class<?> type,final Type genericType,
								  final Annotation[] annotations,
								  final MediaType mediaType) {
			// every application/xml received params are transformed to java in this type
			return (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE) || mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE))
				&& ReflectionUtils.isImplementing(type,OID.class);
		}
		@Override
		public O readFrom(final Class<O> type,final Type genericType,
						  final Annotation[] annotations,
						  final MediaType mediaType,
						  final MultivaluedMap<String,String> httpHeaders,
						  final InputStream entityStream) throws IOException,
							   								     WebApplicationException {
			log.trace("reading {} type",type.getName());
			// xml -> java
			String oidStr = StringPersistenceUtils.load(entityStream).trim();
			O outObj = null;
			if (Strings.isNOTNullOrEmpty(oidStr)) {
				// marshall from xml
				if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)
				 && oidStr.startsWith("<")) {
					outObj = _marshaller.forReading().fromXml(oidStr,
															  type);
				}
				// marshall from json
				else if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)
						&& oidStr.startsWith("{")) {
					outObj = _marshaller.forReading().fromJson(oidStr,
															   type);
				}
				// invoke oid.forId(...) method
				else {
					outObj = OIDs.createOIDFromString(type,
													  oidStr);
				}
			}
			return outObj;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	IndexManagementCommand & EnqueuedJob
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * MessageBodyReader for all {@link IndexManagementCommand}s
	 */
	@Accessors(prefix="_")
	public static abstract class IndexManagementCommandRequestTypeMapperBase
		     			 extends MarshalledObjectRequestTypeMapper<IndexManagementCommand> {

		@Getter private final Marshaller _objectsMarshaller;

		public IndexManagementCommandRequestTypeMapperBase(final Marshaller marshaller) {
			this(marshaller,
				 MediaType.APPLICATION_XML_TYPE,MediaType.APPLICATION_JSON_TYPE);
		}

		public IndexManagementCommandRequestTypeMapperBase(final Marshaller marshaller,
														   final MediaType... mediaType) {
			super(IndexManagementCommand.class,
				  mediaType);
			_objectsMarshaller = marshaller;
		}
	}
	/**
	 * MessageBodyReader for all {@link EnqueuedJob}s
	 */
	@Accessors(prefix="_")
	public static abstract class EnqueuedJobRequestTypeMapperBase
		     			 extends MarshalledObjectRequestTypeMapper<EnqueuedJob> {

		@Getter private final Marshaller _objectsMarshaller;

		public EnqueuedJobRequestTypeMapperBase(final Marshaller marshaller) {
			this(marshaller,
				 MediaType.APPLICATION_XML_TYPE,MediaType.APPLICATION_JSON_TYPE);
		}

		public EnqueuedJobRequestTypeMapperBase(final Marshaller marshaller,
												final MediaType... mediaType) {
			super(EnqueuedJob.class,
				  mediaType);
			_objectsMarshaller = marshaller;
		}
	}
}

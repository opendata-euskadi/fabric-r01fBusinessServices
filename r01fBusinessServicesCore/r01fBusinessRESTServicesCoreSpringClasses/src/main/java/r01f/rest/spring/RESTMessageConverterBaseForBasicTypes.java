package r01f.rest.spring;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.reflection.ReflectionUtils;

@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class RESTMessageConverterBaseForBasicTypes<T>
   implements GenericHttpMessageConverter<T> {

	@Getter private final Class<?> _mappedType;

///////////////////////////////////////////////////////////////////////////////////////////////////
// METHODS TO IMPLEMENT
//////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canRead( final Class<?> type, final MediaType mediaType){
	
		return type.equals(_mappedType);

	}
	@Override
	public boolean canRead(final Type type, final Class<?> contextClass, final  MediaType mediaType) {
		
		return ReflectionUtils.classOfType(type).equals(_mappedType);

	}
	@Override
	public boolean canWrite(final Type type, final Class<?> contextClass, final  MediaType mediaType){
		boolean outWriteable = false;
	
        if (  ReflectionUtils.classOfType(type).equals(_mappedType)) {
		     outWriteable = true;
		}
		log.warn("{} type is {} writeable with {}", ReflectionUtils.classOfType(type),
													outWriteable ? "" : "NOT",
													this.getClass().getName());
		return outWriteable;
	}

	@Override
	public boolean canWrite(final Class<?> type,  final MediaType mediaType) {
	    boolean outWriteable = false;
		if ( type.equals(_mappedType)) {
		     outWriteable = true;
		}
		log.warn("{} class is {} writeable with {}",type.getName(),
													outWriteable ? "" : "NOT",
													this.getClass().getName());
		return outWriteable;

	}
	@Override
	public List<MediaType> getSupportedMediaTypes() {
		List<MediaType> supportedTypes = Lists.newArrayList();
		supportedTypes.add(MediaType.ALL);
		return supportedTypes;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  READ PUBLIC METHODS
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public T read(final Class<? extends T> type, final HttpInputMessage inputMessage) throws IOException {
		return _doRead(type,inputMessage);
	}
	@Override
	public T read( final Type type, final Class<?> contextClass, final HttpInputMessage inputMessage) throws IOException  {
		throw new UnsupportedOperationException(" No implemented : T read( final Type type, final Class<?> contextClass, final HttpInputMessage inputMessage)");
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  WRITE PUBLIC METHODS
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void write(final T object, final MediaType contentType, final HttpOutputMessage outputMessage)  throws IOException {
		_doWrite(object,outputMessage);
	}

	@Override
	public void write(final T object, final Type type, final  MediaType contentType, final  HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		_doWrite(object,outputMessage);
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  PROTECTED METHODS
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected abstract void _doWrite(final T object, final HttpOutputMessage outputMessage) throws IOException ;

	protected abstract T _doRead(final Class<? extends T> type, final HttpInputMessage inputMessage) throws IOException;


}

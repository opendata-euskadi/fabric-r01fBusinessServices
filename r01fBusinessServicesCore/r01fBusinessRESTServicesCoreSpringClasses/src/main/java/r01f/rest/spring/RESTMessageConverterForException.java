package r01f.rest.spring;

import java.io.IOException;



import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import r01f.exceptions.Throwables;
import r01f.model.persistence.PersistenceException;
import r01f.model.services.COREServiceException;

/**
 * {@link CoreServiceExceptionConverter}(s) used to convert {@link Exception}s to {@link Response}s
 *
 * <pre>
 * IMPORTANT!	Do NOT forget to include this types at
 * </pre>
 */
public class RESTMessageConverterForException {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////
	public  static class CoreServiceExceptionConverter
	          extends RESTMessageConverterBaseForBasicTypes<COREServiceException>{

		public CoreServiceExceptionConverter() {
			super(PersistenceException.class);
		}

		@Override
		protected void _doWrite(final COREServiceException exception, HttpOutputMessage outputMessage) throws IOException {
			 outputMessage.getBody().write(Throwables.getStackTraceAsString(exception).getBytes());
		}

		@Override
		protected PersistenceException _doRead(Class<? extends COREServiceException> type,  HttpInputMessage inputMessage) throws IOException {
			throw new IllegalArgumentException( " A exception should not be read ");
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Throwable
/////////////////////////////////////////////////////////////////////////////////////////
	public static class ThrowableExceptionConverter
	          extends RESTMessageConverterBaseForBasicTypes<Throwable> {

		public ThrowableExceptionConverter() {
			super(Throwable.class);
		}
		@Override
		protected void _doWrite(final Throwable exception, HttpOutputMessage outputMessage) throws IOException {
			 outputMessage.getBody().write(Throwables.getStackTraceAsString(exception).getBytes());
		}
		@Override
		protected Throwable _doRead(Class<? extends Throwable> type, HttpInputMessage inputMessage) throws IOException {
			throw new IllegalArgumentException( " A exception should not be read ");
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////


}

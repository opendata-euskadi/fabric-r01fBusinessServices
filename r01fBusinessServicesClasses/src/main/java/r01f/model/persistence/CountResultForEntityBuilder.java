package r01f.model.persistence;

import com.google.common.annotations.GwtIncompatible;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.model.services.COREServiceErrorType;
import r01f.model.services.COREServiceErrorTypes;
import r01f.securitycontext.SecurityContext;
import r01f.types.url.Url;
import r01f.util.types.Strings;

@GwtIncompatible
public abstract class CountResultForEntityBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
	public static class CountResultBuilderForErrorStep<T> {
		protected final SecurityContext _securityContext;
		protected final Class<T> _entityType;
		
		public CountResultBuilderForErrorExtErrorCodeStep<T> because(final Throwable th) {
			CountError<T> err = new CountError<T>(_entityType,
							    				  th);
			return new CountResultBuilderForErrorExtErrorCodeStep<T>(_securityContext,
												 			         err);
		}
		public CountResultBuilderForErrorExtErrorCodeStep<T> because(final CountError<?> otherCRUDError) {
			CountError<T> err = new CountError<T>(_entityType,
												  otherCRUDError);
			return new CountResultBuilderForErrorExtErrorCodeStep<T>(_securityContext,
															  		 err);
		}
		public CountResultBuilderForErrorExtErrorCodeStep<T> becauseClientCannotConnectToServer(final Url serverUrl) {
			CountError<T> err = new CountError<T>(_entityType,
												  PersistenceServiceErrorTypes.CLIENT_CANNOT_CONNECT_SERVER,	
							    				  Strings.customized("Cannot connect to server at {}",serverUrl));
			return new CountResultBuilderForErrorExtErrorCodeStep<T>(_securityContext,
															  		 err);
		}
		public CountResultBuilderForErrorExtErrorCodeStep<T> becauseServerError(final String errData,final Object... vars) {
			CountError<T> err = new CountError<T>(_entityType,
												  COREServiceErrorTypes.SERVER_ERROR,
												  Strings.customized(errData,vars));
			return new CountResultBuilderForErrorExtErrorCodeStep<T>(_securityContext,
															  		 err);
		}
		public CountResultBuilderForErrorExtErrorCodeStep<T> becauseClientError(final COREServiceErrorType errorType,
																		 	    final String msg,final Object... vars) {
			if (!errorType.isClientError()) throw new IllegalArgumentException(errorType + " is NOT a client error!");
			CountError<T> err = new CountError<T>(_entityType,
												  errorType,
												  Strings.customized(msg,vars));
			return new CountResultBuilderForErrorExtErrorCodeStep<T>(_securityContext,
															 		 err);
		}
		public CountResultBuilderForErrorExtErrorCodeStep<T> becauseClientBadRequest(final String msg,final Object... vars) {
			CountError<T> err = new CountError<T>(_entityType,
												  COREServiceErrorTypes.BAD_CLIENT_REQUEST,
												  Strings.customized(msg,vars));
			return new CountResultBuilderForErrorExtErrorCodeStep<T>(_securityContext,
															  		 err);
		}
		public CountResultBuilderForErrorExtErrorCodeStep<T> becauseRequiredRelatedEntityWasNOTFound(final String msg,final Object... vars) {
			CountError<T> err = new CountError<T>(_entityType,
												  PersistenceServiceErrorTypes.RELATED_REQUIRED_ENTITY_NOT_FOUND,
												  Strings.customized(msg,vars));
			return new CountResultBuilderForErrorExtErrorCodeStep<T>(_securityContext,
															 		 err);		
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
	public static class CountResultBuilderForErrorExtErrorCodeStep<T> { 
		protected final SecurityContext _securityContext;
		protected final CountError<T> _err;
		
		public CountError<T> buildWithExtendedErrorCode(final int extErrCode) {
			_err.setErrorCode(extErrCode);
			return _err;
		}
		public CountError<T> build() {
			return _err;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SUCCESS
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
	public static class CountResultBuilderForOKStep<T> {
		
		protected final SecurityContext _securityContext;
		protected final Class<T> _entityType;
		protected final String _requestedOpName;
		
		public CountOK<T> resulting(final long num) {
			CountOK<T> countOk = new CountOK<T>(_entityType,
											 	num);
			return countOk;			
		}
	}
}
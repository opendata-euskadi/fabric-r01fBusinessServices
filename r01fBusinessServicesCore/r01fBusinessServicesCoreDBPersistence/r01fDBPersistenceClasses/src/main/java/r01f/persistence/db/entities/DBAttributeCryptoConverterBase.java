package r01f.persistence.db.entities;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;

import r01f.types.StringBase64;

/**
 * Sample. Must be uptaded to get key from  properties...!!
 * https://gist.github.com/simbo1905/0e696dec7eee5f4bacb2
 * @author iolabaro *
 */

//@Converter
public abstract class DBAttributeCryptoConverterBase
		   implements AttributeConverter<String,StringBase64> {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	protected static final String ALGORITHM = "AES/ECB/PKCS5Padding";
	protected abstract byte[] getKey();
//////////////////////////////////////////////////////////////////////////////////////////////////
// 	CONVERT TO DATABASE COLUMN
/////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public StringBase64 convertToDatabaseColumn(final String attribute) {
		Key key = new SecretKeySpec(getKey(), "AES");
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);			
			
			return  StringBase64.encode(c.doFinal(attribute.getBytes()));
		} catch (final Throwable e) {
			throw new RuntimeException(e);
		}
	}	
//////////////////////////////////////////////////////////////////////////////////////////////////
// 	CONVERT FROM DATABASE COLUMN
/////////////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public String convertToEntityAttribute(final StringBase64 dbData) {
		Key key = new SecretKeySpec(getKey(), "AES");
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key);
			return new String(c.doFinal(dbData.decode().getBytes()));
		} catch (final Throwable e) {
			throw new RuntimeException(e);
		}
	}
}

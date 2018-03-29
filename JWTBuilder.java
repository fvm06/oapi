package JWT;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JWTBuilder {    
    //to string
    public String encodeBase64(byte[] data) {
        byte[] encoded = Base64.getEncoder().withoutPadding().encode(data);
  	return new String(encoded, StandardCharsets.UTF_8);
    }
    //to Base64
    public String decodeBase64(byte[] data) {		
	byte[] decoded=Base64.getDecoder().decode(data);
	return new String(decoded, StandardCharsets.UTF_8);
    }
    //Hash-based Message Authentication Code (HMAC) for the specified secret key and message
    public String signHmacSHA256Base64(String header, String payload, String secret)
                                    throws InvalidKeyException, NoSuchAlgorithmException{
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        byte[] message = (header + "." + payload).getBytes();    
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
	mac.init(secretKey); 
        mac.update(message);
        return this.encodeBase64(mac.doFinal());
    }

}

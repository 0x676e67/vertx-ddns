import com.zf1976.ddns.util.RsaUtil;
import org.junit.jupiter.api.Test;

/**
 * @author mac
 * @date 2021/7/10
 */
public class EncryptTest {
    @Test
    public void encryptTest() throws Exception {
        final var rsaKeyPair = RsaUtil.generateKeyPair();
        final var publicKey = rsaKeyPair.getPublicKey();
        final var privateKey = rsaKeyPair.getPrivateKey();
        System.out.println(publicKey);
        System.out.println(privateKey);
        final var encodeContent = RsaUtil.encryptByPublicKey(publicKey, "LTAI5tDQhy5qHf9AvJocLX7G");
        System.out.println(encodeContent);
        System.out.println(RsaUtil.decryptByPrivateKey(privateKey, encodeContent));

    }
}

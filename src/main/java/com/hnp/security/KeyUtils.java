package com.hnp.security;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtils {

    public static PublicKey loadPublicKey() throws Exception {
        try (InputStream is = KeyUtils.class.getClassLoader().getResourceAsStream("keys/publicKey.pem")) {
            if (is == null) throw new RuntimeException("Public key not found in classpath!");

            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA"); // الگوریتم کلید باید همون باشه که JWT باهاش sign شده
            return kf.generatePublic(spec);
        }
    }
}

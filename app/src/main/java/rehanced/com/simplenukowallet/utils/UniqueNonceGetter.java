package rehanced.com.simplenukowallet.utils;

import java.util.HashMap;

/**
 * Used for temporary caching of responses. Clears once android garbage collects
 */
public class UniqueNonceGetter {

    private int nonce = 0;
    private static UniqueNonceGetter instance;

    public static UniqueNonceGetter getInstance() {
        if (instance == null)
            instance = new UniqueNonceGetter();
        return instance;
    }

    public int getNonce() {
        nonce = nonce +1;
        return nonce;
    }

}

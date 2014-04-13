package peergos.fs;

import peergos.crypto.User;
import peergos.crypto.UserPublicKey;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class Chunk
{
    public static final int MAX_SIZE = 4*1024*1024;
    public static final String ALGORITHM = "AES";
    public static final String MODE = "AES/CFB/NoPadding";

    private final byte[] data;
    private final SecretKey key;

    public Chunk(byte[] data)
    {
        if (data.length > MAX_SIZE)
            throw new IllegalArgumentException("Chunk size must be smaller than " + MAX_SIZE);
        this.data = data;
        key = generateKey();
    }

    public SecretKey getKey()
    {
        return key;
    }

    private SecretKey generateKey()
    {
        try {
            byte[] hash = UserPublicKey.hash(data);
            SecureRandom random = SecureRandom.getInstance(User.SECURE_RANDOM);
            random.setSeed(hash);
            KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM, "BC");
            int keySize = 256;
            kgen.init(keySize, random);
            SecretKey key = kgen.generateKey();
            return  key;
        } catch (NoSuchAlgorithmException|NoSuchProviderException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
    }

    public byte[] encrypt()
    {
        try {
            Cipher cipher = Cipher.getInstance(MODE, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getEncoded(), MODE));
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException|NoSuchProviderException|NoSuchPaddingException|IllegalBlockSizeException|
                BadPaddingException|InvalidKeyException e)
        {
            throw new IllegalStateException("Couldn't encrypt chink: "+e.getMessage());
        }
    }
}
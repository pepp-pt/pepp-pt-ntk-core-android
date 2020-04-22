package org.pepppt.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.security.KeyPairGeneratorSpec;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.pepppt.core.telemetry.Telemetry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;
//https://medium.com/fw-engineering/sharedpreferences-and-android-keystore-c4eac3373ac7
public class SharedPrefUtils {

    private static final String TAG = SharedPrefUtils.class.getSimpleName();
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String ALIAS = "pepppt";
    private static final String TYPE_RSA = "RSA";
    private static final String CYPHER = "RSA/ECB/PKCS1Padding";
    private static final String ENCODING = "UTF-8";

    public static void put(Context ctx, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (value == null) {
            prefs.edit().putString(key, null).apply();
        } else {
            try
            {
                prefs.edit().putString(key, encryptString(ctx, value)).apply();
            } catch (Exception e) {
                e.printStackTrace();
                Telemetry.processException(e);
            }
        }

    }

    public static boolean contains(Context ctx, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.contains(key);
    }
    public static String get(Context ctx, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        final String pref = prefs.getString(key, "");

        if (!TextUtils.isEmpty(pref)) {
            return decryptString(ctx, pref);
        }

        return null;
    }

    private static String encryptString(Context context, String toEncrypt) {
        try {
            final KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKey(context);
            if (privateKeyEntry != null) {
                final PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();

                // Encrypt the text
                Cipher input = Cipher.getInstance(CYPHER);
                input.init(Cipher.ENCRYPT_MODE, publicKey);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CipherOutputStream cipherOutputStream = new CipherOutputStream(
                        outputStream, input);
                cipherOutputStream.write(toEncrypt.getBytes(ENCODING));
                cipherOutputStream.close();

                byte[] vals = outputStream.toByteArray();
                return Base64.encodeToString(vals, Base64.DEFAULT);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Telemetry.processException(e);
            return null;
        }
        return null;
    }

    private static String decryptString(Context context, String encrypted) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKey(context);
            if (privateKeyEntry != null) {
                final PrivateKey privateKey = privateKeyEntry.getPrivateKey();

                Cipher output = Cipher.getInstance(CYPHER);
                output.init(Cipher.DECRYPT_MODE, privateKey);

                CipherInputStream cipherInputStream = new CipherInputStream(
                        new ByteArrayInputStream(Base64.decode(encrypted, Base64.DEFAULT)), output);
                ArrayList<Byte> values = new ArrayList<>();
                int nextByte;
                while ((nextByte = cipherInputStream.read()) != -1) {
                    values.add((byte) nextByte);
                }

                byte[] bytes = new byte[values.size()];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = values.get(i);
                }

                return new String(bytes, 0, bytes.length, ENCODING);
            }

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Telemetry.processException(e);
            return null;
        }

        return null;
    }

    private static KeyStore.PrivateKeyEntry getPrivateKey(Context context) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException,
            IOException, UnrecoverableEntryException {

        KeyStore ks = KeyStore.getInstance(KEYSTORE);

        // Weird artifact of Java API.  If you don't have an InputStream to load, you still need
        // to call "load", or it'll crash.
        ks.load(null);

        // Load the key pair from the Android Key Store
        KeyStore.Entry entry = ks.getEntry(ALIAS, null);

        /* If the entry is null, keys were never stored under this alias.
         */
        if (entry == null) {
            Log.w(TAG, "No key found under alias: " + ALIAS);
            Log.w(TAG, "Generating new key...");
            try {
                createKeys(context);

                // reload keystore
                ks = KeyStore.getInstance(KEYSTORE);
                ks.load(null);

                // reload key pair
                entry = ks.getEntry(ALIAS, null);

                if (entry == null) {
                    Log.w(TAG, "Generating new key failed...");
                    return null;
                }
            } catch (NoSuchProviderException | InvalidAlgorithmParameterException e) {
                Log.w(TAG, "Generating new key failed...");
                e.printStackTrace();
                Telemetry.processException(e);
                return null;
            }
        }

        /* If entry is not a KeyStore.PrivateKeyEntry, it might have gotten stored in a previous
         * iteration of your application that was using some other mechanism, or been overwritten
         * by something else using the same keystore with the same alias.
         * You can determine the type using entry.getClass() and debug from there.
         */
        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            Log.w(TAG, "Not an instance of a PrivateKeyEntry");
            Log.w(TAG, "Exiting signData()...");
            return null;
        }

        return (KeyStore.PrivateKeyEntry) entry;
    }

    /**
     * Creates a public and private key and stores it using the Android Key Store, so that only
     * this application will be able to access the keys.
     */
    private static void createKeys(Context context) throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // Create a start and end time, for the validity range of the key pair that's about to be
        // generated.
        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 25);

        // The KeyPairGeneratorSpec object is how parameters for your key pair are passed
        // to the KeyPairGenerator.  For a fun home game, count how many classes in this sample
        // start with the phrase "KeyPair".
        KeyPairGeneratorSpec spec =
                new KeyPairGeneratorSpec.Builder(context)
                        // You'll use the alias later to retrieve the key.  It's a key for the key!
                        .setAlias(ALIAS)
                        // The subject used for the self-signed certificate of the generated pair
                        .setSubject(new X500Principal("CN=" + ALIAS))
                        // The serial number used for the self-signed certificate of the
                        // generated pair.
                        .setSerialNumber(BigInteger.valueOf(Math.abs(ALIAS.hashCode())))
                        // Date range of validity for the generated pair.
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();

        // Initialize a KeyPair generator using the the intended algorithm (in this example, RSA
        // and the KeyStore.  This example uses the AndroidKeyStore.
        final KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(TYPE_RSA, KEYSTORE);
        kpGenerator.initialize(spec);

        final KeyPair kp = kpGenerator.generateKeyPair();
//        Log.d(TAG, "Public Key is: " + kp.getPublic().toString());
    }

}
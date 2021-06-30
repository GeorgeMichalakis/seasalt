package com.ltonetwork.seasalt.sign;

import com.ltonetwork.seasalt.Binary;
import com.ltonetwork.seasalt.KeyPair;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public class ECDSA implements Signer {
    final X9ECParameters curve;
    final ECDomainParameters domain;
    final BigInteger HALF_CURVE_ORDER;
    final Digest digest;

    public ECDSA(X9ECParameters curve, Digest digest) {
        this.curve = curve;
        this.domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());
        this.HALF_CURVE_ORDER = curve.getN().shiftRight(1);
        this.digest = digest;
    }

    public ECDSA(X9ECParameters curve) {
        this(curve, new SHA256Digest());
    }

    public ECDSA(String curve) {
        this(SECNamedCurves.getByName(curve), new SHA256Digest());
    }

    public KeyPair keyPair() {
        SecureRandom srSeed = new SecureRandom();
        byte[] privateKey = generatePrivateKey(srSeed);
        byte[] publicKey = privateToPublic(privateKey);
        return new KeyPair(publicKey, privateKey);
    }

    public KeyPair keyPairFromSeed(byte[] seed) {
        SecureRandom srSeed = new SecureRandom(seed);
        byte[] privateKey = generatePrivateKey(srSeed);
        byte[] publicKey = privateToPublic(privateKey);
        return new KeyPair(publicKey, privateKey);
    }

    public KeyPair keyPairFromSecretKey(byte[] privateKey) {
        byte[] publicKey = privateToPublic(privateKey);
        return new KeyPair(publicKey, privateKey);
    }

    public KeyPair keyPairFromSecretKey(Binary privateKey) {
        return keyPairFromSecretKey(privateKey.getBytes());
    }

    public Binary signDetached(byte[] msg, byte[] privateKey) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(this.digest));
        signer.init(true, new ECPrivateKeyParameters(new BigInteger(privateKey), domain));
        BigInteger[] signature = signer.generateSignature(msg);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            DERSequenceGenerator seq = new DERSequenceGenerator(baos);
            seq.addObject(new ASN1Integer(signature[0]));
            seq.addObject(new ASN1Integer(toCanonicalS(signature[1])));
            seq.close();
            return new Binary(baos.toByteArray());
        } catch (IOException e) {
            return new Binary(new byte[0]);
        }
    }

    public Binary signDetached(byte[] msg, KeyPair keypair) {
        return signDetached(msg, keypair.getPrivateKey().getBytes());
    }

    public Binary signDetached(byte[] msg, Binary privateKey) {
        return signDetached(msg, privateKey.getBytes());
    }

    public Binary signDetached(String msg, byte[] privateKey) {
        return signDetached(msg.getBytes(), privateKey);
    }

    public Binary signDetached(String msg, KeyPair keypair) {
        return signDetached(msg.getBytes(), keypair.getPrivateKey().getBytes());
    }

    public Binary signDetached(String msg, Binary privateKey) {
        return signDetached(msg.getBytes(), privateKey.getBytes());
    }

    public boolean verify(byte[] msg, byte[] signature, byte[] publicKey) {
        try (ASN1InputStream asn1 = new ASN1InputStream(signature)) {
            ECDSASigner signer = new ECDSASigner();
            signer.init(false, new ECPublicKeyParameters(curve.getCurve().decodePoint(publicKey), domain));

            DLSequence seq = (DLSequence) asn1.readObject();
            BigInteger r = ((ASN1Integer) seq.getObjectAt(0)).getPositiveValue();
            BigInteger s = ((ASN1Integer) seq.getObjectAt(1)).getPositiveValue();
            return signer.verifySignature(msg, r, s);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verify(byte[] msg, byte[] signature, KeyPair keypair) {
        return verify(msg, signature, keypair.getPublicKey().getBytes());
    }

    public boolean verify(byte[] msg, byte[] signature, Binary publicKey) {
        return verify(msg, signature, publicKey.getBytes());
    }

    public boolean verify(byte[] msg, Binary signature, byte[] publicKey) {
        return verify(msg, signature.getBytes(), publicKey);
    }

    public boolean verify(byte[] msg, Binary signature, Binary publicKey) {
        return verify(msg, signature.getBytes(), publicKey.getBytes());
    }

    public boolean verify(byte[] msg, Binary signature, KeyPair keypair) {
        return verify(msg, signature.getBytes(), keypair.getPublicKey().getBytes());
    }

    public boolean verify(Binary msg, byte[] signature, byte[] publicKey) {
        return verify(msg.getBytes(), signature, publicKey);
    }

    public boolean verify(Binary msg, byte[] signature, Binary publicKey) {
        return verify(msg.getBytes(), signature, publicKey.getBytes());
    }

    public boolean verify(Binary msg, byte[] signature, KeyPair keypair) {
        return verify(msg, signature, keypair.getPublicKey().getBytes());
    }

    public boolean verify(Binary msg, Binary signature, byte[] publicKey) {
        return verify(msg.getBytes(), signature.getBytes(), publicKey);
    }

    public boolean verify(Binary msg, Binary signature, Binary publicKey) {
        return verify(msg.getBytes(), signature.getBytes(), publicKey.getBytes());
    }

    public boolean verify(Binary msg, Binary signature, KeyPair keypair) {
        return verify(msg.getBytes(), signature.getBytes(), keypair.getPublicKey().getBytes());
    }

    public boolean verify(String msg, byte[] signature, byte[] publicKey) {
        return verify(msg.getBytes(), signature, publicKey);
    }

    public boolean verify(String msg, byte[] signature, Binary publicKey) {
        return verify(msg.getBytes(), signature, publicKey.getBytes());
    }

    public boolean verify(String msg, byte[] signature, KeyPair keypair) {
        return verify(msg.getBytes(), signature, keypair.getPublicKey().getBytes());
    }

    public boolean verify(String msg, Binary signature, byte[] publicKey) {
        return verify(msg.getBytes(), signature.getBytes(), publicKey);
    }

    public boolean verify(String msg, Binary signature, Binary publicKey) {
        return verify(msg.getBytes(), signature.getBytes(), publicKey.getBytes());
    }

    public boolean verify(String msg, Binary signature, KeyPair keypair) {
        return verify(msg.getBytes(), signature.getBytes(), keypair.getPublicKey().getBytes());
    }


    private byte[] privateToPublic(byte[] privateKey) {
        return curve.getG().multiply(new BigInteger(privateKey)).getEncoded(true);
    }

    private byte[] generatePrivateKey(SecureRandom seed) {
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(domain, seed);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        return privParams.getD().toByteArray();
    }

    private BigInteger toCanonicalS(BigInteger s) {
        return s.compareTo(HALF_CURVE_ORDER) <= 0 ? s : curve.getN().subtract(s);
    }
}

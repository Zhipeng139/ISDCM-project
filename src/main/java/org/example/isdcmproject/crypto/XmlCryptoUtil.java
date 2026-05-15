package org.example.isdcmproject.crypto;

import org.apache.xml.security.encryption.XMLCipher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public final class XmlCryptoUtil {

    private static final String ENC_NS = "http://www.w3.org/2001/04/xmlenc#";

    static {
        org.apache.xml.security.Init.init();
    }

    private XmlCryptoUtil() {}

    public static Document parse(InputStream in) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(in);
    }

    public static void write(Document doc, OutputStream out) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(doc), new StreamResult(out));
    }

    public static Document encryptElement(Document doc, String localName, String passphrase) throws Exception {
        Element target = findFirstByLocalName(doc, localName);
        if (target == null)
            throw new IllegalArgumentException("Elemento no encontrado: <" + localName + ">");

        SecretKey key = deriveAesKey(passphrase);
        XMLCipher xmlCipher = XMLCipher.getInstance(XMLCipher.AES_128);
        xmlCipher.init(XMLCipher.ENCRYPT_MODE, key);
        xmlCipher.doFinal(doc, target, false);
        return doc;
    }

    public static Document encryptDocument(Document doc, String passphrase) throws Exception {
        return encryptElement(doc, doc.getDocumentElement().getLocalName(), passphrase);
    }

    public static Document decrypt(Document doc, String passphrase) throws Exception {
        NodeList list = doc.getElementsByTagNameNS(ENC_NS, "EncryptedData");
        if (list.getLength() == 0)
            throw new IllegalArgumentException("El documento no contiene ningún elemento EncryptedData.");

        SecretKey key = deriveAesKey(passphrase);
        for (int i = list.getLength() - 1; i >= 0; i--) {
            Element enc = (Element) list.item(i);
            XMLCipher xmlCipher = XMLCipher.getInstance();
            xmlCipher.init(XMLCipher.DECRYPT_MODE, key);
            xmlCipher.doFinal(doc, enc);
        }
        return doc;
    }

    private static SecretKey deriveAesKey(String passphrase) throws Exception {
        if (passphrase == null || passphrase.isEmpty())
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(passphrase.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(Arrays.copyOf(hash, 16), "AES");
    }

    private static Element findFirstByLocalName(Document doc, String localName) {
        NodeList all = doc.getElementsByTagNameNS("*", localName);
        if (all.getLength() > 0) return (Element) all.item(0);
        all = doc.getElementsByTagName(localName);
        return all.getLength() > 0 ? (Element) all.item(0) : null;
    }
}

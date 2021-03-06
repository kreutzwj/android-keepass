package com.joelkreutzwieser.apps.keepass.keepass.parser;

import android.content.SyncAdapterType;

import com.google.common.io.CharStreams;
import com.joelkreutzwieser.apps.keepass.keepass.crypto.ProtectedStringCrypto;
import com.joelkreutzwieser.apps.keepass.keepass.domain.Entry;
import com.joelkreutzwieser.apps.keepass.keepass.domain.History;
import com.joelkreutzwieser.apps.keepass.keepass.domain.KeePassFile;
import com.joelkreutzwieser.apps.keepass.keepass.domain.Property;
import com.joelkreutzwieser.apps.keepass.keepass.domain.PropertyValue;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class KeePassDatabaseXmlParser {
    public KeePassFile parse(InputStream inputStream, ProtectedStringCrypto protectedStringCrypto, byte[] key) throws IOException {
        try {
            //String testing = CharStreams.toString(new InputStreamReader(inputStream, "UTF-8"));
            //System.out.println(testing);
            //KeePassFile keePassFile = JAXB.unmarshal(inputStream, KeePassFile.class);
            Serializer serializer = new Persister();
            KeePassFile keePassFile = serializer.read(KeePassFile.class, inputStream);
            keePassFile.protectedStringCrypto = protectedStringCrypto;
            List<Entry> entries = keePassFile.getRoot().getAllEntries();
            for (Entry entry : entries) {
                decryptAndSetValues(entry, protectedStringCrypto);
                History history = entry.getHistory();
                for (Entry historicEntry : history.getHistoricalEntries()) {
                    decryptAndSetValues(historicEntry, protectedStringCrypto);
                }
            }
            //keePassFile.init();

            // Decrypt all encrypted values
            /*List<Entry> entries = keePassFile.getEntries();
            for (Entry entry : entries) {
                decryptAndSetValues(entry, protectedStringCrypto);

                // Also decrypt historic password values
                /*History history = entry.getHistory();
                for (Entry historicEntry : history.getHistoricalEntries()) {
                    decryptAndSetValues(historicEntry, protectedStringCrypto);
                }
            }*/

            return keePassFile;
        } catch (Exception e) {
            throw new IOException("KeeFile Serializer Error", e);
        }
    }

    private void decryptAndSetValues(Entry entry, ProtectedStringCrypto protectedStringCrypto) {
        List<Property> properties = entry.getProperties();
        for (Property property : properties) {
            PropertyValue propertyValue = property.getPropertyValue();

            if (propertyValue.isProtected() && propertyValue.getValue() != null) {
                String decrypted = protectedStringCrypto.decrypt(propertyValue.getValue());
                propertyValue.setValue(decrypted);
            }
        }
    }
}

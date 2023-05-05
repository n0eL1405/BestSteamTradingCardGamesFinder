package de.leon.bstcgf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Copied from Baeldung.com
 *
 * @see <a href="https://www.baeldung.com/java-accessing-maven-properties">www.baeldung.com</a>
 */
public class PropertiesReader {
    private final Properties properties;

    public PropertiesReader(String propertyFileName) throws IOException {
        this.properties = new Properties();

        InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream(propertyFileName);
        this.properties.load(inputStream);
    }

    public String getProperty(String propertyName) {
        return this.properties.getProperty(propertyName);
    }
}

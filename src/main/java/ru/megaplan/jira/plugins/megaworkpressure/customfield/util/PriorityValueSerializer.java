package ru.megaplan.jira.plugins.megaworkpressure.customfield.util;

import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class PriorityValueSerializer {

    private static final  Logger log = Logger.getLogger(PriorityValueSerializer.class);
    private static final ObjectMapper serializer = new ObjectMapper();

    private PriorityValueSerializer() {};

    public static <T> String serialize(T megaPriority) {
        Writer writer = new StringWriter();
        try {
            serializer.writeValue(writer, megaPriority);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return writer.toString();
    }

    public static <T> T unserialize(String s, Class<T> clazz) {
        try {
            return serializer.readValue(s, clazz);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

}
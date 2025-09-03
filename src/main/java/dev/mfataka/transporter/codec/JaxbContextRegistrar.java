package dev.mfataka.transporter.codec;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import lombok.SneakyThrows;

/**
 * @author HAMMA FATAKA
 */
public class JaxbContextRegistrar {
    private final ConcurrentMap<Class<?>, JAXBContext> jaxbContexts = new ConcurrentHashMap<>();

    @SneakyThrows
    public Marshaller createMarshaller(final Class<?> clazz) {
        return getJaxbContext(clazz).createMarshaller();
    }

    @SneakyThrows
    public Unmarshaller createUnmarshaller(final Class<?> clazz) {
        return getJaxbContext(clazz).createUnmarshaller();
    }

    @SneakyThrows
    private JAXBContext getJaxbContext(final Class<?> clazz) {
        Objects.requireNonNull(clazz, "class cannot be null");
        return this.jaxbContexts.computeIfAbsent(clazz, tClass -> {
            try {
                return JAXBContext.newInstance(clazz);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

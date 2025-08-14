package dev.mfataka.transporter.codec;

import java.util.Map;

import jakarta.xml.bind.Unmarshaller;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.support.DefaultStrategiesHelper;

import reactor.core.Exceptions;

import lombok.SneakyThrows;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 14.05.2023 5:41
 */
public class JaxbDecoder extends Jaxb2XmlDecoder {

    private final JaxbContextRegistrar jaxbContexts = new JaxbContextRegistrar();


    @Override
    public Object decode(final @NotNull DataBuffer dataBuffer,
                         final @NotNull ResolvableType targetType,
                         final @Nullable MimeType mimeType,
                         final @Nullable Map<String, Object> hints) {
        try {
            final var helper = new DefaultStrategiesHelper(WebServiceTemplate.class);
            final var messageFactory = (SoapMessageFactory) helper.getDefaultStrategy(WebServiceMessageFactory.class);
            final var message = messageFactory.createWebServiceMessage(dataBuffer.asInputStream());
            return unmarshal(message, targetType.toClass());
        } catch (Throwable ex) {
            throw Exceptions.propagate(ex);
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
    }

    @SneakyThrows
    private Object unmarshal(final SoapMessage message, final Class<?> outputClass) {
        final var unmarshaller = getAndApplyMarshaller(outputClass);
        final var jaxbElement = unmarshaller.unmarshal(message.getPayloadSource(), outputClass);
        return jaxbElement.getValue();
    }

    private Unmarshaller getAndApplyMarshaller(final Class<?> outputClass) {
        final var unmarshaller = this.jaxbContexts.createUnmarshaller(outputClass);
        return getUnmarshallerProcessor().apply(unmarshaller);
    }

}


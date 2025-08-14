package dev.mfataka.transporter.codec;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.support.DefaultStrategiesHelper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import lombok.SneakyThrows;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 14.05.2023 20:23
 */
public class JaxbEncoder implements Encoder<Object> {
    private final JaxbContextRegistrar contextRegistrar = new JaxbContextRegistrar();

    @Override
    public boolean canEncode(final @NotNull ResolvableType elementType,
                             final MimeType mimeType) {
        final var isEncodable = preCheckEncodable(mimeType);
        if (isEncodable) {
            final var outputClass = elementType.toClass();
            return (outputClass.isAnnotationPresent(XmlRootElement.class) ||
                    outputClass.isAnnotationPresent(XmlType.class));
        }
        return false;
    }

    private boolean preCheckEncodable(MimeType mimeType) {
        if (Objects.isNull(mimeType)) {
            return true;
        }
        return getEncodableMimeTypes()
                .stream()
                .anyMatch(candidate -> candidate.isCompatibleWith(mimeType));
    }

    @Override
    public @NotNull Flux<DataBuffer> encode(final @NotNull Publisher<?> inputStream,
                                            final @NotNull DataBufferFactory bufferFactory,
                                            final @NotNull ResolvableType elementType,
                                            final MimeType mimeType,
                                            final Map<String, Object> hints) {
        return Flux.from(inputStream)
                .take(1)
                .concatMap(value -> encode(value, bufferFactory))
                .doOnDiscard(PooledDataBuffer.class, PooledDataBuffer::release);
    }

    @Override
    public @NotNull List<MimeType> getEncodableMimeTypes() {
        return Collections.singletonList(MimeTypeUtils.TEXT_XML);
    }


    private Flux<DataBuffer> encode(final Object value,
                                    final DataBufferFactory bufferFactory) {

        final var buffer = bufferFactory.allocateBuffer();
        return Mono.fromCallable(() -> {
                    final var outputStream = buffer.asOutputStream();
                    final var clazz = ClassUtils.getUserClass(value);
                    final var marshaller = initMarshaller(clazz);

                    final var helper = new DefaultStrategiesHelper(WebServiceTemplate.class);
                    final var messageFactory = helper.getDefaultStrategy(WebServiceMessageFactory.class);
                    final var message = messageFactory.createWebServiceMessage();

                    marshaller.marshal(value, message.getPayloadResult());
                    message.writeTo(outputStream);
                    return buffer;
                })
                .doOnError(throwable -> DataBufferUtils.release(buffer))
                .flux();
    }


    @SneakyThrows
    private Marshaller initMarshaller(final Class<?> clazz) {
        final var marshaller = this.contextRegistrar.createMarshaller(clazz);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        return marshaller;
    }
}

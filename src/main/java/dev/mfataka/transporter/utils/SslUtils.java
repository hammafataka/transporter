package dev.mfataka.transporter.utils;

import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Objects;

import javax.net.ssl.*;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;


/**
 * @author HAMMA FATAKA
 */


@Slf4j
@UtilityClass
public class SslUtils {
    private final String DEFAULT_ALGORITHM = "SunX509";
    private final String DEFAULT_STORE_TYPE = "JKS";
    private final String DEFAULT_PROTOCOL_TYPE = "TLS";


    public SslContext buildNettyContextWithTrustStore(final String trustStorePath, final String trustStorePass) {
        return buildNettyContextWithTrustStore(DEFAULT_ALGORITHM, trustStorePath, trustStorePass, DEFAULT_STORE_TYPE);
    }

    public SslContext buildNettyContext(
            final String trustStorePath,
            final String trustStorePass,
            final String keyStorePath,
            final String keyStorePass) {
        return buildNettyContext(DEFAULT_ALGORITHM, trustStorePath, trustStorePass, keyStorePath, keyStorePass, DEFAULT_STORE_TYPE);

    }

    @SneakyThrows
    public SslContext buildNettyContextWithTrustStore(final String algorithm,
                                                      final String trustStorePath,
                                                      final String trustStorePass,
                                                      final String storeType,
                                                      final String... protocols) {
        final var trustManagerFactory = buildTrustManagerFactory(algorithm, trustStorePath, trustStorePass, storeType);
        return SslContextBuilder
                .forClient()
                .protocols(protocols)
                .trustManager(trustManagerFactory)
                .build();

    }

    @SneakyThrows
    public SslContext buildNettyContext(final String algorithm,
                                        final String trustStorePath,
                                        final String trustStorePass,
                                        final String keyStorePath,
                                        final String keyStorePass,
                                        final String storeType,
                                        final String... protocols) {
        final var trustManagerFactory = buildTrustManagerFactory(algorithm, trustStorePath, trustStorePass, storeType);
        final var keyManagerFactory = buildKeyManagerFactory(algorithm, keyStorePath, keyStorePass, storeType);
        return SslContextBuilder
                .forClient()
                .protocols(protocols)
                .keyManager(keyManagerFactory)
                .trustManager(trustManagerFactory)
                .build();

    }

    @SneakyThrows
    public SslContext buildNettyContext(final String algorithm,
                                        final String trustStorePath,
                                        final String trustStorePass,
                                        final String keyStorePath,
                                        final String keyStorePass,
                                        final String alias,
                                        final String storeType,
                                        final String... protocols) {
        final var trustManagerFactory = buildTrustManagerFactory(algorithm, trustStorePath, trustStorePass, storeType);
        final var keyManagers = buildKeyManagerFactory(algorithm, keyStorePath, keyStorePass, storeType, alias);
        final KeyManager keyManager = keyManagers[0];
        return SslContextBuilder
                .forClient()
                .protocols(protocols)
                .keyManager(keyManager)
                .trustManager(trustManagerFactory)
                .build();

    }


    public SSLSocketFactory buildSocketFactoryWithTrustStore(final String trustStorePath, final String trustStorePass) {
        return buildContextWithTrustStore(trustStorePath, trustStorePass).getSocketFactory();
    }

    public SSLSocketFactory buildSocketFactoryWithKeyStore(final String keyStorePath, final String keyStorePass) {
        return buildContextWithKeyStore(keyStorePath, keyStorePass).getSocketFactory();
    }

    public SSLSocketFactory buildSslSocketFactory(final String keyStorePath,
                                                  final String keyStorePass,
                                                  final String trustStorePath,
                                                  final String trustStorePass) {
        return buildContext(keyStorePath, keyStorePass, trustStorePath, trustStorePass).getSocketFactory();
    }

    public SSLSocketFactory buildSocketFactoryWithTrustStore(final String trustStorePath, final String trustStorePass, final String protocol) {
        return buildContextWithTrustStore(trustStorePath, trustStorePass, protocol).getSocketFactory();
    }

    public SSLSocketFactory buildSocketFactoryWithKeyStore(final String keyStorePath, final String keyStorePass, final String protocol) {
        return buildContextWithKeyStore(keyStorePath, keyStorePass, protocol).getSocketFactory();
    }

    public SSLSocketFactory buildSslSocketFactory(final String keyStorePath,
                                                  final String keyStorePass,
                                                  final String trustStorePath,
                                                  final String trustStorePass,
                                                  final String protocol) {
        return buildContext(keyStorePath, keyStorePass, trustStorePath, trustStorePass, protocol).getSocketFactory();
    }

    public SSLContext buildContextWithTrustStore(final String trustStorePath, final String trustStorePass) {
        return buildContextWithTrustStore(trustStorePath, trustStorePass, DEFAULT_PROTOCOL_TYPE);
    }

    public SSLContext buildContextWithKeyStore(final String keyStorePath, final String keyStorePass) {
        return buildContextWithKeyStore(keyStorePath, keyStorePass, DEFAULT_PROTOCOL_TYPE);
    }

    public SSLContext buildContext(final String keyStorePath,
                                   final String keyStorePass,
                                   final String trustStorePath,
                                   final String trustStorePass) {
        return buildContext(keyStorePath, keyStorePass, trustStorePath, trustStorePass, DEFAULT_PROTOCOL_TYPE);
    }


    public SSLContext buildContextWithKeyStore(final String keyStorePath, final String keyStorePass, final String protocol) {
        return buildContextWithKeyStore(DEFAULT_ALGORITHM, keyStorePath, keyStorePass, DEFAULT_STORE_TYPE, protocol);
    }

    public SSLContext buildContextWithTrustStore(final String trustStorePath, final String trustStorePass, final String protocol) {
        return buildContextWithTrustStore(DEFAULT_ALGORITHM, trustStorePath, trustStorePass, DEFAULT_STORE_TYPE, protocol);
    }

    public SSLContext buildContext(final String keyStorePath,
                                   final String keyStorePass,
                                   final String trustStorePath,
                                   final String trustStorePass,
                                   final String protocol) {
        return buildContext(DEFAULT_ALGORITHM,
                protocol,
                keyStorePath,
                keyStorePass,
                DEFAULT_STORE_TYPE,
                trustStorePath,
                trustStorePass,
                DEFAULT_STORE_TYPE
        );
    }

    @SneakyThrows
    public SSLContext buildContextWithTrustStore(final String algorithm,
                                                 final String trustStorePath,
                                                 final String trustStorePass,
                                                 final String trustStoreType,
                                                 final String protocol) {
        final var sslContext = SSLContext.getInstance(protocol);
        final var trustManagerFactory = buildTrustManagerFactory(algorithm, trustStorePath, trustStorePass, trustStoreType);
        sslContext.init(new KeyManager[]{}, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    @SneakyThrows
    public SSLContext buildContextWithKeyStore(final String algorithm,
                                               final String keyStorePath,
                                               final String keyStorePass,
                                               final String keyStoreType,
                                               final String protocol) {
        final var sslContext = SSLContext.getInstance(protocol);
        final var keyManagerFactory = buildKeyManagerFactory(algorithm, keyStorePath, keyStorePass, keyStoreType);
        sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{}, new SecureRandom());
        return sslContext;
    }

    @SneakyThrows
    public SSLContext buildContext(final String algorithm,
                                   final String protocol,
                                   final String keyStorePath,
                                   final String keyStorePass,
                                   final String keyStoreType,
                                   final String trustStorePath,
                                   final String trustStorePass,
                                   final String trustStoreType) {
        final var sslContext = SSLContext.getInstance(protocol);
        final var keyManagerFactory = buildKeyManagerFactory(algorithm, keyStorePath, keyStorePass, keyStoreType);
        final var trustManagerFactory = buildTrustManagerFactory(algorithm, trustStorePath, trustStorePass, trustStoreType);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }


    @SneakyThrows
    public KeyStore buildKeyStore(final String keyStoreType, final String keyStorePath, final String keyStorePassword) {
        InputStream keystoreStream = null;
        try {
            final var keyStore = KeyStore.getInstance(keyStoreType);
            keystoreStream = Files.newInputStream(Paths.get(keyStorePath));
            keyStore.load(keystoreStream, keyStorePassword.toCharArray());
            return keyStore;
        } finally {
            if (Objects.nonNull(keystoreStream)) {
                keystoreStream.close();
            }
        }
    }

    public TrustManagerFactory buildTrustManagerFactory(final String defaultAlgorithm, final String trustStorePath, final String trustStorePass) {
        return buildTrustManagerFactory(defaultAlgorithm, trustStorePath, trustStorePass, DEFAULT_STORE_TYPE);
    }

    @SneakyThrows
    public TrustManagerFactory buildTrustManagerFactory(final String defaultAlgorithm, final String trustStorePath, final String trustStorePass, final String trustStoreType) {
        final var trustStore = buildKeyStore(trustStoreType, trustStorePath, trustStorePass);
        final var trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm);
        trustManagerFactory.init(trustStore);
        return trustManagerFactory;
    }

    public KeyManagerFactory buildKeyManagerFactory(final String defaultAlgorithm, final String keyStorePath, final String keyStorePass) {
        return buildKeyManagerFactory(defaultAlgorithm, keyStorePath, keyStorePass, DEFAULT_STORE_TYPE);
    }


    @SneakyThrows
    public KeyManager[] buildKeyManagerFactory(final String defaultAlgorithm, final String keyStorePath, final String keyStorePass, final String keyStoreType, final String alias) {
        var keyManagerFactory = buildKeyManagerFactory(defaultAlgorithm, keyStorePath, keyStorePass, keyStoreType);
        final var keyManager = (X509KeyManager) keyManagerFactory.getKeyManagers()[0];
        final var km = delegateManager(alias, keyManager);
        return new KeyManager[]{km};
    }

    @SneakyThrows
    public KeyManagerFactory buildKeyManagerFactory(final String defaultAlgorithm, final String keyStorePath, final String keyStorePass, final String keyStoreType) {
        final var keyManagerFactory = KeyManagerFactory.getInstance(defaultAlgorithm);
        final var keyStore = buildKeyStore(keyStoreType, keyStorePath, keyStorePass);
        keyManagerFactory.init(keyStore, keyStorePass.toCharArray());
        return keyManagerFactory;
    }

    private static X509ExtendedKeyManager delegateManager(final String alias, final X509KeyManager keyManager) {
        return new X509ExtendedKeyManager() {
            @Override
            public String[] getClientAliases(String keyType, Principal[] issuers) {
                return keyManager.getClientAliases(keyType, issuers);
            }

            @Override
            public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
                return alias;
            }

            @Override
            public String[] getServerAliases(String keyType, Principal[] issuers) {
                return keyManager.getServerAliases(keyType, issuers);
            }

            @Override
            public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
                return keyManager.chooseServerAlias(keyType, issuers, socket);
            }

            @Override
            public X509Certificate[] getCertificateChain(String alias) {
                return keyManager.getCertificateChain(alias);
            }

            @Override
            public PrivateKey getPrivateKey(String alias) {
                return keyManager.getPrivateKey(alias);
            }
        };
    }

    public static TrustManager[] insecureTrustManagers() {
        return InsecureTrustManagerFactory.INSTANCE.getTrustManagers();
    }
}



BaseTransporter
---------------

BaseTransporter is the base interface for the Transporter library.
It offers basic configuration options such as setting the client configuration,
enabling the client to follow redirection on 3xx status codes,
enabling the client resolver for DNS and hostname,
setting the timeout for the client, configuring SSL,
enabling two-way (mutual) TLS, adding proxy configurations,
adding a logger, trusting all certificates,
and building preset configurations to org.springframework.web.reactive.function.client.WebClient.



The BaseTransporter interface provides methods for setting up basic configuration for the Transporter.

Client Configuration
^^^^^^^^^^^^^^^^^^^^

The ``withClientConfig`` method sets the client configuration.

.. code-block:: java

    BaseTransporter withClientConfig(final TransporterConfiguration clientConfig);

Redirection
^^^^^^^^^^^

The ``withRedirection`` method configures the client to follow redirection on 3xx status codes.

.. code-block:: java

    BaseTransporter withRedirection();

Resolver
^^^^^^^^

The ``withResolver`` method enables the client resolver for DNS and hostname.

.. code-block:: java

    BaseTransporter withResolver(final TransporterClientResolver resolver);

Timeout
^^^^^^^

The ``withTimeOut`` method sets the timeout for the client.

.. code-block:: java

    BaseTransporter withTimeOut(long timeOut);
    BaseTransporter withTimeOut(long timeOut, @NotNull TimeUnit timeUnit);

Secure
^^^^^^

The ``secure`` method sets the SSL configuration for the client using the specified trust store.

.. code-block:: java

    BaseTransporter secure(@NotNull final String trustStorePath, @NotNull final String trustStorePassword);

MTLS
^^^^

The ``withMtls`` method sets the MTLS configuration for the client using the specified key store.

.. code-block:: java

    BaseTransporter withMtls(@NotNull final String keyStorePath, @NotNull final String keyStorePassword);

Proxy
^^^^^

The ``withProxy`` method sets the proxy configuration for the client using the specified proxy address, port, and schema.

.. code-block:: java

    BaseTransporter withProxy(@NotNull final String proxyAddress, final int proxyPort, @NotNull final String proxySchema);

Debugging
^^^^^^^^^

The ``withDebugging`` method enables or disables debugging for the client.

.. code-block:: java

    BaseTransporter withDebugging(@NotNull final Boolean enabled);

Logger
^^^^^^

The ``withLogger`` method adds a logger to the client.

.. code-block:: java

    BaseTransporter withLogger();

Trust All
^^^^^^^^^

The ``trustAll`` method configures the client to trust all SSL certificates.

.. code-block:: java

    BaseTransporter trustAll();

REST Builder
^^^^^^^^^^^^

The ``toRestBuilder`` method builds preset configurations for a REST client and returns a ``TransporterBuilder`` object for further configuration.

.. code-block:: java

    TransporterBuilder toRestBuilder();

CXF Builder
^^^^^^^^^^^

The ``toCxfBuilder`` method builds preset configurations for a CXF client and returns a ``TransporterCxfBuilder<T>`` object for further configuration.

.. code-block:: java

    <T> TransporterCxfBuilder<T> toCxfBuilder(final Class<T> type);



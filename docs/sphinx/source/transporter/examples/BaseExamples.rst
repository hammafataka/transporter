==========================================
Examples of how to Use BaseTransporter
==========================================


There are various ways to instantiate transporter, one of them is through Injecting BaseTransporter and
configure each configurations provided in BaseTransporter, then calling ``toRestBuilder`` or ``toCxfBuilder``.

.. code-block:: java
   :caption: Insecure Transporter for Rest

      @Autowired
      private final BaseTransporter baseTransporter;

      private TransporterBuilder createInsecureRestBuilder() {
          return baseTransporter.withLogger()
                  .trustAll()
                  .withDebugging(true)
                  .withRedirection()
                  .withTimeOut(10, TimeUnit.SECONDS)
                  .toRestBuilder();
      }

.. code-block:: java
   :caption: Secured Transporter for Rest


      @Autowired
      private final BaseTransporter baseTransporter;
      private final String trustStorePath;
      private final String trustStorePassword;

      private TransporterBuilder createSecuredRestBuilder() {
          return baseTransporter.withLogger()
                  .secure(trustStorePath, trustStorePassword)
                  .withDebugging(true)
                  .withRedirection()
                  .withTimeOut(10, TimeUnit.SECONDS)
                  .toRestBuilder();
      }


.. code-block:: java
   :caption: Secured mtls Transporter for Rest

      @Autowired
      private final BaseTransporter baseTransporter;
      private final String keyStorePath;
      private final String keyStorePassword;
      private final String trustStorePath;
      private final String trustStorePassword;

      private TransporterBuilder createSecuredRestBuilder() {
          return baseTransporter.withLogger()
                  .secure(trustStorePath, trustStorePassword)
                  .withMtls(keyStorePath, keyStorePassword)
                  .withDebugging(true)
                  .withRedirection()
                  .withTimeOut(10, TimeUnit.SECONDS)
                  .toRestBuilder();
      }

.. attention::
      Currently mtls is not supported for CXF builder, including other features like timeout.

.. code-block:: java
   :caption: Secured Transporter for CXF

      @Autowired
      private final BaseTransporter baseTransporter;
      private final String trustStorePath;
      private final String trustStorePassword;

      private TransporterCxfBuilder<String> createSecuredRestBuilder() {
          return baseTransporter.withLogger()
                  .secure(trustStorePath, trustStorePassword)
                  .withDebugging(true)
                  .withRedirection()
                  .toCxfBuilder(String.class);
      }



.. code-block:: java
   :caption: Insecure Transporter for CXF


      @Autowired
      private final BaseTransporter baseTransporter;

      private TransporterCxfBuilder<String> createSecuredRestBuilder() {
          return baseTransporter.withLogger()
                  .withDebugging(true)
                  .toCxfBuilder(String.class);
      }


.. hint::
      All provided methods in BaseTransporter can be automatically applied by single method ``withClientConfig(final TransporterConfiguration clientConfig)``, below is example of it.


.. code-block:: java
   :caption: Automatically apply all configurations via ``withClientConfig(final TransporterConfiguration clientConfig)``

      @Autowired
      private final BaseTransporter baseTransporter;

      private TransporterBuilder createTransporterBuilder(final TransporterConfiguration clientConfig) {
          return baseTransporter
                  .withClientConfig(clientConfig)
                  .toRestBuilder();
      }




**Example: Setting a Timeout**

To set a timeout for a request with the Transporter library, you can use the ``withTimeOut`` method:

.. code-block:: java

    BaseTransporter baseTransporter = new BaseTransporterImpl()
        .withClientConfig(new TransporterConfiguration())
        .withRedirection()
        .withTimeOut(10, TimeUnit.SECONDS);

**Example: Using a Proxy**

To use a proxy with the Transporter library, you can set the proxy address, port, and schema using the ``withProxy`` method:

.. code-block:: java

    BaseTransporter baseTransporter = new BaseTransporterImpl()
        .withClientConfig(new TransporterConfiguration())
        .withRedirection()
        .withProxy("proxyAddress", 8080, "http");

**Example: Enabling Debugging**

To enable debugging for the Transporter library, you can use the ``withDebugging`` method:

.. code-block:: java

    BaseTransporter baseTransporter = new BaseTransporterImpl()
        .withClientConfig(new TransporterConfiguration())
        .withRedirection()
        .withDebugging(true);

**Example: Using a Logger**

To use a logger with the Transporter library, you can use the ``withLogger`` method:

.. code-block:: java

    BaseTransporter baseTransporter = new BaseTransporterImpl()
        .withClientConfig(new TransporterConfiguration())
        .withRedirection()
        .withLogger();




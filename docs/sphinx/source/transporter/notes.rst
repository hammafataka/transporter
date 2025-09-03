========================================
Notes to be aware for using transporter
========================================

Transporter has some hidden features that you need days to dig unless you have been to this page.
followings are notes to be taken while using transporter.

.. note::
   Transporter has ``TransporterConfiguration.java`` which is a class for configuring transporter automatically.

.. code-block:: yaml
   :caption: available Transporter properties

      transporter:
        resolver: NOOP_RESOLVER
        secured: true
        mtlsEnabled: false
        trustStorePath: path/to/jks
        trustStorePassword: jksPassword
        keyStorePath: path/to/jks
        keyStorePassword: jksPassword
        checkRequiredFields: false
        requireNonNullEnabled: false
        authHeaderLoggingEnabled: true
        debugEnabled: false

.. note::
   Transporter has ``ConfigurationResolver.java`` for safe usage of configuration and nullability in some methods while using transporter,
   this resolver is using ``TransporterConfiguration.java`` as primary source to override configuration when null is passed.

.. code-block:: java
   :caption: Automatically resolve nullable from properties, assuming ``transporter.resolver: DEFAULT_RESOLVER`` is present in configs

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


.. note::
   Now in provided example there is no call for ``withResolver()``, so in background when calling ``toRestBuilder()``
   then resolver will be assigned as DEFAULT_RESOLVER by  ConfigurationResolver.

   The same can be applied to any other properties defined in ``TransporterConfiguration.java``




.. note::
   Transporter can be reloaded when using it properly, this requires good knowledge on spring, to implement that logic,
   there are two ways, first is when fully reloading SpringContext is achieved, then transporter will be reloaded automatically when imported via injecting BaseTransporter to obtain.
   the other way to do that is by not injecting BaseTransporter and instantiating like following and just current environment is reloaded.


.. code-block:: java
   :caption: Reload configs automatically on Environment reloaded

    private Environment environment
    public TransporterBuilder createTransporterBuilder() {
        return new BaseTransporterImp(new ConfigurationResolverImp(environment))
                .toRestBuilder();
    }


.. caution::
   When using ``ResponseReceiver.java`` method ``.data()`` is blocking method, meaning does not guarantee full reactive non-blocking approach,
   other methods guarantee full reactive non-blocking approach


.. note::
   Transporter has Resolvers for Dns/hostnames, check here for more info about Resolvers




.. note::
   Transporter by default has Error handlers which handles some basic errors that can be predicted for more infor check ``ErrorHandlers`` class,
   following statuses is handled.

   * 400
   * 401
   * 403
   * 404
   * 405
   * 406
   * 409
   * 415
   * 422
   * 429
   * 500
   * 501
   * 503
   * 504



.. note::
   Transporter provided features like checkFields or nonNullFields from received response,
   for checkFields response models should be constructed under ``com.fasterxml.jackson`` library.
   however nonNullFields does not need any custom implementation, it will basically just check for all fields and fail if any is null

   example of this can be shown as below:

.. code-block:: java
   :caption:  checkFields DemoResponse

      import com.fasterxml.jackson.annotation.JsonCreator;
      import com.fasterxml.jackson.annotation.JsonProperty;

      import lombok.Builder;
      import lombok.Data;
      import lombok.NoArgsConstructor;

      /**
       * @author HAMMA FATAKA
       */
      @Data
      @NoArgsConstructor
      @Builder
      public class DemoResponse {
          private String result;
          private String resultMessage;

          @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
          public DemoResponse(@JsonProperty(value = "result", required = true) String result,@JsonProperty(value = "resultMessage") String resultMessage) {
              this.result = result;
              this.resultMessage = resultMessage;
          }
      }

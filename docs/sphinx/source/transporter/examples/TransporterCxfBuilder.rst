============================================
Examples of how to Use TransporterCxfBuilder
============================================

For example, to create an instance of `TransporterCxfBuilder`, you can use the `toCxfBuilder` method from the `BaseTransporter` interface:

.. code-block:: java

    TransporterCxfBuilder<MyService> builder = baseTransporter.toCxfBuilder(MyService.class);

Once you have an instance of `TransporterCxfBuilder`, you can use its methods to configure the CXF client. For example:

.. code-block:: java

    MyService service = builder.url("http://example.com/service")
                          .username("my-username")
                          .password("my-password")
                          .build();


You can also add interceptors to the CXF client using the `interceptorIn` and `interceptorOut` methods:

.. code-block:: java

    MyService service = builder.url("http://example.com/service")
                          .username("my-username")
                          .password("my-password")
                          .interceptorIn(new MyInInterceptor())
                          .interceptorOut(new MyOutInterceptor())
                          .build();

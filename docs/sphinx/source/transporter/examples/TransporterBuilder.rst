==========================================
Examples of how to Use TransporterBuilder
==========================================


The Transporter library provides a simple and easy-to-use API for executing HTTP calls in REST or CXF.

To use the library, first create a ``BaseTransporter`` object and configure it with the desired settings:

.. code-block:: java

    BaseTransporter baseTransporter = new BaseTransporterImpl()
        .withClientConfig(new TransporterConfiguration())
        .withRedirection()
        .withTimeOut(10, TimeUnit.SECONDS);

Next, build a ``TransporterBuilder`` or ``TransporterCxfBuilder`` object from the ``BaseTransporter`` object:

.. code-block:: java

    TransporterBuilder transporterBuilder = baseTransporter.toRestBuilder();

Finally, use the ``TransporterBuilder`` or ``TransporterCxfBuilder`` object to build and configure a ``Transporter`` object:

.. code-block:: java

    Transporter transporter = transporterBuilder
        .withBaseUrl("https://example.com")
        .build();

Once you have a ``Transporter`` object, you can use it to execute HTTP requests:

.. code-block:: java

    TransporterData<String> response = transporter
        .get(null)
        .sendAndReceive("/endpoint")
        .data();

The Transporter library provides many options for configuring and customizing HTTP requests. You can set authentication credentials, headers, URL parameters, and more. You can also use different HTTP methods such as GET, POST, PUT, and DELETE.

Here are some more examples of using the TransporterBuilder :

**Example 1: Using Basic Authentication**

To use basic authentication with the Transporter library, you can set the username and password using the ``basicAuth`` method:

.. code-block:: java

    Transporter transporter = transporterBuilder
        .withBaseUrl("https://example.com")
        .build()
        .basicAuth("username", "password");

**Example 2: Using Bearer Authentication**

To use bearer authentication with the Transporter library, you can set the bearer token using the ``bearerAuth`` method:

.. code-block:: java

    Transporter transporter = transporterBuilder
        .withBaseUrl("https://example.com")
        .build()
        .bearerAuth("token");

**Example 3: Setting Headers**

To set headers for a request with the Transporter library, you can use the ``withHeader`` method:

.. code-block:: java

    Transporter transporter = transporterBuilder
        .withBaseUrl("https://example.com")
        .build()
        .withHeader("Header-Name", "Header-Value");

**Example 4: Setting URL Parameters**

To set URL parameters for a request with the Transporter library, you can use the ``withUrlParam`` method:

.. code-block:: java

    Transporter transporter = transporterBuilder
        .withBaseUrl("https://example.com")
        .build()
        .withUrlParam("paramName", "paramValue");

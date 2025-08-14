TransporterCxfBuilder
-----------------------

The TransporterCxfBuilder interface provides methods for building and configuring a CXF client.

URL
^^^

The ``url`` method sets the URL for the CXF client.

.. code-block:: java

    TransporterCxfBuilder<T> url(@NotNull final String url);

Username()
^^^^^^^^^^^

The ``username`` method sets the username for the CXF client.

.. code-block:: java

    TransporterCxfBuilder<T> username(@NotNull final String username);

Password()
^^^^^^^^^^^

The ``password`` method sets the password for the CXF client.

.. code-block:: java

    TransporterCxfBuilder<T> password(@NotNull final String password);

Interceptors
^^^^^^^^^^^^

The ``interceptorIn`` and ``interceptorOut`` methods add an interceptor to the CXF client for incoming and outgoing messages, respectively.

.. code-block:: java

    <I extends Message> TransporterCxfBuilder<T> interceptorIn(@NotNull final Interceptor<I> interceptor);
    <I extends Message> TransporterCxfBuilder<T> interceptorOut(@NotNull final Interceptor<I> interceptor);

Build
^^^^^

The ``build`` method builds and returns a CXF client of the specified type.

.. code-block:: java

    T build();


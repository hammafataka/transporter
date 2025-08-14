

TransporterBuilder
-------------------

TransporterBuilder is an interface that provides a set of methods for configuring and building a Transporter.

Base URL
^^^^^^^^

The ``withBaseUrl`` method sets the base URL for the Transporter.

.. code-block:: java

    TransporterBuilder withBaseUrl(@NotNull final String baseUrl);

Resolver
^^^^^^^^

The ``resolver`` method sets the resolver for the Transporter.

.. code-block:: java

    TransporterBuilder resolver(@NotNull final TransporterClientResolver transporterClientResolver);

Build
^^^^^

The ``build`` method builds and returns a Transporter object.

.. code-block:: java

    Transporter build();

HTTP Methods
^^^^^^^^^^^^

The ``post``, ``get``, ``put``, and ``delete`` methods create and return a Transporter object configured to use the specified HTTP method.

.. code-block:: java

    Transporter post(@Nullable final Object nullableBody);
    Transporter get(@Nullable final Object nullableBody);
    Transporter put(@Nullable final Object nullableBody);
    Transporter delete(@Nullable final Object nullableBody);



Transporter
-----------

The Transporter interface provides methods for setting up and executing HTTP requests.

Basic Authentication
^^^^^^^^^^^^^^^^^^^^

The ``basicAuth`` method sets encoded basic authentication for the client.

.. code-block:: java

    Transporter basicAuth(@NotNull final String username, @NotNull final String password);

Bearer Authentication
^^^^^^^^^^^^^^^^^^^^^

The ``bearerAuth`` method sets encoded bearer authentication for the client.

.. code-block:: java

    Transporter bearerAuth(@NotNull final String token);

HTTP Method
^^^^^^^^^^^

The ``method`` method sets the HTTP method to use for the request.

.. code-block:: java

    Transporter method(@NotNull final TransporterMethod method);

Body Value
^^^^^^^^^^

The ``bodyValue`` method sets the body value for the request.

.. code-block:: java

    Transporter bodyValue(@NotNull final Object body);

Headers
^^^^^^^

The ``withHeader`` method adds a header to the requested client.

.. code-block:: java

    Transporter withHeader(@NotNull final String headerName, @NotNull final String headerValue);

URL Parameters
^^^^^^^^^^^^^^

The ``withUrlParam`` method adds a URL parameter to the request.

.. code-block:: java

    Transporter withUrlParam(@NotNull final String paramName, @NotNull final String paramValue);

Content Length
^^^^^^^^^^^^^^

The ``withContentLength`` method sets the content length for the request.

.. code-block:: java

    Transporter withContentLength();

Send Request
^^^^^^^^^^^^

The ``send`` method sends a request to the specified URL using the specified HTTP method.

.. code-block:: java

    WebClient.RequestBodySpec send(@NotNull final String url);
    WebClient.RequestBodySpec send(@NotNull final String url, @NotNull final TransporterMethod method);

Send and Receive Request
^^^^^^^^^^^^^^^^^^^^^^^^^^

The ``sendAndReceive`` method sends a request to the specified URL using the specified HTTP method and returns a ``TransporterReceiver`` object that can be used to receive the response.

.. code-block:: java

    TransporterReceiver sendAndReceive(@NotNull final String url);
    TransporterReceiver sendAndReceive(@NotNull final String url, @NotNull final TransporterMethod method);

TransporterReceiver
-------------------

The ``TransporterReceiver`` interface provides methods for receiving and processing the response from a sent request.

Data
^^^^

The ``data`` method returns a ``Mono<TransporterData<T>>`` object containing the response data of the specified type.

.. code-block:: java

    <T> Mono<TransporterData<T>> data(@NotNull final Class<T> responseType);
    TransporterData<String> data();

Entity Data
^^^^^^^^^^^

The ``entityData`` method returns a ``Mono<ResponseEntity<T>>`` object containing the response entity data of the specified type.

.. code-block:: java

    <T> Mono<ResponseEntity<T>> entityData(@NotNull final Class<T> responseType);

Mono Data
^^^^^^^^^

The ``monoData`` method returns a ``Mono<T>`` object containing the response data of the specified type.

.. code-block:: java

    <T> Mono<T> monoData(@NotNull final Class<T> responseType);

Require Non-Null
^^^^^^^^^^^^^^^^

The ``requireNonNull`` method enables or disables checking for non-null values in the response.

.. code-block:: java

    TransporterReceiver requireNonNull(@Nullable final Boolean enabled);

Check Required Fields
^^^^^^^^^^^^^^^^^^^^^

The ``checkRequiredFields`` method enables or disables checking for required fields in the response.

.. code-block:: java

    TransporterReceiver checkRequiredFields(@Nullable final Boolean enabled);

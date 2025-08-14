==========================================
Examples of how to Use ResponseReciever
==========================================


The `TransporterReceiver` interface provides methods for receiving and processing the response from a sent request. Here are some examples of using the `TransporterReceiver` interface:

**Example: Receiving Data**

To receive data from a sent request with the Transporter library, you can use the `data` method:

.. code-block:: java

    TransporterData<String> response = transporter
        .post(body)
        .sendAndReceive("/endpoint")
        .data();

**Example: Receiving Entity Data**

To receive entity data from a sent request with the Transporter library, you can use the `entityData` method:

.. code-block:: java

    Mono<ResponseEntity<String>> response = transporter
        .post(body)
        .sendAndReceive("/endpoint")
        .entityData(String.class);

**Example: Receiving Mono Data**

To receive Mono data from a sent request with the Transporter library, you can use the `monoData` method:

.. code-block:: java

    Mono<String> response = transporter
        .get(null)
        .sendAndReceive("/endpoint")
        .monoData(String.class);

**Example: Checking for Non-Null Values**

To check for non-null values in the response from a sent request with the Transporter library, you can use the `requireNonNull` method:

.. code-block:: java

    TransporterData<String> response = transporter
        .get(null)
        .sendAndReceive("/endpoint")
        .requireNonNull(true)
        .data();


**Example: Checking for required fields Values**

To check for required fields in the response from a sent request with the Transporter library, you can use the `checkRequiredFields` method:

.. code-block:: java

    TransporterData<String> response = transporter
        .get(null)
        .sendAndReceive("/endpoint")
        .checkRequiredFields(true)
        .data();


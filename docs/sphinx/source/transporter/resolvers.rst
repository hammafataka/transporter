======================================
Transporter Client Resolver
======================================

Transporter provides automatic DNS/hostname resolvers by default for HTTP REST calls.


Supported types
------------------

* NOOP_RESOLVER
* DEFAULT_RESOLVER
* CUSTOM



NOOP_RESOLVER
^^^^^^^^^^^^^^
This resolver will do no-operation for resolving hostname


DEFAULT_RESOLVER
^^^^^^^^^^^^^^^^^
This resolver will apply DefaultAddressResolverGroup provided by Netty library, where it checks for hostnames internally and validate it.


CUSTOM
^^^^^^^

This resolver is custom resolver made by Transporter on top of netty library,
internally it builds DnsAddressResolverGroup with NioDatagramChannel type for channels

.. note::
    If no resolver is supplied to transporter by default resolver will be NOOP_RESOLVER, this can be also on cases where supplied value is not
    in ``TransporterClientResolver`` enum class
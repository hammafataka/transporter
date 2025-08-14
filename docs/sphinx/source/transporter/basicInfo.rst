Transporter Library
===================

Basic info
-------------

Transporter is a Java library built on top of Spring Boot and using a fully reactive approach.
It facilitates HTTP calls in REST or CXF format and offers four main interfaces:
BaseTransporter, TransporterBuilder, TransporterCxfBuilder, and Transporter.
Main purpose of this library is for reducing overly duplicated codes for configuring clients of REST or CXF.
It simply provides simplicity with performance and solving complex configurations.



.. toctree::
    :maxdepth: 1
    :caption: Basic infor

    BaseTransporter <interfaces/BaseTransporter>
    TransporterBuilder <interfaces/TransporterBuilder>
    TransporterCxfBuilder <interfaces/TransporterCxfBuilder>
    Transporter <interfaces/Transporter>

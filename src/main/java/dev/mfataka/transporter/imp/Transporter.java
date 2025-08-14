package dev.mfataka.transporter.imp;

import java.net.URI;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import dev.mfataka.transporter.enums.TransporterMethod;

/**
 * @author HAMMA FATAKA<br>
 * Project: base<br>
 * Date: 3/19/2022 2:29 AM
 */
public interface Transporter {

    /**
     * will set encoded basic auth for client
     *
     * @param username username for basic auth
     * @param password password for basic auth
     * @return {@link Transporter} for more configs
     */
    Transporter basicAuth(@NotNull final String username, @NotNull final String password);


    /**
     * will set encoded basic auth for client
     *
     * @param token bearer token
     * @return {@link Transporter} for more configs
     */
    Transporter bearerAuth(@NotNull final String token);

    /**
     * sets http method.
     *
     * @param method The method to use for the request.
     * @return A Transporter object.
     */
    Transporter method(@NotNull final TransporterMethod method);

    Transporter bodyValue(@NotNull final Object body);

    Transporter contentType(@NotNull final MediaType mediaType);

    /**
     * adds header to requested client
     *
     * @param headerName  name of the header
     * @param headerValue value of header
     * @return {@link  Transporter} for more config set
     */
    Transporter withHeader(@NotNull final String headerName, @NotNull final String headerValue);

    /**
     * Adds a URL parameter to the request
     *
     * @param paramName  The name of the parameter to be added to the URL.
     * @param paramValue The value of the parameter.
     * @return {@link  Transporter} for more config set
     */
    Transporter withUrlParam(@NotNull final String paramName, @NotNull final String paramValue);

    /**
     * Adds a URL parameter to the request using a function to build the URI
     *
     * @param uriFunction The function to build the URI
     * @return {@link  Transporter} for more config set
     */

    WebClient.RequestBodySpec send(@NotNull final Function<UriBuilder, URI> uriFunction);

    /**
     * adds content length automatically based on body
     *
     * @return {@link WebClient.ResponseSpec} if it was successful
     */
    Transporter withContentLength();

    /**
     * method to execute configured request
     *
     * @param url path for request destination
     * @return {@link WebClient.ResponseSpec} if it was successful
     */
    WebClient.RequestBodySpec send(@NotNull final String url);

    /**
     * method to execute configured request
     *
     * @param url    path for request destination
     * @param method request method to be sent upon
     * @return {@link WebClient.ResponseSpec} if it was successful
     */
    WebClient.RequestBodySpec send(@NotNull final String url, @NotNull final TransporterMethod method);

    /**
     * method to execute configured request and receive response using a function to build the URI
     *
     * @param uriFunction The function to build the URI
     * @return {@link TransporterReceiver}
     */
    TransporterReceiver sendAndReceive(@NotNull final Function<UriBuilder, URI> uriFunction);

    /**
     * method to execute configured request and receive response
     *
     * @param url path for request destination
     * @return {@link TransporterReceiver}
     */
    TransporterReceiver sendAndReceive(@NotNull final String url);

    /**
     * method to execute configured request and receive response
     *
     * @param url    path for request destination
     * @param method request method to be sent upon
     * @return {@link TransporterReceiver}
     */
    TransporterReceiver sendAndReceive(@NotNull final String url, @NotNull final TransporterMethod method);

}

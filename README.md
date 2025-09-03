# transporter

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.mfataka/transporter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.mfataka/transporter)
[![javadoc](https://javadoc.io/badge2/dev.mfataka/transporter/javadoc.svg)](https://javadoc.io/doc/dev.mfataka/transporter)

A tiny builder-style wrapper around **Spring WebFlux `WebClient`** that makes HTTP calls ergonomic and configurable.

- Fluent **builder** → `BaseTransporter` → `TransporterBuilder` → `Transporter`
- Clean **send & receive** pipeline → `TransporterReceiver`
- Works with **JSON**, **forms**, **binary**, and **SOAP (JAXB)** payloads
- First-class support for **timeouts, redirects, proxies, SSL/mTLS, trust-all**
- Optional **Logbook** HTTP logging & debug traces
- **DNS resolvers** (default/custom/noop)
- **Reactive** (Mono/Flux) & **blocking** receive helpers
- Converts responses to **DTOs**, **ResponseEntity**, **String**, **DataBuffer**, or **TransporterData<T>**

> Java 17+, Spring WebFlux

---

## Coordinates

```groovy
  implementation("dev.mfataka:transporter:${TRANSPORTER_VERSION}")
```

---

## Core types (what you’ll use)

- `BaseTransporter` — high-level config (timeouts, SSL, proxy, logging, resolver, etc.); hands off to…
- `TransporterBuilder` — sets base URL, **REST vs SOAP** mode, data limits, and builds a `Transporter` with an optional
  **preset HTTP method** (`post/get/put/delete`) or `build()` for ad-hoc methods.
- `Transporter` — composes request details (**method**, **headers**, **params**, **contentType**, **auth**, **body**)
  and **sends**:
    - `send(String url)` / `send(Function<UriBuilder,URI>)` → returns `WebClient.RequestBodySpec`
    - `sendAndReceive(...)` → returns a `TransporterReceiver`
- `TransporterReceiver` — converts the response into:
    - `monoData(Class<T>)`, `fluxData(Class<T>)`, `entityData(Class<T>)`
    - `transporterData(Class<T>)` (Mono of wrapper), `transporterDataBlock(Class<T>)` (blocking)
    - `transporterData()` (blocking string), `mapToBuffer()` (Flux<DataBuffer>)
    - `.requireNonNull(true)` & `.checkRequiredFields(true)` to enforce field checks

> Implementation classes you’ll see in logs: `DefaultBaseTransporter`, `DefaultTransporterBuilder`,
`DefaultTransporter`, `DefaultTransporterReceiver`.

---

## Quick start (REST JSON)

```java

@Component
@RequiredArgsConstructor
class UserClient {

    private final DefaultBaseTransporter base; // Spring will inject (it’s @Component)

    record CreateUser(String name, String email) {
    }

    record User(String id, String name, String email) {
    }

    Mono<User> create(String name, String email) {
        var transporter = base
                .withDebugging(true)
                .withLogger()                // enable Logbook logging
                .withRedirection()           // follow 3xx
                .withTimeOut(30, TimeUnit.SECONDS)
                .toBuilder()
                .withBaseUrl("https://api.example.com")
                .restService()               // JSON/REST mode (default codecs)
                .post(new CreateUser(name, email));  // preset method+body

        return transporter
                .bearerAuth("your.jwt.or.token")     // or .basicAuth(user, pass)
                .contentType(MediaType.APPLICATION_JSON)
                .withHeader("X-Request-Id", UUID.randomUUID().toString())
                .withUrlParam("source", "portal")
                .sendAndReceive("/users")
                .requireNonNull(true)                // drop null-required fields early
                .checkRequiredFields(true)           // enforce Jackson “required” docs (see DemoResponse usage)
                .monoData(User.class);
    }
}
```

**Blocking variant** (when you absolutely need it):

```java
var data = transporter
        .sendAndReceive("/users/123")
        .transporterDataBlock(User.class);   // returns TransporterData<User>

if(data.

ok()){
        System.out.

println(data.get().

name());
        }else{
        System.err.

println("Failed: "+data.message());
        }
```

---

## Ad-hoc requests

```java
var tx = base.toBuilder()
        .withBaseUrl("https://httpbin.org")
        .restService()
        .build();                        // no preset method

var receiver = tx
        .method(TransporterMethod.GET)
        .withUrlParam("q", "transporter")
        .withHeader("Accept", "application/json")
        .sendAndReceive("/get");

Mono<Map> map = receiver.monoData(Map.class);
```

---

## Sending forms, computing Content-Length

```java
var formTx = base.toBuilder()
        .withBaseUrl("https://api.example.com")
        .restService()
        .post(Map.of("name", "Nika", "email", "nika@example.com"));

formTx.

contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .

withContentLength()                 // computes length from serialized body
      .

sendAndReceive("/signup")
      .

transporterDataBlock(String .class); // blocking as String
```

---

## SOAP mode (JAXB codecs)

```java
var soapTx = base.toBuilder()
        .withBaseUrl("https://soap.example.com")
        .soapService()      // registers JaxbEncoder/JaxbDecoder
        .post(new MySoapEnvelope(...));

var response = soapTx.contentType(MediaType.TEXT_XML)
        .sendAndReceive("/service")
        .monoData(MySoapResponse.class);
```

> In SOAP mode the builder auto-registers `JaxbEncoder` & `JaxbDecoder`.

---

## Auth helpers

```java
tx.basicAuth("user","pass").

sendAndReceive("/secure").

monoData(Void .class);
tx.

bearerAuth("token").

sendAndReceive("/me").

monoData(Profile .class);
```

---

## Timeouts, redirects, proxies

```java
base.withTimeOut(20)              // seconds
    .

withRedirection()
    .

withProxy("corp.proxy",8080,"http")
    .

toBuilder()
    .

withBaseUrl("https://api.example.com")
    .

get(null)
    .

sendAndReceive("/health")
    .

monoData(String .class);
```

---

## SSL, Trust-all, and **mTLS**

```java
// Trust all (dev only!)
base.trustAll().

toBuilder() ...;

// One-way TLS (custom truststore)
        base.

secure("/opt/certs/truststore.jks","changeit")
    .

toBuilder() ...;

// mTLS (mutual TLS) – provide keystore
        base.

withMtls("/opt/certs/keystore.p12","changeit")
    .

withClientConfig(TransporterConfiguration.builder()
        .

sslEnabled(true)
        .

trustStorePath("/opt/certs/truststore.jks")
        .

trustStorePass("changeit")
        .

keystorePath("/opt/certs/keystore.p12")
        .

keystorePass("changeit")
        .

build())
        .

toBuilder() ...;
```

> Under the hood `DefaultTransporterBuilder` builds a Netty `SslContext` based on your config. If `trustAll=true` and
`sslEnabled=false` it uses `InsecureTrustManagerFactory`. If `sslEnabled=true`, it loads trust/keystore (and optional
> alias) via `SslUtils`.

---

## DNS resolvers

```java
base.withResolver(TransporterClientResolver.DEFAULT_RESOLVER) // DefaultAddressResolverGroup
    .

toBuilder()
    .

resolver(TransporterClientResolver.CUSTOM)               // DnsAddressResolverGroup (udp/tcp)
    .

withBaseUrl("https://api.example.com")
    .

get(null)
    .

sendAndReceive("/ping")
    .

monoData(String .class);
```

> If you don’t know what to pick, **don’t override it**. `CUSTOM` builds a low-level `DnsNameResolverBuilder` (UDP/TCP
> via Netty).

---

## Logging & debugging

```java
base.withDebugging(true)   // richer internal debug logs
    .

withLogger()          // add Zalando Logbook client handler
    .

toBuilder()
    .

withBaseUrl("https://api.example.com")
    .

get(null)
    .

sendAndReceive("/ping")
    .

monoData(String .class);
```

To **log Authorization headers**, set `transporter.authHeaderLoggingEnabled=true` (or enable via
`ConfigurationResolver`, see below).

---

## Receiving responses

```java
// As DTO
Mono<User> user = tx.sendAndReceive("/users/1").monoData(User.class);

// As ResponseEntity<T> (status + headers + body)
Mono<ResponseEntity<User>> entity = tx.sendAndReceive("/users/1").entityData(User.class);

// As Flux<T> (NDJSON/streaming arrays)
Flux<Event> events = tx.sendAndReceive("/events").fluxData(Event.class);

// As raw buffers
Flux<DataBuffer> buffers = tx.sendAndReceive("/blob/123").mapToBuffer();

// TransporterData<T> wrapper (reactive & blocking)
Mono<TransporterData<User>> wrapped = tx.sendAndReceive("/users/1").transporterData(User.class);
TransporterData<User> blocking = tx.sendAndReceive("/users/1").transporterDataBlock(User.class);
```

Validation toggles per request:

```java
tx.sendAndReceive("/users")
  .

requireNonNull(true)
  .

checkRequiredFields(true)
  .

monoData(User .class);
```

> These flags delegate to your `TransporterConfiguration` and the internal mapping checkers (`MappingChecker`,
`MonoCheckers`, `FluxCheckers`, `EntityCheckers`). They honor model documentation like `DemoResponse` for required
> fields.

---

## URL building styles

```java
// 1) Absolute/relative string
tx.method(TransporterMethod.GET).

sendAndReceive("/users");

// 2) UriBuilder function
tx.

method(TransporterMethod.GET)
  .

sendAndReceive(b ->b.

path("/search").

queryParam("q","nika").

build());
```

---

## Typed HTTP interfaces (Spring 6 proxies)

```java
public interface GitHubApi {
    @GetExchange("/repos/{owner}/{repo}")
    Mono<Map<String, Object>> repo(@PathVariable String owner, @PathVariable String repo);
}

var builder = base.toBuilder().withBaseUrl("https://api.github.com").restService();
GitHubApi api = builder.declareClient(GitHubApi.class);  // uses HttpServiceProxyFactory
Mono<Map<String, Object>> repo = api.repo("spring-projects", "spring-framework");
```

If you need a custom factory:

```java
HttpServiceProxyFactory factory = builder.getHttpServiceProxyFactory("github");
GitHubApi api2 = builder.declareClient(GitHubApi.class, factory);
```

---

## Configuration via `ConfigurationResolver`

`DefaultBaseTransporter.toBuilder()` calls your `ConfigurationResolver.resolveBaseConfigs(config)` so you can inject
defaults (from `application.yml`, env vars, etc.).

**Properties your resolver may honor** (as per `ConfigurationResolver` interface):

- `transporter.resolver` → **TransporterClientResolver** name
- `transporter.trustStorePath` / `transporter.trustStorePassword`
- `transporter.keyStorePath` / `transporter.keyStorePassword`
- `transporter.requireNonNullEnabled` *(boolean)*
- `transporter.checkRequiredFields` *(boolean)*
- `transporter.authHeaderLoggingEnabled` *(boolean)*
- `transporter.loggingEnabled` *(boolean)*  → Logbook
- `transporter.sslEnabled` *(boolean)*
- `transporter.mtlsEnabled` *(boolean)*
- `transporter.debugEnabled` *(boolean)*
- `transporter.trustAll` *(boolean)*
- `transporter.alias` *(string)*  → mTLS alias
- plus base builder fields like `baseUrl`, `timeout`, `timeUnit`, `dataLimit` in `TransporterConfiguration`

**Example resolver skeleton:**

```java

@Component
class AppConfigurationResolver implements ConfigurationResolver {

    @Value("${transporter.resolver:}")
    private String resolver;

    @Value("${transporter.trustStorePath:}")
    private String trustStorePath;

    // ... other @Value injections

    @Override
    public void resolveBaseConfigs(@NotNull TransporterConfiguration cfg) {
        Optional.ofNullable(resolver).filter(s -> !s.isBlank())
                .ifPresent(cfg::setResolver);
        // set cfg.setSslEnabled, cfg.setTrustStorePath, cfg.setTimeout, etc.
    }

    // Implement resolve* methods to return either passed value or the configured one
}
```

---

## Recipes

**Large payloads**

```java
base.toBuilder()
    .

withBaseUrl("https://files.example.com")
    .

dataLimit(20*1024*1024)       // 20 MB max in-memory
    .

get(null)
    .

sendAndReceive("/download")
    .

mapToBuffer();                     // stream buffers
```

**Form URL-encoded + params**

```java
tx.contentType(MediaType.APPLICATION_FORM_URLENCODED)
  .

withUrlParam("locale","en")
  .

sendAndReceive("/submit")
  .

transporterDataBlock(String .class);
```

**DELETE with body**

```java
base.toBuilder().

delete(Map.of("hard","true"))
        .

sendAndReceive("/users/123")
    .

monoData(Void .class);
```

---

## Error handling

- All send/receive methods propagate WebClient errors.
- Internally, receivers use `ErrorHandlers::handleError` then produce a failed `TransporterData` (message) or error
  Mono/Flux as appropriate.
- Use `.onErrorResume(...)` in your code for recovery, or rely on `transporterDataBlock()` which returns a **failed**
  wrapper instead of throwing.

---

## Cheatsheet

```java
// Build once per host
var builder = base
                .withTimeOut(30)
                .withLogger()
                .withDebugging(true)
                .toBuilder()
                .withBaseUrl("https://api.example.com")
                .restService();

// Preset method or on-the-fly
var t = builder.post(new Dto());
// t = builder.build().method(TransporterMethod.POST).bodyValue(new Dto());

t.

contentType(MediaType.APPLICATION_JSON)
 .

bearerAuth("token")
 .

withHeader("X-Id","123")
 .

withUrlParam("verbose","true")
 .

sendAndReceive("/endpoint")
 .

requireNonNull(true)
 .

checkRequiredFields(true)
 .

monoData(Response .class);
```

---

## Notes & Guarantees

- `DefaultTransporter` **resets** auth/body/headers/params after each `send*` (see `buildBodyAndHeader`’s `finally`
  block). Reuse the same instance safely for multiple calls.
- If you call `send(...)` **without** setting a method you’ll get an `IllegalStateException`. Use builder’s
  `post/get/put/delete()` or `method(...)` first.
- SOAP mode only toggles **codecs**; endpoints and headers are still under your control.

---

## License

GPL-3.0 © Mohammed (Hamma) Fataka

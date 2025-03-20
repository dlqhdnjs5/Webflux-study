# 1. 리액티브 시스템과 리액티브 프로그래밍

## 리액티브 시스템이란?
'반응을 잘하느 시스템' 즉 클라이언트 요청에 즉각적으로 응답함으로써 지연시간을 최소하하는 시스템.

### 리액티브 시스템의 이해
리액티브 시스템은 반응성, 탄력성, 회복성, 메시지 기반의 네 가지 특성을 가진다.

- **MEANS** (리액티브 시스템에서 주요 통신수단으로 무엇을 사용 할것인가)
    - 메시지 기반(Message-Driven): 시스템은 비동기 메시지 전달을 통해 컴포넌트 간 통신해야 한다.
- **FORM** (메시지 기반 통신을 통해 어떤 형태를 지니는 시스템으로 형성되는지를 나타낸다)
    - 탄력성(Elasticity): 작업량이 변화하더라도 일정한 응답을 유지 하는것
    - 회복성(Resilience): 시스템에 장애가 발생하더라도 응답성을 유지하는것
- **VALUE** (리액티브 시스템의 핵심 가치)
    - 반응성(Responsiveness): 시스템은 신속하게 응답해야 한다.


### 리액티브 프로그래밍 이란?
**리액티브 시스템을 구축하는데 필요한 프로그래밍 모델** .
리액티브시스템에서의 비동기 메세지 통신은 Non-Blocking I/O 방식으로 이루어진다.

Block I/O 방식의 통신에서는 해당 스레드가 작업을 처리할 때까지 남아 있는 작업들은 해당 작업이 끝날 때까지 차단되어 대기,
반면에 Non-Blocking I/O 방식의 통신에서는 말 그대로 스레드가 차단되지 않는다.

### operators
part1 , part2 참고

# Spring WebFlux 개요
Spring webflux 와 Spring Mvc의 기술 스택의 비교.

## Spring WebFlux의 탄생배경
* Spring WebFlux는 Spring 5.0에 추가된 새로운 모듈이다.
* 기존의 spring mvc는 서블릿 기반의 blocking I/O로서 대량의 트래픽을 감당하기 힘들어 적은수의 스레드로 높은 처리량을 보여주는 Non-Blocking I/O방식의 Webflux가 탄생하게 되었다.

## Spring Webflux의 기술 스택 차이점
### 서버
* MVC
    * 서블릿 기반의 프레임워크이기 때문에 아파치 톰캣같은 서블릿 컨테이너에서 Blocking I/O방식으로 동작
* Webflux
    * Netty 서버를 사용하여 Non-Blocking I/O방식으로 동작

### 서버 API
* mvc
    * 서블릿 API를 사용
* WebFlux
    * 기본 서버 엔진이 netty이지만 jetty나 undertow같은 리액티브 스트림즈 어댐터를 이용한 스트림즈를 지원

### 보안
* mvc
    * 표준 서블릿 필터를 사용하는 spring security가 서블릿 컨테이너와 통합됨
* WebFlux
    * WebFliter를 이ㅛㅇㅇ해서 Spring Security를 Spring webflux에서 사용

### 데이터 액세스
* mvc
    * Blocking I/O를 사용하는 JDBC, JPA, JMS, JTA등의 데이터 액세스 기술을 사용
* WebFlux
    * Non-Blocking I/O를 사용하는 R2DBC, MongoDB 등의 데이터 액세스 기술을 사용




## Spring webflux의 요청 처리 흐름
![img.png](img.png)

1. 요청이 들어오면 Netty 등의 서버 엔진을 거쳐 HttpHandler 가 들어오는 요청을 전달 받음
2. ServerWebExchange 는 WebFilterChain 에서 전처리 과정을 거친 후 WebHandler 인터페이스의 구현체인 DispatcherHandler 에게 전달
3. DispatcherServlet 과 유사한 DispatcherHandler 는 HandlerMapping List 를 원본 Flux 의 소스로 전달 받음
4. ServerWebExchange 를 처리할 핸들러를 조회
5. 조회한 핸들러의 호출은 HandlerAdapter 에게 위임
6. HandlerAdapter 는 ServerWebExchange 를 처리할 핸들러를 호출
7. Controller 또는 HandlerFunction 형태의 핸들러에서 요청을 처리한 후 응답 데이터를 리턴
8. 핸들러로부터 리턴 받은 응답 데이터를 처리할 HandlerResultHandler 를 조회
9. 조회한 HandlerResultHandler 를 통해 response 로 리턴
   (HttpHandler는 Netty 이외의 다양한 서버 엔진에서 지원하는 서버 API 를 추상화 하며, ServerHttpRequest, ServerHttpResponse 를 포함하는 ServerWebExchange 를 생성한 후 WebFilterChain 을 통해 전달)


### Spring Webflux의 핵심 컴포넌트
* HttpHandler
  HttpHandler는 다른 유형의 Http 서버 API로 request와 response를 처리하기 위해 추상화된 단하나의 메서드만 가진다.
```java
public interface HttpHandler {
    Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response);
}
```

* HttpWebHandlerAdapter
    * 아래는 HttpHandler의 구현체인 HttpWebHandlerAdapter 클래스 코드 일부
    * handle 메서드에서 ServerWebExchange를 생성하고 HttpHandler의 handle 메서드를 호출
```java
public class HttpWebHandlerAdapter implements WebHandler {
    private final HttpHandler httpHandler;
    public HttpWebHandlerAdapter(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }
    @Override
    public Mono<Void> handle(ServetHttpRequest request, ServerHttpResponse response) {
        // ...
        // ...
        ServerWebExchange exchange = createExchange(request, response);
    }
}
```


* WebFilter
  핸들러 요청을 처리하기 전에 전처리 작업을 하도록 해주며, webfliter는 주로 보안이나 세션타임아웃 처리등 애플리케이션에서 공통으로 필요한 전처리에 사용
```java
public interface WebFilter {
    Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain);
}
```
파라미터로 전달받은 WebFilterChain을 통해 다음 WebFilter로 전달하거나, 전처리 작업을 수행한 후 WebHandler를 호출할 수 있다.

(webFilter 예제)
```java
public class SampleWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        /**
         * doAfterTerminate()는 종료이벤트(onComplete, onError)발생 시 호출되는 메서드
         */
        return chain.filter(exchange).doAfterTerminate(() -> {
            if (path.startsWith("/api")) {
                System.out.println("api: " + path);
            }
        });
    }
}
```

```kotlin
@Component
class CustomWebFilter: WebFilter {
  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    val path = exchange.request.path.value()
    return chain.filter(exchange).doAfterTerminate {
      println("Request path: $path , status : ${exchange.response.statusCode}")
    }
  }
}
```

Spring Webflux는 클라이언트의 요청부터 응답까지 Reactor의 두가지 타입인 mono나 Flux의 Operator체인으로 구성된 하나의 길다란 Sequence라고 생각하면 좀더 쉽게 접근할 수 있다.
따라서 Spring Webflux에서 사용할 수 있는 Filter역시 Reactor Sequence의 일부가 된다는 사실 역시 기억하면 좋다.

* HandlerFilterFunction
  함수형 기반의 요청 핸들러에 적용 할 수 있는 Filter로서, WebFilter와 유사하지만 HandlerFilterFunction은 HandlerFunction을 처리하기 전후로 전처리 작업을 수행할 수 있다.
```java
public interface HandlerFilterFunction<T, R> {
    default Mono<R> filter(ServerRequest request, HandlerFunction<T, R> next) {
        return next.handle(exchange);
    }
}
```
HanlderFilterFunction은 filter 메서드로 정의되어 있으며 파리미터로 전달받은 HandlerFunction에 연결된다.

```java
import org.springframework.web.reactive.function.server.ServerRequest;

public class SampleHandlerFilterFunction implements HandlerFilterFunction<ServerRequest, ServerResponse> {
    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerRequest, ServerResponse> next) {
        String path = request.requestPath().value();
        
        return next.handle(request).doAfterTerminate(() -> {
            if (path.startsWith("/api")) {
                System.out.println("api: " + path);
            }
        });
    }
}
```

```kotlin
class HandlerFilter: HandlerFilterFunction<ServerResponse, ServerResponse> {
    override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
       val path = request.requestPath().value()

        return next.handle(request).doAfterTerminate {
            println("Request path: $path , status : ${request.exchange().response.statusCode}")
        }
    }
}
```

WebFilter의 구현체는 SpringBean으로 등록되는 반면 HandlerFilterFunction은 RouterFunction에 등록되어 사용된다.(함수형 기반의 요청핸들러)
따라서 Spring Bean으로 등록되지 않는다.


#### 차이점
* Webfilter
    * 애플리케이션 내 정의된 모든 핸들러에 공통적으로 동작한다. 따라서 애너테이션 기반 요청 핸들러와 함수형 기반의 요청 핸들러에서 모두 동작
* HandlerFilterFunction
    * 함수형 기반의 요청 핸들러에만 동작한다. 함수형 기분의 핸들러에서만 제한적으로 필터링 작업을 수행하고 싶다면 HandlerFilterFunction을 사용



#### DispatcherHandler
DispatcherHandler는 WebHandler 인터페이스의 구현체로서 Spring MVC에서 Front Controller 패턴이 적용된 DispatcherServlet과 유사하게 중앙에서 먼저 요청을 전달 받은 후 다른 컴포넌트에 요청 처리를 위임함.
DispatcherHandler 자체가 SpringBean 이며 ApplicationContext에서 HandlerMapping, HandlerAdapter, HandlerResultHandler등의 요청 처리를 위한 위임 컴포넌트를 검색함.

아래는 DispatcherHandler의 일부 코드이다.
```java
public class DispatcherHandler implements WebHandler, PreFlightRequestHandler, ApplicationContextAware {
    @Nullable
    private List<HandlerMapping> handlerMappings;
    @Nullable
    private List<HandlerAdapter> handlerAdapters;
    @Nullable
    private List<HandlerResultHandler> resultHandlers;

    public DispatcherHandler() {
    }

    public DispatcherHandler(ApplicationContext applicationContext) {
        this.initStrategies(applicationContext);
    }

    @Nullable
    public final List<HandlerMapping> getHandlerMappings() {
        return this.handlerMappings;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.initStrategies(applicationContext);
    }

    protected void initStrategies(ApplicationContext context) {
        Map<String, HandlerMapping> mappingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
        ArrayList<HandlerMapping> mappings = new ArrayList(mappingBeans.values());
        AnnotationAwareOrderComparator.sort(mappings);
        this.handlerMappings = Collections.unmodifiableList(mappings);
        Map<String, HandlerAdapter> adapterBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
        this.handlerAdapters = new ArrayList(adapterBeans.values());
        AnnotationAwareOrderComparator.sort(this.handlerAdapters);
        Map<String, HandlerResultHandler> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerResultHandler.class, true, false);
        this.resultHandlers = new ArrayList(beans.values());
        AnnotationAwareOrderComparator.sort(this.resultHandlers);
    }

    public Mono<Void> handle(ServerWebExchange exchange) {
        if (this.handlerMappings == null) {
            return this.createNotFoundError();
        } else {
            return CorsUtils.isPreFlightRequest(exchange.getRequest()) ? this.handlePreFlight(exchange) : Flux.fromIterable(this.handlerMappings).concatMap((mapping) -> {
                return mapping.getHandler(exchange);
            }).next().switchIfEmpty(this.createNotFoundError()).flatMap((handler) -> {
                return this.invokeHandler(exchange, handler);
            }).flatMap((result) -> {
                return this.handleResult(exchange, result);
            });
        }
    }

    private <R> Mono<R> createNotFoundError() {
        return Mono.defer(() -> {
            Exception ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "No matching handler");
            return Mono.error(ex);
        });
    }

    private Mono<HandlerResult> invokeHandler(ServerWebExchange exchange, Object handler) {
        if (ObjectUtils.nullSafeEquals(exchange.getResponse().getStatusCode(), HttpStatus.FORBIDDEN)) {
            return Mono.empty();
        } else {
            if (this.handlerAdapters != null) {
                Iterator var3 = this.handlerAdapters.iterator();

                while(var3.hasNext()) {
                    HandlerAdapter handlerAdapter = (HandlerAdapter)var3.next();
                    if (handlerAdapter.supports(handler)) {
                        return handlerAdapter.handle(exchange, handler);
                    }
                }
            }

            return Mono.error(new IllegalStateException("No HandlerAdapter: " + handler));
        }
    }

    private Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        return this.getResultHandler(result).handleResult(exchange, result).checkpoint("Handler " + result.getHandler() + " [DispatcherHandler]").onErrorResume((ex) -> {
            return result.applyExceptionHandler(ex).flatMap((exResult) -> {
                String text = "Exception handler " + exResult.getHandler() + ", error=\"" + ex.getMessage() + "\" [DispatcherHandler]";
                return this.getResultHandler(exResult).handleResult(exchange, exResult).checkpoint(text);
            });
        });
    }

    private HandlerResultHandler getResultHandler(HandlerResult handlerResult) {
        if (this.resultHandlers != null) {
            Iterator var2 = this.resultHandlers.iterator();

            while(var2.hasNext()) {
                HandlerResultHandler resultHandler = (HandlerResultHandler)var2.next();
                if (resultHandler.supports(handlerResult)) {
                    return resultHandler;
                }
            }
        }

        throw new IllegalStateException("No HandlerResultHandler for " + handlerResult.getReturnValue());
    }

    public Mono<Void> handlePreFlight(ServerWebExchange exchange) {
        return Flux.fromIterable(this.handlerMappings != null ? this.handlerMappings : Collections.emptyList()).concatMap((mapping) -> {
            return mapping.getHandler(exchange);
        }).switchIfEmpty(Mono.fromRunnable(() -> {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        })).next().then();
    }
}

```
initsStrategies 메서드는 ApplicationContext에서 HandlerMapping, HandlerAdapter, HandlerResultHandler등의 컴포넌트를 검색하여 설정한다.
DispatcherHandler는 HandlerMapping, HandlerAdapter, HandlerResultHandler등의 컴포넌트를 검색하여 요청을 처리하며, HandlerMapping은 요청을 처리할 핸들러를 조회하고, HandlerAdapter는 핸들러를 호출하고, HandlerResultHandler는 핸들러로부터 리턴받은 응답 데이터를 처리한다.



### Spring Webflux의 Non-Blocking 프로세스 구조
Spring Mvc와 Webflux는 동시성 모델과 스레드에 대한 기본전략에서 차이점이 있다.
Blocking I/O 방식의 MVC는 요청을 처리하는 스레드가 차단될 수 있기 때문에 기본적으로 대용량의 스레드 풀을 사용해서 하나의 요청을 하나의 스레드가 처리,
반면 Non-Blocking I/O 방식의 Spring Webflux는 스레드가 차단되지 않기때문에 적은 수의 고정된 스레드 풀을 사용해서 더많은 요청을 처리하며 이벤트 루프방식을 사용한다.

![img_2.png](img_2.png)
1. 클라이언트로부터 들어오는 요청을 핸들러가 전달 받음
2. 전달받은 요청을 이벤트 루프에 푸시
3. 이벤트 루프는 요청에 대한 작업을 처리
4. 작업이 완료되면 완료이벤트를 이벤트 루프에 푸시
5. 등록한 콜백을 호출해 처리결과 전달.

### Spring webflux의 스레드 모델
Spring Webflux는 Non-blocking I/O 를 지웒하는 Netty 등의 서버 엔진에서  적은 수의 고정된 크기의 스레드(일반적으로 cpu 코어 개수만큼의 스레드 생성)로 높은 처리량을 보여줄 수 있다.
(Reactor Netty에서는 CPU 코에의 개수가 4보다 작은 경우 최소 4개의 워커 스레드 생성하며 4보다 많으면 코어 개수만큼의 스레드를 생성한다.)

하지만, 서버측에서 복잡한 연산을 처리하는 등의 CPU 집약적인 작업을 하거나, 클라이언트의 요청부터 응답 처리 전 과정안에 Blocking 되는 지점이 존재한다면
오히려 성능이 저하될수 있다. 이를 보완하고자 클라이언트의 요청을 처리하기 위해 서버 엔진에서 제공하는 스레드 풀이 아닌 다른 스레드 풀을 사용할 수 있는 메커니즘을 제공 하는데 그것이 지난 챕터의 스케쥴러 이다.

#### 애너테이션 기본 컨트롤러
* 기존 mvc 에너테이션 기반의 컨트롤러에서 Mono 타입을 리턴한다는 점 외에 달라진것은 없다


```
Spring Webflux는 spring mvc에서 지원하던 @ResponseBody 애너테이션과 ResponseEntity 타입을 그대로 지원.
Spring MVC와의 차이점은 리액티브 타입을 이용해 데이터를 비동기적으로 response body에 렌더링 되도록 한다는점이다.
```


#### 함수형 엔드포인트
```
Spring WebFlux는 기존의 애너테이션 기반 프로그래밍 모델과 함께 함수형 엔드포인트를 기반으로 하는 새로운 프로그래밍 모델을 지원.
함수형 엔드포인트에서는 들어오는 요청을 라우팅하고, 라우팅된 요청을 처리하며 결과 값을 응답으로 리턴하는 등의 모든 작업을 하나의 함수 체인에서 처리함.
```

함수형 엔드포인트는 요청을 처리하기위해 HandlerFunction 이라는 함수형 기반의 핸들러를 사용한다.
이는 요청 처리를 위한 ServerRequest 하나만 handle 메서드에 전달받아 처리하고 ServerResponse를 리턴한다.

* ServerRequest
  HandlerFunction 에 의해 처리되는 HTTP request를 표현함 따라서 HttpHeaders, method, URI, query parameter, request body 등의 정보를 포함한다.
* ServerResponse
  HandlerFunction 에 의해 생성된 HTTP response를 표현함. 따라서 HTTP status code, headers, body 등의 정보를 포함한다.

Request 라우팅을 위한 RouterFunction
RouterFunction은 HandlerFunction을 라우팅하기 위한 함수형 인터페이스이다.(@ReuqestMapping 애너테이션과 동일한 기능을 한다.)
RouterFUnction은 요청을 위한 데이터 뿐만 아니라 요청 처리를 위한 동작가지 RouterFunction 의 파라미터로 제공한다는 차이점이 있음.


### 함수형 엔드포인트에서의 request body 유효성 검증
함수형 엔드포인트는 Spring의 validator 인터페이스를 구현한 Custom Validator 을 이용해서 request body에 유효성 검증을 적용할 수 있음

```kotlin
@Component("bookValidator")
class BookValidator: Validator {
    override fun supports(clazz: Class<*>): Boolean {
        return Book::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: org.springframework.validation.Errors) {
        val book = target as Book
        if (book.title.isBlank()) {
            errors.rejectValue("title", "title.empty")
        }
    }
}


@Service
class Handler(
  val bookValidator: BookValidator,
  val bookRepository: BookRepository
) {
  fun createBook(
    serverRequest: ServerRequest
  ): Mono<ServerResponse> {
    println("Creating a book")
    return serverRequest.bodyToMono(Book::class.java)
      .doOnNext { it -> validate(it) } // 유효성 검증을 위해 주입받은 BookValidator 를 이용해 doOnNext에서 validate 메서드를 호출
      .flatMap { bookRepository.createBook(it) }
      .flatMap { ServerResponse.ok().build() }

    /**
     * Spring webflux에서는 유효성 검증을 진행하는 로직 역시 Operator 체인 안에서 진행됨.
     */
  }
  
  ... 

  /**
   * 유효성 검증을 위한 함수
   */
  fun validate(book: Book) {
    val errors = Book::class.simpleName?.let { BeanPropertyBindingResult(book, it) }
    bookValidator.validate(book, errors!!)

    if (errors.hasErrors()){
      throw ServerWebInputException(errors.toString())
    }
  }
}

```

Spring webflux에서는 유효성 검증을 진행하는 로직 역시 Operator 체인 안에서 진행한다.



## Spring Data R2DBC
R2DBC는 관계형 데이터베이스에 리액티브 프로그래밍 API를 제공하기 위한 개방형 사양 이면서, 드라이버 벤더가 구현하고 클라이언트가 사용하기 위한 SPI(Service Provider Interface)다.

### Spring Data R2DBC
Spring Data R2DBC는 R2DBC 기반 Repository를 좀더 쉽게 구현하게 해주는 Spring Data Family의 일원이며 Spring이 추구하는 추상화 기법이 적용되어 있다.
Spring Data R2DBC는 JPA같은 ORM 프레임워크에서 제공하는 캐싱, 지연로딩, 기타 ORM 프레임워크에서 가지고 있는 특징들이 제거되어 단순하고 심플하다.
또한 보일러 플레이트 코드의 양을 대폭 줄일 수 있다.


### R2DBC Repository를 이용한 데이터 액세스
Spring Data R2dbc 는 Repository를 지원합니다.
```kotlin
@Repository
interface BookRepositoryR2dbc: ReactiveCrudRepository<BookEntity, Long> {
    fun findByIsbn(isbn: String): Mono<BookEntity>
}
```

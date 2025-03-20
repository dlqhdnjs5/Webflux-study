package com.study.tuco.reactive.part2.`14_operators`

import org.reactivestreams.Subscription
import reactor.core.Disposable
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.ConnectableFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import java.nio.file.Files
import java.nio.file.Path
import java.security.CryptoPrimitive
import java.time.Duration
import java.time.LocalDateTime
import java.util.stream.Collectors
import java.util.zip.DataFormatException

class Main {
    companion object {
        val log = org.slf4j.LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            //justOrEmpty()
            //fromIterable()
            //fromStream()
            //range()
            //defer()
            //defer2()
            //justTest()
            //using()
            //generate()
            // create_pull_ver()
            // create_backpressure()

            // ---시퀀스 필터링 오퍼레이터---
            // filter()
            // filterWhen()
            // skip()
            // skip_time()
            // take()
            // take_time()
            // take_last()
            // takeUntil()

            // ---시퀀스 변환 오퍼레이터---
            // map()
            // flatMap()
            // flatMap_ex2()
            // merge()
            //zip()
            //zip2()
            //zip3()
            // and()
            // and2()
            // collectList()
            //collectMap()

            // ---doXXXX 오퍼레이터---
            // consumer, runnable 타입의 함수형 인터페이스를 파라미터로 가지기 때문에 별도의 리턴값이 없음
            // doOnXXXX()
            // 에러처리_operator()
            // 에러처리_operator2()
            //onErrorReturn()
            //onErrorResume()
            //onErrorContinue()
            //onErrorRetry()
            //onErrorRetry2()

            // elapsed()
            // elapsed2()
            //window()
            //buffer()
            //bufferTimeout()
            //groupBy()
            //groupBy2()


            // ---다수의  subscriber에게 flux를 멀티 캐스팅 하기위한 operator---
            // publish()
            // autoConnect()
            // refCount()


            Thread.sleep(10000)

        }


        /**
         * emit할 데이터가 null 일 경우 npe발생시키지 않고 conComplete만 호출
         */
        fun justOrEmpty() {
            Mono.justOrEmpty<Any>(null)
                .subscribe(
                    { log.info("justOrEmpty: {$it}") },
                    { log.error("justOrEmpty: Error") },
                    { log.info("justOrEmpty: Completed") }
                )
        }

        /**
         * Iterable에 포함된 데이터를 emit하는 flux를 생성
         */
        fun fromIterable() {
            Flux.fromIterable(listOf(1, 2, 3, 4, 5))
                .subscribe {
                    log.info("fromIterable: $it")
                }
        }

        /**
         * Stream에 포함된 데이터를 emit하는 flux를 생성
         */
        fun fromStream() {
            Flux.fromStream(listOf(1, 2, 3, 4, 5).stream())
                .filter { it % 2 == 0 }
                .subscribe {
                    log.info("fromStream: $it")
                }
        }

        /**
         *  n부터 1식 증가시키는 Flux를 생성
         */
        fun range() {
            Flux.range(1, 5)
                .subscribe {
                    log.info("range: $it")
                }
        }

        /**
         * defer는 operator를 선언한 시점에 데이터를 emit하는게 아닌 구독 시점에 emit하는 Flux/mono를 생성
         * emit을 지연시키기 때문에 꼭 피룡한 시점에 데이터를 emit하여 불필요한 프로세스를 줄인다.
         *
         * HOT Publisher
         * just - 구독 여부와 상관없이 emit (구독이 발생하면 emit된 데이터를 reply하여 subscriber에게 전달)
         * Mono.just()는 단일 값을 즉시 감싸서 Mono로 반환한다. 값을 이미 알고 있고, 그 값을 비동기적으로 반환해야 할 때 유용하다. 예를 들어, 이미 계산된 값이나 상수를 Mono로 반환하고자 할 때 사용된다.
         *
         *
         * defer - 구독 시점에 emit
         * 이는 값을 지연 계산하고, Mono가 구독될 때 비로소 계산이 이루어지도록 할 때 유용하다.
         *
         * 예시) https://100100e.tistory.com/535
         * https://binux.tistory.com/135
         */
        fun defer() {
            log.info("start ${LocalDateTime.now()}")

            val justMono = Mono.just(LocalDateTime.now())
            val deferMono = Mono.defer { Mono.just(LocalDateTime.now()) }

            Thread.sleep(2000)

            justMono.subscribe { log.info("#onNext just1 ${it}") }
            deferMono.subscribe { log.info(("#onNext defer1 ${it}")) }

            Thread.sleep(2000)

            justMono.subscribe { log.info("#onNext just2 ${it}") }
            deferMono.subscribe { log.info("#onNext defer2 ${it}") }
        }

        fun defer2() {
            log.info("start ${LocalDateTime.now()}")

            val sayDefault: () -> Mono<String> = {
                log.info("# Say hi")
                Mono.just("Hi")
            }

            val sayDefault2: () -> Mono<String> = {
                log.info("# Say bye")
                Mono.just("bye")
            }

            Mono.defer(sayDefault)
                .delayElement(Duration.ofSeconds(3))
                .switchIfEmpty(sayDefault())
                //.switchIfEmpty ( Mono.defer(sayDefault) )
                .subscribe { log.info("#onNext $it") }

            Thread.sleep(35000)
        }

        /*fun justTest()  {
            fun businessLogic1(): String {
                log.info("# businessLogic1")
                Thread.sleep(1000)
                return "businessLogic1"
            }

            fun businessLogic2(): String {
                log.info("# businessLogic2")
                Thread.sleep(1000)
                return "businessLogic2"
            }

            val monoExample = {
                val isDefer = true
                if (isDefer) {
                    Mono.just(businessLogic2())
                } else {
                    Mono.just(businessLogic1())
                }
            }

            monoExample().subscribe { log.info("#onNext $it") }

            Thread.sleep(5000)
        }*/

        /**
         * 자원을 가져와 처리하느 코드를 작성할 때 사용
         */
        fun using() {
            val path: Path =
                Path.of("C:\\study\\Reactive\\src\\main\\kotlin\\com\\study\\tuco\\reactive\\14_operators\\example.txt")
            Flux.using(
                { Files.lines(path) },
                { reader -> Flux.fromStream(reader) },
                { reader -> reader.close() }
            ).subscribe { log.info("onNext: $it") }
        }

        /**
         * generate
         * 일종의 while처럼 데이터를 생성.
         * Operator프로그래밍 방식으로 signal이벤트를 발생시키며 동기적으로 데이터를 하나씩 순차적으로 emit할때 사용
         * 첫번째 파라미터는 emit할 숫자의 초깃값을 지정 (음수, 양수 모두 가능)
         *  state는 1씩 증가하는 숫자를 상태값으로 정의
         */
        fun generate() {
            Flux.generate({ 0 }, { state, sink -> // 첫번째 파라미터는 state의 초기값으로 0으로 지정
                sink.next("3 x $state = ${3 * state}") // 방출할 데이터를 sink.next()로 전달
                if (state == 10) sink.complete()
                state + 1// 초깃값 셋팅
            }).subscribe { log.info("onNext1: $it") }


            // 튜플로 사용.
            val dan = 3
            Flux.generate({ Tuples.of(dan, 1) }, { state, sink ->
                sink.next("${state.t1} x ${state.t2} = ${state.t1 * state.t2}")

                if (state.t2 == 9) sink.complete()

                Tuples.of(state.t1, state.t2 + 1)
            }).subscribe { log.info("onNext2: $it") }
        }


        /**
         * create operator는 generate와 다르게 한번에 여러건의 데이터를 비동기 적으로 emit 할 수 있다.
         * 아래는 subscriber에서 요청한 경우에만 create operator내에서 요청개수만큼의 데이터를 emit하는 예제코드
         */
        fun create_pull_ver() {
            var size = 0
            var count = -1
            val dataSource = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // emit 대상

            Flux.create { sink: FluxSink<Int> ->
                // 2. 구독 요청 발생시 요청한 개수만큼 데이터를 emit
                sink.onRequest { request -> // request: request 요청 개수
                    try {
                        Thread.sleep(1000)

                        for (i in 0..request) {
                            if (count >= 9) {
                                sink.complete() // 4. datasourceList 숫자 모두 emit하면 onComplete signal 발생.
                                break
                            } else {
                                count++
                                sink.next(dataSource[count])
                            }
                        }
                    } catch (InterruptedException: Exception) {
                        sink.error(InterruptedException)
                    }
                }
                sink.onDispose { log.info("# clean up") } // 6. 완료되어 구독이 취소되면 호출
            }.subscribe(object : BaseSubscriber<Int>() {
                var size = 0

                /**
                 * 3. onNext 발생시 2개만큼 데이터를 요청
                 */
                override fun hookOnNext(value: Int) {
                    size++
                    log.info("# onNext $value")
                    if (size == 2) {
                        request(2)
                        size = 0
                    }
                }

                /**
                 * 1.구독 발생시 한번에 두개의 데이터 요청
                 */
                override fun hookOnSubscribe(subscription: Subscription) {
                    request(2)
                }

                /**
                 * 5. onComplete 발생시 호출
                 */
                override fun hookOnComplete() {
                    log.info("# onComplete")
                }
            })
        }

        fun create_push_ver() { // ??
            val priceEmitter: CryptoPrimitive
        }

        fun create_backpressure() {
            var start = 1
            var end = 4

            Flux.create(
                { sink: FluxSink<Int> ->
                    sink.onRequest { request ->
                        log.info("#requested : $request")

                        try {
                            Thread.sleep(500)
                            for (i in start..end) {
                                sink.next(i)
                            }
                            start += 4
                            end += 4
                        } catch (InterruptedException: Exception) {
                            sink.error(InterruptedException)
                        }
                    }

                    sink.onDispose { log.info("# clean up") }
                },
                FluxSink.OverflowStrategy.DROP // backpressure의 drop 전략
            ).subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel(), 2)
                .subscribe(object : BaseSubscriber<Int>() {
                    override fun hookOnSubscribe(subscription: Subscription) {
                        request(2) // 처음 두 개의 데이터 요청
                    }

                    override fun hookOnNext(value: Int) {
                        log.info("#onNext $value")
                    }
                }
                )

            Thread.sleep(6000)
        }

        /**
         * 시퀀스 필터링 오퍼레이터
         * 동기적
         */
        fun filter() {
            Flux.range(1, 10)
                .filter { it % 2 == 0 }
                .subscribe { log.info("filter: $it") }
        }


        /**
         * 시퀀스 필터링 오퍼레이터
         */

        /**
         * 비동기적
         * 오퍼레이터 내부에서 inner sequence를 통해 조건에 맞는 데이터인지 비동기적으로 테스트 한후 결과가
         * true라면 downstream에게 전달
         *
         * 아래 예제가 설명에 부족할경우
         * https://www.inflearn.com/community/questions/1300908/filterwhen%EC%9D%98-%EC%B0%A8%EC%9D%B4?srsltid=AfmBOopiB0qDOCyV587Y-CA0aoID-t6s8aq8N-8PppRi9lblKWXyQulc
         * 확인
         */
        fun filterWhen() {
            Flux.range(1, 10)
                .filterWhen { Mono.just(it % 2 == 0) }
                .subscribe { log.info("filterWhen: $it") }
        }

        /**
         * skip operator는 emit할 데이터의 개수를 제한하는 오퍼레이터 (skip(5)는 처음 5개의 데이터를 skip하고 나머지 데이터를 emit)
         */
        fun skip() {
            Flux.range(1, 10)
                .skip(5)
                .subscribe { log.info("skip: $it") }

            Thread.sleep(5500)
        }

        fun skip_time() {
            Flux.interval(Duration.ofSeconds(1))
                .skip(Duration.ofSeconds(5))
                .subscribe { log.info("#onNext: $it") }

            Thread.sleep(5500)
        }

        /**
         * take operator는 emit할 데이터의 개수를 제한하는 오퍼레이터 (take(3)은 처음 3개의 데이터만 emit)
         */
        fun take() {
            Flux.interval(Duration.ofSeconds(1))
                .take(3)
                .subscribe { log.info("#onNext: $it") }

            Thread.sleep(4000)
        }

        fun take_time() {
            Flux.interval(Duration.ofSeconds(1))
                .take(Duration.ofSeconds(3))
                .subscribe { log.info("#onNext: $it") }

            Thread.sleep(4000)
        }

        /**
         * takeLast operator는 emit할 데이터의 개수를 제한하는 오퍼레이터 (takeLast(3)은 마지막 3개의 데이터만 emit)
         */
        fun take_last() {
            Flux.range(1, 10)
                .takeLast(3)
                .subscribe { log.info("#onNext: $it") }
        }

        /**
         * takeUntil operator는 안의 표현식이 true가 될때까지 emit함 (takeUntil(5)은 5까지만 emit)
         * emit된 데이터에 표현식을 평가할 때 사용한 데이터가 포함됨
         */
        fun takeUntil() {
            Flux.range(1, 10)
                .takeUntil { it == 5 }
                .subscribe { log.info("#onNext: $it") }
        }

        /**
         * takeUntil 과는 달리 파라미터로 입력한 람다 표현식이 true가 되는 동안에만 emit
         * 표현식을 평가할 때 사용한 데이터는 포함되지 않음
         */
        fun takeWhile() {
            Flux.range(1, 10)
                .takeWhile { it < 5 }
                .subscribe { log.info("#onNext: $it") }
        }

        /**
         * next operator는 첫번째 데이터만 emit하고 onComplete signal을 발생시키는 오퍼레이터
         */
        fun next() {
            Flux.range(1, 10)
                .next()
                .subscribe { log.info("#onNext: $it") }
        }


        /**
         * 시퀀스 변환 오퍼레이터
         */

        /**
         * map operator는 데이터를 변환하는 오퍼레이터
         * map operator 내부에서 에러 발생 시 sequence가 종료되지 않고 계속 진행되도록 하는 기능 지원
         */
        fun map() {
            Flux.just("1-circle", "2-triangle", "3-rectangle")
                .map { it.replace("circle", "rectangle") }
                .subscribe { log.info("#onNext: $it") }
        }

        fun flatMap() {
            Flux.just("good", "bad")
                .flatMap {
                    Flux.just("dog", "cat", "cow")
                        .map { animal -> "$animal is $it" }
                }
                .subscribe { log.info("#onNext: $it") }
        }

        /**
         * inner sequence 를 비동기적으로 실행하면 데이ㅓ의 emit 순서를 보장하지 않는다
         */
        fun flatMap_ex2() {
            Flux.range(2, 8)
                .flatMap { dan ->
                    Flux.range(1, 9)
                        .publishOn(Schedulers.parallel())
                        .map { "$dan * $it = ${dan * it}" }
                }
                .subscribe { log.info("#onNext: $it") }

            Thread.sleep(1000)
        }

        /**
         * 파라미터로 입력되는 publisher의 시퀀스를 연결해서 데이터를 순차적으로 emit
         * 특히 먼저 입력된 publisher의 시쿠너스가 종료될 때까지 나머지 publisher의 시퀀스는 subscribe되지 않고 대기한다.
         */
        fun concat() {
            val flux1 = Flux.just("A", "B", "C")
            val flux2 = Flux.just("D", "E", "F")

            Flux.concat(flux1, flux2)
                .subscribe { log.info("#onNext: $it") }
        }

        /**
         * 파라미터로 입력되는 publisher의 시퀀스에서 데이터를 교차로 배치하는 방식으로 병합합니다.
         * concat처럼 먼저 입력된 publisher의 시퀀스가 종료되고 나머지 publisher 시퀀스가 구독되는 것과 달리
         * merge는 모든 publisher의 시퀀스를 동시에 구독하고 데이터를 emit
         */
        fun merge() {
            Flux.merge(
                Flux.just(1, 2, 3, 4).delayElements(Duration.ofMillis(300)),
                Flux.just(5, 6, 7, 8).delayElements(Duration.ofMillis(500))
            ).subscribe { log.info("#onNext: $it") }

            Thread.sleep(2000)
        }

        /**
         * 파라미터로 입력되는 Publisher sequence에서 emit된 데이터를 결합하는데, 각 publisher가 데이터를 하나씩 emit할때까지 "기다렸다가" 결합
         *
         * zipWhen, zipWith 도 있음
         * zipWhen은 각 요소에 대해 비동기 함수를 적용하여 생성된 Publisher의 결과와 원래 요소를 결합합니다
         * zipWith는 두 Publisher의 요소를 순차적으로 결합합니다.
         */
        fun zip() {
            Flux.zip(
                Flux.just(1, 2, 3).delayElements(Duration.ofMillis(300)),
                Flux.just(4, 5, 6).delayElements(Duration.ofMillis(500))
            ).subscribe { log.info("# onNext: ${it}") }

            Thread.sleep(2000)
        }

        /**
         * zpi2
         * zip 의 세번째 파라미터로 combinator을 추가해 두개의 Flux가 emit하는 한쌍의 데이터를 combinator에서 전달바다 변환작업 거친후 subscriber에게 전달
         */
        fun zip2() {
            Flux.zip(
                Flux.just(1, 2, 3).delayElements(Duration.ofMillis(300)),
                Flux.just(4, 5, 6).delayElements(Duration.ofMillis(500)),
                { n1, n2 -> n1 * n2 }
            ).subscribe { log.info("# onNext: ${it}") }

            Thread.sleep(2000)
        }

        fun zip3() {
            fun getNum(): Flux<Tuple3<Int, Int, Int>> {
                return Flux.zip(
                    Flux.fromIterable(listOf(1, 2, 3)),
                    Flux.fromIterable(listOf(4, 5, 6)),
                    Flux.fromIterable(listOf(7, 8, 9)),
                )
            }


            getNum().subscribe {
                log.info("# onNext: ${it.t1 + it.t2 + it.t3}")
            }

            Thread.sleep(2000)
        }



        /**
         * and는 Mono의 complete signal과 파라미터로 입력된 publisher의 complete signal을 결합하여 새로운 Mono를 만든다.
         * 즉 Mono의 파라미터로 입력된 Publisher의 sequence가 모두 종료되었음을 subscriber에게 알릴 수 있다.
         *
         * * 즉 and() 는 모든 sequence가 종료되길 기다렸다가 최종적으로 onComplete signal만 전송한다.
         * 또한 위 stream연산을 기다리지 않고 실행한다. (동시에 처음 publisher와 and 내 publisher가 실행된다.)
         */
        fun and() {
            Mono.just("taks 1")
                .delayElement(Duration.ofSeconds(1))
                .doOnNext { log.info("#Mono doOnNext $it") }
                .and(
                    Flux.just("task 2", "task 3", "task 4")
                        .delayElements(Duration.ofMillis(500))
                        .doOnNext { log.info("FluxDoOnNext: $it") }
                )
                .subscribe(
                    { log.info("#onNext: $it") },
                    { log.error("#onError: $it") },
                    { log.info("#onComplete") }
                )


            Thread.sleep(3000)
        }

        /**
         * and2
         * 두개의 작업이 성공적으로 완성된후 complete를 실행
         */
        fun and2() {
            fun bootAppServer(): Mono<String> {
                return Mono.just("started app server")
                    .delayElement(Duration.ofSeconds(2))
                    .doOnNext { log.info(it) }
            }

            fun bootDbServer(): Mono<String> {
                return Mono.just("started db server")
                    .delayElement(Duration.ofSeconds(4))
                    .doOnNext { log.info(it) }
            }

            bootAppServer()
                .and(bootDbServer())
                .subscribe(
                    { log.info("#onNext: $it") },
                    { log.error("#onError: $it") },
                    { log.info("#onComplete all servers are started") }
                )

            Thread.sleep(6000)
        }

        /**
         * collectList
         * Flux가 emit하는 모든 데이터를 List로 수집하여 emit
         */
        fun collectList() {
            Flux.range(1, 10)
                .collectList()
                .subscribe { log.info("#onNext: $it") }

            Thread.sleep(6000)
        }

        /**
         *  collectMap
         *  Flux가 emit하는 모든 데이터를 Map으로 수집하여 emit
         */
        fun collectMap() {
            Flux.range(1, 26)
                .collectMap({ it }, { it + 1 })
                .subscribe { log.info("#onNext: $it") }

            Thread.sleep(6000)
        }


        /**
         * doOnXXXX 오퍼레이터
         * doXXXX 오퍼레이터
         * 부수효과
         * 정해진 결과를 돌려주는것 이외의 어떤일을 하게되면 부수효과가 있는 함수라고 함
         * consumer, runnable 타입의 함수형 인터페이스를 파라미터로 가지기 때문에 별도의 리턴값이 없음
         */
        fun doOnXXXX() {
            Flux.range(1, 5)
                .doFinally { log.info("# doFinally 1: $it") }
                .doFinally { log.info("# doFinally 2: $it") }
                .doOnNext { log.info("#range > doOnNext $it") }
                .doOnRequest { log.info("# doOnRequest: $it") }
                .doOnSubscribe { log.info("# doOnSubscribe 1") }
                .doFirst { log.info("# doFirst") }
                .filter { it % 2 == 1 }
                .doOnNext { "#filter > doOnNext() : $it" }
                .doOnComplete { log.info("# doOnComplete") }
                .subscribe(object : BaseSubscriber<Int>() {

                    /**
                     * 3. onNext 발생시 2개만큼 데이터를 요청
                     */
                    override fun hookOnNext(value: Int) {
                        log.info("hookOnNext $value")
                        request(1)
                    }

                    /**
                     * 1.구독 발생시 한번에 두개의 데이터 요청
                     */
                    override fun hookOnSubscribe(subscription: Subscription) {
                        request(1)
                    }

                    /**
                     * 5. onComplete 발생시 호출
                     */
                    override fun hookOnComplete() {
                        log.info("# onComplete")
                    }
                })
        }

        /**
         * 에러처리 오퍼레이터
         * error()는 파라미터로 지정된 에러로 종료하는 Flux를 생성
         * 마치 java의 throw 키워드를 사용해서 예외를 의도적으로 던지는 역할, 주로 체크 예외를 캐치해서 다시 던지는경우 사용
         */
        fun 에러처리_operator() {
            Flux.range(1, 5)
                .flatMap {
                    if ((it * 2) % 3 == 0) {
                        Flux.error(IllegalArgumentException("Not allowed multiple of 3"))
                    } else {
                        Mono.just(it * 2)
                    }

                }
                .subscribe({ log.info("# onNext: $it") }, { log.error("# onError: $it") })
        }

        fun 에러처리_operator2() {
            val convert = { ch: Char ->
                if (!Character.isAlphabetic(ch.code)) {
                    throw DataFormatException("Not Alphabet")
                }

                Mono.just("Converted to ${Character.toUpperCase(ch)}")
            }

            Flux.just('a', 'b', '3', 'd')
                .flatMap {
                    try {
                        convert(it)
                    } catch (DataFormatException: Exception) {
                        Flux.error(DataFormatException)
                    }
                }.subscribe({ log.info("# onNext: $it") }, { log.error("# onError: $it") })
        }

        /**
         * onErrorReturn
         * 에러가 발생했을때 대체값을 emit하고 onComplete signal을 발생시키는 오퍼레이터
         */
        fun onErrorReturn() {
            val getBooks = {
                Flux.fromIterable(listOf("book1", "book2", "book3"))
                    .map { if (it == "book2") throw RuntimeException("Error") else it }
            }

            getBooks()
                .map { it.toUpperCase() }
                .onErrorReturn("no Pen Name")
                .subscribe({ log.info("# onNext: $it") }, { log.error("# onError: $it") }, { log.info("# onCoplete") })
        }

        fun onErrorReturn2() {
            val getBooks = {
                Flux.fromIterable(listOf("book1", "book2", "book3"))
                    .map { if (it == "book2") throw RuntimeException("Error") else it }
            }

            getBooks()
                .map { it.toUpperCase() }
                .onErrorReturn(DataFormatException::class.java, "no Pen Name")
                .onErrorReturn(RuntimeException::class.java, "runTimeException")
                .subscribe({ log.info("# onNext: $it") }, { log.error("# onError: $it") }, { log.info("# onCoplete") })
        }

        /**
         * onErrorResume
         * 에러가 발생했을때 대체값을 emit하는 오퍼레이터
         * onErrorReturn과 다르게 대체값을 emit하는 대신 대체값을 emit하는 Publisher를 리턴
         * switchIfEmpty는 업스트림의 결과가 없을때 대체값을 emit하는 오퍼레이터 , empty인게 없으면 무시하고 다음스트림으로 넘어감
         */
        fun onErrorResume() {
            val getBooksFromCache: (String) -> Flux<String> = { keyword: String ->
                Flux.fromIterable(listOf("book1", "book2", "book3"))
                    .filter { it.contains(keyword) }
                    .switchIfEmpty(Flux.error(IllegalArgumentException("Not found")))
            }

            val getBooksFromDb: (String) -> Flux<String> = { keyword: String ->
                Flux.fromIterable(listOf("book4", "book5", "book6"))
                    .filter { it.contains(keyword) }
                    .switchIfEmpty(Flux.error(IllegalArgumentException("Not found from Db")))
            }


            getBooksFromCache("book4")
                .doOnError { log.error("error occurred, $it") }
                .onErrorResume { e ->
                    getBooksFromDb("book6")
                }
                .subscribe({ log.info("# onNext: $it") }, { log.error("# onError: $it") }, { log.info("# onCoplete") })

        }

        /**
         * onErrorContinue
         * 에러가 발생했을 때, 에러 영역 내에 있는 데이터는 제거하고
         * upStream에서 후속 데이터를 emit 하는 방식으로 에러를 복구 할 수 있도록 함.
         * onError까지 오지 않음.
         * reactor 공식문서에서 명확하지않은 시퀀스의 동작으로 개발자가 의도하지 않은 상황을 발생시킬수 있음으로 신중히 사용하기를 권고함
         * 대부분의 에러는 Operator내부에서 doOnErorr를 통해 로그를 기록하고 onErrorResume으로 처리할수 있다고 함.
         */
        fun onErrorContinue() {
            Flux.just(1, 2, 3, 4, 0, 6, 12)
                .map { 12 / it }
                .onErrorContinue { e, v -> log.error("Error: $e, Value: $v") }
                .subscribe({ log.info("# onNext: $it") }, { log.error("# onError: $it") }, { log.info("# onComplete") })
        }


        /**
         * retry
         * publisher가 데이터를 emit하는 과정에서 에러가 발생하면 파라미터로 입력한 횟수만큼
         * 원본 FLux의 시퀀스를 다시 구독합니다.
         * 만약 파라미터로 MAX_VALUE를 입력하면 무한정 재시도.
         */
        fun onErrorRetry() {
            val count = intArrayOf(1)
            Flux.range(1, 3)
                .delayElements(Duration.ofSeconds(1))
                .map {
                    try {
                        if (it == 3 && count[0] == 1) {
                            count[0]++
                            Thread.sleep(1000)
                        }
                    } catch (InterruptedException: Exception) {
                    }
                    it
                }.timeout(Duration.ofMillis(1500))
                .retry(1)
                .subscribe({ log.info("# onNext: $it") }, { log.error("# onError: $it") }, { log.info("# onComplete") })

            Thread.sleep(7000)
        }

        fun onErrorRetry2() {
            val getBooks: () -> Flux<String> = {
                val count = intArrayOf(0)

                Flux.fromIterable(listOf("book1", "book2", "book3"))
                    .delayElements(Duration.ofMillis(500))
                    .map {
                        try {
                            count[0]++
                            if (count[0] == 3) {
                                Thread.sleep(2000)
                            }
                        } catch (InterruptedException: Exception) {
                        }
                        log.info("#getbooks map: $it")
                        it
                    }.timeout(Duration.ofSeconds(2))
                    .retry(1)
                    .doOnNext { log.info("#getbooks doOnNext: $it") }
            }

            getBooks()
                .collect(Collectors.toSet()) // 중복제거?
                .map { it }
                .subscribe(
                    { bookset -> bookset.stream().forEach { log.info("#subscribe bookname: $it") } },
                    { log.error("#onError: $it") },
                    { log.info("#onComplete") }
                )
        }

        /**
         * elapsed
         * Flux가 emit하는 데이터 사이의 시간을 측정하는 오퍼레이터
         * 측정 시간 단위는 milliseconds
         */
        fun elapsed() {
            Flux.range(1, 5)
                .delayElements(Duration.ofSeconds(1))
                .elapsed()
                .subscribe { log.info("#onNext: t1 ${it.t1}, t2: ${it.t2}}") }
        }

        /**
         * repeat + elapsed
         * repeat() 오퍼레이터는 Flux가 emit하는 데이터를 파라미터로 입력한 횟수만큼 반복하는 오퍼레이터
         */
        fun elapsed2() {
            Mono.defer { Mono.just("Hello ${kotlin.random.Random.nextInt()}").delayElement(Duration.ofMillis(300)) }
                .repeat(4) // 처음 defer에서 한번 값 생성후 4번 반복
                .elapsed()
                .map {
                    Tuples.of(it.t1, it.t2)
                }
                .subscribe { log.info("#onNext: t1 ${it.t1}, t2: ${it.t2}}") }
        }

        /**
         * window
         * Flux가 emit하는 데이터를 파라미터로 입력한 크기만큼 묶어서 Flux로 만드는 오퍼레이터
         * window() 오퍼레이터는 파라미터로 입력한 크기만큼 데이터를 분할해서 Flux로 만들어서 emit
         *
         * 사용처:
         * 1. 데이터를 일정한 크기로 묶어서 처리할때 사용
         * 2. 데이터를 일정한 크기로 묶어서 다른 스트림으로 전달할때 사용
         * ex) 쭉 들어오는데이터의 몇개씩 sum값이나 집계값을 구할때 사용
         */
        fun window() {
            Flux.range(1, 10)
                .window(3)
                .flatMap {
                    log.info("===================")
                    it
                }
                .subscribe(object : BaseSubscriber<Int>() {
                    override fun hookOnSubscribe(subscription: Subscription) {
                        subscription.request(2)
                    }

                    override fun hookOnNext(value: Int) {
                        Thread.sleep(2000)
                        log.info("#onNext: $value")
                        request(2)
                    }
                }
                )
        }


        /**
         * buffer
         * Flux가 emit하는 데이터를 파라미터로 입력한 크기만큼 묶어서 List로 만드는 오퍼레이터
         * 높은 처리량을 요구하는 애플리케이션이 잇다면 들어오는 데이터를 순차적으로
         * 처리하기 보다 batch insert같은 일괄 작업에 buffer() 오퍼레이터를 사용하면 좋다.
         */
        fun buffer() {
            Flux.range(1, 95)
                .buffer(10)
                .flatMap {
                    log.info("===================")
                    Flux.just(it)
                }
                .subscribe { log.info("#onNext: $it") }
        }

        /**
         * bufferTimeout
         * bufferTimeout() 오퍼레이터는 파라미터로 입력한 시간동안 데이터를 모아서 List로 만드는 오퍼레이터
         * 또는 maxSize나 maxTime중에서 먼저조건에 부합할 때까지 emit된 데이터를 list버퍼로 emit
         */
        fun bufferTimeout() {
            Flux.range(1, 20)
                .map {
                    if (it < 10) {
                        Thread.sleep(100)
                    } else {
                        Thread.sleep(300)
                    }

                    it
                }.bufferTimeout(3, Duration.ofMillis(400))
                .subscribe { log.info("#onNext: $it") }
        }

        /**
         * groupBy
         * groupBy() 오퍼레이터는 Flux가 emit하는 데이터를 특정 기준으로 그룹핑하는 오퍼레이터
         */
        fun groupBy() {
            Flux.fromIterable(listOf("jack" to "book1", "wei" to "book2", "jack" to "book3", "tony" to "book4"))
                .groupBy { it.first }
                .flatMap {
                    it.map { "${it.second}  + ( ${it.first} )"}.collectList()
                }
                .subscribe { group ->
                    log.info("#book by author: $group")
                }
        }

        /**
         * groupBy
         * groupby()의 두번째 파라미터 함수를 주어 downstream으로 emit할 데이터에 대한 가공처리를 미리 한다.
         */
        fun groupBy2() {
            Flux.fromIterable(listOf("jack" to "book1", "wei" to "book2", "jack" to "book3", "tony" to "book4"))
                .groupBy({it.first }, { "${it.second}  + ( ${it.first} )"})
                .flatMap {
                   it.collectList()
                }
                .subscribe { group ->
                    log.info("#book by author: $group")
                }
        }

        /**
         *   ---다수의  subscriber에게 flux를 멀티 캐스팅 하기위한 operator---
         *   subscribe가 구독하면 Upstream에서 emit된 데이터가 구독중인 모든 subscriber에게 멀티캐스팅되는데,
         *   이를 가능하게 해주는 Operator들은 cold Sequence를 hot Sequence로 동작하게 하는 특징이 있다.
         */


        /**
         * publish
         * 구독을 하더라도 구독시점에 즉시 데이터를 emit하지 않고, connect()를 호출하는 시점에 비로소 데이터를 emit
         * connect()를 호출하기 전까지는 데이터를 방출하지 않는다.
         * ConnectableFlux 를 리턴한다.
         */
        fun publish() {
            val flux: ConnectableFlux<Int> = Flux.range(1, 5).delayElements(Duration.ofMillis(300))
                                              .publish()

            Thread.sleep(500)
            flux.subscribe{ log.info("#subscriber 1: $it") }

            Thread.sleep(200)
            flux.subscribe{ log.info("#subscriber 2: $it") }

            flux.connect() // 이때 emit

            Thread.sleep(1000)
            flux.subscribe { log.info("#subscriber 3: $it") } // 구독전에 이미 방출된, 이미 지난 데이터는 받지 못하게 된다.

            Thread.sleep(2000)
        }


        /**
         * autoConnect
         * connect() 와 달리 파라미터로 입력한 숫자만큼 구독자가 생기면 자동으로 connect()를 호출
         * 그리고 connect와 달리 ConnectableFlux를 리턴하지 않고 Flux를 리턴
         */
        fun autoConnect() {
            val publisher: Flux<String> = Flux.just("A", "B", "C", "D", "E")
                .delayElements(Duration.ofMillis(300))
                .publish().autoConnect(2)  // publish 다음에 옴

            Thread.sleep(500)
            publisher.subscribe { log.info("#subscriber 1: $it") }

            Thread.sleep(500)
            publisher.subscribe { log.info("#subscriber 2: $it") }

            Thread.sleep(500)
            publisher.subscribe { log.info("#subscriber 3: $it") }

            Thread.sleep(1000)
        }


        /**
         * refCount
         * refCount는 주ㅡ로 무한 스트림 상황에서 모든 구독이 취소될 경우연결을 해제하는데 사용
         * while 문처럼 무한스트림에서 나가고 싶을때 사용하는 출구 용도?
         */
        fun refCount() {
            val publisher: Flux<Long> = Flux.interval(Duration.ofMillis(500))
                .publish().autoConnect(1) // 이렇게 publish 다음에 autoConnect 가 옴2개의 subscriber가 생기면 connect()를 호출
                .publish().refCount(1) // publish 다음에 refCount가온다.

            val disposable1:Disposable = publisher.subscribe { log.info("#subscriber 1: $it") }

            Thread.sleep(2100)
            disposable1.dispose()

            publisher.subscribe { log.info("#subscriber 2: $it") }


            Thread.sleep(2500)

            /**
             * 첫번째 구독 발생후 2.1초후 구독 해제함
             * 이시점에 모든 구독이 취소된 상태임으로 연결이 해재되고 두번째 구독이 발생할경우 updatream에 다시 연결된
             */
        }
    }
}



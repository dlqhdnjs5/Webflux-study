package com.study.tuco.reactive.part1

import com.study.tuco.reactive.part2.`14_operators`.Main
import lombok.SneakyThrows
import org.reactivestreams.Subscription
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import reactor.util.context.Context
import java.time.Duration

class Sandbox2 {
    companion object {
        val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)
        val subscriber: (String) -> Unit = { tmp -> println(tmp) }
        val subscriber2: (String) -> Unit = { tmp -> println("""${tmp} hell nah""") }
        val subscriber3: (Int) -> Unit = { println("#subscriber3 $it") }
        val subscriber4: (Int) -> Unit = { println("#subscriber4 $it") }


        @JvmStatic
        fun main(args: Array<String>) {

            // concatWith
            /*Flux.just("A", "B", "C")
                .map { """${it}1""" }
                .concatWith(Flux.just("D", "E"))
                .subscribe(subscriber)*/

            //coldAndHotSequence()



            /*Flux.range(1, 6)
                .doOnRequest { data -> println("doOnRequest2 $data") }
                .map { "$it" }
                .subscribe(subscriber)
*/


            // subscribeOnAndPublishOn()
            // test()
            //doOnNextTest()
            // syncMany_Reply()
            //syncMany_multicast()
            // schedulers()
            // context()
            // contextApi1()
            // contextApi2()
            // contextThread()
            // contextStreamFlow()
            // contextInnerSequence()
            // contextRealExample()

            //debugMod()
            // debugMod_checkPoint()

            // 내 코드
            //errorTest()
            errorTest2()
            // errorTest3()
            // errorTest4()
            // errorTest5()

            //flux_비동기적데이터방출()
            //flux_동기적데이터방출()


            Thread.sleep(10000L)
        }

        /**
         * 처음 emit한 데이터는 정상적으로 emit되지만 나머지 데이터들은 Drop 된다
         */
        fun syncOne() {
            // Sink one
            val sinkOne: Sinks.One<String> = Sinks.one()
            sinkOne.emitValue("hello Reactor", Sinks.EmitFailureHandler.FAIL_FAST)
            sinkOne.emitValue("hi Reactor", Sinks.EmitFailureHandler.FAIL_FAST)
            // EMITFAIL_FAST : 이미 값이 존재하면 에러를 발생시키고, EMIT_FAIL_NON_SERIALIZED : 이미 값이 존재하면 무시하고 다음 값을 emit
            val mono: Mono<String> = sinkOne.asMono()

            mono.subscribe{ println("# subscribe1 $it") }
            mono.subscribe{ println("# subscribe2 $it") }
        }

        /**
         * Sink.Many를 리턴하지 않고 ManySpec이라는 인터페이스를 리턴
         * Sink.Many를 리턴하지 않는 이유는 Sink.Many를 리턴하면 다른 스레드에서 데이터를 emit 할 수 있기 때문에
         *
         * unitcast: one to one : 즉 단 하나의 subscriber에게만 데이터를 emit하는것
         * multicast: one to many : 여러 subscriber에게 데이터를 emit하는것
         */
        fun syncMany_unicast() {
            // Sink many
            // unicast: one to one 방식으로 하나의 구독자에게만 데이터를 emit 안그러면 에러 발생
            val unicastSink: Sinks.Many<Int> = Sinks.many().unicast().onBackpressureBuffer()
            unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST)
            unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST)

            val fluxView: Flux<Int> = unicastSink.asFlux()

            fluxView.subscribe { println("sink asMany unicast subscribe1 $it")}

            // 구독중인데 다시 emit 하니 3을 방출하게 됨
            unicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST)
            fluxView.subscribe { println("sink asMany unicast subscribe2 $it")}// one to one 으로 여러 구독을 하게되니 에러 발생
        }

        /**
         * Sink.Many를 리턴하지 않고 ManySpec이라는 인터페이스를 리턴
         * Sink.Many를 리턴하지 않는 이유는 Sink.Many를 리턴하면 다른 스레드에서 데이터를 emit 할 수 있기 때문에
         *
         * multicast: one to many : 여러 subscriber에게 데이터를 emit하는것
         */
        fun syncMany_multicast() {
            // multicast: 여러 구독자에게 데이터를 emit 할 수 있음
            val multiCastSink: Sinks.Many<Int> = Sinks.many().multicast().onBackpressureBuffer()
            multiCastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST)
            multiCastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST)
            multiCastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST)
            multiCastSink.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST)

            val multiCastFluxView: Flux<Int> = multiCastSink.asFlux()


            multiCastFluxView.subscribe(subscriber3)
            multiCastFluxView.subscribe(subscriber4)

            multiCastSink.emitNext(5, Sinks.EmitFailureHandler.FAIL_FAST)
            multiCastSink.emitNext(6, Sinks.EmitFailureHandler.FAIL_FAST)
        }

        /**
         * 구독전에 이미 emit된 데이터가 있더라도 처음 emit된 데이터로부터 모든 데이터들이 subscriber에게 전될됨
         */
        fun syncMany_Reply() {
            val sinkManyReply: Sinks.Many<Int> = Sinks.many().replay().limit(2) //reply : 들어온데이터 마지막을 기준으로 최대 2개의 데이터를 저장하고 새로운 구독자에게 방출
            val fluxReply: Flux<Int> = sinkManyReply.asFlux()

            sinkManyReply.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST)
            sinkManyReply.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST)
            sinkManyReply.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST)


            fluxReply.subscribe(subscriber3)

            sinkManyReply.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST)

            fluxReply.subscribe(subscriber4)
        }

        fun test() {
            val second = { a: String ->
                a.toMono()
                    .map { println("second $it")

                        it}
                    .doOnNext { println("don on next") } // onNext 는 위 시퀀스 다음에 실행이 된다.

            }

            Flux.fromArray(arrayOf("a", "b", "c"))
                .subscribe {
                    println("first $it")
                    second(it).subscribe {
                        println("third $it")
                    }
                }
        }

        fun doOnNextTest() {
            Flux.fromArray(arrayOf(1, 2, 3,4,5,6))
                .doOnNext {  Thread.sleep(1000L)
                    if (it > 5) throw RuntimeException("stop") }
                .doOnNext { println("##doOnNext $it") }
                .subscribe(subscriber3)
        }

        fun backPressure() {
            // backpressure
            Flux.range(1, 6)
                .doOnRequest { data -> println("doOnRequest $data") }
                .subscribe(object : BaseSubscriber<Int>() {
                    override fun hookOnSubscribe(subscription: Subscription) {
                        request(3)
                    }

                    @SneakyThrows
                    override fun hookOnNext(value: Int) {
                        Thread.sleep(2000L)
                        println("hookOnNext $value")
                        request(3)
                    }
                })

            // 이게 뭐였지
            // backpressure Error strategy
            Flux.interval(Duration.ofMillis(1L))
                .onBackpressureError()
                .doOnNext { println("do onNext : $it") }
                .publishOn(Schedulers.parallel())
                .subscribe(
                    {
                        try {
                            Thread.sleep(5L)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    },
                    { error -> println("Error : $error") }
                )
        }

        fun coldAndHotSequence() {
            //cold sequence
            val flux2: Flux<String>  = Flux.just("korean", "japan", "chingching").map { it.toUpperCase() }
            flux2.subscribe(subscriber)
            Thread.sleep(2000L)
            flux2.subscribe(subscriber)

            // share (hot sequence)
            val flux1: Flux<String> =
                Flux.fromIterable(listOf("korea", "japan", "ching", "chang", "chong")).map { it.toUpperCase() }
                    .delayElements(
                        Duration.ofSeconds(1)
                    ).share() // share를 사용하면 hot sequence로 변경됨.
            flux1.subscribe(subscriber)

            Thread.sleep(3000L)

            flux1.subscribe(subscriber2)

            Thread.sleep(10000L)
        }

        fun subscribeOnAndPublishOn() {
            // subscribeOn
           val flux = Flux.merge(Flux.just(1,2,3,4), Flux.just(5,6,7,8))
                .doOnNext { logger.info("$it doOnNext") }
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe { logger.info("$it doOnsubscribe") }


            flux.subscribe{ logger.info("subscriber1 onNext $it")}

            // publishOn (publishOn() 을 기준으로 downstream에 실행 스레드를 변경한다)
            /*Flux.fromArray(arrayOf(1,2,3,5,7))
                .doOnNext { logger.info("$it doOnNext") }
                .doOnSubscribe { logger.info("$it doOnsubscribe") }
                .publishOn(Schedulers.parallel())
                .subscribe { logger.info("subscriber2 onNext $it")}*/

            // parallel - 물리적인 코어개수만큼 쓰레드를 사용하여 병렬처리 한다 ex) 4코어 8스레드 이면 8개의 쓰레드를 사용해서 실행
            /*Flux.fromArray(arrayOf(1,2,3,4))
                .parallel() // parallel(3) 이런식으로 쓰레드 개수를 지정할 수 있음
                .runOn(Schedulers.parallel()) // -> parallel()을 사용하면 runOn을 사용해야함, parallel()은 cpu의 논리적인 코어수에 맞게 '사전에' 골고루 분배하는 역할만하고 실제로 병렬 작업은 runOn()이 담당.
                .subscribe { logger.info("subscribe $it") }*/

            // no subscribeOn and publishOn
            // 1건씩 업스트림 처리후 방출시에 doOnNExt 를 실행, 다음에 또 한건식 다운스트림으로 넘김 근데 내려가는데 순서 없음.
            /*Flux.fromArray(arrayOf(1, 3, 5, 7))
                .subscribeOn(Schedulers.boundedElastic()) // subscribe 순간부터 쓰레드가 변경됨.
                .doOnNext { logger.info("# doOnNext fromArray $it") }
                .filter { it > 3 }
                .doOnNext { logger.info("# doOnNext filter $it") }
                .publishOn(Schedulers.parallel()) // publish 다음으로 메인쓰레드가 paraller로 변경됨
                .map { it * 10 }
                .doOnNext { logger.info("# doOnNext map $it") }
                .subscribe { logger.info("onNext: $it") }*/
        }

        fun schedulers() {
            // immediate - 현재 쓰레드를 유지하는것, 공통 API등에서 별도의 쓰레드를 추가 할당하고 싶지 않을때 사용.
            Flux.fromArray(arrayOf(1, 3, 5, 7))
                .publishOn(Schedulers.parallel())
                .filter { it > 3 }
                .doOnNext { logger.info("# doOnNext filter $it") }
                .publishOn(Schedulers.immediate())
                .map { it * 10 }
                .doOnNext { logger.info("# doOnNext map $it") }
                .subscribe { logger.info("onNext: $it") }

            // single - 단일 쓰레드를 사용하는 스케줄러, 병렬처리가 필요없는 작업에 사용
            val task =  { taskName: String ->  Flux.fromArray(arrayOf(1, 3, 5, 7))
                .publishOn(Schedulers.single())
                .filter { it > 3 }
                .doOnNext { logger.info("# doOnNext filter $it") }
                .map { it * 10 }
                .doOnNext { logger.info("#taskName: $taskName # doOnNext map $it") }
            }

            task("task1").subscribe { logger.info("onNext: $it") } // 두작업을 하지만 하나의 쓰레드에서만 작동함.
            task("task2").subscribe { logger.info("onNext: $it") }


            // newSingle - 새로운 단일 쓰레드를 사용하는 스케줄러
            val newSingleTask =  { taskName: String ->  Flux.fromArray(arrayOf(1, 3, 5, 7))
                .publishOn(Schedulers.newSingle("newSingleTask", true))
                .filter { it > 3 }
                .doOnNext { logger.info("# doOnNext filter $it") }
                .map { it * 10 }
                .doOnNext { logger.info("#taskName: $taskName # doOnNext map $it") }
            }

            newSingleTask("task1").subscribe { logger.info("onNext: $it") } // 메서드를 호출할때마다 새로운 쓰레드가 생성됨.
            newSingleTask("task2").subscribe { logger.info("onNext: $it") }


            // Scheduler.boundedElastic() - ExecutorService 기반의 쓰레드 풀 을 생성한 후, 그안에서 정해진 수만큼의 스레드를 사용하여 작업 처리후 반납하여 재사용.
            // 기본적으로 CPU 코어수 * 10 만큼의 스레드를 생성하고, 60초간 아무작업이 없으면 스레드를 종료함.
            // 최대 10만개 의 작업이 큐에서 대기 가능
            // 주로 Blocking I/O 작업에 사용, 실행 시간이 긴 Blocking I/O 작업이 포함된경우 다른 스레드에 영향주지 않도록 전용 쓰레드를 할당해서 Blocking I/O 작업을 처리함.
        }

        /**
         * context
         *  Operator 같은 reactor 구성요소 간에 전파되는 key/value 형태의 저장소
         *  Reactor의 Context는 ThreadLocal과 다소 유사한 면이 있지만, 각각의실행 스레드와 매핑되는 threadLocal과 달리
         *  실행 스레드와 매핑되는 것이 아니라 Subscriber와 매핑됨
         *
         *  실행순서
         *  1. subscribe 발생시 contextWrite()를 통해 context를 변경
         *  2. transformDeferredContextual()을 통해 context를 가져와 처리
         *  3. deferContextual를 통해 context를 가져와 처리
         *  4. 그후 subscribeOn을 통한 스트림 처리 실행
         */
        fun context() {
            Mono.deferContextual { ctx ->
                logger.info("# deferContextual $ctx")
                Mono.just("Hello " + ctx.get("firstName"))
                    .doOnNext { logger.info("# just doOnNext $it") }
            }.subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel())
                .transformDeferredContextual { mono, ctx ->
                    logger.info("# transformDeferredContextual $mono $ctx")
                    mono.map { "$it ${ctx.get<String>("lastName")}"}
                }
                .contextWrite { context ->
                    logger.info("# contextWrite $context")
                    context.put("lastName", "jobs")
                }
                .contextWrite {
                    context ->
                    logger.info("# contextWrite2 $context")
                    context.put("firstName", "steve")
                }
                .subscribe { logger.info("# onNext $it") }  // context의 생명주기는 subscriber의 생명주기와 동일함. 즉 subscriber가 종료되면 context도 종료된다.
                                                            // 두 쓰레드는 subscriber에서 사용되는 값에 영향을 주지 않음.
        }

        // 아래부터 시작해서contextWrite 부터 실행이 된다.
        fun contextApi1() {
            val key1 = "company"
            val key2 = "firstName"
            val key3 = "lastName"

            Mono.deferContextual { ctx ->
                Mono.just("${ctx.get<String>(key1)} , ${ctx.get<String>(key2)} , ${ctx.get<String>(key3)}")
            }
                .publishOn(Schedulers.parallel())
                .contextWrite { context ->
                    logger.info("# contextWrite1 $context")
                    context.putAll(Context.of(key2, "steave", key3, "Jobs").readOnly())
                }
                .contextWrite { context ->
                    logger.info("# contextWrite2 $context")
                    context.put(key1, "Apple")
                }
                .transformDeferredContextual { mono, ctx -> // contextWrite 보다 위에 존재해야 contextWrite에 의해 변경된 context를 가져올 수 있음
                    logger.info("# transformDeferredContextual $mono $ctx")
                    mono
                }
                .subscribe {
                    logger.info("# onNext $it")
                }


            Mono.deferContextual { ctx ->
                Mono.just(
                    "${ctx.get<String>(key1)} , ${
                        ctx.getOrEmpty<String>(key2).orElse("no firstName")
                    } ${ctx.getOrDefault<String>(key3, "no lastName")}"
                )
            }.publishOn(Schedulers.parallel())
                .contextWrite { context -> context.put(key1, "Apple") }
                .subscribe { logger.info("# onNext ${it}") }
        }

        fun contextApi2() {
            val key1 = "company"
            val key2 = "firstName"
            val key3 = "lastName"

            Mono.deferContextual { ctx ->
                Mono.just(
                    "${ctx.get<String>(key1)} , ${
                        ctx.getOrEmpty<String>(key2).orElse("no firstName")
                    } ${ctx.getOrDefault<String>(key3, "no lastName")}"
                )
            }.publishOn(Schedulers.parallel())
                .contextWrite { context -> context.put(key1, "Apple") }
                .subscribe { logger.info("# onNext ${it}") }
        }

        /**
         * context는 구독별로 연결되는 특징이 있기 때문에 구독이 발생할 때마다 해당하는 하나의 Context가 하나의 구독에 연결된다.
         */
        fun contextThread() {
            val key1 = "company"

            val mono: Mono<String> = Mono.deferContextual { ctx ->
                Mono.just(ctx.get<String>(key1))
            }.publishOn(Schedulers.parallel())


            mono.contextWrite { context -> context.put(key1, "Apple") }
                .subscribe { logger.info("# subscribe1 onNext $it") }

            mono.contextWrite { context -> context.put(key1, "Microsoft") }
                .subscribe { logger.info("# subscribe2 onNext $it") }
        }

        fun contextStreamFlow() {
            val key1 = "company"
            val key2 = "name"

            Mono.deferContextual { ctx ->
                Mono.just(ctx.get<String>(key1))
            }.publishOn(Schedulers.parallel())
                .contextWrite { context -> context.put(key2, "Bill") }
                .transformDeferredContextual { mono, context ->
                    mono.map { "$it , ${context.getOrDefault(key2, "Steve")}" }
                }
                .contextWrite { context -> context.put(key1, "Apple") }
                .subscribe { logger.info("# onNext $it") }
        }

        fun contextInnerSequence() {
            val key1 = "company"

            Mono.just("Steve")
              //.transformDeferredContextual { stringMono, contextView ->
              //    contextView.get("role")
              //}
                .flatMap { name ->
                    Mono.deferContextual { outerContext ->
                        Mono.just( "${outerContext.get<String>(key1)} $name")
                            .transformDeferredContextual { mono, innerContext ->
                                mono.map { data -> "$data , ${innerContext.get<String>("role")}" }
                            }
                            .contextWrite { innerContext -> innerContext.put("role", "CEO") }
                    }
                }.publishOn(Schedulers.parallel())
                .contextWrite { outerContext -> outerContext.put(key1, "Apple") }
                .subscribe { logger.info("# onNext $it") }
        }

        fun contextRealExample() {
            data class Book(val isbn: String, val bookName: String, val author: String) {}

            val HEADER_AUTH_TOKEN = "authToken"


            val postBook: (Mono<Book>) -> Mono<String>  = { book: Mono<Book> ->
                Mono.zip(book,
                    Mono.deferContextual { context -> Mono.just(context.get<String>(HEADER_AUTH_TOKEN)) }
                ).flatMap { tuple ->
                    val response = "POST the book(${tuple.t1.bookName} , ${tuple.t1.author}) with token ${tuple.t2}"
                    Mono.just(response)
                }
            }

            val mainMono: Mono<String> = postBook(Mono.just(Book("1234", "Reactor", "bowon")))
                .contextWrite { context -> Context.of(HEADER_AUTH_TOKEN, "eyhfskdvnos091") }

            mainMono.subscribe { logger.info("# onNext $it")}
        }

        fun debugMod() {
            Hooks.onOperatorDebug()

            val fruits: Map<String, String> = mapOf("apple" to "사과", "banana" to "바나나", "grape" to "포도", "melon" to "멜론")


            Flux.fromArray(arrayOf("APPLES", "BANANAS", "GRAPES", "PEARS"))
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel())
                .map { it.lowercase() }
                .map { it.substring(0, it.length -1 ) }
                .map { fruits[it] }
                .map { "맛있는 $it" }
                .subscribe ({ logger.info("#onNext $it")}, { logger.error("#onError $it") })
        }

        fun debugMod_checkPoint() {
            Flux.just(2, 4, 6, 8)
                .zipWith(Flux.just(1, 2, 3, 0)) { x, y -> x / y }
                .map { num -> num + 2 }
                .checkpoint()
                .subscribe(
                    { logger.info("#onNExt $it")},
                    { logger.error("#onError $it")}
                )
        }

        fun errorTest() {
            val subTask = { parameter:Int  -> Flux.just(parameter)
                .map { if (it == 3) throw RuntimeException("Error") else it }
                .map {
                    logger.info("int: $it")
                    it
                }

                //.doOnError { logger.error("doOnError $it") }
                /*.onErrorResume {
                    logger.error("에러 발생 ${it.message}")
                    Mono.error {  RuntimeException("에러 발생") }
                }*/
                .subscribe { logger.info("onNext $it") }
            }


            Flux.just(1,2,3,4)
                .map { subTask(it) // 3은 subtask내에서 에러를 불러일으키지만 계속 main stream은 진행됨
                it}
                .map {
                    logger.info("main int: $it")
                    it
                }
                .onErrorResume { logger.error("~!~!!~parents doOnError $it")
                    Flux.error(it)
                }
                .subscribe(subscriber3)
        }

        /**
         * 하위 스트림에서 독립적으로 subscribe을 하면 에러를 방출해도 상위 스트림에서는 에러를 처리하지 못하고 상위에선 다음 스트림을 처리함.
         * 그렇게 되면 하위 스트림에서 에러가 발생하면 상위 스트림에서는 에러를 처리하지 못하고 다음 스트림을 처리하게 됨.
         * 따라서 하나의 스트림으로 만들어주는게 중요.
         */
        fun errorTest2() {
            val subTask = { parameter: Int ->
                Flux.just(parameter)
                    .map {
                        if (it == 3) throw RuntimeException("Error")
                        else it
                    }
                    .map {
                        logger.info("int: $it")
                        it
                    }
                    .map { it * 2 }
                    .doOnError {
                        logger.error("하위 doOnError $it")
                    }
            }

            Flux.just(1, 2, 3, 4)
                .flatMap { subTask(it) } // subTask의 Flux를 상위 스트림과 결합
                .doOnError { logger.error("상위 스트림에서 에러 처리: ${it.message}") }
                .onErrorResume {
                    logger.info("대체 스트림으로 전환합니다.")
                    Flux.just(10, 20, 30)
                }
                .subscribe(
                    { logger.info("onNext $it") },
                    { logger.error("최종 에러 처리: ${it.message}") },
                    { logger.info("스트림 완료") }
                )
        }

        fun errorTest3() {
            Flux.just(1,2,3,4,5)
                .map {
                    if (it == 3) throw RuntimeException("Error")
                    else it
                }
                .map {
                    logger.info("int: $it")
                    it
                }
                .map { it * 2 }
                .doOnError {
                    logger.error("하위 doOnError ${it.message}")
                }.subscribe()
        }

        fun errorTest4() {
            Flux.range(1, 6)
            .map { if (it == 3) throw RuntimeException("Error") else it }
                .map { logger.info("#next $it")
                it}
                .delayElements(Duration.ofSeconds(3))
                .doOnNext { println("doOnNext $it") }
                .onErrorResume { logger.error("error $it", it)
                    Flux.empty()
                }
                .subscribe(subscriber3)
        }

        fun errorTest5() {
            Mono.just(1)
                .map { it + 1 }
                .map {
                    logger.info("what")
                    throw Exception("error")
                 it }
                .map { it + 1 }
                .onErrorResume {
                    Mono.empty() }
                .subscribe()
        }

        /**
         * flux를 하면 다음 데이터 올때까지 기다리지 않고 바로바로 다음 데이터를 처리함
         */
        fun flux_비동기적데이터방출() {
            Flux.just(1, 2, 3, 4)
                .flatMap { number ->
                    if (number == 2) {
                        Mono.just(number)
                            .delayElement(Duration.ofSeconds(3)) // 2인 경우 3초 지연
                    } else {
                        Mono.just(number)
                    }
                }
                .flatMap {
                    logger.info("# flatMap: $it")
                    Mono.just(it)
                }
                .flatMap {
                    if (it == 2) {
                        Mono.just(it)
                            .delayElement(Duration.ofSeconds(3))
                    } else {
                        Mono.just(it)
                    }
                }
                .subscribe { logger.info("# onNext: $it") }

            Thread.sleep(6000)
        }

        fun flux_동기적데이터방출() {
            Flux.just(1, 2, 3, 4)
                .map { number ->
                    if (number == 2) {
                        Thread.sleep(3000)
                        number

                    } else {
                        number
                    }
                }
                .subscribe { Main.log.info("# onNext: $it") }

            Thread.sleep(6000)
        }
    }
}
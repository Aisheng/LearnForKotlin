import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object RxJava {

    fun today(): Flowable<String> {
        return Flowable.create({
            it.onNext("Today")
            it.onNext("is")
            it.onNext(SimpleDateFormat("yyyy-MM-dd").format(Date()))
            it.onNext("a nice day")
            it.onComplete()
        }, BackpressureStrategy.BUFFER)
    }

    fun numbers(): Flowable<Int> {
        return Flowable.range(1, 10)
    }

    fun flatMap() {
        today().flatMap { Flowable.just(it.length) }
                .map { it.plus(100) }
                .doOnSubscribe { println("doOnSubscribe") }
                .subscribe {
                    println(it)
                }
    }

    fun interval(): Flowable<Long> {
        return Flowable.intervalRange(1, 10, 0, 300, TimeUnit.MILLISECONDS)
    }

    fun interval2(): Flowable<Long> {
        return Flowable.intervalRange(100, 10, 1000, 300, TimeUnit.MILLISECONDS)
    }

    fun merge() {
        Flowable.merge(interval(), interval2())
                .map {
                    if (it == 104L) {
                        return@map Throwable()
                    } else {
                        return@map it
                    }
                }
                .doOnComplete { println("doOnComplete") }
                .onErrorReturn { 99 }
                .subscribe { println(it) }
    }

    fun thread() {
        Flowable.create<Int>({
            println(Thread.currentThread().name)
            it.onNext(1)
            it.onComplete()
        }, BackpressureStrategy.BUFFER)
                .map { it.times(100) }
                .subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnNext {
                    println(it)
                    println(Thread.currentThread().name)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    println(Thread.currentThread().name)
                    println(it)
                }
    }

    fun zip() {
        Flowable.zip(today(), today(), BiFunction<String, String, String> { t1, t2 -> return@BiFunction t1 })
    }

    fun retry() {
        today()
                .doOnNext {
                    if (it.length == 2) {
                        throw Throwable()
                    }
                }
                .retryWhen {
                    // 重试10次，每次时间延迟时间增加500毫秒
                    Flowable.zip(Flowable.range(1, 10), it, BiFunction<Int, Throwable, Int> { t1, _ -> t1 })
                            .flatMap { Flowable.timer(500.times(it.toLong()), TimeUnit.MILLISECONDS) }
                }
                .doOnError {
                    println("onError")
                }
                .onErrorReturnItem("it a mistake item")
                .doOnComplete { println("it is complete") }
                .subscribe { println(it) }
    }


    fun repeat() {
        today()
                .doOnNext { println("onNext") }
                .doOnComplete { println("onComplete") }
                .repeatWhen {
                    //  无论什么情况，只要调用除了 it 的 onError 和 onComplete 之外，today将被重新订阅
                    //  用作轮询等操作
                    return@repeatWhen it.flatMap { return@flatMap Flowable.just(1) }
                            .delay(1000, TimeUnit.MILLISECONDS)
                            .doOnNext { println(it) }
                }
                .doOnSubscribe { println("onSubscribe") }
                .subscribe { println(it) }
    }

    fun timer() {
        Flowable.timer(100, TimeUnit.MILLISECONDS)
                .subscribe { println(it) }
    }


}
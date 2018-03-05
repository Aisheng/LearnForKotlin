import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
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


}
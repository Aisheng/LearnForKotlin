import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object RxJava {

    fun today(): Flowable<String> {
        return Flowable.create({
            println(Thread.currentThread().name)
            it.onNext("Today")
            it.onNext("is")
            it.onNext(SimpleDateFormat("yyyy-MM-dd").format(Date()))
            it.onNext("a nice day")
        }, BackpressureStrategy.ERROR)
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
                    println(it)
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
                    it.printStackTrace()
                }
                .onErrorReturnItem("it a mistake item")
                .doOnComplete { println("it is complete") }
                .subscribe { println(it) }
    }


    fun repeat() {
        today()
                .doOnNext { }
                .doOnComplete { println("onComplete") }
                .repeatWhen {
                    //  无论什么情况，只要调用除了 onError 和 onComplete 之外，today将被重新订阅
                    //  用作轮询等操作
                    println("repeatWhen")
                    return@repeatWhen it.flatMap { return@flatMap Flowable.just(1) }
                            .delay(1000, TimeUnit.MILLISECONDS)
                    // timer 在调用 onNext 之后会发送一个 onComplete 事件，所以只能重复一次
//                    return@repeatWhen Flowable.timer(1000, TimeUnit.MILLISECONDS)
                }
                .doOnSubscribe { println("onSubscribe") }
                .subscribe { println(it) }
    }

    fun timer() {
        Flowable.timer(100, TimeUnit.MILLISECONDS)   //timer 除了发送onNext 后面还有一个onComplete
                .doOnNext { println("onNext") }
                .doOnComplete { println("onComplete") }
                .subscribe { println(it) }
    }


    fun delay() {
        today()
                .delay(500, TimeUnit.MILLISECONDS)
                .doOnNext { println("onNext") }
                .doOnComplete { println("onComplete") }   //如果 source 不发送onComplete事件，delay 不会发送（只是延迟）
                .subscribe { println(it) }

    }

    fun throttle() {
        Flowable.intervalRange(1, 100, 0, 100, TimeUnit.MILLISECONDS)
//                .throttleFirst(500,TimeUnit.MILLISECONDS)  //500毫秒内到达的第一个事件
                .debounce(50, TimeUnit.MILLISECONDS)  //收到事件50毫秒之后没有再次收到，发送该事件，界面操作时，更新时机的问题
                .subscribe { println(it) }
    }

    fun single() {
        Single.just(1).subscribe()
    }

    fun subject() {

        val subject: PublishSubject<Long> = PublishSubject.create()

        //可以调用Subject 的onNext ，也可以做为Observable 的 Observer
        Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                //.doOnNext { subject.onNext(it) }
                .subscribe(subject)

        subject.subscribe { println(it) }
    }

    fun sample() {
        Observable.create(ObservableOnSubscribe<Int> {
            var i = 0
            while (i < 5) {
                println(Thread.currentThread().name)
                it.onNext(i++)
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .sample(2, TimeUnit.SECONDS)   //每隔两秒钟取样
                .filter {
                    println(Thread.currentThread().name)
                    it.rem(2) == 0
                }
                .observeOn(Schedulers.computation())
                .subscribe {
                    println(Thread.currentThread().name)
                    println(it)
                }
    }

    fun concat() {
        Flowable.concat(today(), interval2())
                .subscribe { println(it) }
    }

    fun test() {
        var mSubscription: Subscription? = null
        Flowable.create<String>({
            try {
                val file = File("D:\\github\\RxjavaForKotlin\\青花瓷.txt")
                if (file.isFile && file.exists()) {
                    val inputStreamReader = InputStreamReader(FileInputStream(file), "gbk")
                    val bufferReader = BufferedReader(inputStreamReader)
                    var str = bufferReader.readLine()
                    while (str != null && !it.isCancelled) {    //打印歌词，下游 request 一个就发送一个
                        while (it.requested() == 0.toLong()) {
                            if (it.isCancelled) {
                                break
                            }
                        }
                        it.onNext(str)
                        str = bufferReader.readLine()
                    }

                    bufferReader.close()
                    inputStreamReader.close()
                    it.onComplete()
                }
            } catch (e: Exception) {
                it.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(object : Subscriber<String> {

                    override fun onSubscribe(s: Subscription?) {
                        mSubscription = s
                        mSubscription?.request(1)
                    }

                    override fun onError(t: Throwable?) {

                    }

                    override fun onComplete() {
                        println("over")
                    }

                    override fun onNext(t: String?) {   //打印一个之后，隔一秒请求一个
                        println(t)
                        Thread.sleep(1000)
                        mSubscription?.request(1)
                    }
                })
    }
}
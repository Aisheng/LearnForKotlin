/**
 * Created by Lidaisheng on 2018/3/17.
 * <br>Email:lidaisheng7142@163.com</br>
 */
object StandardBlock {


    var filed = 1

    fun runTest() {
        filed.run {

        }
    }

    fun letTest() {
        let {
            it.filed = 3
        }
    }

    fun applyTest() {
        apply {
            this@StandardBlock
        }
    }

    fun alsoTest() {
        also {

        }
    }

    fun withTest() {
        with(filed) {

        }
    }
}
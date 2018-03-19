
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Lambda {

    private List<Integer> numbers = new ArrayList<>();

    public Lambda() {
        for (int i = 0; i < 10; i++) {
            numbers.add(i * i);
        }
    }

    /**
     * 从 number 中挑出比 K 大的值
     */
    public void test(int k) {
        numbers.stream()
                .filter(function.apply(k).apply(k))
                .forEach(print);

//        numbers.stream().filter(integer -> integer > k)
//                .forEach(System.out::println);
    }


    private Consumer<Integer> print = System.out::println;

    private Predicate<Integer> above30 = i -> i > 30;

    /**
     * 级联 Lambda 表达式 和 selector(int k) 等价
     */
    private Function<Integer, Predicate<Integer>> select = integer -> number -> number > integer;

    /**
     * 乱写一通
     */
    private Function<Integer, Function<Integer, Predicate<Integer>>> function = integer -> k -> number -> number > k + integer;

    private Predicate<Integer> selector(int k) {
        return integer -> integer > k;
    }
}

/**
 * @description:
 * @author: jasper
 * @create: 2021-01-06 15:56
 */
public class Test {
    public static void main(String[] args) {
        String x = new String("ab");
        change(x);
        System.out.println(x);
    }

    public static void change(String x) {
        x = "cd";
    }
}

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: jasper
 * @create: 2020-12-25 11:33
 */
public class Test {
    public static void main(String[] args) {
        Map<String,String> m = new HashMap<>();
        m.put("a","a");
        String a = m.get("a");
        m.remove("a");
        System.out.println(a);
    }
}

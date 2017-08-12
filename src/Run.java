import java.util.concurrent.ConcurrentHashMap;

public class Run {
    public static void main(String[] args) {
        ConcurrentHashMap<String,Integer> map = new ConcurrentHashMap<>();
        map.put("Test",1);
        System.out.println(map.containsKey("Test"));
    }
}

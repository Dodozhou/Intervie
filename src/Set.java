import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author youzeliang
 * on 2018/3/5
 */
public class Set {
    public static void main(String[] args) {
        List list=new ArrayList();
        list.add("a");
        list.add("b");
        list.add("a");

        HashSet hashSet = new HashSet();
        hashSet.add("fss");
        hashSet.add("fss");
        System.out.println(hashSet);
    }
}

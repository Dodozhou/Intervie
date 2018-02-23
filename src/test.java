import java.util.ArrayList;

/**
 * Created by youzeliang on 2017/12/15.
 */
public class test {



    //面试好问题，String 类为什么要设计为final

    // 为了安全，
    String name = "1";

    public static void main(String[] args) {

        ArrayList arrayList = new ArrayList();

    }

    void talk() {
        System.out.println("我叫" + this.name);
    }


}

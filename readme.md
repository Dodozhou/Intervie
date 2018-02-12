###最开始的1.0版本是[朋友](https://github.com/llysrv)整理的，后面去了大厂然后就没整理了。欢迎大家follow he



### Mybatis

2. Xml映射文件中，除了常见的select|insert|updae|delete标签之外，还有哪些标签？
    * 还有很多其他的标签，<resultMap>、<parameterMap>、<sql>、<include>、<selectKey>，加上动态sql的9个标签，trim|where|set|foreach|if|choose|when|otherwise|bind等，其中<sql>为sql片段标签，通过<include>标签引入sql片段，<selectKey>为不支持自增的主键生成策略标签。
3. 最佳实践中，通常一个Xml映射文件，都会写一个Dao接口与之对应，请问，这个Dao接口的工作原理是什么？Dao接口里的方法，参数不同时，方法能重载吗？
    * Dao接口，就是人们常说的Mapper接口，接口的全限名，就是映射文件中的namespace的值，接口的方法名，就是映射文件中MappedStatement的id值，接口方法内的参数，就是传递给sql的参数。Mapper接口是没有实现类的，当调用接口方法时，接口全限名+方法名拼接字符串作为key值，可唯一定位一个MappedStatement，举例：com.mybatis3.mappers.StudentDao.findStudentById，可以唯一找到namespace为com.mybatis3.mappers.StudentDao下面id = findStudentById的MappedStatement。在Mybatis中，每一个<select>、<insert>、<update>、<delete>标签，都会被解析为一个MappedStatement对象。
    * Dao接口里的方法，是不能重载的，因为是全限名+方法名的保存和寻找策略。
    * Dao接口的工作原理是JDK动态代理，Mybatis运行时会使用JDK动态代理为Dao接口生成代理proxy对象，代理对象proxy会拦截接口方法，转而执行MappedStatement所代表的sql，然后将sql执行结果返回。



8. Mybatis是如何将sql执行结果封装为目标对象并返回的？都有哪些映射形式？
    * 答：第一种是使用<resultMap>标签，逐一定义列名和对象属性名之间的映射关系。第二种是使用sql列的别名功能，将列别名书写为对象属性名，比如T_NAME AS NAME，对象属性名一般是name，小写，但是列名不区分大小写，Mybatis会忽略列名大小写，智能找到与之对应对象属性名，你甚至可以写成T_NAME AS NaMe，Mybatis一样可以正常工作。
    * 有了列名与属性名的映射关系后，Mybatis通过反射创建对象，同时使用反射给对象的属性逐一赋值并返回，那些找不到映射关系的属性，是无法完成赋值的。
9. Mybatis能执行一对一、一对多的关联查询吗？都有哪些实现方式，以及它们之间的区别。
    * 答：能，Mybatis不仅可以执行一对一、一对多的关联查询，还可以执行多对一，多对多的关联查询，多对一查询，其实就是一对一查询，只需要把selectOne()修改为selectList()即可；多对多查询，其实就是一对多查询，只需要把selectOne()修改为selectList()即可。
    * 关联对象查询，有两种实现方式，一种是单独发送一个sql去查询关联对象，赋给主对象，然后返回主对象。另一种是使用嵌套查询，嵌套查询的含义为使用join查询，一部分列是A对象的属性值，另外一部分列是关联对象B的属性值，好处是只发一个sql查询，就可以把主对象和其关联对象查出来。
    * 那么问题来了，join查询出来100条记录，如何确定主对象是5个，而不是100个？其去重复的原理是<resultMap>标签内的<id>子标签，指定了唯一确定一条记录的id列，Mybatis根据<id>列值来完成100条记录的去重复功能，<id>可以有多个，代表了联合主键的语意。
    * 同样主对象的关联对象，也是根据这个原理去重复的，尽管一般情况下，只有主对象会有重复记录，关联对象一般不会重复。
    * 举例：下面join查询出来6条记录，一、二列是Teacher对象列，第三列为Student对象列，Mybatis去重复处理后，结果为1个老师6个学生，而不是6个老师6个学生。
```sql
       t_id    t_name           s_id
|          1 | teacher      |      38 |
|          1 | teacher      |      39 |
|          1 | teacher      |      40 |
|          1 | teacher      |      41 |
|          1 | teacher      |      42 |
|          1 | teacher      |      43 |
```
10. Mybatis是否支持延迟加载？如果支持，它的实现原理是什么？
    * 答：Mybatis仅支持association关联对象和collection关联集合对象的延迟加载，association指的就是一对一，collection指的就是一对多查询。在Mybatis配置文件中，可以配置是否启用延迟加载lazyLoadingEnabled=true|false。
    * 它的原理是，使用CGLIB创建目标对象的代理对象，当调用目标方法时，进入拦截器方法，比如调用a.getB().getName()，拦截器invoke()方法发现a.getB()是null值，那么就会单独发送事先保存好的查询关联B对象的sql，把B查询上来，然后调用a.setB(b)，于是a的对象b属性就有值了，接着完成a.getB().getName()方法的调用。这就是延迟加载的基本原理。
    * 当然了，不光是Mybatis，几乎所有的包括Hibernate，支持延迟加载的原理都是一样的。
11. Mybatis的Xml映射文件中，不同的Xml映射文件，id是否可以重复？
    * 答：不同的Xml映射文件，如果配置了namespace，那么id可以重复；如果没有配置namespace，那么id不能重复；毕竟namespace不是必须的，只是最佳实践而已。
    * 原因就是namespace+id是作为Map<String, MappedStatement>的key使用的，如果没有namespace，就剩下id，那么，id重复会导致数据互相覆盖。有了namespace，自然id就可以重复，namespace不同，namespace+id自然也就不同。
12. Mybatis中如何执行批处理？
    * 答：使用BatchExecutor完成批处理。
13. Mybatis都有哪些Executor执行器？它们之间的区别是什么？
    * 答：Mybatis有三种基本的Executor执行器，SimpleExecutor、ReuseExecutor、BatchExecutor。
    * SimpleExecutor：每执行一次update或select，就开启一个Statement对象，用完立刻关闭Statement对象。
    * ReuseExecutor：执行update或select，以sql作为key查找Statement对象，存在就使用，不存在就创建，用完后，不关闭Statement对象，而是放置于Map<String, Statement>内，供下一次使用。简言之，就是重复使用Statement对象。
    * BatchExecutor：执行update（没有select，JDBC批处理不支持select），将所有sql都添加到批处理中（addBatch()），等待统一执行（executeBatch()），它缓存了多个Statement对象，每个Statement对象都是addBatch()完毕后，等待逐一执行executeBatch()批处理。与JDBC批处理相同。
    * 作用范围：Executor的这些特点，都严格限制在SqlSession生命周期范围内。
14. Mybatis中如何指定使用哪一种Executor执行器？
    * 答：在Mybatis配置文件中，可以指定默认的ExecutorType执行器类型，也可以手动给DefaultSqlSessionFactory的创建SqlSession的方法传递ExecutorType类型参数。
15. Mybatis是否可以映射Enum枚举类？
    * 答：Mybatis可以映射枚举类，不单可以映射枚举类，Mybatis可以映射任何对象到表的一列上。映射方式为自定义一个TypeHandler，实现TypeHandler的setParameter()和getResult()接口方法。TypeHandler有两个作用，一是完成从javaType至jdbcType的转换，二是完成jdbcType至javaType的转换，体现为setParameter()和getResult()两个方法，分别代表设置sql问号占位符参数和获取列查询结果。
16. Mybatis映射文件中，如果A标签通过include引用了B标签的内容，请问，B标签能否定义在A标签的后面，还是说必须定义在A标签的前面？
    * 答：虽然Mybatis解析Xml映射文件是按照顺序解析的，但是，被引用的B标签依然可以定义在任何地方，Mybatis都可以正确识别。
    * 原理是，Mybatis解析A标签，发现A标签引用了B标签，但是B标签尚未解析到，尚不存在，此时，Mybatis会将A标签标记为未解析状态，然后继续解析余下的标签，包含B标签，待所有标签解析完毕，Mybatis会重新解析那些被标记为未解析的标签，此时再解析A标签时，B标签已经存在，A标签也就可以正常解析完成了。
17. 简述Mybatis的Xml映射文件和Mybatis内部数据结构之间的映射关系？
    * 答：Mybatis将所有Xml配置信息都封装到All-In-One重量级对象Configuration内部。在Xml映射文件中，<parameterMap>标签会被解析为ParameterMap对象，其每个子元素会被解析为ParameterMapping对象。<resultMap>标签会被解析为ResultMap对象，其每个子元素会被解析为ResultMapping对象。每一个<select>、<insert>、<update>、<delete>标签均会被解析为MappedStatement对象，标签内的sql会被解析为BoundSql对象。
18. 为什么说Mybatis是半自动ORM映射工具？它与全自动的区别在哪里？
    * 答：Hibernate属于全自动ORM映射工具，使用Hibernate查询关联对象或者关联集合对象时，可以根据对象关系模型直接获取，所以它是全自动的。而Mybatis在查询关联对象或关联集合对象时，需要手动编写sql来完成，所以，称之为半自动ORM映射工具。

    
# 计算机网络

2. osi七层  
    ![osi](https://raw.githubusercontent.com/llysrv/Interview/master/img/OSI.gif)

3. TCP  
三次握手  
![三次握手](https://raw.githubusercontent.com/llysrv/Interview/master/img/%E4%B8%89%E6%AC%A1%E6%8F%A1%E6%89%8B.png)  
四次断开  
因为之前存在没有传送完的数据，所以要等待。  
![四次断开](https://raw.githubusercontent.com/llysrv/Interview/master/img/%E5%9B%9B%E6%AC%A1%E6%8C%A5%E6%89%8B.jpg)  
小故事  
三次握手：  
    * A：喂，你听得到吗？  
    * B：我听得到呀，你听得到我吗？  
    * A：我能听到你，balabala……  
四次挥手：  
    * A：喂，我不说了。
    * B：我知道了。等下，上一句还没说完。balabala…..  
    * B：好了，说完了，我也不说了。  
    * A：我知道了。  
    * A等待2MSL,保证B收到了消息,否则重说一次”我知道了”(如果没收到将进行重传)  
4. TCP与UDP之间的区别
    1. 基于连接vs无连接  
    TCP是面向连接的协议，而UDP是无连接的协议。
    2. 可靠性不同  
        * TCP提供交付保证，这意味着一个使用TCP协议发送的消息是保证交付给客户端的。
        * UDP是不可靠的，它不提供任何交付的保证。一个数据报包在运输途中可能会丢失。
    3. 有序性
        * TCP保证了消息的有序性，该消息将以从服务器端发出的同样的顺序发送到客户端，
        * UDP不提供任何有序性或序列性的保证，数据包将以任何可能的顺序到达。
    4. 数据边界  
        TCP不保存数据的边界，而UDP保证。
        在传输控制协议，数据以字节流的形式发送，
        并没有明显的标志表明传输信号消息（段）的边界。
        在UDP中，数据包单独发送的，只有当他们到达时，才会再次集成。包有明确的界限来哪些包已经收到，这意味着在消息发送后，在接收器接口将会有一个读操作，来生成一个完整的消息。虽然TCP也将在收集所有字节之后生成一个完整的消息，但是这些信息在传给传输给接受端之前将储存在TCP缓冲区，以确保更好的使用网络带宽
    5. 速度  
    因为TCP必须创建连接，以保证消息的可靠交付和有序性，他需要做比UDP多的多。
    6. 重量级vs轻量级  
    由于上述的开销，TCP被认为是重量级的协议，而与之相比，UDP协议则是一个轻量级的协议。因为UDP传输的信息中不承担任何间接创造连接，保证交货或秩序的的信息。这也反映在用于承载元数据的头的大小。
    7. 头大小  
    TCP具有比UDP更大的头。
    8. 流量控制  
        * TCP有流量控制。在任何用户数据可以被发送之前，TCP需要三数据包来设置一个套接字连接。TCP处理的可靠性和拥塞控制。
        * UDP不能进行流量控制。

1. https://www.nowcoder.com/test/question/done?tid=9501873&qid=46337#summary

# Java


### 并发
1. 乐观锁，悲观锁
    * 乐观锁假设认为数据一般情况下不会造成冲突，所以在数据进行提交更新的时候，才会正式对数据的冲突与否进行检测，如果发现冲突了，则让返回用户错误的信息，让用户决定如何去做。
    * 悲观锁指的是对数据被外界（包括本系统当前的其他事务，以及来自外部系统的事务处理）修改持保守态度，
    因此，在整个数据处理过程中，将数据处于锁定状态。悲观锁的实现，往往依靠数据库提供的锁机制
1. volatile关键字的作用  
    * 保证变量的可见性
    * 保证指令集不被重排
1. Callable与Future
    * CallableAndFutureTask，利用Thread启动单线程
    ```java
    void testCallableAndFutureTask() throws InterruptedException, ExecutionException {
        FutureTask<Integer> future = new FutureTask<>(new Callable<Integer>() { //后面用lambda了
            @Override
            public Integer call() throws Exception {
                return new Random().nextInt(100);
            }
        });
        new Thread(future).start();
        Thread.sleep(5000);// 可能做一些事情
        System.out.println(future.get());
    }
    ```
    * CallableAndFuture，利用线程池，启动单线程
    ```java
    void testCallableAndFuture() throws InterruptedException, ExecutionException {
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        Future<Integer> future = threadPool.submit(() -> new Random().nextInt(100));
            Thread.sleep(5000);// 可能做一些事情
            System.out.println(future.get());
    }
    ```

    * CallableAndFuture，利用线程池，启动多线程
    ```java
    void multipleReturnValues() throws InterruptedException, ExecutionException {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        CompletionService<Integer> cs = new ExecutorCompletionService<>(threadPool);
        for (int i = 1; i < 5; i++) {
            final int taskID = i;
            cs.submit(() -> taskID);
        }
        // 可能做一些事情
        for (int i = 1; i < 5; i++) {
                System.out.println(cs.take().get());
        }
    }
    ```
    * 和Runnable接口中的run()方法不同, Callable接口中的call()方法是有返回值的
1. CountDownLatch
    * 先看用法
    ```java
     public static void main(String[] args) throws InterruptedException {
         CountDownLatch latch = new CountDownLatch(2);//两个工人的协作  
         Worker worker1 = new Worker("张三", latch);
         Worker worker2 = new Worker("李四", latch);
         worker1.start();
         worker2.start();
         latch.await();
         System.out.println("all work done.");
     }
 
     static class Worker extends Thread {
         String workerName;
         CountDownLatch latch;
 
         Worker(String workerName, CountDownLatch latch) {
             this.workerName = workerName;
             this.latch = latch;
         }
 
         public void run() {
             try {
                 System.out.println("Worker " + workerName + " do work begin.");
                 Thread.sleep(1000);
                 System.out.println("Worker " + workerName + " do work complete");
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } finally {
                 latch.countDown();//工人完成工作，计数器减一  
             }
         }
     }   
    ```
    * 主线程必须在启动其他线程后立即调用CountDownLatch.await()方法。这样主线程的操作就会在这个方法上阻塞，直到其他线程完成各自的任务。
    * CountDownLatch只有await()和countDown()方法，分别表示等待和减一
1. CyclicBarrier  
    * CyclicBarrier简单的理解就是内存屏障
    * CyclicBarrier初始化时规定一个数目，然后计算调用了CyclicBarrier.await()进入等待的线程数。当线程数达到了这个数目时，所有进入等待状态的线程被唤醒并继续。 
    * CyclicBarrier初始时还可带一个Runnable的参数， 此Runnable任务在CyclicBarrier的数目达到后，所有其它线程被唤醒前被执行。
    * 具体见代码
    ```java
    class TestCyclicBarrier {
        private static final int THREAD_NUM = 5;
    
        public static void main(String[] args) {
            //当所有线程到达barrier时执行，这里的lambda是Runnable(可选参数)
            CyclicBarrier cb = new CyclicBarrier(THREAD_NUM, () -> System.out.println("Inside Barrier"));
    
            for (int i = 0; i < THREAD_NUM; i++) {
                new Thread(() -> {
                    System.out.println("Worker's waiting");
                    //线程在这里等待，直到所有线程都到达barrier。
                    try {
                        cb.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    System.out.println("ID:" + Thread.currentThread().getId() + " Working");
                }).start();
            }
        }
    }
    /* 执行结果
    Worker's waiting
    Worker's waiting
    Worker's waiting
    Worker's waiting
    Worker's waiting
    Inside Barrier
    ID:15 Working
    ID:14 Working
    ID:12 Working
    ID:16 Working
    ID:13 Working*/
    ```

1. 阻塞队列BlockingQueue
    * 就是一个带阻塞功能的队列
    * 放入数据：
        * offer(anObject):如果BlockingQueue可以容纳, 则返回true,否则返回false.
        * offer(E o, long timeout, TimeUnit unit): 可以设定等待的时间，如果在指定的时间内，还不能往队列中，则返回失败。
        * put(anObject): 如果BlockQueue没有空间,则调用此方法的线程被阻塞，直到BlockingQueue里面有空间再继续.
    * 获取数据：
        * poll(): 若不能立即取出返回null;
        * poll(long timeout, TimeUnit unit): 如果在指定时间内，队列一旦有数据可取，则立即返回队列中的数据，否则返回null
        * take():若BlockingQueue为空， 则进入等待状态直到BlockingQueue有新的数据被加入; 
        * drainTo(Collection<? super E> c, int maxElements):一次性从BlockingQueue获取所有可用的数据对象（还可以指定获取数据的个数，第二个参数可选），通过该方法，可以提升获取数据效率；不需要多次分批加锁或释放锁。

1. 怎么检测一个线程是否持有对象监视器
    * Thread类提供了一个holdsLock(Object obj)方法
    * 当且仅当对象obj的监视器被某当前线程持有的时候才会返回true，注意这是一个static方法.

1. Java编程写一个会导致死锁的程序
    ```Java
    class DeadLock {
        private static final String obj1 = "obj1";
        private static final String obj2 = "obj2";
    
        public static void main(String[] args) {
            ExecutorService es = Executors.newFixedThreadPool(2);
            es.submit(() -> {
                System.out.println("Lock1 running");
                while (true) {
                    synchronized (DeadLock.obj1) {
                        System.out.println("Lock1 lock obj1");
                        Thread.sleep(3000);//获取obj1后先等一会儿，让Lock2有足够的时间锁住obj2
                        synchronized (DeadLock.obj2) {
                            System.out.println("Lock1 lock obj2");
                        }}}});
    
            es.submit(() -> {
                System.out.println("Lock1 running");
                while (true) {
                    synchronized (DeadLock.obj2) {
                        System.out.println("Lock2 lock obj2");
                        Thread.sleep(3000);
                        synchronized (DeadLock.obj1) {
                            System.out.println("Lock2 lock obj1");
                        }}}});
        }
    }
    ```

1. 什么是多线程的上下文切换
    * CPU控制权由一个已经正在运行的线程切换到另外一个就绪并等待获取CPU执行权的线程的过程。   
1. 什么是自旋
    * 让等待锁的线程不要被阻塞，而是在synchronized的边界做忙循环，这就是自旋。
1. 什么是CAS
    * CAS，全称为Compare and Set，即比较-设置。
    * 假设有三个操作数：内存值V、旧的预期值A、要修改的值B，
    * 当且仅当预期值A和内存值V相同时，才会将内存值修改为B并返回true，
    * 否则什么都不做并返回false。当然CAS一定要volatile变量配合，这样才能保证每次拿到的变量是主内存中最新的那个值
1. 什么是AQS【待学习】
    * http://www.cnblogs.com/waterystone/p/4920797.html
    * 内容较多，以后再学
    * 简单说一下AQS，AQS全称为AbstractQueuedSynchronizer，翻译过来应该是抽象队列同步器。
    * 如果说java.util.concurrent的基础是CAS的话，那么AQS就是整个Java并发包的核心了，ReentrantLock、CountDownLatch、Semaphore等等都用到了它。
    * AQS实际上以双向队列的形式连接所有的Entry，比方说ReentrantLock，所有等待的线程都被放在一个Entry中并连成双向队列，前面一个线程使用ReentrantLock好了，则双向队列实际上的第一个Entry开始运行。
    * AQS定义了对双向队列所有的操作，而只开放了tryLock和tryRelease方法给开发者使用，开发者可以根据自己的实现重写tryLock和tryRelease方法，以实现自己的并发功能。
1. Semaphore有什么作用
    ```Java
    class SemaphoreTest {
        public static void main(String[] args) {
            ExecutorService exec = Executors.newCachedThreadPool();
            // 只能5个线程同时访问 
            final Semaphore semp = new Semaphore(5);
            for (int index = 0; index < 20; index++) {
                final int NO = index;
                exec.execute(() -> {
                    try {
                        // 获取许可 
                        semp.acquire();
                        System.out.println("Accessing: " + NO);
                        Thread.sleep((long) (Math.random() * 10000));
                        semp.release();
                    } catch (InterruptedException ignored) {
                    }
                });
            }
            exec.shutdown();
        }
    }
    ```
    * Java中的Semaphore是一种新的同步类，它是一个计数信号。
    * 从概念上讲，信号量维护了一个许可集合。如有必要，在许可可用前会阻塞每一个 acquire()，然后再获取该许可。每个 release()添加一个许可，从而可能释放一个正在阻塞的获取者。
    * 但是，不使用实际的许可对象，Semaphore只对可用许可的号码进行计数，并采取相应的行动。
    * 信号量常常用于多线程的代码中，比如数据库连接池。

1. Java中什么是竞态条件？ 
    * 计算的正确性取决于多个线程的交替执行时序时，就会发生竞态条件。

1. 一个线程运行时发生异常会怎样？
    * 简单的说，如果异常没有被捕获该线程将会停止执行。
    * Thread.UncaughtExceptionHandler是用于处理未捕获异常造成线程突然中断情况的一个内嵌接口。当一个未捕获异常将造成线程中断的时候JVM会使用Thread.getUncaughtExceptionHandler()来查询线程的UncaughtExceptionHandler并将线程和异常作为参数传递给handler的uncaughtException()方法进行处理。

1. Java中interrupted 和 isInterrupted方法的区别？
    * interrupted() 和 isInterrupted()的主要区别是前者会将中断状态清除而后者不会。
    * Java多线程的中断机制是用内部标识来实现的，调用Thread.interrupt()来中断一个线程就会设置中断标识为true。
    * 当中断线程调用静态方法Thread.interrupted()来检查中断状态时，中断状态会被清零。
    * 而非静态方法isInterrupted()用来查询其它线程的中断状态且不会改变中断状态标识。
    * 简单的说就是任何抛出InterruptedException异常的方法都会将中断状态清零。无论如何，一个线程的中断状态有有可能被其它线程调用中断来改变。
1. 为什么你应该在循环中检查等待条件?
    * http://www.tengleitech.com/archives/107092
1.  Java中的同步集合与并发集合有什么区别？
    * http://www.tengleitech.com/archives/107098
1.  如何写代码来解决生产者消费者问题？
    * http://www.tengleitech.com/archives/107116

1. 有三个线程T1，T2，T3，怎么确保它们按顺序执行？
    * http://www.tengleitech.com/archives/107158
1. Thread类中的yield方法有什么作用？
    * Yield方法可以暂停当前正在执行的线程对象，让其它有相同优先级的线程执行。它是一个静态方法而且只保证当前线程放弃CPU占用而不能保证使其它线程一定能占用CPU，执行yield()的线程有可能在进入到暂停状态后马上又被执行。
1. 写出3条你遵循的多线程最佳实践
    * 给你的线程起个有意义的名字。  
    这样可以方便找bug或追踪。OrderProcessor, QuoteProcessor or TradeProcessor 这种名字比 Thread-1. Thread-2 and Thread-3 好多了，给线程起一个和它要完成的任务相关的名字，所有的主要框架甚至JDK都遵循这个最佳实践。
    * 避免锁定和缩小同步的范围  
    锁花费的代价高昂且上下文切换更耗费时间空间，试试最低限度的使用同步和锁，缩小临界区。因此相对于同步方法我更喜欢同步块，它给我拥有对锁的绝对控制权。
    * 多用同步类少用wait 和 notify  
    首先，CountDownLatch, Semaphore, CyclicBarrier 和 Exchanger 这些同步类简化了编码操作，而用wait和notify很难实现对复杂控制流的控制。其次，这些类是由最好的企业编写和维护在后续的JDK中它们还会不断优化和完善，使用这些更高等级的同步工具你的程序可以不费吹灰之力获得优化。
    * 多用并发集合少用同步集合  
    这是另外一个容易遵循且受益巨大的最佳实践，并发集合比同步集合的可扩展性更好，所以在并发编程时使用并发集合效果更好。如果下一次你需要用到map，你应该首先想到用ConcurrentHashMap。我的文章Java并发集合有更详细的说明。    
1.  Java中的fork join框架是什么？
    * fork join框架是JDK7中出现的一款高效的工具，Java开发人员可以通过它充分利用现代服务器上的多处理器。它是专门为了那些可以递归划分成许多子模块设计的，目的是将所有可用的处理能力用来提升程序的性能。fork join框架一个巨大的优势是它使用了工作窃取算法，可以完成更多任务的工作线程可以从其它线程中窃取任务来执行。
1. Java线程池中submit() 和 execute()方法有什么区别？
    * 两个方法都可以向线程池提交任务，
    * execute()方法的返回类型是void，它定义在Executor接口中, 
    * 而submit()方法可以返回持有计算结果的Future对象，它定义在ExecutorService接口中，它扩展了Executor接口，
    * 其它线程池类像ThreadPoolExecutor和ScheduledThreadPoolExecutor都有这些方法。

### 其他
1. 优化反射性能
    * 利用缓存
    * 对Field、Method、Constructor进行setAccessible(true);


    
12. List的实现类
    * LinkedList 
    * ArrayList 默认自动扩容1.5倍
    
13. Map的实现类
    * HashMap  
    先hash，hash冲突的用链表连起来（jdk1.8 当链表长度超过8时，链表变成红黑树）
    * TreeMap  
    红黑树
    * LinkedHashMap  
    默认按照插入排序，  
    accessOrder为true时，记录访问顺序，最近访问的放在最后
    * hashTable 与 concurrentHashMap 区别
        * HashTable每次同步执行的时候都要锁住整个结构
        * ConcurrentHashMap锁的方式是稍微细粒度的，ConcurrentHashMap里面的操作只锁当前数据所在的桶

1. Servlet的生命周期，是否是线程安全的？  
    1. Servlet 生命周期可被定义为从创建直到毁灭的整个过程。以下是 Servlet 遵循的过程：
        * Servlet 通过调用 init () 方法进行初始化。
        * Servlet 调用 service() 方法来处理客户端的请求。  
        service() 方法检查 HTTP 请求类型（GET、POST、PUT、DELETE 等），并在适当的时候调用 doGet、doPost、doPut，doDelete 等方法。
        * Servlet 通过调用 destroy() 方法终止（结束）。
        * 最后，Servlet 是由 JVM 的垃圾回收器进行垃圾回收的。
    2. 不是线程安全的，因为Servlet只会被实例化一次。如果Servlet中含有可变的域



# 操作系统
1. 进程和线程的区别  
每个进程拥有自己的一套变量，而线程之间则共享数据的。
    
3. 进程的通信方式  【待理解】
    * 管道：管道是一种半双工的通信方式，数据只能单向流动，而且只能在具有亲缘关系的进程间使用。进程的亲缘关系通常是指父子进程关系。
    * 信号量：信号量是一个计数器，可以用来控制多个进程对共享资源的访问。它常作为一种锁机制，防止某进程正在访问共享资源时，其他进程也访问该资源。因此，主要作为进程间以及同一进程内不同线程之间的同步手段。
    * 消息队列：消息队列是由消息的链表，存放在内核中并由消息队列标识符标识。消息队列克服了信号传递信息少、管道只能承载无格式字节流以及缓冲区大小受限等缺点。
    * 共享内存：共享内存就是映射一段能被其他进程所访问的内存，这段共享内存由一个进程创建，但多个进程都可以访问。共享内存是最快的 IPC 方式，它是针对其他进程间通信方式运行效率低而专门设计的。它往往与其他通信机制，如信号两，配合使用，来实现进程间的同步和通信。
    * 套接字：套解口也是一种进程间通信机制，与其他通信机制不同的是，它可用于不同及其间的进程通信。

4. 缓冲区溢出
    * 缓冲区溢出是指当计算机向缓冲区填充数据时超出了缓冲区本身的容量，溢出的数据覆盖在合法数据上。  
    * 危害有以下两点：
        * 程序崩溃，导致拒绝额服务
        * 跳转并且执行一段恶意代码  
    * 造成缓冲区溢出的主要原因是程序中没有仔细检查用户输入。 

5. 死锁  
在两个或者多个并发进程中，如果每个进程持有某种资源而又等待其它进程释放它或它们现在保持着的资源，在未改变这种状态之前都不能向前推进，称这一组进程产生了死锁。通俗的讲就是两个或多个进程无限期的阻塞、相互等待的一种状态。  
死锁产生的四个条件（有一个条件不成立，则不会产生死锁）
    * 互斥条件：一个资源一次只能被一个进程使用
    * 请求与保持条件：一个进程因请求资源而阻塞时，对已获得资源保持不放
    * 不剥夺条件：进程获得的资源，在未完全使用完之前，不能强行剥夺
    * 循环等待条件：若干进程之间形成一种头尾相接的环形等待资源关系 
    
处理策略：鸵鸟策略、预防策略、避免策略、检测与恢复策略



7. 分页和分段有什么区别？  
    * 页是信息的物理单位，分页是为实现离散分配方式，以消减内存的外零头，提高内存的利用率；或者说，分页仅仅是由于系统管理的需要，而不是用户的需要。
    * 段是信息的逻辑单位，它含有一组其意义相对完整的信息。分段的目的是为了能更好的满足用户的需要。
    * 页的大小固定且由系统确定，把逻辑地址划分为页号和页内地址两部分，是由机器硬件实现的，因而一个系统只能有一种大小的页面。段的长度却不固定，决定于用户所编写的程序，通常由编辑程序在对源程序进行编辑时，根据信息的性质来划分。
    * 分页的作业地址空间是一维的，即单一的线性空间，程序员只须利用一个记忆符，即可表示一地址。分段的作业地址空间是二维的，程序员在标识一个地址时，既需给出段名，又需给出段内地址。
    
8. 操作系统中进程调度策略
    * 先来先服务
    * 优先级
    * 短作业优先
    * 时间片轮转
    * 最高响应比优先算法(HRN)：FCFS可能造成短作业用户不满，SPF可能使得长作业用户不满，于是提出HRN，选择响应比最高的作业运行。响应比=1+作业等待时间/作业处理时间。

9. 进程同步机制
    【待补充】

5. 中断和轮询的特点
    * 对I/O设备的程序轮询的方式，是早期的计算机系统对I/O设备的一种管理方式。它定时对各种设备轮流询问一遍有无处理要求。轮流询问之后，有要求的，则加以处理。在处理I/O设备的要求之后，处理机返回继续工作。
    * 程序中断是指CPU在正常运行程序的过程中，由于预先安排或发生了各种随机的内部或外部事件，使CPU中断正在运行的程序，而转到为响应的服务程序去处理。
    * 轮询——效率低，等待时间很长，CPU利用率不高。
    * 中断——容易遗漏一些问题，CPU利用率高。

6. 临界区  
每个进程中访问临界资源的那段程序称为临界区，每次只准许一个进程进入临界区，进入后不允许其他进程进入。
    * 如果有若干进程要求进入空闲的临界区，一次仅允许一个进程进入；
    * 任何时候，处于临界区内的进程不可多于一个。如已有进程进入自己的临界区，则其它所有试图进入临界区的进程必须等待；
    * 进入临界区的进程要在有限时间内退出，以便其它进程能及时进入自己的临界区；
    * 如果进程不能进入自己的临界区，则应让出CPU，避免进程出现“忙等”现象。
1. 数据传输率（C）=记录位密度（D） x   线速度( V )
1. https://www.nowcoder.com/test/question/done?tid=9502589&qid=44781#summary


# 设计模式

### 常见的23种设计模式
0. 详解参考 [Java之美[从菜鸟到高手演变]之设计模式](http://blog.csdn.net/zhangerqing/article/details/8194653/?spm=5176.100239.blogcont69707.9.A2VX6A#comments) ，我只是对这篇文章进行了提炼，错误的简单更正，能让自己回忆起来。

1. 工厂方法模式
    * 普通工厂模式，用字符串产生实例
    * 多个工厂方法模式，直接调用相应的建造实例的方法
    * 静态工厂方法模式，多个工厂方法模式里的方法置为静态的，不需要创建工厂实例，直接调用即可
2. 抽象工厂模式
    * 为了解决程序扩展问题
    * 每个类对应一个工厂，然后这些工厂实现一个接口，使用时对相相应的工厂进行实例化，然后通过接口获取实例。
    ```java
    Provider provider = new SendMailFactory(); //Privider为所有工厂实现的接口
    Sender sender = provider.produce(); //produce方法获取该工厂想要产生的实例
    sender.Send(); 
    ```
3. 单例模式（Singleton）
    * 实现一，静态加载
    ```java
    class Singleton {
        private static Singleton instance = new Singleton();
        private Singleton (){}
        public static Singleton getInstance() {
            return instance;
        }
    }
    ```
    * 实现二，双重锁定
    ```java
    class Singleton {
        private static Singleton instance = null;
        private Singleton() {}
        public static Singleton getInstance() {
            if (instance == null) {
                synchronized (Singleton.class) {
                    if (instance == null) {
                        instance = new Singleton();
                    }
                }
            }
            return instance;
        }
    }
    ```
4. 建造者模式（Builder）
    * Builder：给出一个接口，以规范产品对象的各个组成成分的建造。规定要实现复杂对象的哪些部分的创建。（人的身体有哪些部分）
    * ConcreteBuilder：实现Builder接口，具体化复杂对象的各部分的创建。 在建造过程完成后，提供产品的实例。（某个人具体每部分的样子）
    * Director：调用具体建造者来创建复杂对象的各个部分，在指导者中不涉及具体产品的信息，只负责保证对象各部分完整创建或按某种顺序创建。（按照样子来创建一个人）
    * Product：要创建的复杂对象。（从Builder中接收这个人）
5. 原型模式（Prototype）
    * 用原型实例指定创建对象的种类，并通过拷贝这些原型创建新的对象
    * 原型类需要具备两个条件
        * 实现Cloneable接口
        * 重写Object类中的clone方法，如果是浅拷贝的话，直接super.clone()
    * 优点
        * 创建对象比直接new一个对象在性能上要好的多，因为Object类的clone方法是一个本地方法，它直接操作内存中的二进制流，特别是复制大对象时，性能的差别非常明显。
        * 简化对象的创建，使得创建对象就像我们在编辑文档时的复制粘贴一样简单。
6. 适配器模式（Adapter）
    * 适配器模式将一个类的接口转换成客户期望的另一个接口，让原本不兼容的接口可以合作无间。
    * 适配器对象实现原有接口
    * 适配器对象组合一个实现新接口的对象
    * 对适配器原有接口方法的调用被委托给新接口的实例的特定方法
    ```java
    public class Adapter implements OldInterface{ //实现旧接口  
        private NewInterface newInterface;//组合新接口  
        //在创建适配器对象时，必须传入一个新接口的实现类 
        public Adapter(NewInterface newInterface) {
            this.newInterface = newInterface;
        }
        //将对旧接口的调用适配到新接口 
        @Override
        public void oldFunction() {
            newInterface.newFunction();
        }
    }  
    ```
7. 装饰器模式(Decorator)
    * 简而言之，就是用一个装饰器，将需要装饰的类进行装饰，类似于AOP代理
    * Component：组件对象的接口，可以给这些对象动态的添加职责；
    * ConcreteComponent：具体的组件对象，实现了组件接口。该对象通常就是被装饰器装饰的原始对象，可以给这个对象添加职责；
    * Decorator：所有装饰器的父类，需要定义一个与组件接口一致的接口(主要是为了实现装饰器功能的复用，即具体的装饰器A可以装饰另外一个具体的装饰器B，因为装饰器类也是一个Component)，并持有一个Component对象，该对象其实就是被装饰的对象。如果不继承组件接口类，则只能为某个组件添加单一的功能，即装饰器对象不能在装饰其他的装饰器对象。
    * ConcreteDecorator：具体的装饰器类，实现具体要向被装饰对象添加的功能。用来装饰具体的组件对象或者另外一个具体的装饰器对象。
    ```java
    Component c1 = new ConcreteComponent(); //首先创建需要被装饰的原始对象(即要被装饰的对象)  
    Decorator decoratorA = new ConcreteDecoratorA(c1); //给对象透明的增加功能A并调用  
    decoratorA.operation();
    Decorator decoratorB = new ConcreteDecoratorB(c1); //给对象透明的增加功能B并调用  
    decoratorB.operation();
    Decorator decoratorBandA = new ConcreteDecoratorB(decoratorA);//装饰器也可以装饰具体的装饰对象，此时相当于给对象在增加A的功能基础上在添加功能B  
    decoratorBandA.operation();
     ```
8. 代理模式
    * 抽象角色：声明真实对象和代理对象的共同接口。    
    * 代理角色：代理对象角色内部含有对真实对象的引用，从而可以操作真实对象，同时代理对象提供与真实对象相同的接口以便在任何时刻都能代替真实对象。同时，代理对象可以在执行真实对象操作时，附加其他的操作，相当于对真实对象进行封装（也就是Decorator？）。
    * 真实角色：代理角色所代表的真实对象，是我们最终要引用的对象
9. 外观模式
    * 外观模式是为了解决类与类之间的依赖关系的，外观模式就是将他们的关系放在一个Facade[fə'sɑːd]类中，降低了类类之间的耦合度，该模式中没有涉及到接口。
    ```java
    //Facade
    public class Computer {
    //...cpu, memory, disk之间没有关系，如果没有Computer他们将互相严重依赖
        public void startup(){
            System.out.println("start the computer!");
            cpu.startup();
            memory.startup();
            disk.startup();
            System.out.println("start computer finished!");
        }
    }  
    ```
10. 桥接模式
    * 桥接的用意是：将抽象化与实现化解耦，使得二者可以独立变化
    * 常见的JDBC桥DriverManager就是桥接模式。
    * 设计一个桥，传入需要连接的对象，然后用桥来调用方法（那和实现一个拥有该方法的接口有什么区别呢？为了解决历史遗留问题？）
11. 组合模式(部分-整体模式)
    * 将对象组合成树形结构以表示“部分整体”的层次结构。组合模式使得用户对单个对象和使用具有一致性。
    * 如果你想要创建层次结构，并可以在其中以相同的方式对待所有元素，那么组合模式就是最理想的选择
12. 享元模式
    * 实现对象的共享，即共享池，当系统中对象多的时候可以减少内存的开销，通常与工厂模式一起使用。
    * 例如jdbc连接池
13. 策略模式（strategy）
    * 把一个类中经常改变或者将来可能改变的部分提取出来，作为一个接口，然后在类中包含这个对象的实例，这样类的实例在运行时就可以随意调用实现了这个接口的类的行为。
    * 比如定义一系列的算法,把每一个算法封装起来, 并且使它们可相互替换，使得算法可独立于使用它的客户而变化。
14. 模板方法模式
    * 父类中，有一个主方法，再定义1...n个方法，可以是抽象的，也可以是实际的方法
    * 定义一个类，继承该父类，重写方法，通过调用类，实现对子类的调用
    * 一般不需要变的方法可以为定义为final
15. 观察者模式
    * 当一个对象变化时，其它依赖该对象的对象都会收到通知，并且随着变化
    * 用一个类来保存依赖关系，并通知依赖者被依赖者的变化情况
16. 迭代子模式
    * 顺序访问聚集中的对象
17. 责任链模式
    * 有多个对象，每个对象持有对下一个对象的引用，这样就会形成一条链，请求在这条链上传递，直到某一对象决定处理该请求。
    * 发出者并不清楚到底最终那个对象会处理该请求
    * 责任链模式可以实现，在隐瞒客户端的情况下，对系统进行动态的调整。
18. 命令模式
    * 抽象命令（Command）：定义命令的接口，声明执行的方法。
    * 具体命令（ConcreteCommand）：具体命令，实现要执行的方法，它通常是“虚”的实现；通常会有接收者，并调用接收者的功能来完成命令要执行的操作。
    * 接收者（Receiver）：真正执行命令的对象。任何类都可能成为一个接收者，只要能实现命令要求实现的相应功能。
    * 调用者（Invoker）：要求命令对象执行请求，通常会持有命令对象，可以持有很多的命令对象。这个是客户端真正触发命令并要求命令执行相应操作的地方，也就是说相当于使用命令对象的入口。
    * 客户端（Client）：命令由客户端来创建，并设置命令的接收者。
19. 备忘录模式
    * 目的是保存一个对象的某个状态，以便在适当的时候恢复对象
    * 假设有原始类A，A中有各种属性，A可以决定需要备份的属性
    * 备忘录类B是用来存储A的一些内部状态
    * 类C就是一个用来存储备忘录的，且只能存储，不能修改等操作。
20. 状态模式
    * 通过改变状态来改变行为
21. 访问者模式【似懂非懂】
    * 封装某些作用于某种数据结构中各元素的操作，它可以在不改变数据结构的前提下定义作用于这些元素的新的操作。
    ```java
    class A {
        void method1() { System.out.println("我是A"); }
        void method2(B b) { b.showA(this); }
    }
    class B {
        void showA(A a) { a.method1(); }
        public static void main(String[] args) {
            A a = new A();
            a.method1();
            a.method2(new B());
        }
    }
    ```
22. 中介者模式
    * 定义一个中介对象来封装系列对象之间的交互。
    * 中介者使各个对象不需要显示地相互引用，从而使其耦合性松散，而且可以独立地改变他们之间的交互。
23. 解释器模式
    * 就是写个解释器，比如正则表达式解释器，四则运算解释器。
### 常见问题
1. 单例模式相较于静态类的优点
    * 静态类对接口不友好，Java8 才出现静态方法。
    * 单例可以被延迟初始化，静态类一般在第一次加载是初始化。之所以延迟加载，是因为有些类比较庞大，所以延迟加载有助于提升性能。不过用静态实现的单例模式没有这个优点。
    * 单例类可以被继承，他的方法可以被override。但是静态类内部方法都是static，无法被override。
    * 单例类比较灵活，毕竟从实现上只是一个普通的Java类，只要满足单例的基本需求，你可以在里面随心所欲的实现一些其它功能，但是静态类不行。

# 数学
### 概率论
1. https://www.nowcoder.com/test/question/done?tid=9501873&qid=46327#summary


# 小算法 或 智商题
只总结一些我接触的少，或者常见但是忘记了的算法 
1. Reservoir Sampling  
    【待补充】
2. 排序
    * 简单修改了一下[DualPivotQuicksort](https://github.com/llysrv/Interview/blob/master/src/sort/DualPivotQuicksort.java)的源码
    * 源码很强
    * 含有：
        * 插入排序
        * 普通快排
        * 双轴快排
        * Tim sort
    * 还有各种特判优化
    * 值得一默
        
3. 哈夫曼树

1. 老鼠喝可乐
    * 问题：有15瓶可乐，其中有一瓶是坏的，问需要多少只老鼠同时喝才能知道哪一瓶可乐是坏的
    * 答案：不懂。。我感觉是二进制问题，我猜的是4
1. 2、5、7改变开关
1. 两个人约6~7点见面，如果等超过15min就回家，问能见面的概率
1. 堆排序
1. 给你一个整型数组，把它补充成回文数组，要求所有的数字和最小
    * 如： \[1,2,3,1,2\] -> \[1,2,1,3,1,2,1\] -> 11
1. Hash
    * http://blog.csdn.net/u011080472/article/details/51177412
1. 有一个苹果，两个人轮流抛硬币，抛到正面的可以吃苹果。问先抛的人吃到苹果的概率
1. a1,a2,a3,a4入栈，一共有多少种出栈顺序
1. kmp
    ```java
    public class KMP {
        public static int[] getNext(char[] s) {
           int next[] = new int[s.length + 1];
           for (int i = 1, j = 0; i < s.length; i++) {
               while (j > 0 && s[i] != s[j]) j = next[j];
               if (s[i] == s[j]) j++;
               next[i + 1] = j;
            }
           return next;
        }

        public static void search(char[] s, char[] t) {
            int[] next = getNext(t);
            System.out.println(Arrays.toString(next));
            for (int i = 0, j = 0; i < s.length; i++) {
                while (j > 0 && s[i] != t[j]) j = next[j];
                if (s[i] == t[j]) j++;
                if (j == t.length) {
                    System.out.println("find at position " + (i - j + 1));
                    j = next[j];
                }}}}
    ```

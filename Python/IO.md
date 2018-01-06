### IO
1. 在IO编程中，就存在速度严重不匹配的问题举个例子来说，比如要把100M的数据写入磁盘，CPU输出100M的数据只需要0.01秒，可是磁盘要接收这100M数据可能需要10秒
    1. CPU等着，也就是程序暂停执行后续代码，等100M的数据在10秒后写入磁盘，再接着往下执行，这种模式称为同步IO；
    2. 另一种方法是CPU不等待，只是告诉磁盘，“您老慢慢写，不着急，我接着干别的事去了”，于是，后续代码可以立刻接着执行，这种模式称为异步IO。
    * 同步和异步的区别就在于是否等待IO执行的结果。好比你去麦当劳点餐，你说“来个汉堡”，服务员告诉你，对不起，汉堡要现做，需要等5分钟，于是你站在收银台前面等了5分钟，拿到汉堡再去逛商场，这是同步IO。
    你说“来个汉堡”，服务员告诉你，汉堡需要等5分钟，你可以先去逛商场，等做好了，我们再通知你，这样你可以立刻去干别的事情（逛商场），这是异步IO。
    * 使用异步IO来编写程序性能会远远高于同步IO，但是异步IO的缺点是编程模型复杂。想想看，你得知道什么时候通知你“汉堡做好了”，而通知你的方法也各不相同。如果是服务员跑过来找到你，这是回调模式，如果服务员发短信通知你，你就得不停地检查手机，这是轮询模式。总之，异步IO的复杂度远远高于同步IO(讲道理应该吧这些东西根据自己的理解写到其他地方的)
    

* 用open()打开文件, 标识符有r,rb,w,wb
* file.read()输出文件的全部内容
* encoding指定编码格式，如读取gbk编码的文件f = open('/Users/michael/gbk.txt', 'r', encoding='gbk')
* open()函数还接收一个errors参数，表示如果遇到编码错误后如何处理。最简单的方式是直接忽略,errors='ignore'
* file.read(size) 读取size个字节
* file.readline() 读取一行，会保留换行符
* file.close()关闭 with可以带资源释放
```Python
mm = open('IOtest/kkk/密码.txt', 'r')
print(mm.read(10))
k = mm.readline()
print(len(k))  # 字符串长度里面有换行符
print(mm.readline())
for line in mm.readlines():
    print(line)
mm.close()

old_pic = open('IOtest/斧头.jpg', 'rb')
new_pic = open('IOtest/aa.jpg', 'wb')
new_pic.write(old_pic.read())
old_pic.close()
new_pic.close()
with open('IOtest/斧头.jpg', 'rb') as old_pic:
    with open('IOtest/aa.jpg', 'wb') as new_pic:
        new_pic.write(old_pic.read())
```

### StringIO 
* 类似于java的StringBuffer
* 可以向文件一样读取StringIO
```Python
from io import StringIO
f = StringIO()
f.write('hello')
f.write(' ')
f.write('world!')
print(f.getvalue())
```

### ByteIO
* 类似与StringIO，只不过里面存的是Byte
```Python
from io import BytesIO
f = BytesIO()
f.write('中文'.encode('utf-8'))
print(f.getvalue())
```

### file操作
* os.path.join(A, B) 会用当前系统的分隔符连接AB
* os.path.split(dir) 会把dir分割成两部分，用最后一个系统的路径分隔符分开
* os.path.splitext() 以文件的扩展名分割
* os.mkdir(dir) 创建某个目录
* os.rmdir(dir) 删除某个目录
* os.rename('test.txt', 'test.py') 重命名
* os.remove('test.py') 删除
```Python
import os
print(os.name)
print(os.environ)
print(os.path.abspath('.'))
print(os.mkdir(''))
```
查找dir下，名字里包含key的全部文件
```Python
def search(key, path):
    for son in os.listdir(path):
        son = os.path.join(path, son)
        if os.path.isfile(son):
            file = os.path.split(son)[1]
            if file.find(key) != -1:
                print(os.path.abspath(son))
        else:
            search(key, son)
    return "ok"

search('a', 'D:\\PycharmProjects')
```

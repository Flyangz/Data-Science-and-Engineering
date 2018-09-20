# Data Structure and Algorithm

[TOC]

高效增删改查数据。

数据库，文件压缩，多任务切换，通信录，游戏中的寻路......

目标：数组，栈，队列，链表，二分搜索树，堆

线性

树结构

图结构

时间复杂度：均摊，动荡（lazy）

## Data Structure

### 线

#### 数组

适合：索引有语义的情况（索引不能太大）

时间复杂度：增删O(n)，改查：索引O(1)；内容O(n)

注意：

- resize：new新数组，遍历旧数组的elem到新数组，data指向新数组

- 增的范围包含size，删改查不包含

- add（实际上是insert）：index合法？full？

  for (int i = size - 1; i >= index; i--) {
  data[i + 1] = data[i];
  }

  增加，修改size

- remove：index合法？取，覆盖

  for (int i = index + 1; i < size; i++) {
  data[i - 1] = data[i];
  }
  size--;
  data[size] = null;

  缩容？缩容前记得加上data.length / 2 != 0

- addLast和addFirst复用add，其他get，remove类似

- 涉及index的记得检查合法性

- 设计object比较记得用equals

- removeElement按照内容删除，结合find（根据内容查找出索引）

```java
public class Array<E> {

    private E[] data;
    private int size;

    public Array(int capacity) {
        data = (E[]) new Object[capacity];
        size = 0;
    }

    public Array() {
        this(10);
    }

    public int getSize() {}
    public int getCapacity() {}
    public boolean isEmpty() {}
    public void addLast(E e) {}
    public void addFirst(E e) {}
    public void add(int index, E e) {}
    public E get(int index) {}
    public E getFirst() {}
    public E getLast() {}
    public void set(int index, E e) {}
    public boolean contains(E e) {}
    public int find(E e) {}
    public E remove(int index) {}
    public E removeFirst() {}
    public E removeLast() {}
    public void removeElement(E e) {}
    private void resize(int newCapacity) {}
        
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(String.format("Array: size = %d, capacity = %d\n", size, data.length));
        res.append("[");
        for (int i = 0; i < size; i++) {
            res.append(data[i]);
            if (i != size - 1) {
                res.append(", ");
            }
        }
        res.append("]");
        return res.toString();
    }   
```

#### 栈

时间复杂度：增删O(1)均摊，查peek：O(1)

注意：

- push就是array.addLast，pop就是array.removeLast，peek就是array.getLast

```JAVA
public class ArrayStack<E> implements Stack<E> {

    Array<E> array;

    public ArrayStack(int capacity){
        array = new Array<>(capacity);
    }

    public ArrayStack(){
        array = new Array<>();
    }
    
    @Override
    public int getCapacity(){}
    @Override
    public int getSize() {}
    @Override
    public boolean isEmpty() {}
    @Override
    public void push(E e) {}
    @Override
    public E pop() {}
    @Override
    public E peek() {}
    public String toString() {}
}
```

#### 队列

##### 数组队列

时间复杂度：增O(1)均摊，删O(n)，查front：O(1)

注意：

- enqueue就是array.addLast，dequeue就是array.removeFirst，getFront就是array.getFrist

```JAVA
public class ArrayQueue<E> implements Queue<E> {

    private Array<E> array;

    public ArrayQueue(int capacity) {
        array = new Array<>(capacity);
    }

    public ArrayQueue() {
        array = new Array<>();
    }

    public int getCapacity() {}
    @Override
    public String toString() {}
    @Override
    public int getSize() {}
    @Override
    public boolean isEmpty() {}
    @Override
    public void enqueue(E e) {}
    @Override
    public E dequeue() {}
    @Override
    public E getFront() {}
}
```

##### 循环队列

时间复杂度：增O(1)均摊，删O(1)均摊，查front：O(1)

注意：

- 构造函数的capacity+1，getCapacity是capacity-1

- isEmpty条件front == tail，full条件(tail + 1) % capacity == front。每个有可能超过capacity的front或tail的移动都要加% capacity

- resize用getCapacity而不是数组的length

- 两种遍历方式：

  for (int i = 0; i < size; i++) {
  newLoopQueue[i] = data[(i + front) % data.length];
  }

  for (int i = front; i != tail; i = (i + 1) % data.length) {
  res.append(data[i]);

  }

- enqueue：full？resize，入队，修改tail和size

- dequeue：isEmpty？，出列，修改front和size，是否需要resize？

```JAVA
public class LoopQueue<E> implements Queue<E> {

    private E[] data;
    private int front, tail;
    private int size;

    public LoopQueue(int capacity) {
        data = (E[]) new Object[capacity + 1];
        front = 0;
        tail = 0;
        size = 0;
    }

    public LoopQueue() {
        this(10);
    }

    public int getCapacity() {}
    @Override
    public int getSize() {}
    @Override
    public boolean isEmpty() {}
    @Override
    public void enqueue(E e) {}
    @Override
    public E dequeue() {}
    @Override
    public String toString() {}
    @Override
    public E getFront() {}
    private void resise(int newCapacity) {}
}
```

#### 链表

最简单的动态数据结构，不适合索引有语义的情况。

##### 单向列表

时间复杂度：增删改查O(n)看位置，逐渐递增

注意：

- Node实现：成员变量E e和Node next

- 移动：

  index前

  Node pre = dummyhead;
  for (int i = 0; i < index; i++) {
  pre = pre.next;
  }

  index当前

  Node cur = dummyhead.next;
  for (int i = 0; i < index; i++) {
  cur = cur.next;
  }

- 遍历

  while (cur != null)

  for(int i = 0; i < size; i++)

- add：index合法性，移动到index，添加（先新建一个newnode，然后把它的next指向index的node，最后将index前的node pre指向newnode

  pre.next = new Node(e, pre.next);
  size++;

- remove：先取出res，即pre.next，然后pre.next指向res.next，最后res.next指向null

  Node res = pre.next;
  pre.next = res.next;
  res.next = null;
  size--;

```java
public class LinkedList<E> {

    private class Node {
        public E e;
        public Node next;

        public Node(E e, Node next) {
            this.e = e;
            this.next = next;
        }

        public Node(E e) {this(e, null);}

        public Node() {this(null, null);}
        
        @Override
        public String toString() {
            return e.toString();
        }
    }

    private Node dummyhead;
    private int size;

    public LinkedList() {
        dummyhead = new Node();
        size = 0;
    }
    public int getSize() {}
    public boolean isEmpty() {}
    
    //练习题才用的方法
    public void add(int index, E e) {}
    public void addFirst(E e) {}
    public void addLast(E e) {}

    //练习题才用的方法
    public E get(int index) {}
    public E getFirst() {}
    public E getLast() {}
    
    //练习题才用的方法
    public void set(int index, E e) {}
    public boolean contains(E e) {}
    public E remove(int index) {}
    public E removeFirst() {}
    public E removeLast() {}
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

        Node cur = dummyhead.next;
        while (cur != null) {
            res.append(cur + "->");
            cur = cur.next;
        }

//        for(Node cur = dummyhead.next ; cur != null ; cur = cur.next)
//            res.append(cur + "->");

        res.append("null");
        return res.toString();
    }
}
```

##### 链表栈LinkedListStack：First为栈的出口

##### 链表队列LinkedListQueue：

注意：

- 成员变量：head, tail和size

- enqueue：isEmpty？`tail = new Node(e); head = tail;` ：`tail.next = new Node(e); tail = tail.next;`，size++

- dequeue：isEmpty？取head，head指向head.next

  Node res = head;
  head = head.next;
  size--;
  if(isEmpty()) { tail = head; }

```java
public class LinkedListQueue<E> implements Queue<E> {

    private class Node {}

    private Node head, tail;
    private int size;

    public LinkedListQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    @Override
    public int getSize() {}
    @Override
    public boolean isEmpty() {}
    @Override
    public void enqueue(E e) {}
    @Override
    public E dequeue() {}
    @Override
    public E getFront() {}
    @Override
    public String toString() {}
```

双链表

循环链表

### 树

二叉树：具有唯一的根节点，每个Node有elem, left 和 right，最多一个夫节点。

**二分搜索树**

每个节点的值大于其左子树的所有节点的值，小于其右子树的所有节点的值。

注意：

- add：root = add(root, e)，下面是private add的思路

  最底层问题null：return new Node(e);

  递归：

  if (e.compareTo(node.e) < 0) node.left = add(node.left, e)

  最后返回修改后的node

- contains: return contains(root, e) ，下面是private contains的思路

  最底层问题null：return false;

  递归：

  if (相等) return ture; else if(小于)return contains(node.left, e); else ...

  最后返回修改后的node

- 遍历

  if (node == null) return;
  System.out.println(node.e);
  preOrder(node.left);
  preOrder(node.right);

- levelOrder：通过LinkedList实现，先把root放入队列，while队列不为空，取出第一个node，打印node.e，如果node.left不为null，放入队列，node.right同样，然后回到while循环。

- minimum：检查index，调用private minimum，如果node.left == null，返回node，否则继续调用。删除最小值：如果node.left == null，则返回右节点（记得加上node.right = null），否则node.left = removeMin(node.left)，最后返回修改后的node。

- remove：

  最底层问题null：return null;

  递归：当前node和e的比较，非相等情况，node.left = remove(node.left, e)，相等时有三种情况：

  - 左子树为空？右子树为空？处理参照removeMin。
  - 两个都不是空，则找当前节点的继承者，最接近的大于或小于，即对右子树使用removeMin，或左子树使用removeMax。得到的继承者连接原节点的左右子树，原节点左右子树指向空。

```JAVA
public class BST<E extends Comparable<E>> {

    private class Node {
        public E e;
        public Node left, right;

        public Node(E e) {
            this.e = e;
            this.left = null;
            this.right = null;
        }
    }

    private Node root;
    private int size;

    public BST() {
        root = null;
        size = 0;
    }

    public int size() {}
    public boolean isEmpty() {}
    public void add(E e) {}
    private Node add(Node node, E e) {}
    public boolean contains(E e) {}
    private boolean contains(Node node, E e) {}
    public void preOrder() {}
    private void preOrder(Node node) {}
    public void midOrder() {}
    private void midOrder(Node node) {}
    public void postOrder() {}
    private void postOrder(Node node) {}
    public void levelOrder() {}
    public E minimum() {}
    private Node minimum(Node node) {}
    public E removeMin() {}
    private Node removeMin(Node node) {}
    public void remove(E e) {
        root = remove(root, e);
    }
    private Node remove(Node node, E e) {
        if (node == null) {
            return null;
        }

        if (e.compareTo(node.e) < 0) {
            node.left = remove(node.left, e);
            return node;
        } else if (e.compareTo(node.e) > 0) {
            node.right = remove(node.right, e);
            return node;
        } else {
            if (node.left == null) {
                Node rightNode = node.right;
                node.right = null;
                size--;
                return rightNode;
            } else if (node.right == null) {
                Node liftNode = node.left;
                node.left = null;
                size--;
                return liftNode;
            } else {
                Node successor = minimum(node.right);
                successor.left = node.left;
                successor.right = removeMin(node.right);
                node.right = node.left = null;
                return successor;
            }
        }
    }
}
```

---

## Algorithm

### Sort

#### Selection Sort

思路：loop找[i, n)区间的最小值，这个过程中，先设定第一个数i为暂minIndex，然后遍历[i+1, n)找最小值，每当(arr[j] < arr[minIndex])时，minIndex = j。遍历完[i+1, n)后就找到了[i, n)的最小值，然后交换arr[i]和arr[真minIndex]。

```JAVA
public class SelectionSort {

    public static void sort(Comparable[] arr) {
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            // 寻找[i, n)区间里的最小值的索引
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j].compareTo(arr[minIndex]) < 0) {
                    minIndex = j;
                }
            }

            //找到之后swap
            Comparable t = arr[i];
            arr[i] = arr[minIndex];
            arr[minIndex] = t;
        }
    }

    public static void main(String[] args) {
        Comparable[] arr = {10, 99, 8, 7, 6, 5, 4, 3, 2, 1};
        SelectionSort.sort(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
    }
}
```



红黑树


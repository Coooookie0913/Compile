```c
//if.c
int main() {
    int a = getint();
    int b = getint();
    int c = 0;
    if (a == b) {
        c = 5;
    } else {
        c = 10;
    }
    putint(c);
    return 0;
}
```

```LLVM
declare i32 @getint()
declare void @putint(i32)
define  i32 @main() {
  %1 = alloca i32
  %2 = alloca i32
  %3 = alloca i32
  %4 = alloca i32
  store i32 0, i32* %1 //有什么意义？
  
  
  %5 = call i32 () @getint()
  store i32 %5, i32* %2 //a
  %6 = call i32 () @getint()
  store i32 %6, i32* %3 //b
  store i32 0, i32* %4 //c
  //每存储一个变量，都要提前开好空间
  
  %7 = load i32, i32* %2
  %8 = load i32, i32* %3
  %9 = icmp eq i32 %7, %8
  br i1 %9, label %10, label %11

10:                                               ; preds = %0
  store i32 5, i32* %4
  br label %12

11:                                               ; preds = %0
  store i32 10, i32* %4
  br label %12

12:                                               ; preds = %11, %10
  %13 = load i32, i32* %4
  call void @putint(i32 %13)
  ret i32 0
}
```





- 后续需要支持含数组的函数定义和调用吗
- 为什么要把 block 定义成 value ？而不是 user ？（还没写 basicblock 但是认为它也 use 了很多 instruction呀）
- 可不可以粗暴的认为 `<ty> , <ty>*` 是同一个 `ty` 吗? `save load getelementptr` 中的
- `ret void` 是否可以直接不生成



- 指令的LLVMType写的有问题，应该是指令Type





- mod 对应的是有符号除法还是无符号除法
- 什么时候使用Zext指令（好像没用到？）



- b\[3][3]

  - 如果是传值 b\[0][1]

    ```
     %16 = getelementptr [3 x [3 x i32]], [3 x [3 x i32]]* @b, i32 0, i32 0, i32 1
     %17 = load i32, i32* %16
    ```

  - 传指针

    ```
    ;b[1]
    %20 = getelementptr [3 x i32], [3 x i32]* %19, i32 0, i32 %18
    ;b
    %21 = getelementptr [3 x [3 x i32]], [3 x [3 x i32]]* @b, i32 0, i32 0
    
    %22 = call i32 @a3(i32 %17, i32* %20, [3 x i32]* %21)
    ```

    

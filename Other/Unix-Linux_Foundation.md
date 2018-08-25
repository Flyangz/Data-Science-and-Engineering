# Unix/Linux Foundation  
## The Shell  
The shell is a program that takes keyboard commands and passes them to the operating system to carry out.bash是一种shell。  
  
Terminal Emulators:  program to interact with the shell.黑框  
  
“#” superuser privileges（`sudo -s` `su root`）  
### Simple Commands  
`date "+%y年%m月%日"`  
`cal -y 2018`  
df: current amount of free space on your disk drives  
  
 virtual terminals or virtual consoles  
## Navigation  
pwd, cd, ls  
  
a hierarchical directory structure  
inside a single directory and we can see the files contained in the directory and the pathway to the directory above us (called the parent directory) and any subdirectories below us.   
  
Absolute Pathnames: starts from the root directory  
Relative Pathnames:starts from the working directory, `cd ../..` `cd (./)bin` `cd -`只能跳一次，再跳往回 `cd ~user_name`Changes the working directory to the home directory of user_name. For example, cd ~bob will change the directory to the home directory of user “bob.”  
  
`ls -a`  
Linux has no concept of a “file extension”   
use underscore to represent spaces  
  
## Exploring The System  
ls, file, less(zless看gzip文件)  
`ls /usr` ` ls ~ /usr` list both the user's home directory (symbolized by the “~” character) and the /usr directory   
`ls -l`  
`ls -lt` sort theresult by the file's modification time`ls -lt --reverse` Unix用-ltr  
  
### Common ls Options  
-a	Displays all files.    
-d	Displays only directories.    
-F	Flags filenames.    
-h  display file sizes in    
human readable format    
-g	Displays the long format listing, but exclude the owner name.    
-S  Sort results by file size.    
-t	Displays newest files first. (based on timestamp)    
  
### A Longer Look At Long Format  
`-rw-r--r-- 1 root root 3576296 2007-04-03 11:05 Experience ubuntu.ogg`  
`-rw-r--r--` | Access rights to the file. The first character indicates the type of file. dash: regular file, d: directory  
The next three characters are the access rights for the file's owner, the next three are for members of the file's group, and the final three are for everyone else.     
`1` File's number of hard links.     
`root` The username of the file's owner.    
`root` The name of the group which owns the file.    
`32059` Size of the file in bytes.    
`2007-04-03 11:05` Date and time of the file's last modification.    
  
### Determining A File's Type With file  
`file picture.jpg`  
  
### Viewing File Contents With less  
b, space, g, G,/characters,q  
### A Guided Tour  
Directories Found On Linux Systems  
### Symbolic Links  
`lrwxrwxrwx 1 root root 11 2007-08-11 07:34 libc.so.6 -> libc-2.6.so`  
  
## Manipulating Files And Directories  
cp, mv, rm, mkdir, ln  
前三个都是可怕的命令，最好都配合-i。另外，rm前先用ls确定内容，按上，改ls为rm  
`cp -u *.html destination`  
### Wildcards  
`?` Matches any single character  
`[!characters]` Matches any character that is not a member of the set  
characters  
`[[:class:]]` Matches any character that is a member of the specified class具体指`[:alnum:]` `[:alpha:]` `[:digit:]` `[:lower:]` `[:upper:]`   
  
`mkdir dir1 dir2 dir3`  
`mkdir A/B/C -p`  
  
`cp item directory`  
`-a` Copy the files and directories and all of their attributes.  
`-i` Before overwriting an existing file, prompt the user for confirmation.   
`-r` Recursively copy directories and their contents.   
`-u` only copy files that either don't exist, or are newer than the existing corresponding files  
`-v`Display informative messages as the copy is performed.  
  
`cp -i file1 file2`  If file2 does not exist, it is created.  
`cp file1 file2 dir1` Copy file1 and file2 into directory dir1. dir1 must already exist.  
`cp dir1/* dir2` `cp -r dir1 dir2`  
  
### ln – Create Links  
`ln file link` 如`ln fun dir1/fun-hard`  
`ln -s item link`  create a hard link where “item” is either a file or a directory  
`ls -li`可以查看索引节点，从而确认为同一个文件  
`ln -s ../fun dir1/fun-sym`注意，当fun在dir1的上一层时，要加../（或者用绝对路径）  
  
symbolic links: most file operations are carried out on the link's target, not the link itself. rm is an exception. When you delete a link, it is the link that is deleted, not the target.  
硬链接数为0时才为真正删除  
## Working With Commands  
`type`命令种类, `which`命令位置, `help` builtins命令的信息, man程序手册页（program）, `apropos`搜索manual内容, `info`s an alternative to man pages, `whatis`, `alias`  
Commands: An executable program, A command built into the shell itself, A shell function, An alias  
  
Creating Your Own Commands With alias  
先用type确定名字是否被占用，然后  
` alias foo='cd /usr; ls; cd -'`  
`unalias foo`  
  
## I/O Redirection  
`cat`reads one or more files and copies them to standard output  
`sort`  
`uniq`  
`grep`  
`wc`Print Line, Word, And Byte Counts`wc ls-output.txt`  
`head` `head -n 5 ls-output.txt`  
`tail`  
`tee`The tee program reads standard input and copies it to both standard output (allowing the data to continue down the pipeline) and to one or more files.`ls /usr/bin | tee ls.txt | grep zip` 屏幕显示，并将显示存到ls.txt  
  
### Standard Input, Output, And Error  
 programs such as ls actually send their results to a special file called standard output (often expressed as stdout) and their status messages to another file called standard error (stderr)By default, both standard output and standard error are linked to the screen and not saved into a disk file.In addition, many programs take input from a facility called standard input (stdin) which is, by default, attached to the keyboard.  
   
### Redirecting Standard Output  
`ls -l /usr/bin > ls-output.txt`   
`> ls-output.txt`create new empty file  
警惕`>`的overwrite  
`ls -l /usr/bin >> ls-output.txt`  
### Redirecting Standard Error  
standard input, output and error对应`0, 1,  2`  
`ls -l /bin/usr 2>ls-error.txt`将error写到txt里  
`cmd > file 2>&1`表示error也写到1的file里，即output的file  
`cmd < file1 > file2`输出到file2  
`ls -l /bin/usr &>> ls-output.txt`不管哪种结果都写进文件里  
`ls -l /bin/usr 2> /dev/null`Disposing Of Unwanted Output  
### Redirecting Standard Input  
`cat movie.mpeg.0* > movie.mpeg`wildcards always expand in sorted order  
Ctrl-d:  end of file (EOF)   
`cat > lazy_dog.txt`  
`cat < lazy_dog.txt`=`cat lazy_dog.txt`  
### Pipelines  
`ls -l /usr/bin | less`  
注意command1 > file1，command1 | command2  
Filters  
`ls /bin /usr/bin | sort | less`  
`ls /bin /usr/bin | sort | uniq | less`  
### grep – Print Lines Matching A Pattern  
`ls /bin /usr/bin | sort | uniq | grep zip`  
`grep "^ntf" xxx.txt`查找xxx.txt文件里面行以ntf开头的  
`“ntf$`结尾的  
-i ignore case  
-v only print lines that do not match the pattern  
  
## Seeing The World As The Shell Sees It---echo  
### Expansion  
`echo *`the names of the files in the current working directory  
`echo /usr/*/share`  
output:/usr/kerberos/share /usr/local/share  
`echo ~`  
`echo $((2 + 2))`  
`echo Front-{A,B,C}-Back` `mkdir {2007..2009}-{01..12}`  
  
variables  
`echo $USER`variable named “USER” contains your username`printenv | less`查看 available variables  
  
Command Substitution  
`ls -l $(which cp)`这样可以在不知道cp位置的情况下查看cp dir的内容。注意，这里$把which cp的结果变为expansion  
### Quoting  
The shell provides a mechanism called quoting to selectively suppress unwanted expansions  
  
word-splitting looks for the presence of spaces, tabs, and newlines (linefeed characters) and treats them as delimiters between words. This means that unquoted spaces, tabs, and newlines are not considered to be part of the text.  
`echo this is a test`不管单词间有多少空格  
the output: this is a test  
`echo The total is $100.00` $1函数返回值为空  
the output: The total is 00.00  
`echo "The balance for user $USER is: \$5.00"`改正  
  
Double Quotes  
The exceptions are “$”, “\” (backslash), and “`” (backquote).  
```ls -l "two words.txt"```对付含空格的文件  
parameter expansion, arithmetic expansion, and command substitution still take place within double quotes  
```echo "$USER $((2+2)) $(cal)"```这里会产生3个函数的结果  
```  
//输入：echo text ~/*.txt {a,b} $(echo foo) $((2+2)) $USER  
//结果：text /home/me/ls-output.txt a b foo 4 me  
```  
Single Quotes can suppress all expansions  
  
Escape Sequence   
| \a | 响铃（”警告”－导致计算机嘟嘟响） |  
| \b | 退格符 |  
| \n | 新的一行。在类 Unix 系统中，产生换行。 |  
| \r | 回车符 |  
| \t | 制表符 |  
`echo -e "Time's up\a"` 或 `echo "Time's up" $'\a'`  
  
## Advanced Keyboard Tricks  
clear, history  
### Command Line Editing  
Cursor Movement  
`Ctrl-a` `Ctrl-e`  
`option-左右`  
`ctrl-l`  
Modifying Text  
`Ctrl-k` `Ctrl-u` `esc-d` `esc-delete` `esc-l` `esc-u` `ctrl-t` `esc-t`  
### Completion  
两下tab `esc-*`  
  
### Using History  
`history | less`  
`history | grep /usr/bin`搜历史中的‘/usr/bin’  
`!88`引用第88条历史命令  
  
## Permissions  
`id` `chmod`Change File Mode `umask` `su` `sudo` `chown`  
`chgrp` `passwd`  
### Owners, Group Members, And Everybody Else  
`cat /etc/shadow, /etc/group, /etc/passwd`查看组，用户情况。也可以用groupmod加两下tab  
### Reading, Writing, And Executing  
dir的x决定一切，w允许dir里文件的操作  
only the file’s owner or the superuser can change the mode of a file or directory  
  
bin程序相关，boot启动，dev设备，etc配置，lib库  
 touch创建file  
 .开头为隐藏文件  
   
 tree  
 `mv old_name new_name`  
   
  
  
## 压缩和解压  
`tar -zcvf name.tar.gz file_name`gz压缩打包  
`tar -zxvf name.tar.gz -C 路径`解包到某路径  
`tar -jcvf name.tar.bz2 file_name`bz2压缩打包  
`zip name.zip file_name`  
  
## process  
`ps` `top` `htop`  
  
## 开关机  
`reboot` `shutdown -h now`h后为停留的时间 `s`  
##状态  
`df -h`硬盘使用情况  
`du -h`当前路径大小  
## 网络  
`ifconfig`网络情况  
`ping ip地址`是否能跟该ip通信  
`ssh user_name@对方ip`远程控制，然后输对方密码  
## 用户  
`sudo useradd -m xx`添加用户，m为在home创建一个该用户的家目录。默认再创建一个组。而这个用户默认不在sudo和adm组里，即该用户不能用sudo  
`su - xx`切换用户，`-`为切换到该账户的家目录`exit`回到原来用户  
`sudo passwd xx`设置密码，再输为重设  
`whoami`  
`who`有ip的为远程  
`chown user_name file`  
`chgrp group_name file`改文件的组  
`chmod u=rwx，g=r, o=r file`修改权限u,g,o  
`chmod 731 file` r-4,w-2,x-1  
  
## 编辑器  
vim:   
### 默认命令模式 
光标：hjkl， HML, 20G到20行，G最后一行，gg首行，w,b单词长度移动，（,）段落  
翻页：ctrl+f,b  
yy复制当前行，4yy，p粘贴，ddji剪切一行,2dd，u撤销，ctrl+r反撤销，  
删除：D删到尾，d0删到首，x往后删，X往前删,dw删单词  
选择：V行，>>向右，.重复  
替换：r一个字母，R往后一直替代  
搜索：/,n,N  
进入编辑：i插入，大I行首，大A行末，o下一行，大O上一行  
### 编辑模式 
###末行模式  
`%s/abc/123/g` `1,10s/abc/123/g`  
`wq` `!q`不保持退出   
  
## shell编程  
`echo $SHELL`查看当前shell类型  
`echo $?`0为正常执行  
`vim xx.sh`  
```  
#! /bin/sh #说明用哪个shell执行  
```  
`chmod a+x file.sh`给执行权限，全部允许x  
`./file.sh`执行脚本 `.`  
或者不改权限`source file.sh`直接执行，结果会传递给shell  
### 变量  
  
环境变量env  
`env | grep 'PWD'`查环境变量位置  
unset 删除变量  
  
本地变量set（当前进程全部变量）  
设置本地变量时，“=”号两边不能有空格  
export variable_name变为环境变量  
栈保存本地变量  
  
`$variabel`取变量值  
赋值右边的默认为字符，除非```A=`cmd` ```，这时`echo $A`显示cmd执行结果。而数字就$(())来转换  
  
`$[base#n]`n转换为base进制  
`touch -- -hello`用--消除第三个-的效果  
  
### 语句  
test或[]判断：  
`[ 1 -gt 2 ]`判断1大于等于2，用`echo $?`显示结果。  
其他判断eq, ne, lt, le, ge  
`[ -d dir ]` `[ -f file ]` `[ -z string ]` `[ string1 = string2 ]`  
```  
#下面会报错，判断缺少arg  
s1=  
s2=hello  
[ $s1 = $s2 ]  
#正确如下  
[ "$s1" = $s2 ]  
#如果s1没有事先定义为空，结果一样  
```  
`[ ! exp ]` `[ exp1 -a exp2 ]` `[ exp1 -o exp2 ]`  
  
if语句  
```  
echo "ajkdfjk"  
read x  
if [ "$x" = "yes" ]; then  
    echo "jdkfjk"  
elif [ "$x" = "no" ]; then  
    echo "jdkfj"  
else  
    echo "?"  
    exit 1 # unix用return  
fi  
```  
`then:`表示什么都不用做  
case  
```  
case "$x" in  
yes|y|YES)  
    echo "jdkfj";;  
[nN]*)  
    echo "dfjdk";;  
*) #相当于else  
    echo "?"  
    return 1;;  
esac  
return 0  
```  
for  
```  
for i in a b c; do  
    echo "ddfd"  
done  
```  
while  
```  
while [ "$x" != "secret" ]; do  
    echo "Sorry, try again"  
    read x  
done  
```  
for i in {1..100}  
可以加break和continue  
  
### 位置参数和特殊变量  
```
# $1 参数位置,$0一般不用，代表文件本身  
# $# 参数数量 
# $* 表示参数数列
# $$ 当前进程
# shift 输入的参数位置向前移，如`cmd a b c`中b变为$1,a就没了 
```
  
### 函数  
```  
foo()  
{  
    echo "dfd"  
}  
```  
注意}和命令分开，否则命令的最后要加；  
  
### 调试  
`-n`检查语法错误  
部分调试  
```  
set -x  
...  
set +x  
```  
  
### 正则表达  
字符类  
`.`一个字符  
[a-z]  
^反  
[[:xxx:]]  
数量  
`[0-9]?\.[0-9]`匹配0.0, 2.3, .5等。?匹配零次或一次  
`[a-zA-Z0-9_.-]+@[a-zA-Z0-9_.-]+`匹配邮箱。+表示一次或多次  
`*`匹配零次或多次，当连续匹配时，有一个不匹配就停。匹配了一次就断了的不符合  
`[1-9][0-9]{2}`匹配100到999，精确2次  
`{N,}`至少N次  
`{,N}`最多N次  
`{N,M}`  
位置  
`^n`行首n  
`\<n`单词n开头`\>n`  
`\bnr\b`单词nr开头nr结尾`\B`  
特殊字符  
\  
()  
|  
  
grep搜索内容  
一般用egrep搜是扩展正则extended，如果直接用grep的话，`?+(){}`需要在前面加\转义，为basic  
  
`grep "hello" ./* -ni`查所有文件有"hello"的，且显示第几行,不区分大小写  
  
find搜索文件，可以按文件访问时间，大小，名字，权限，属主等等  
 `sudo find ./ -name "name"`sudo为在有权限，在当前目录find  
 `find /tmp -size +1M`在tmp目录下找1m以上的  
 `find ./ -size +4k -size -5m`之间  
 `find -amin n`最后n分钟访问的  
 `find . -type d -exec ls -l {} \;`.在当前文件，找dir，并执行ls -l，后面规定写法。注意-exec和|不一样，前者是find完后对得到的结果ls，后者只能当前ls  
`find . -name *.py | xargs file`这个xargs可以在find的同时输出，不需要像exec等到find完再执行  
  
### sed流编辑器  
`| tee new_file`一般后面配合这个  
`sed -n /pattern/p`打印匹配pattern的行；d为删除，-n为只输出匹配的  
`/pattern/s/pattern1/pattern2/g`查找符合pattern的行，并将符合pattern1的替换为pattern2；去掉g则只替换第一个。原文件不改变  
`sed "2,5d" file`删除2到5行  
`-r`extended正则  
`sed 's/bc/-&-/' file`将找到的bc变为-bc-，&代表找到的内容。s前面不加pattern，即文件全局找  
```  
<html><head><title>hello world</title></head>  
<body>welcome to the world of regexp!</body><html>  
  
sed 's/<[^>]*>//g file'  
```  
`awk`处理列  
  
##补充  
### 括号  
1.`${var}AA`  
2.`echo $(ls)`shell扫描一遍命令行，发现了`$(cmd)`结构，便将`$(cmd)`中的cmd执行一次，得到其标准输出，再将此输出放到原来命令echo `$(ls)`中的`$(ls)`位置  
3.()和{}都是对一串的命令进行执行，但有所区别：  
A,()只是对一串命令重新开一个子shell进行执行   
B,{}对一串命令在当前shell执行   
C,()和{}都是把一串的命令放在括号里面，并且命令之间用;号隔开   
D,()最后一个命令可以不用分号   
E,{}最后一个命令要用分号   
F,{}的第一个命令和左括号之间必须要有一个空格  
G,()里的各命令不必和括号有空格   
H,()和{}中括号里面的某个命令的重定向只影响该命令，但括号外的重定向则影响到括号里的所有命令  
4.`${var:=string}`若var为空时，用string替换`${var:=string}`的同时，把string赋给变量var  
5.匹配替换结构  
`${var%pattern}`去掉var右边最短的匹配模式,`${var%%pattern}`右边最长,`${var#pattern}`左边最短,`${var##pattern}`左边最长`  
6.${var/pattern1/pattern2}  
```  
var=testcase  
echo ${var%s*e}  
echo ${var%%s*e}  
```  
The output: testca, te  
### 数组  
```  
A=(a1 a2 a3)  
echo ${#A[@]}  
echo ${var/pattern1/pattern2} #替换  
unset A[1] #删除  
```  
  
## 练习  
```  
#! /bin/bash  
#$1 为要测试的日志文件  
awk '{print $1}' $1 | sort | uniq -c | sort -klnr | head -n3  
#访问前10的ip地址  
cat access.log|awk '{print $1}'|sort|uniq -c|sort -nr|head -10  
```  
1.计算1-100  
2.将一个目录下所有文件扩展名改为bak  
```  
for i in *.*; do  
    mv $1 ${i%%.*}.bak  
done  
```  
3.编译`gcc file0 -o file1`  
4.打印root可以使用可执行的文件数  
```  
$(find ~ -root -type f | xargs ls -l | sed '/-..x/p' | wc -l  
```  
5.找交换分区Swap的大小  
```  
free -m | sed -n '/Swap/p' | awk '{print $2}' #打印出来才能传递给awk？  
ps aux | sed -n '/nginx/p' | awk '{print $2}' | head -n2  
```  
6.
```  
cat /etc/passwd | awk -F: '{if ($7!="") print $7}' | sort | uniq -c  
#-F分隔符为：，  
```  
7.文件整理  
`join file1.txt file2.txt | sort -k 2`默认相同列为主键？然后按第二行sort  


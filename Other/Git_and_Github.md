 **1.选择位置**     
`mkdir learngit` #创建一个实验git的文档learngit   
`cd learngit  
pwd`

**2.创建版本库**  
`git init` #把当前目录变成Git可以管理的仓库(.git文件隐藏的)
`iv readme.txt` #创建一个txt
`git add readme.txt`#添加到缓存区
`git commit -m "命名" `#提交修改，并对本次修改命名
每次修改，如果不用git add到暂存区，那就不会加入到commit中
`git status` #查看工作区情况
`git diff reade.txt` #查看变化
Git自动为我们创建了唯一一个master分支，所以，现在，git commit就是往master分支上提交更改。

**3.读档**  
`git log` #回到过去前，查看提交历史。加上 --pretty=oneline --abbrev-commit更简洁
`git reflog` #回到未来前，查看命令历史。如果退出后找不到提交历史时用
`git reset --hard commit_id` #读取哪个版本，不需要全输。或者用HEAD^^/该命令还可以把在暂存区的修改退回工作间
`git checkout -- readme.txt` #撤销当前阶段的全部修改（未commit的，但在工作区已修改或已add的）

**4.删除**  
`rm file` #对工作区的操作
`git rm file`和`git commit` #对版本库的操作
`git checkout -- test.txt` #能找回误删，但只能是最新的版本  

**5.本地Git仓库和GitHub仓库之间的传输是通过SSH加密**   
如果.ssh目录下没有`id_rsa`和`id_rsa.pub`  
创建SSH Key `ssh-keygen -t rsa -C "youremail@example.com"`一路回车，一般不用密码。然后在github创建SSH Key时复制id_rsa.pub到key里。可以多个key（不同电脑）  

**6.本地git和远程git**    
创建远程git  
`git remote add origin git@github.com:本地路径.git`  
关联git库  
`git push -u origin master` #远程git为空，加-u，以后再用这条命令（去掉-u）来更新库    
当你第一次使用Git的clone或者push命令连接GitHub时，会得到一个警告一般输入yes回车就是了
查看远程库
`git remote -v`
推送分支，一般master主分支和dev开发分支都要同步
`git push origin 分支名/标签名` 
创建远程origin的dev分支到本地，本地和远程分支的名称最好一致  
`git checkout -b dev origin/dev`  
push冲突时
`git pull`  有时要设定远程和本地的链接，即pull到哪个分支上。此时用`git branch --set-upstream-to branch-name origin/branch-name`后再用pull


**7.克隆**  
`git clone git@github.com:github_account/gitskills（要clone的文件名）.git`
Git支持多种协议，包括https，但通过ssh支持的原生git协议速度最快。  

**8.分支**  
创建dev分支，并切换到dev分支  
`git checkout -b dev`  
查看分支  
`git branch`  
换回master分支  
`git checkout master`  
当前合并到dev  (冲突后用git status查看冲突情况，然后vi, add和commit修复)
`git merge dev`  
禁用fast forward，Git就会在merge时生成一个新的commit，这样，从分支历史上就可以看出分支信息。
`git merge --no-ff -m "merge with no-ff" dev`
删除分支  
`git branch -d dev` 或 `git branch -D <name>`（用于没合并的分支）  
挂起分支(正在dev上)  
`git stash`  
回归挂起的分支
`git checkout dev`  
`git stash list`  
`git stash pop`或`git stash apply`+`git stash drop`  
每添加一个新功能，最好新建一个feature分支
把历史分叉变为直线  
`git rebase`

**9.标签**  
切换到打标签的分支上  
`git tag 名字 (commit id)` 不加名字就查看所有tag，默认打在最新commit上  
`git tag -a v0.1 -m "version 0.1 released" 1094adb`-a指定标签名，-m指定说明文字  
查看标签信息  
`git show <tagname>`  
如果这个commit既出现在master分支，又出现在dev分支，那么在这两个分支上都可以看到这个标签。  
删除  
`git tag -d v0.1`  
推送所有标签到远程  
`git push origin --tags`
推送后的删除(先删本地)  
`git tag -d v0.9`  
`git push origin :refs/tags/v0.9`  

**10.自定义**  
显示颜色  
`git config --global color.ui true`  
忽略特殊文件  

配置别名  
`git config --global alias.last 'log -1'`(用git last就能显示最近一次的提交)  
`git config --global alias.lg "log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit"`  
加上--global是针对当前用户起作用的，如果不加，那只针对当前的仓库起作用  

配置文件    
`cat .git/config`当前库  
`cat .gitconfig`当前用户  

搭建Git服务器





# .gitignore

> 在使用Git的过程中，我们喜欢有的文件比如日志，临时文件，编译的中间文件等不要提交到代码仓库，这时就要设置相应的忽略规则，来忽略这些文件的提交。简单来说一个场景：在使用git add .的时候，遇到了把你不想提交的文件也添加到了缓存中去的情况，比如项目的本地配置信息，如果你上传到Git中去其他人pull下来的时候就会和他本地的配置有冲突，所以这样的个性化配置文件我们一般不把它推送到 git 服务器中中，但是又为了偷懒每次添加缓存的时候都想用git add .而不是手动一个一个文件添加，该怎么办呢？很简单，git为我们提供了一个.gitignore文件只要在这个文件中申明那些文件你不希望添加到git中去，这样当你使用git add .的时候这些文件就会被自动忽略掉。

![](https://oss.yiki.tech/gmall/202303080349044.png)

> 以下是我常用的 忽略文件配置

```.gitignore
# Java
*.iml
.idea/
target/
.mvn/
mvnw*
logs/

# Compiled class file
*.class

# Log file
*.log

# BlueJ files
*.ctxt

# Mobile Tools for Java (J2ME)
.mtj.tmp/

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# virtual machine crash logs, see http://www.java.com/en/download/help/error_hotspot.xml
hs_err_pid*
```
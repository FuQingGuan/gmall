# git 忽略文件

> .gitignore 文件是一个文本文件，通常位于 Git 仓库的根目录，用于指定 Git 忽略哪些文件或目录，这些文件或目录不会被 Git 管理或跟踪。
>
> 当我们在开发项目时，有些文件不需要纳入版本管理，如编译生成的中间文件、日志文件、临时文件等等，这些文件不但占用空间，而且对代码维护也没有帮助，因此可以将其加入到 .gitignore 文件中，这样 Git 在提交代码时就会自动忽略这些文件。

## 语法

| 语法 | 作用                                                         |
| ---- | ------------------------------------------------------------ |
| #    | `#` 开头的行表示注释，将被 Git 忽略                          |
|      | 空行将被 Git 忽略                                            |
| *    | 匹配任意数量的字符，但不包括目录分隔符（`/`）                |
| **   | 匹配任意数量的字符，包括目录分隔符                           |
| ?    | 匹配一个字符，但不包括目录分隔符                             |
| !    | 表示不忽略，即使之前有类似规则，也要包含这个文件或文件夹     |
| /    | 以斜杠（`/`）开头表示仅匹配该文件夹下的文件或文件夹，而不匹配其子文件夹 |
| /    | 以斜杠结尾表示仅匹配该文件夹，而不匹配其下的任何文件或文件夹 |
| !    | 如果文件或文件夹名字前面有一个感叹号（`!`），表示不忽略该文件或文件夹，即使之前有类似规则，也要包含这个文件或文件夹 |

### 例子

```shell
# 忽略所有 .class 文件
*.class

# 但是不忽略 App.class 文件
!App.class

# 忽略 build 目录下的所有文件
build/

# 忽略 doc 目录下的所有 .txt 文件
doc/*.txt

# 忽略 doc 目录下的所有 .pdf 文件
doc/**/*.pdf
```

## 我常用的 .gitignore 配置

```shell
# Java
*.iml
.idea/
target/
.mvn/
mvnw*
logs/
```

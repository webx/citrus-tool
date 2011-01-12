#!/bin/bash

#-------------------------------------------------------------------
#    需要设置如下环境变量：
#
#      JAVA_HOME           - JDK的安装路径
#-------------------------------------------------------------------

# 判断是否在cygwin环境下
cygwin=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

# 确定安装了java，并设置了JAVA_HOME环境变量.
noJavaHome=false
if [ -z "$JAVA_HOME" ] ; then
    noJavaHome=true
fi
if $cygwin ; then
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath -u "$JAVA_HOME"`
fi
if [ ! -e "$JAVA_HOME/bin/java" ] ; then
    noJavaHome=true
fi

# 设置JAVA_CMD
if $noJavaHome
then JAVA_CMD=`which java`
else JAVA_CMD="$JAVA_HOME/bin/java"
fi

noJavaCmd=false
if [ -z "$JAVA_CMD" ] ; then
    noJavaCmd=true
fi
if $cygwin ; then
    [ -n "$JAVA_CMD" ] &&
        JAVA_CMD=`cygpath -u "$JAVA_CMD"`
fi

if $noJavaCmd ; then
    echo "Error: JAVA_HOME environment variable is not set."
    exit 1
fi

# 执行java -jar.
CMD="exec \"$JAVA_CMD\" -jar \"$0\" $@"
eval $CMD

exit 0;

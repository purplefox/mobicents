#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Mobicents Media Server -JSR309 Example Bootstrap Script                 ##
##                                                                          ##
### ====================================================================== ###

### $Id: run.sh abhayani@redhat.com $ ###

DIRNAME=`dirname $0`
PROGNAME=`basename $0`
GREP="grep"

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

#
# Helper to complain.
#
warn() {
    echo "${PROGNAME}: $*"
}

#
# Helper to puke.
#
die() {
    warn $*
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
        
    Linux)
        linux=true
        ;;
esac


# Force IPv4 on Linux systems since IPv6 doesn't work correctly with jdk5 and lower
if [ "$linux" = "true" ]; then
   JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JSR309_EXAMPLE_HOME" ] &&
        JSR309_EXAMPLE_HOME=`cygpath --unix "$JSR309_EXAMPLE_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup JSR309_EXAMPLE_HOME
if [ "x$JSR309_EXAMPLE_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JSR309_EXAMPLE_HOME=`cd $DIRNAME/..; pwd`
fi
export JSR309_EXAMPLE_HOME

# Increase the maximum file descriptors if we can
if [ "$cygwin" = "false" ]; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ]; then
	if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ]; then
	    # use the system max
	    MAX_FD="$MAX_FD_LIMIT"
	fi

	ulimit -n $MAX_FD
	if [ $? -ne 0 ]; then
	    warn "Could not set maximum file descriptor limit: $MAX_FD"
	fi
    else
	warn "Could not query system maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

# Setup the classpath
runjar="$JSR309_EXAMPLE_HOME/bin/run.jar"
if [ ! -f "$runjar" ]; then
    die "Missing required file: $runjar"
fi

JSR309_EXAMPLE_BOOT_CLASSPATH="$runjar"

if [ "x$JSR309_EXAMPLE_CLASSPATH" = "x" ]; then
    JSR309_EXAMPLE_CLASSPATH="$JSR309_EXAMPLE_BOOT_CLASSPATH"
else
    JSR309_EXAMPLE_CLASSPATH="$JSR309_EXAMPLE_CLASSPATH:$JSR309_EXAMPLE_BOOT_CLASSPATH"
fi


# If -server not set in JAVA_OPTS, set it, if supported
SERVER_SET=`echo $JAVA_OPTS | $GREP "\-server"`
if [ "x$SERVER_SET" = "x" ]; then

    # Check for SUN(tm) JVM w/ HotSpot support
    if [ "x$HAS_HOTSPOT" = "x" ]; then
	HAS_HOTSPOT=`"$JAVA" -version 2>&1 | $GREP -i HotSpot`
    fi

    # Enable -server if we have Hotspot, unless we can't
    if [ "x$HAS_HOTSPOT" != "x" ]; then
	# MacOS does not support -server flag
	if [ "$darwin" != "true" ]; then
	    JAVA_OPTS="-server $JAVA_OPTS"
	fi
     fi
fi

# Setup MMS specific properties
JAVA_OPTS="-Dprogram.name=$PROGNAME $JAVA_OPTS"

# Setup the java endorsed dirs
JSR309_EXAMPLE_ENDORSED_DIRS="$JSR309_EXAMPLE_HOME/lib"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JSR309_EXAMPLE_HOME=`cygpath --path --windows "$JSR309_EXAMPLE_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    JSR309_EXAMPLE_CLASSPATH=`cygpath --path --windows "$JSR309_EXAMPLE_CLASSPATH"`
    JSR309_EXAMPLE_ENDORSED_DIRS=`cygpath --path --windows "$JSR309_EXAMPLE_ENDORSED_DIRS"`
fi

# Display our environment
echo "========================================================================="
echo ""
echo "  MMS JSR309 Example Bootstrap Environment"
echo ""
echo "  JSR309_EXAMPLE_HOME: $JSR309_EXAMPLE_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  CLASSPATH: $JSR309_EXAMPLE_CLASSPATH"
echo ""
echo "========================================================================="
echo ""

      "$JAVA" $JAVA_OPTS \
         -Djava.ext.dirs="$JSR309_EXAMPLE_ENDORSED_DIRS" \
         -classpath "$JSR309_EXAMPLE_CLASSPATH" \
         org.mobicents.servers.media.examples.jsr309.Jsr309Example "$@"
      JSR309_EXAMPLE_STATUS=$?
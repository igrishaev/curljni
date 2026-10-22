
SHARED_LIB = resources/$(shell java java/main/org/curljni/OS.java)

SRC_PATH = java/main/org/curljni
C_PATH = c

JAVAFILES = \
	$(SRC_PATH)/Arena.java \
	$(SRC_PATH)/Const.java \
	$(SRC_PATH)/ReadStream.java \
	$(SRC_PATH)/OS.java \
	$(SRC_PATH)/WriteCallback.java \
	$(SRC_PATH)/WriteStream.java \
	$(SRC_PATH)/IResource.java \
	$(SRC_PATH)/Accumulator.java \
	$(SRC_PATH)/Headers.java \
	$(SRC_PATH)/FILE.java \
	$(SRC_PATH)/Native.java \
	$(SRC_PATH)/CurlEasy.java \
	$(SRC_PATH)/Err.java \
	$(SRC_PATH)/IWriteHandler.java

MODULES = \
	$(C_PATH)/Foo.c \
	$(C_PATH)/bytebuffer.c \
	$(C_PATH)/accum.c \
	$(C_PATH)/read_data.c \
	$(C_PATH)/curl_data.c \
	$(C_PATH)/write_data.c \
	$(C_PATH)/org_curljni_Arena.c \
	$(C_PATH)/org_curljni_ReadStream.c \
	$(C_PATH)/org_curljni_WriteCallback.c \
	$(C_PATH)/org_curljni_WriteStream.c \
	$(C_PATH)/org_curljni_Accumulator.c \
	$(C_PATH)/org_curljni_Headers.c \
	$(C_PATH)/org_curljni_FILE.c \
	$(C_PATH)/org_curljni_Native.c \
	$(C_PATH)/org_curljni_CurlEasy.c \

OBJECTS = $(MODULES:.c=.o)

JAVA_HOME ?= $(error Please specify JAVA_HOME)

JAVA_INC = \
	-I${JAVA_HOME}/include \
	-I${JAVA_HOME}/include/darwin \
	-I${JAVA_HOME}/include/win32 \
	-I${JAVA_HOME}/include/linux

CURL_HOME = /opt/homebrew/opt/curl

CC = gcc
CFLAGS = -Wall -ansi -pedantic -fPIC ${JAVA_INC} -I${CURL_HOME}/include -L${CURL_HOME}/lib # -DDEBUG

LIBS = -lcurl

all: clear headers sep $(SHARED_LIB) sync-resources

CLASSES_DIR = target/classes

sync-resources:
	mkdir -p $(CLASSES_DIR)
	cp resources/* $(CLASSES_DIR)

sep:
	$(info .........................................)
	$(info .........................................)
	$(info .........................................)

headers:
	javac -h $(C_PATH) $(JAVAFILES)

%.o: %.c %.h
	$(CC) $(CFLAGS) -c -o $@ $*.c

$(SHARED_LIB): $(OBJECTS)
	$(CC) $(CFLAGS) $(LIBS) -shared -o $@ $(OBJECTS)

clear:
	rm -rf target
	find . -name '*.log'     -delete
	find . -name '*.o'       -delete
	find . -name '*.dylib'   -delete
	find . -name '*.lib'     -delete
	find . -name '*.so'      -delete
	find . -name '*.dll'     -delete
	find . -name '*.class'   -delete
	find . -name '.DS_Store' -delete

acc:
	$(CC) -Wall -ansi -pedantic -c -o accum.o accum.c

module ?= $(error module=... parameter not set)

new-module: SENTRY = __$(shell echo $(module) | tr 'a-z' 'A-Z')_H__
new-module: FILE_H = $(module).h
new-module: FILE_C = $(module).c
new-module:
	touch $(FILE_C)
	echo "#include \"$(FILE_H)\""    	  >> $(FILE_C)
	touch $(FILE_H)
	echo "#ifndef $(SENTRY)"         	  >> $(FILE_H)
	echo "#define $(SENTRY)"         	  >> $(FILE_H)
	echo ""                          	  >> $(FILE_H)
	echo "/* A big thing starts here! */" >> $(FILE_H)
	echo ""                          	  >> $(FILE_H)
	echo "#endif /* $(SENTRY) */"    	  >> $(FILE_H)
	mv $(module).* c

repl:
	DEBUG=1 lein with-profile +test repl

test:
	mvn test

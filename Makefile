JAVA_FILES:=$(shell find lib/kppk/cli/java/kppk.cli java/kppk.jpx -name '*.java' -not -name 'module-info.java'| paste -sd ' ' -)
TEST_FILES:=$(shell find java/kppk.jpx -name '*Test.java' -not -name 'module-info.java' | sed "s/java\/kppk\.jpx\///g" | sed "s/\.java//g" | sed "s/\//./g"| paste -sd ' ' -)

ifndef GRAAL_HOME
$(error GRAAL_HOME is not set, install GraalVM from https://www.graalvm.org/ and set GRAAL_HOME)
endif

NATIVE:=$(GRAAL_HOME)/bin/native-image
JAVAC:=$(GRAAL_HOME)/bin/javac
JAVA:=$(GRAAL_HOME)/bin/java

.PHONY: javac
javac: target
	@$(JAVAC) -d target -sourcepath java/kppk.jpx:lib/kppk/cli/java/kppk.cli $(JAVA_FILES)

.PHONY: test
test: javac $(TEST_FILES)
$(TEST_FILES):
	TEST $@
	@$(JAVA) -cp target -ea $@

target:
	mkdir target

.PHONY: native
native: javac
	$(NATIVE) $(NATIVE_FLAGS) -H:Name=jpx -H:+ReportUnsupportedElementsAtRuntime \
		--delay-class-initialization-to-runtime=kppk.jpx.config.JPXConfig \
		--delay-class-initialization-to-runtime=kppk.jpx.json.impl.JSONMessages \
		-cp target kppk.jpx.Main
	mkdir -p ./bin
	mv jpx ./bin/jpx$(NATIVE_SUFFIX)
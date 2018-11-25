JAVA_FILES:=$(shell find java/kppk.jpx -name '*.java'| paste -sd ' ' -)

ifndef GRAAL_HOME
$(error GRAAL_HOME is not set, install GraalVM from https://www.graalvm.org/ and set GRAAL_HOME)
endif

NATIVE:=$(GRAAL_HOME)/bin/native-image
JAVAC:=$(GRAAL_HOME)/bin/javac

.PHONY: javac
javac:
	@$(JAVAC) -d target -sourcepath java/kppk.jpx $(JAVA_FILES)


.PHONY: native
native: javac
	$(NATIVE) $(NATIVE_FLAGS) -H:Name=jpx -H:+ReportUnsupportedElementsAtRuntime -cp target kppk.jpx.Main
	mkdir -p ./bin
	mv jpx ./bin/jpx
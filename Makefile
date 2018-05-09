JAVA_FILES:=$(shell find src -name '*.java'| paste -sd ' ' -)

ifndef GRAAL_HOME
$(error GRAAL_HOME is not set, install GraalVM from https://www.graalvm.org/ and set GRAAL_HOME)
endif

NATIVE:=$(GRAAL_HOME)/bin/native-image
JAVAC:=$(GRAAL_HOME)/bin/javac

.PHONY: javac
javac:
	@$(JAVAC) -d target -sourcepath src $(JAVA_FILES)


.PHOHY: native
native: javac

	$(NATIVE) -cp target org.jpx.Main
	mkdir -p ./bin
	mv org.jpx.Main ./bin/jpx
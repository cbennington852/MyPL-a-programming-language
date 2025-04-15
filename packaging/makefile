#!/bin/sh

TEMP_DIR=temp
MYPL_LOCATION=../mypl
JAR_LOCATION=../target/mypl-jar-with-dependencies.jar
DEBIAN_LOCATION=

build:
	mvn compile assembly:single
test:
	mvn test
build-deb:
	echo "building deb"
	mkdir -p $(TEMP_DIR)
	mkdir -p $(TEMP_DIR)/usr/local/mypl
	mkdir -p $(TEMP_DIR)/DEBIAN
	cp $(JAR_LOCATION) $(TEMP_DIR)/usr/local/mypl
	cp $(DEBIAN_LOCATION)DEBIAN/postinst $(TEMP_DIR)/DEBIAN/
	cp $(DEBIAN_LOCATION)DEBIAN/copyright $(TEMP_DIR)/DEBIAN/
	cp $(DEBIAN_LOCATION)DEBIAN/changelog $(TEMP_DIR)/DEBIAN/
	cp $(DEBIAN_LOCATION)DEBIAN/postrm $(TEMP_DIR)/DEBIAN/
	cp $(DEBIAN_LOCATION)DEBIAN/control $(TEMP_DIR)/DEBIAN/
	chmod +x $(TEMP_DIR)/DEBIAN/postinst
	chmod +x $(TEMP_DIR)/DEBIAN/postrm
	chmod +x $(TEMP_DIR)/usr/local/mypl/mypl-jar-with-dependencies.jar
	dpkg-deb --root-owner-group --build $(TEMP_DIR)
	mv $(TEMP_DIR).deb mypl.deb
lint-deb:
	echo "linting deb"
	#lintian  counter-v2.0.0.deb || true


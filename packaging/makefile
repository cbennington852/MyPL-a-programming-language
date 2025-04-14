#!/bin/sh

TEMP_DIR=temp
MYPL_LOCATION=mypl
JAR_LOCATION=mypl-jar-with-dependencies.jar

build:
	mvn compile assembly:single
test:
	mvn test
build-deb:
	echo "building deb"
	mkdir -p $(TEMP_DIR)
	mkdir -p $(TEMP_DIR)/usr/local/mypl
	cp $(MYPL_LOCATION) /usr/local/mypl
	cp $(JAR_LOCATION) /usr/local/mypl
	cp packaging/DEBIAN/postinst $(TEMP_DIR)/DEBIAN/
	cp packaging/DEBIAN/copyright $(TEMP_DIR)/DEBIAN/
	cp packaging/DEBIAN/changelog $(TEMP_DIR)/DEBIAN/
	cp packaging/DEBIAN/postrm $(TEMP_DIR)/DEBIAN/
	cp packaging/DEBIAN/prerm $(TEMP_DIR)/DEBIAN/
	chmod +x $(TEMP_DIR)/DEBIAN/postinst
	chmod +x $(TEMP_DIR)/DEBIAN/postrm
	chmod +x $(TEMP_DIR)/DEBIAN/prerm
	dpkg-deb --root-owner-group --build $(TEMP_DIR)
	mv $(TEMP_DIR).deb mypl.deb
lint-deb:
	echo "linting deb"
	#lintian  counter-v2.0.0.deb || true


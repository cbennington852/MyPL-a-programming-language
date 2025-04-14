 #!/bin/bash

mkdir /usr/local/mypl
cp mypl /usr/local/mypl
cp mypl-jar-with-dependencies.jar /usr/local/mypl

# Install Java 21 JDK
sudo apt update
sudo apt install -y openjdk-21-jdk

# Add alias only if it doesn't already exist
if ! grep -q "alias mypl=" ~/.bashrc; then
    echo "alias mypl='/usr/local/mypl/mypl'" >> ~/.bashrc
    echo "Alias added. You may need to run 'source ~/.bashrc' or restart your terminal."
else
    echo "Alias already exists in .bashrc"
fi

source ~/.bashrc

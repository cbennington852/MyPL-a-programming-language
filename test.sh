mvn compile assembly:single
FILE_NAME=$1
printf "\n\e[40;35m=======================MYPL CODE=================================\e[0m\n"
cat -n $FILE_NAME
printf "\n\e[40;36m=======================VM CODE=================================\e[0m\n"
./mypl -m IR $FILE_NAME
printf "\n\e[40;32m=======================OUTPUT==================================\e[0m\n"
if ./mypl $FILE_NAME > /dev/null 2>&1; then
    ./mypl $FILE_NAME
else 
    printf "\e[31m"
    ./mypl $FILE_NAME
    printf "\e[0m\n"
fi

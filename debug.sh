mvn compile assembly:single
printf "\n\e[40;35m=======================MYPL CODE=================================\e[0m\n"
cat -n test.mypl
printf "\n\e[40;36m=======================VM CODE=================================\e[0m\n"
./mypl -m IR test.mypl 
printf "\n\e[40;37m=======================DEBUG=================================\e[0m\n"
./mypl -m DEBUG test.mypl
printf "\n\e[40;32m=======================OUTPUT==================================\e[0m\n"
if ./mypl test.mypl > /dev/null 2>&1; then
    ./mypl test.mypl
else 
    printf "\e[31m"
    ./mypl test.mypl
    printf "\e[0m\n"
fi
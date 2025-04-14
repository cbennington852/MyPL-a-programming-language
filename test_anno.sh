mvn compile assembly:single
FILE_NAME=$1
printf "\n\e[40;35m=======================MYPL CODE=================================\e[0m\n"
cat -n $FILE_NAME
printf "\n\e[40;36m=======================LEXER=================================\e[0m\n"
./mypl -m LEX $FILE_NAME
printf "\n\e[40;36m=======================PARSE=================================\e[0m\n"
./mypl -m PARSE $FILE_NAME
printf "\n\e[40;35m=======================PRINT=================================\e[0m\n"
./mypl -m PRINT $FILE_NAME
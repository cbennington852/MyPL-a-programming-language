LC_NUMERIC="en_US.UTF-8"

echo "Source Files : "
cat src/main/java/cpsc326/*.java | printf "%'d\n" $(wc -l)

echo "Test Files : "
cat src/test/java/cpsc326/*.java | printf "%'d\n" $(wc -l)

echo "Total Line count : "
cat src/test/java/cpsc326/*.java src/main/java/cpsc326/*.java  | printf "%'d\n" $(wc -l)
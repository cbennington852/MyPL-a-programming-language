/**
 * CPSC 326, Spring 2025
 * Example program 1 for HW-5.
 */

package cpsc326;

/**
 * Class for HW5 program 1.
 */
public class HW5Prog1 {

  // Implement the following MyPL program: 
  // 
  // bool is_prime(n: int) {
  //   var m: int = n / 2
  //   var v: int = 2
  //   while v <= m {
  //     var r: int = n / v
  //     var p: int = r * v
  //     if p == n {
  //       return false
  //     }
  //     v = v + 1
  //   }
  //   return true
  // }
  //
  // void main() {
  //   println("Please enter integer values to sum (prime to quit)")
  //   var sum: int = 0
  //   while true {
  //     print("Enter an int: ")
  //     var val: int = int_val(readln())
  //     if is_prime(val) {
  //       println("The sum is: " + str_val(sum))
  //       println("Goodbye!")
  //       return null
  //     }
  //     sum = sum + val
  //   }
  // }
  
  public static void main(String[] args) {
    VMFrameTemplate m = new VMFrameTemplate("main");
    VMFrameTemplate p = new VMFrameTemplate("is_prime");

    // TODO: Add the instructions for main (m) and is_prime (p)
    //       templates to implement the corresponding main and
    //       is_prime functions above.

  // bool is_prime(n: int) {
  p.add(VMInstr.STORE(0)); //0 == n
  //   var m: int = n / 2
  p.add(VMInstr.LOAD(0));
  p.add(VMInstr.PUSH(2));
  p.add(VMInstr.DIV());
  p.add(VMInstr.STORE(1)); // 1 == m
  //   var v: int = 2
  p.add(VMInstr.PUSH(2));
  p.add(VMInstr.STORE(2)); //2 == v
  //   while v <= m {
    p.add(VMInstr.LOAD(2)); // v
    p.add(VMInstr.LOAD(1)); // m
    p.add(VMInstr.CMPLE());
    p.add(VMInstr.JMPF(32)); //figure out later
    //     var r: int = n / v
    p.add(VMInstr.NOP());//9
    p.add(VMInstr.LOAD(0)); // n
    p.add(VMInstr.LOAD(2)); // v
    p.add(VMInstr.DIV());
    p.add(VMInstr.STORE(3)); //r
    //     var p: int = r * v
    p.add(VMInstr.LOAD(3)); //r
    p.add(VMInstr.LOAD(2)); //v
    p.add(VMInstr.MUL());
    p.add(VMInstr.STORE(4)); //p
    //     if p == n {
    //       return false
    //     }
      p.add(VMInstr.LOAD(4)); //p
      p.add(VMInstr.LOAD(0)); //n
      p.add(VMInstr.CMPEQ());
      p.add(VMInstr.JMPF(26)); //figure out later
      //inside the if loop
      p.add(VMInstr.PUSH(false));
      p.add(VMInstr.RET());
      //end of the if loop
      p.add(VMInstr.NOP()); //26
    //     v = v + 1
    p.add(VMInstr.LOAD(2));//v
    p.add(VMInstr.PUSH(1));
    p.add(VMInstr.ADD());
    p.add(VMInstr.STORE(2));//v
    p.add(VMInstr.JMP(7)); //figure out later
  //   }
  p.add(VMInstr.NOP()); //32
  p.add(VMInstr.PUSH(true));
  p.add(VMInstr.RET());
  //   return true
  // }
    


  // void main() {
  //   println("Please enter integer values to sum (prime to quit)")
  m.add(VMInstr.PUSH("Please enter integer values to sum (prime to quit)\n"));
  m.add(VMInstr.WRITE());
  //   var sum: int = 0
  m.add(VMInstr.PUSH(0));
  m.add(VMInstr.STORE(0));

  //   while true {
  //     print("Enter an int: ")
  //     var val: int = int_val(readln())
  //     if is_prime(val) {
  //       println("The sum is: " + str_val(sum))
  //       println("Goodbye!")
  //       return null
  //     }
  //     sum = sum + val
  //   }
  // }
  m.add(VMInstr.NOP());
  m.add(VMInstr.PUSH(true));
  m.add(VMInstr.JMPF(20));
    //inside while
    m.add(VMInstr.PUSH("Enter an int: "));
    m.add(VMInstr.WRITE());
    m.add(VMInstr.READ());
    m.add(VMInstr.TOINT());
    m.add(VMInstr.STORE(1));
    //if stmt
      m.add(VMInstr.LOAD(1));
      m.add(VMInstr.CALL("is_prime"));
      m.add(VMInstr.JMPF(24));
      m.add(VMInstr.PUSH("The sum is: "));
      m.add(VMInstr.LOAD(0));
      m.add(VMInstr.TOSTR());
      m.add(VMInstr.ADD());
      m.add(VMInstr.WRITE());
      m.add(VMInstr.PUSH("\nGoodbye!\n"));
      m.add(VMInstr.WRITE());
      m.add(VMInstr.PUSH(VM.NULL));
      m.add(VMInstr.RET());
    m.add(VMInstr.NOP());//and of if24
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.LOAD(1));
    m.add(VMInstr.ADD());
    m.add(VMInstr.STORE(0));
    m.add(VMInstr.JMP(5));

    // create the vm: 
    VM vm = new VM();
    vm.debugMode(false);
    // add the frames to the vm:
    vm.add(m);
    vm.add(p);
    // run the program: 
    vm.run();
  }
}

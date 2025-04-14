/**
 * CPSC 326, Spring 2025
 * Example program 2 for HW-5.
 */

package cpsc326;

/**
 * Class for HW5 program 2.
 */
public class HW5Prog2 {

  // Implement the following MyPL program: 
  // 
  // struct Team {
  //   name: string, 
  //   wins: int, 
  //   games: int
  // }
  // 
  // void main() {
  //   var teams: Team = new Team[2]
  //   teams[0] = new Team("a", 10, 20);
  //   teams[1] = new Team("b", 18, 20);
  //   double sum = 0.0;
  //   sum = sum + (dbl_val(teams[0].wins) / dbl_val(teams[0].games))
  //   sum = sum + (dbl_val(teams[1].wins) / dbl_val(teams[1].games))
  //   print("The average win percentage is: ")
  //   print(sum / 2)
  //   println("")
  // }
  
  public static void main(String[] args) {
    VMFrameTemplate m = new VMFrameTemplate("main");

    // TODO: Add the instructions to the main (m) template to
    //       implement the above main function.

  // struct Team {
  //   name: string, 
  //   wins: int, 
  //   games: int
  // }

  // 
  // void main() {
  //   var teams: Team = new Team[2]
  m.add(VMInstr.PUSH(2));
  m.add(VMInstr.ALLOCA());
  m.add(VMInstr.STORE(0));
  //   teams[0] = new Team("a", 10, 20);
  m.add(VMInstr.LOAD(0)); //array to add to 
  m.add(VMInstr.PUSH(0)); //index
  m.add(VMInstr.ALLOCS());
  m.add(VMInstr.DUP());
  m.add(VMInstr.DUP());
  m.add(VMInstr.DUP());
  m.add(VMInstr.PUSH("a"));
  m.add(VMInstr.SETF("name"));
  m.add(VMInstr.PUSH(10));
  m.add(VMInstr.SETF("wins"));
  m.add(VMInstr.PUSH(20));
  m.add(VMInstr.SETF("games"));
  m.add(VMInstr.SETI());
  //   teams[1] = new Team("b", 18, 20);
   m.add(VMInstr.LOAD(0)); //array to add to 
  m.add(VMInstr.PUSH(1)); //index
  m.add(VMInstr.ALLOCS());
  m.add(VMInstr.DUP());
  m.add(VMInstr.DUP());
  m.add(VMInstr.DUP());
  m.add(VMInstr.PUSH("b"));
  m.add(VMInstr.SETF("name"));
  m.add(VMInstr.PUSH(18));
  m.add(VMInstr.SETF("wins"));
  m.add(VMInstr.PUSH(20));
  m.add(VMInstr.SETF("games"));
  m.add(VMInstr.SETI());
  //   double sum = 0.0;
  m.add(VMInstr.PUSH(0.0));
  m.add(VMInstr.STORE(1));
  /////////////////////////////////////////////////////////////////////////////////
  //   sum = sum + (dbl_val(teams[0].wins) / dbl_val(teams[0].games))
  ////////////////////////////////////////////////////////////////////////////////
  //accessing teams[0].wins
  m.add(VMInstr.LOAD(0)); //oid 
  m.add(VMInstr.PUSH(0)); //index
  m.add(VMInstr.GETI()); //get the array oid
  m.add(VMInstr.GETF("wins")); //get the field of thing
  //applying dbl_val
  m.add(VMInstr.TODBL()); //to double
  //accessing teams[0].games
  m.add(VMInstr.LOAD(0)); //oid 
  m.add(VMInstr.PUSH(0)); //index
  m.add(VMInstr.GETI()); //get the array oid
  m.add(VMInstr.GETF("games")); //get the field of thing
  //applying dbl_val
  m.add(VMInstr.TODBL()); //to double
  //dividing
  m.add(VMInstr.DIV()); //to double
  m.add(VMInstr.LOAD(1)); //to double
  m.add(VMInstr.ADD()); //to double
  m.add(VMInstr.STORE(1)); //to double
  /////////////////////////////////////////////////////////////////////////////////
  //   sum = sum + (dbl_val(teams[1].wins) / dbl_val(teams[1].games))
  ////////////////////////////////////////////////////////////////////////////////
  //accessing teams[1].wins
  m.add(VMInstr.LOAD(0)); //oid 
  m.add(VMInstr.PUSH(1)); //index
  m.add(VMInstr.GETI()); //get the array oid
  m.add(VMInstr.GETF("wins")); //get the field of thing
  //applying dbl_val
  m.add(VMInstr.TODBL()); //to double
  //accessing teams[1].games
  m.add(VMInstr.LOAD(0)); //oid 
  m.add(VMInstr.PUSH(1)); //index
  m.add(VMInstr.GETI()); //get the array oid
  m.add(VMInstr.GETF("games")); //get the field of thing
  //applying dbl_val
  m.add(VMInstr.TODBL()); //to double
  //dividing
  m.add(VMInstr.DIV()); //to double
  m.add(VMInstr.LOAD(1)); //to double
  m.add(VMInstr.ADD()); //to double
  m.add(VMInstr.STORE(1)); //to double
  ///////////////////////////////////////////////////////////////////
  //   print("The average win percentage is: ")
  ///////////////////////////////////////////////////////////////////
  m.add(VMInstr.PUSH("The average win percentage is: ")); //to double
  m.add(VMInstr.WRITE()); //to double
  m.add(VMInstr.LOAD(1)); //to double
  m.add(VMInstr.PUSH(2.0)); //to double
  m.add(VMInstr.DIV()); //to double
  m.add(VMInstr.WRITE()); //to double
  m.add(VMInstr.PUSH("")); //to double
  m.add(VMInstr.WRITE()); //to double

  //   print(sum / 2.0)
  //   println("")
  // }

    // create the vm: 
    VM vm = new VM();
    vm.debugMode(false);
    // add the frame: 
    vm.add(m);
    // run the vm: 
    vm.run();
  }
}

/* This code was generated automatically by Score */

#include "result.hpp"

 example::example() {}
 example::~example() {}
void example::set_x(int ox) {
    x = ox;
  }
int example::get_x() {
    return x;
  }
int example::stx = 0;
int main_dummy() {
  example* ex_inst = new example();
  ex_inst.set_x(5);
  caout << ex_inst.get_x() << endl;
  return 0;
}

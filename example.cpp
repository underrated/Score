#include <iostream>
#include <systemc>
#include <vector>
#include <string>

using namespace std;

int x;

class example {

  int x = 1;
  int *y = NULL;
  vector<string> names;

  public:
  example() {}
  ~example() {}

  void set_x(int ox) {
    x = ox;
  }

  int get_x() {
    return x;
  }

};

class example1 {
};

int f(int);

class example2: public example, example1 {
};

int main_dummy() {
  example* ex_inst = new example();
  ex_inst.set_x(5);
  caout << ex_inst.get_x() << endl;
  return 0;
}

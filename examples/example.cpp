#include <iostream>
#include <systemc>
#include <vector>
#include <string>

#ifndef __example__cpp
#define __example__cpp

using namespace std;

int global;

// template class
template <typename T = int>
class example_tpl {
  T tpl_field;

  T get_field() {
    return tpl_field;
  }

  void set_field(T field) {
    this.tpl_field = field;
  }
};

#define THIS

// function template
template<typename T>
T funct(int x) {
  return x + 1;
}

class example {
  // Field variable
  string field;

  // Field variable with inital value
  int x = 1;

  // Field pointer variable with initial value
  int *y = NULL;

  // Template class instance
  vector<string> names;

  // Function pointer
  void *(*foo)(int *);

  public:
  example() {}
  ~example() {}

  void set_x(int ox) {
    x = ox;
  }

  int get_x() {
    return x;
  }

  float prototype(int arg);

};

class example1 {
};

int f(int);

class example2: public example, example1 {
};

int standalone_prototype(int z);

int main_dummy() {
  example* ex_inst = new example();
  ex_inst.set_x(5);
  caout << ex_inst.get_x() << endl;
  return 0;
}

#define X 1

#endif

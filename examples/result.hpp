/* This code was generated automatically by Score */

#include <iostream>
#include <systemc>
#include <vector>
#include <string>
#ifndef __example__cpp
#define __example__cpp
using namespace std;
int global;
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
template<typename T>
T funct(int x) {
  return x + 1;
}
class example {
string field; 
int x = 1; 
int *y = NULL; 
vector<string> names; 
void *(*foo)(int *); 
 example();
 ~example();
void set_x(int ox);
int get_x();
float prototype(int arg); 
};
class example1 {
};
int f(int);
class example2 : public example, example1 {
};
int standalone_prototype(int z);
int main_dummy();
#define X 1
#endif

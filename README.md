Introduction
============

Big C++ projects require a strict separation between the declaration and the implementation of functions and class methods. The headers(.h, .hpp) usually contain the preprocessor directives, typedefs, variable declarations, class declarations, field declarations and method prototytes while the source files(.c, .cc, .cpp) contain the method/function implementations and initializations of static fields. 

This separation is necesarry for many reasons. 

Sometimes the headers are shipped along with the binaries so that the user can have access to the API of the product without seeing the implementation details.

Another reason is that it establishes a clear policy on how headers should be included: every source file includes all the headers it needs and all the headers need to be guarded against multiple inclusions. This way we avoid spending too much time deciding the "include architecture" of the project and how to avoid various include-related problems like cyclic inclusions.

However not all developers like this way of working especially newcomers to the world of C++. Even more often, Java programmers experimenting with C++ are quickly discouraged by this unnatural way of organizing code and for good reasons. Everytime you want to add a new method to a class you need to declare it in the header file and then jump to source file, declare it again and then implement it. This jumping between files can really affect a programmer's focus and ultimately his productivity. There are of course solutions for easing this pain such as using a good IDE or certain command line tools that generate the missing code. 

But wouldn't it be nice to write "unseparated" code like you do in Java and then have it split into headers and sources by a nice and handy tool?

Score
=====

Score is the tool that does just that. It generates C++ header and source files from monolithic unseparated source files which contain both the declarations and the implementations.

For example let's take the following unseparated C++ code:

```cpp
// File: unseparated.cc

#include <iostream>

using namespace std;

class Foo {
  private:
  int bar;

  public:
  Foo(int bar) {
    this.bar = bar
  }
  
  ~Foo() { 
    cout << "Foo destroyed."
  }

  int get_bar() {
    return bar;
  }

  void set_bar(int bar) {
    this.bar = bar;
  }
}

int global_field;

int global_function() {
  return 0;
}

```

It will be split into the following 2 files:
```cpp
// File: header.h
#include <iostream>

using namespace std;

class Foo {
  private:
  int bar;

  Foo(int bar);
  ~Foo();

  int get_bar();

  void set_bar(int bar);
}

int global_field;

int global_function();
```

```cpp
// File: source.cpp

#include "header.h"

Foo::Foo(int bar) {
  this.bar = bar;
}

Foo::~Foo() {
  cour<<"Foo has been destroyed.";
}

int Foo::get_bar() {
  return bar;
}

int Foo::set_bar(int bar) {
  this.bar = bar;
}

int global_function() {
  return 0;
}

```

Advantages
==========

Unlike other solutions (like Lazy C++ for example), Score supports the latest C++14 standard and it is written in Java so that all the Java enthusiasts can easily tweak it for their own needs.

Limitations
===========

All preprocessor directives are copied into the generated header file as they are without considering their meaning. Fancy #ifdefs or file inclusions in unusual places are not currently supported. I plan to add preprocessor support later on.

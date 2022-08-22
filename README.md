Score
=====

Score is a tool that generates C++ header and source files from monolithic unseparated source files which contain both declarations and implementations of methods.

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

Running *"Score -i unseparated.cc -oh header.h -os source.cpp"* would split the code above into the following 2 files:
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
  cout<<"Foo has been destroyed.";
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

Build and install
=========================
The easiest way to build Score is by using gradle:

```bash
git clone https://github.com/underrated/Score.git
cd Score
gradle build
```
You can also import the cloned repo as a Java project into Eclipse and do the build from there.

After the gradle build is done you will find a Score.zip or a Score.tar archive in the following folder:

*./build/distributions*

Extract the archive wherever you like and add the Score/bin folder to the PATH variable.

Then you can run *"Score -h"* to learn what arguments it takes.

Usage
=====
```
usage: Score -i input.hpp -o output
 -h          Print help message.
 -hp <arg>   Relative path to the included header used in the #include
             statement. e.g #include "<header_path>/header.h"
 -i <arg>    Path to input file.
 -o <arg>    Path to generated files in the form <folder>/<name_root>. Two
             files will be created, one with the hpp extension added to
             the name root and one with the cpp extensions.
 -oh <arg>   Path to the generated header file.
 -os <arg>   Path to the generated source file.

```

Motivation
==========
Big C++ projects require a strict separation between the declaration and the implementation of methods. The headers(.h, .hpp) usually contain the preprocessor directives, typedefs, variable declarations, class declarations, field declarations and method prototypes while the source files(.c, .cc, .cpp) contain the method implementations and initializations of static fields. 

This separation is necesarry for many reasons:

* It makes recompilation faster. Instead of compiling one huge .cpp file (that includes all the headers) every time you make a change, you compile smaller .cpp files (containing the relevant parts of your application) into object files which you later link together into a shared or a static library. That way, whenever you make a change in the code, you won't need to recompile everything all over again but only the .cpp files that contain or include the change. 

* Sometimes the headers are shipped along with the binaries so that the user can have access to the API of the product without seeing the implementation details.

* Separation establishes a clear policy on how headers should be included: every source file includes all the headers it needs and all the headers need to be guarded against multiple inclusions. This way we avoid spending too much time deciding the "include architecture" of the project and how to avoid various include-related problems like cyclic inclusions.

However not all developers like this way of working especially newcomers to the world of C++. Even more often, Java programmers experimenting with C++ are quickly discouraged by this unnatural way of organizing code and for good reasons. Every time you want to add a new method to a class you need to declare it in the header file and then jump to source file, declare it again and then implement it. This jumping between files can really affect a programmer's focus and ultimately his productivity. There are of course solutions for easing this pain such as using a good IDE or certain command line tools that generate the missing code. 

But wouldn't it be nice to write "unseparated" code like you do in Java and then have it split into headers and sources by a nice and handy tool?

Advantages
==========
Unlike other solutions (like Lazy C++ for example), Score supports the latest C++14 standard and it is written in Java so that all the Java enthusiasts can easily tweak it for their own needs.

Limitations
===========
All preprocessor directives are copied into the generated header file as they are without considering their meaning. Fancy #ifdefs or file inclusions in unusual places are not currently supported. I plan to add preprocessor support later on.

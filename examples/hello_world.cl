class Bar inherits IO {
   foo(a : Integer, b : Integer) : Integer {
        a + b 
   };
};

class Foo inherits Bar {
   foo(a : Integer, b : Integer) : Integer {
        a + b 
   };
};

class Main inherits Foo {
   foo(a : Integer, b : Integer) : Integer {
        a + b
   };
   
   main(): SELF_TYPE {
	out_string("Hello, World.\n")
   };
};

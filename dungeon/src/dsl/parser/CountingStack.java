package dsl.parser;

import java.util.Stack;

public class CountingStack<E> extends Stack<E> {
  Stack<Counter> counterStack = new Stack<>();

  public class Counter {
    public Counter() {
      this.name = "";
    }

    public Counter(String name) {
      this.name = name;
    }

    private String name;
    private Integer count = 0;

    public void inc() {
      this.count++;
    }

    public void dec() {
      this.count--;
    }

    public void add(Integer x) {
      this.count += x;
    }

    public void sub(Integer x) {
      this.count -= x;
    }

    public Integer get() {
      return this.count;
    }
  }

  public CountingStack() {
    counterStack.push(new Counter());
  }

  public void pushCounter() {
    this.counterStack.push(new Counter());
  }

  public void pushCounter(String name) {
    this.counterStack.push(new Counter(name));
  }

  public Integer popCounter() {
    var counter = this.counterStack.pop();
    return counter.get();
  }

  public Integer getCurrentCount() {
    return this.counterStack.peek().get();
  }

  public Counter getCurrentCounter() {
    return this.counterStack.peek();
  }

  @Override
  public E push(E item) {
    E ret = super.push(item);
    this.counterStack.peek().inc();
    return ret;
  }

  @Override
  public synchronized E pop() {
    E ret = super.pop();
    this.counterStack.peek().dec();
    return ret;
  }
}

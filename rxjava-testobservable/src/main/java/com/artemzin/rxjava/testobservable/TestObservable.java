package com.artemzin.rxjava.testobservable;

import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Subscriber;

public class TestObservable<T> extends Observable<T> {

  public static <T> TestObservable<T> from(final Observable<T> observable) {
    return new TestObservable<T>(new OnSubscribe<T>() {
      @Override public void call(Subscriber<? super T> subscriber) {
        observable.subscribe(subscriber);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  protected TestObservable(OnSubscribe<T> f) {
    super(f);
  }

  /**
   * Asserts that source {@link Observable} emits error.
   * If source {@link Observable} won't emit error or error won't be equal to expected one then
   * this Operator will emit {@link AssertionError}.
   *
   * @param expectedError expected error that should be emitted by source {@link Observable}.
   *                      Operator will compare them via {@link Throwable#equals(Object)} call.
   * @return {@link TestObservable}.
   * @see #expectError(Class)
   */
  public TestObservable<T> expectError(final Throwable expectedError) {
    return new TestObservable<T>(new OnSubscribe<T>() {
      @Override public void call(final Subscriber<? super T> subscriber) {
        TestObservable.this.subscribe(new Subscriber<T>() {
          @Override public void onCompleted() {
            subscriber.onError(new AssertionError("No errors were thrown by the source Observable"));
          }

          @Override public void onError(Throwable e) {
            if (expectedError.equals(e)) {
              subscriber.onCompleted();
            } else {
              AssertionError assertionError = new AssertionError("Expected exception is not equal to actual one. Expected = " + expectedError + ", actual = " + e);
              assertionError.initCause(e);
              subscriber.onError(assertionError);
            }
          }

          @Override public void onNext(T t) {
            subscriber.onNext(t);
          }
        });
      }
    });
  }

  /**
   * Asserts that source {@link Observable} emits error of particular type.
   * If source {@link Observable} won't emit error or error type won't be equal to expected one then
   * this Operator will emit {@link AssertionError}.
   *
   * @param expectedErrorClass expected error type. Note that it has to be concrete type you expect.
   * @return {@link TestObservable}.
   * @see #expectError(Throwable)
   */
  public TestObservable<T> expectError(final Class<? extends Throwable> expectedErrorClass) {
    return new TestObservable<T>(new OnSubscribe<T>() {
      @Override public void call(final Subscriber<? super T> subscriber) {
        TestObservable.this.subscribe(new Subscriber<T>() {
          @Override public void onCompleted() {
            subscriber.onError(new AssertionError("No errors were thrown by the source Observable"));
          }

          @Override public void onError(Throwable e) {
            if (expectedErrorClass.equals(e.getClass())) {
              subscriber.onCompleted();
            } else {
              AssertionError assertionError = new AssertionError("Expected exception type is not equal to actual one. Expected = " + expectedErrorClass + ", actual = " + e.getClass());
              assertionError.initCause(e);
              subscriber.onError(assertionError);
            }
          }

          @Override public void onNext(T t) {
            subscriber.onNext(t);
          }
        });
      }
    });
  }

  /**
   * Asserts that source {@link Observable} emits only one value and value is equals to expected one.
   * If source {@link Observable} won't emit value or value won't be equal to expected then
   * this Operator will emit {@link AssertionError}.
   * <p/>
   * If source {@link Observable} will emit an exception after the value then exception will be passed down.
   *
   * @param expectedValue expected value of the emission, can be {@code null}.
   * @return {@link TestObservable}.
   */
  public TestObservable<T> expectOnlyOneValue(final T expectedValue) {
    return new TestObservable<T>(new OnSubscribe<T>() {
      @Override public void call(final Subscriber<? super T> subscriber) {
        final AtomicBoolean valueWasEmitted = new AtomicBoolean();

        TestObservable.this.subscribe(new Subscriber<T>() {
          @Override public void onCompleted() {
            if (valueWasEmitted.get()) {
              subscriber.onCompleted();
            } else {
              subscriber.onError(new AssertionError("Source Observable hadn't emitted any values"));
            }
          }

          @Override public void onError(Throwable e) {
            if (valueWasEmitted.get()) {
              subscriber.onError(e);
            } else {
              AssertionError assertionError = new AssertionError("Source Observable emitted exception before expected value");
              assertionError.initCause(e);
              subscriber.onError(assertionError);
            }
          }

          @Override public void onNext(T actualValue) {
            if (valueWasEmitted.compareAndSet(false, true)) {
              if (objectsEquals(expectedValue, actualValue)) {
                subscriber.onNext(actualValue);
              } else {
                subscriber.onError(new AssertionError("Expected value is not equal to actual. Expected = " + expectedValue + ", actual = " + actualValue));
              }
            } else {
              subscriber.onError(new AssertionError("Expected only one value from source Observable, unexpected value = " + actualValue));
            }
          }
        });
      }
    });
  }

  /**
   * Returns {@code true} if the arguments are equal to each other
   * and {@code false} otherwise.
   * Consequently, if both arguments are {@code null}, {@code true}
   * is returned and if exactly one argument is {@code null}, {@code
   * false} is returned.  Otherwise, equality is determined by using
   * the {@link Object#equals equals} method of the first
   * argument.
   *
   * @param a an object
   * @param b an object to be compared with {@code a} for equality
   * @return {@code true} if the arguments are equal to each other
   * and {@code false} otherwise
   * @see Object#equals(Object)
   */
  private static boolean objectsEquals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
  }
}

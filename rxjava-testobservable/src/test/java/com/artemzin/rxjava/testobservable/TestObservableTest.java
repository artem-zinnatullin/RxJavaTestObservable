package com.artemzin.rxjava.testobservable;

import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.OnErrorNotImplementedException;
import rx.observers.TestSubscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class TestObservableTest {

  @Test
  public void expectError_byErrorObject_shouldThrowExceptionWhenObservableEmitsUnexpectedException() {
    Throwable error = new RuntimeException();

    TestObservable<Object> testObservable = TestObservable
      .from(Observable.error(error))
      .expectError(new IllegalStateException()); // Expected exception != actual.

    try {
      testObservable.subscribe();
      failBecauseExceptionWasNotThrown(OnErrorNotImplementedException.class);
    } catch (OnErrorNotImplementedException expected) {
      AssertionError cause = (AssertionError) expected.getCause();
      assertThat(cause).hasMessage("Expected exception is not equals to actual one. Expected = java.lang.IllegalStateException, actual = java.lang.RuntimeException");
      assertThat(cause.getCause()).isSameAs(error);
    }
  }

  @Test
  public void expectError_byErrorObject_shouldThrowExceptionWhenObservableNotEmitsException() {
    TestObservable<String> testObservable = TestObservable
      .from(Observable.just("test"))
      .expectError(new IllegalStateException());

    try {
      testObservable.subscribe();
      failBecauseExceptionWasNotThrown(OnErrorNotImplementedException.class);
    } catch (OnErrorNotImplementedException expected) {
      AssertionError cause = (AssertionError) expected.getCause();
      assertThat(cause).hasMessage("No errors were thrown by the source Observable");
    }
  }

  @Test
  public void expectError_byErrorObject_shouldPassWhenObservableEmitsExpectedException() {
    Throwable error = new RuntimeException();

    TestObservable<Object> testObservable = TestObservable
      .from(Observable.error(error))
      .expectError(error);

    // We don't except any exceptions here.
    testObservable.subscribe();
  }

  @Test
  public void expectError_byErrorObject_shouldPassEmissionUntilExpectedErrorOcurres() {
    final Throwable error = new RuntimeException();

    TestObservable<String> testObservable = TestObservable
      .from(Observable.create(new Observable.OnSubscribe<String>() {
        @Override public void call(Subscriber<? super String> subscriber) {
          subscriber.onNext("1");
          subscriber.onNext("2");
          subscriber.onNext("3");
          subscriber.onError(error);
        }
      }))
      .expectError(error);

    TestSubscriber<String> testSubscriber = new TestSubscriber<String>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertValues("1", "2", "3");
    testSubscriber.assertNoErrors();
    testSubscriber.assertCompleted();
  }

  @Test
  public void expectError_byErrorObject_shouldPassEmissionUntilUnexpectedErrorOcurres() {
    final Throwable error = new RuntimeException();

    TestObservable<String> testObservable = TestObservable
      .from(Observable.create(new Observable.OnSubscribe<String>() {
        @Override public void call(Subscriber<? super String> subscriber) {
          subscriber.onNext("1");
          subscriber.onNext("2");
          subscriber.onNext("3");
          subscriber.onError(error);
        }
      }))
      .expectError(new IllegalStateException()); // Expected exception != actual.

    TestSubscriber<String> testSubscriber = new TestSubscriber<String>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertValues("1", "2", "3");
    AssertionError assertionError = (AssertionError) testSubscriber.getOnErrorEvents().get(0);
    assertThat(assertionError).hasMessage("Expected exception is not equals to actual one. Expected = java.lang.IllegalStateException, actual = java.lang.RuntimeException");
    assertThat(assertionError.getCause()).isSameAs(error);

    testSubscriber.assertNotCompleted();
  }

  @Test
  public void expectError_byErrorObject_shouldPassEmissionUntilSequenceCompletes() {
    TestObservable<Integer> testObservable = TestObservable
      .from(Observable.create(new Observable.OnSubscribe<Integer>() {
        @Override public void call(Subscriber<? super Integer> subscriber) {
          subscriber.onNext(1);
          subscriber.onNext(2);
          subscriber.onNext(3);
          subscriber.onCompleted();
        }
      }))
      .expectError(new RuntimeException());

    TestSubscriber<Integer> testSubscriber = new TestSubscriber<Integer>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertValues(1, 2, 3);
    AssertionError assertionError = (AssertionError) testSubscriber.getOnErrorEvents().get(0);
    assertThat(assertionError).hasMessage("No errors were thrown by the source Observable");
    testSubscriber.assertNotCompleted();
  }
}
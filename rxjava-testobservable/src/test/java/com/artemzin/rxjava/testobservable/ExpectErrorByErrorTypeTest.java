package com.artemzin.rxjava.testobservable;

import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.OnErrorNotImplementedException;
import rx.observers.TestSubscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class ExpectErrorByErrorTypeTest {

  @Test
  public void expectError_shouldThrowExceptionWhenObservableEmitsExceptionOfUnexpectedType() {
    Throwable error = new IllegalStateException();

    TestObservable<Object> testObservable = TestObservable
      .from(Observable.error(error))
      .expectError(RuntimeException.class);

    try {
      testObservable.subscribe();
      failBecauseExceptionWasNotThrown(OnErrorNotImplementedException.class);
    } catch (OnErrorNotImplementedException expected) {
      AssertionError cause = (AssertionError) expected.getCause();
      assertThat(cause).hasMessage("Expected exception type is not equal to actual one. Expected = class java.lang.RuntimeException, actual = class java.lang.IllegalStateException");
      assertThat(cause.getCause()).isSameAs(error);
    }
  }

  @Test
  public void expectError_shouldThrowExceptionWhenObservableNotEmitsException() {
    TestObservable<Integer> testObservable = TestObservable
      .from(Observable.just(1))
      .expectError(RuntimeException.class);

    try {
      testObservable.subscribe();
      failBecauseExceptionWasNotThrown(OnErrorNotImplementedException.class);
    } catch (OnErrorNotImplementedException expected) {
      AssertionError cause = (AssertionError) expected.getCause();
      assertThat(cause).hasMessage("No errors were thrown by the source Observable");
    }
  }

  @Test
  public void expectError_shouldPassWhenObservableEmitsExceptionOfExpectedType() {
    TestObservable<Object> testObservable = TestObservable
      .from(Observable.error(new IllegalStateException()))
      .expectError(IllegalStateException.class);

    // We don't except any exceptions here.
    testObservable.subscribe();
  }

  @Test
  public void expectError_shouldPassEmissionUntilErrorOfExpectedTypeOcurres() {
    TestObservable<Integer> testObservable = TestObservable
      .from(Observable.create(new Observable.OnSubscribe<Integer>() {
        @Override public void call(Subscriber<? super Integer> subscriber) {
          subscriber.onNext(1);
          subscriber.onNext(2);
          subscriber.onNext(3);
          subscriber.onError(new IllegalStateException());
        }
      }))
      .expectError(IllegalStateException.class);

    TestSubscriber<Integer> testSubscriber = new TestSubscriber<Integer>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertValues(1, 2, 3);
    testSubscriber.assertNoErrors();
    testSubscriber.assertCompleted();
  }

  @Test
  public void expectError_shouldPassEmissionUntilErrorOfUnexpectedTypeOcurres() {
    TestObservable<Integer> testObservable = TestObservable
      .from(Observable.create(new Observable.OnSubscribe<Integer>() {
        @Override public void call(Subscriber<? super Integer> subscriber) {
          subscriber.onNext(1);
          subscriber.onNext(2);
          subscriber.onNext(3);
          subscriber.onError(new RuntimeException());
        }
      }))
      .expectError(IllegalStateException.class);

    TestSubscriber<Integer> testSubscriber = new TestSubscriber<Integer>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertValues(1, 2, 3);
    AssertionError assertionError = (AssertionError) testSubscriber.getOnErrorEvents().get(0);
    assertThat(assertionError).hasMessage("Expected exception type is not equal to actual one. Expected = class java.lang.IllegalStateException, actual = class java.lang.RuntimeException");
    testSubscriber.assertNotCompleted();
  }

  @Test
  public void expectError_shouldPassEmissionUntilSequenceCompletes() {
    TestObservable<Integer> testObservable = TestObservable
      .from(Observable.create(new Observable.OnSubscribe<Integer>() {
        @Override public void call(Subscriber<? super Integer> subscriber) {
          subscriber.onNext(1);
          subscriber.onNext(2);
          subscriber.onNext(3);
          subscriber.onCompleted();
        }
      }))
      .expectError(IllegalStateException.class);

    TestSubscriber<Integer> testSubscriber = new TestSubscriber<Integer>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertValues(1, 2, 3);
    AssertionError assertionError = (AssertionError) testSubscriber.getOnErrorEvents().get(0);
    assertThat(assertionError).hasMessage("No errors were thrown by the source Observable");
    testSubscriber.assertNotCompleted();
  }
}

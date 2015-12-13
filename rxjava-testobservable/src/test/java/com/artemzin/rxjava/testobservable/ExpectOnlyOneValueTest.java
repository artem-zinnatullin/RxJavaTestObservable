package com.artemzin.rxjava.testobservable;

import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ExpectOnlyOneValueTest {

  @Test
  public void expectOnlyOneValue_shouldPassIfActualValueEqualsToExpected() {
    TestObservable<String> testObservable = TestObservable
      .from(Observable.just("test"))
      .expectOnlyOneValue("test");

    testObservable.subscribe();
  }

  @Test
  public void expectOnlyOneValue_shouldPassIfActualValueEqualsToExpectedNull() {
    TestObservable<Object> testObservable = TestObservable
      .from(Observable.just(null))
      .expectOnlyOneValue(null);

    testObservable.subscribe();
  }

  @Test
  public void expectOnlyOneValue_shouldFailIfActualValueIsNotEqualToExpected() {
    TestObservable<String> testObservable = TestObservable
      .from(Observable.just("test"))
      .expectOnlyOneValue("not test");

    TestSubscriber<String> testSubscriber = new TestSubscriber<String>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertNoValues();
    assertThat(testSubscriber.getOnErrorEvents().get(0))
      .isExactlyInstanceOf(AssertionError.class)
      .hasMessage("Expected value is not equal to actual. Expected = not test, actual = test");

    testSubscriber.assertNotCompleted();
  }

  @Test
  public void expectOnlyOneValue_shouldFailIfObservableEmitsMoreThanOneValue() {
    TestObservable<String> testObservable = TestObservable
      .from(Observable.from(asList("1", "2", "3")))
      .expectOnlyOneValue("1");

    TestSubscriber<String> testSubscriber = new TestSubscriber<String>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertValue("1");
    assertThat(testSubscriber.getOnErrorEvents().get(0))
      .isExactlyInstanceOf(AssertionError.class)
      .hasMessage("Expected only one value from source Observable, unexpected value = 2");

    testSubscriber.assertNotCompleted();
  }

  @Test
  public void expectOnlyOneValue_shouldFailIfObservableEmitsErrorBeforeAnyValue() {
    Throwable error = new IllegalStateException();

    TestObservable<Object> testObservable = TestObservable
      .from(Observable.error(error))
      .expectOnlyOneValue("1");

    TestSubscriber<Object> testSubscriber = new TestSubscriber<Object>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertNoValues();
    AssertionError assertionError = (AssertionError) testSubscriber.getOnErrorEvents().get(0);
    assertThat(assertionError).hasMessage("Source Observable emitted exception before expected value");
    assertThat(assertionError.getCause()).isSameAs(error);
  }

  @Test
  public void expectOnlyOneValue_shouldPassErrorAsIsIfObservableEmitsErrorAfterValue() {
    final Throwable error = new RuntimeException();

    TestObservable<String> testObservable = TestObservable
      .from(Observable.create(new Observable.OnSubscribe<String>() {
        @Override public void call(Subscriber<? super String> subscriber) {
          subscriber.onNext("1");
          subscriber.onError(error);
        }
      }))
      .expectOnlyOneValue("1");

    TestSubscriber<String> testSubscriber = new TestSubscriber<String>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertValue("1");
    testSubscriber.assertError(error);
    testSubscriber.assertNotCompleted();
  }

  @Test
  public void expectOnlyOneValue_shouldFailIfObservableCompletesWithoutEmission() {
    TestObservable<Object> testObservable = TestObservable
      .from(Observable.empty())
      .expectOnlyOneValue("1");

    TestSubscriber<Object> testSubscriber = new TestSubscriber<Object>();
    testObservable.subscribe(testSubscriber);

    testSubscriber.assertNoValues();
    assertThat(testSubscriber.getOnErrorEvents().get(0))
      .isExactlyInstanceOf(AssertionError.class)
      .hasMessage("Source Observable hadn't emitted any values");

    testSubscriber.assertNotCompleted();
  }
}

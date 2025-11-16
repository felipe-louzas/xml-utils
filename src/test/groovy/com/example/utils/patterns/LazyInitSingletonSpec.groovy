package com.example.utils.patterns

import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

class LazyInitSingletonSpec extends Specification {

    static class DefaultImpl {}

    static class NoDefaultCtor {
        private NoDefaultCtor() {}
    }

    def "lazy initialization occurs only on first get"() {
        given:
        def counter = new AtomicInteger()
        def holder = LazyInitSingleton.of({
            counter.incrementAndGet()
            new DefaultImpl()
        } as Supplier<DefaultImpl>)

        expect:
        !holder.initialized

        when:
        def first = holder.get()

        then:
        holder.initialized
        counter.get() == 1

        when:
        def second = holder.get()

        then:
        first.is(second)
        counter.get() == 1
    }

    def "explicit set before lazy init"() {
        given:
        def holder = LazyInitSingleton.of(DefaultImpl)
        def injected = new DefaultImpl()

        when:
        holder.set(injected)

        then:
        holder.initialized
        holder.get().is(injected)
    }

    def "explicit set after initialization throws"() {
        given:
        def holder = LazyInitSingleton.of(DefaultImpl)
        holder.get()

        when:
        holder.set(new DefaultImpl())

        then:
        thrown(IllegalStateException)
    }

    def "set using supplier after initialization throws"() {
        given:
        def holder = LazyInitSingleton.of(DefaultImpl)
        holder.get()

        when:
        holder.set({ new DefaultImpl() } as Supplier<DefaultImpl>)

        then:
        thrown(IllegalStateException)
    }

    def "setIfAbsent when absent returns true and sets value"() {
        given:
        def holder = LazyInitSingleton.of(DefaultImpl)
        def injected = new DefaultImpl()

        when:
        def result = holder.setIfAbsent(injected)

        then:
        result
        holder.initialized
        holder.get().is(injected)
    }

    def "setIfAbsent when present returns false"() {
        given:
        def holder = LazyInitSingleton.of(DefaultImpl)
        def first = holder.get()

        when:
        def result = holder.setIfAbsent(new DefaultImpl())

        then:
        !result
        holder.get().is(first)
    }

    def "reset clears the instance"() {
        given:
        def holder = LazyInitSingleton.of(DefaultImpl)
        def first = holder.get()

        when:
        holder.reset()

        then:
        !holder.initialized

        when:
        def second = holder.get()

        then:
        !first.is(second)
    }

    def "of(Class) fails if no default constructor exists"() {
        when:
        LazyInitSingleton.of(NoDefaultCtor).get()

        then:
        thrown(IllegalStateException)
    }

    def "concurrent get() calls must initialize supplier exactly once"() {
        given:
        def counter = new AtomicInteger()
        def holder = LazyInitSingleton.of({
            counter.incrementAndGet()
            new DefaultImpl()
        } as Supplier<DefaultImpl>)

        def executor = Executors.newFixedThreadPool(12)
        def tasks = (1..25).collect { { holder.get() } as Callable<DefaultImpl> }

        when:
        def futures = executor.invokeAll(tasks)
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        def results = futures.collect { it.get() }
        def first = results[0]

        then:
        results.every { it.is(first) }
        counter.get() == 1
    }

    def "null validation for Supplier constructor"() {
        when:
        LazyInitSingleton.of(null as Supplier)

        then:
        thrown(NullPointerException)
    }

    def "null validation for Class constructor"() {
        when:
        LazyInitSingleton.of(null as Class)

        then:
        thrown(NullPointerException)
    }
}

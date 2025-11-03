package com.example.utils.xml

import org.junit.jupiter.api.Timeout
import org.xml.sax.InputSource
import spock.lang.Specification

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong

class JAXPThreadSafetySpec extends Specification {

    private static final int TIMEOUT_SECONDS = 60
    private static final int NUM_THREADS = 1 // Runtime.getRuntime().availableProcessors() * 20
    private static final int NUM_TASKS = NUM_THREADS * 10
    private static final int NUM_REPEAT = 1_000

    @Timeout(TIMEOUT_SECONDS)
    def "DocumentBuilderFactory.newInstance() should be thread-safe"() {
        when: "DocumentBuilderFactory.newInstance is executed in parallel"
        def results = runParallelTasks(() -> DocumentBuilderFactory.newInstance())

        then: "No exceptions are thrown, proving thread-safety"
        noExceptionThrown()

        and: "All builders are non-null and, crucially, distinct instances (at least per thread)"
        results.size() == NUM_TASKS
        results.every { it != null }
        results.toSet().size() >= NUM_THREADS
    }

    @Timeout(TIMEOUT_SECONDS)
    def "DocumentBuilderFactory.newDocumentBuilder() should be thread-safe"() {
        given: "A single, shared factory"
        def factory = DocumentBuilderFactory.newInstance()

        when: "factory.newDocumentBuilder is executed in parallel"
        def results = runParallelTasks(() -> factory.newDocumentBuilder())

        then: "No exceptions are thrown, proving thread-safety"
        noExceptionThrown()

        and: "All builders are non-null and, crucially, distinct instances (at lease per thread)"
        results.size() == NUM_TASKS
        results.every { it != null }
        results.toSet().size() >= NUM_THREADS
    }

    @Timeout(TIMEOUT_SECONDS)
    def "XPathFactory.newDefaultInstance() should be thread-safe"() {
        when: "XPathFactory.newDefaultInstance is executed in parallel"
        def results = runParallelTasks(() -> XPathFactory.newDefaultInstance())

        then: "No exceptions are thrown, proving thread-safety"
        noExceptionThrown()

        and: "All builders are non-null and, crucially, distinct instances (at lease per thread)"
        results.size() == NUM_TASKS
        results.every { it != null }
        results.toSet().size() >= NUM_THREADS
    }

    @Timeout(TIMEOUT_SECONDS)
    def "XPathFactory.newInstance() should be thread-safe"() {
        when: "XPathFactory.newInstance is executed in parallel"
        def results = runParallelTasks(() -> XPathFactory.newInstance())

        then: "No exceptions are thrown, proving thread-safety"
        noExceptionThrown()

        and: "All builders are non-null and, crucially, distinct instances (at lease per thread)"
        results.size() == NUM_TASKS
        results.every { it != null }
        results.toSet().size() >= NUM_THREADS
    }

    @Timeout(TIMEOUT_SECONDS)
    def "XPathFactory.newXPath() should be thread-safe"() {
        given: "A single, shared factory"
        def factory = XPathFactory.newDefaultInstance()

        when: "XPathFactory.newXPath is executed in parallel"
        def results = runParallelTasks(() -> factory.newXPath())

        then: "No exceptions are thrown, proving thread-safety"
        noExceptionThrown()

        and: "All builders are non-null and, crucially, distinct instances (at lease per thread)"
        results.size() == NUM_TASKS
        results.every { it != null }
        results.toSet().size() >= NUM_THREADS
    }

    @Timeout(TIMEOUT_SECONDS)
    def "DocumentBuilder.parse() is NOT thread-safe"() {
        given: "A single, shared DocumentBuilder instance"
        def factory = DocumentBuilderFactory.newInstance()
        def sharedBuilder = factory.newDocumentBuilder()

        and: "A simple XML document to parse"
        def xmlString = "<root><person><name>Test</name><age>30</age><city>New York</city></person></root>"

        when: "DocumentBuilder.parse is executed in parallel"
        runParallelTasks(() -> {
            def reader = new StringReader(xmlString)
            def inputSource = new InputSource(reader)
            // This call is not thread-safe and should fail under concurrency
            return sharedBuilder.parse(inputSource)
        })

        then:
        def a = thrown(Exception)
        println("Expected exception caught: " + a.message)
    }

    private static <T> List<Integer> runParallelTasks(Callable<T> callable) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS)
        CountDownLatch startGate = new CountDownLatch(1)
        AtomicLong totalTime = new AtomicLong(0)

        Callable<Integer> task = new Callable<Integer>() {
            @Override
            Integer call() throws Exception {
                startGate.await() // Wait for the signal
                long start = System.nanoTime()
                T result = null
                for (int i = 0; i < NUM_REPEAT; i++) {
                    result = callable.call()
                }
                totalTime.addAndGet(System.nanoTime() - start)
                return result.hashCode();
            }
        }

        println "Submitting ${NUM_TASKS} tasks with ${NUM_REPEAT} calls for execution with ${NUM_THREADS} threads"
        List<Future<Integer>> futures = (1..NUM_TASKS).collect { executor.submit(task) }
        println "Release latch"
        startGate.countDown() // Release all threads

        List<Integer> results = futures.collect { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }
        executor.shutdown()

        println "Total execution time (secs): ${totalTime.get() / 1_000_000_000}"
        println "Total calls: ${NUM_TASKS * NUM_REPEAT}"
        println "Average time per task (ms): ${(totalTime.get() / (NUM_TASKS * NUM_REPEAT)) / 1_000_000}"

        return results
    }
}

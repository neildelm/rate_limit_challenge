package challenge.src.test.java.com.example.challenge.ratelimit;

import challenge.src.main.java.com.example.challenge.ratelimit.RateLimiter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.Test;

/**
 * Unit tests
 */
public class TestRateLimiter {

    /**
     * Test RateLimiter with fixed time and a burst of 2
     */
    @Test
    public void testBurst2() {
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        final RateLimiter rateLimiter = new RateLimiter(clock, 2, 1000);
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertFalse(rateLimiter.tryConsume("127.0.0.1"));
        assertFalse(rateLimiter.tryConsume("127.0.0.1"));
    }

    /**
     * Test RateLimiter with fixed time two keys
     */
    @Test
    public void testTwoKeys() {
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        final RateLimiter rateLimiter = new RateLimiter(clock, 1, 1000);
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertFalse(rateLimiter.tryConsume("127.0.0.1"));
        assertTrue(rateLimiter.tryConsume("10.0.0.1"));
        assertFalse(rateLimiter.tryConsume("10.0.0.1"));
    }

    /**
     * General test of RateLimiter
     */
    @Test
    public void testRateLimiter() {
        // create test clock
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        final RateLimiter rateLimiter = new RateLimiter(clock, 2, 1000);
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertFalse(rateLimiter.tryConsume("127.0.0.1"));
        // add 1 second to the clock
        Duration duration = Duration.ofSeconds(1);
        rateLimiter.setClock(clock.offset(rateLimiter.getClock(), duration));
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertFalse(rateLimiter.tryConsume("127.0.0.1"));
        // add 2 seconds to the clock
        duration = Duration.ofSeconds(2);
        rateLimiter.setClock(clock.offset(rateLimiter.getClock(), duration));
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertFalse(rateLimiter.tryConsume("127.0.0.1"));
    }

    /**
     * Test RateLimiter discards tokens if duration greater than bucket limit
     */
    @Test
    public void testTokensDiscarded() {
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        final RateLimiter rateLimiter = new RateLimiter(clock, 2, 1000);
        // Consume all initial tokens
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertFalse(rateLimiter.tryConsume("127.0.0.1"));
        // add 3 seconds to the clock
        Duration duration = Duration.ofSeconds(3);
        rateLimiter.setClock(clock.offset(rateLimiter.getClock(), duration));
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertTrue(rateLimiter.tryConsume("127.0.0.1"));
        assertFalse(rateLimiter.tryConsume("127.0.0.1"));
    }

    /**
     * Test empty key throws unsupported operation exception
     */
    @Test(expected=UnsupportedOperationException.class)
    public void testEmptyKey() {
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        final RateLimiter rateLimiter = new RateLimiter(clock, 1, 1000);
        rateLimiter.tryConsume("");
    }

    /**
     * Test null key throws unsupported operation exception
     */
    @Test(expected=UnsupportedOperationException.class)
    public void testNullKey() {
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        final RateLimiter rateLimiter = new RateLimiter(clock, 1, 1000);
        rateLimiter.tryConsume(null);
    }

}

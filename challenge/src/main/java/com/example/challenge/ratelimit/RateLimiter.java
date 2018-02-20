package challenge.src.main.java.com.example.challenge.ratelimit;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A rate limiter using the <a href="https://en .wikipedia.org/wiki/Token_bucket">Token Bucket algorithm</a>.
 * <p>
 * A key is meant to represent some unique identifier for which each key has its own bucket.
 * Tokens are added to the bucket every millisecondsPerToken. When the bucket is full,
 * extra tokens are lost. The rate limiter is used to check a key for conformance by calling tryConsume.
 * If there are tokens in a key's bucket, then one token will be consumed and the method will return true,
 * but if the bucket is empty, the method will return false.
 */

public class RateLimiter {

    /**
     * <p>
     * @param clock
     *            Clock used for getting the time using {@link Clock#instant()}.
     * @param burst
     *            number of tokens allowed in a burst
     * @param millisecondsPerToken
     *            token renew rate
     */
    private static final int TOKENS = 0;
    private static final int LAST_CHECKED = 1;

    private Clock clock;
    private long burst;
    private long millisecondsPerToken;

    private ConcurrentHashMap<String, AtomicLong[]> tokenBucket;

    public RateLimiter(final Clock clock, final long burst, final long millisecondsPerToken) {
        this.clock = clock;
        this.burst = burst;
        this.millisecondsPerToken = millisecondsPerToken;
        // Use ConcurrentHashMap as thread-safe. Store the current number of tokens and the time
        // the key was last checked in milliseconds against each key
        this.tokenBucket = new ConcurrentHashMap<String, AtomicLong[]>();
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Clock getClock() {
        return clock;
    }

    /**
     * <p>
     * @param key
     *            identifying the resource that is being rate limited, e.g. IP number
     * @return {@code true} if token was consumed
     */
    public boolean tryConsume(final String key) throws UnsupportedOperationException  {

        if (key == null) {
            throw new UnsupportedOperationException("Null key resource identifier not supported");
        }
        if (key.isEmpty()) {
            throw new UnsupportedOperationException("Empty key resource identifier not supported");
        }

        if (!tokenBucket.containsKey(key)) {
            // Use burst to initially populate number of tokens in bucket
            // (assume burst is token limit for bucket)
            populateBucket(key, new AtomicLong(burst));
        }
        else {
            AtomicLong[] currentBucket = tokenBucket.get(key);
            long numTokens = currentBucket[TOKENS].get() + calculateNewTokens(currentBucket[LAST_CHECKED]);
            if (numTokens > 0) {
                populateBucket(key, new AtomicLong(numTokens));
            }
            else {
                return false;
            }
        }
        return true;

    }

    // Assume burst is bucket limit
    private long calculateNewTokens(AtomicLong lastCheckTime) {
        long newTokens = (timeSinceLastCheck(lastCheckTime) / millisecondsPerToken);
        if (newTokens > burst) {
            return burst;
        }
        else {
            return newTokens;
        }
    }


    // Use clock to calculate time since token last consumed in milliseconds
    private long timeSinceLastCheck(AtomicLong lastCheck) {
        return clock.millis() - lastCheck.get();
    }

    private void populateBucket(final String key, AtomicLong numTokens) {
        //consume a token and get current time
        long newTokenValue = numTokens.decrementAndGet();

        //Populate bucket with token value and last checked time using clock
        tokenBucket.put(key, new AtomicLong[] {new AtomicLong(newTokenValue), new AtomicLong(clock.millis())});
    }
}

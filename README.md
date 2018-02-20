# rate_limit_challenge
A rate limiter using the Token Bucket algorithm (https://en.wikipedia.org/wiki/Token_bucket).

A key is meant to represent some unique identifier for which each key has its own bucket.
Tokens are added to the bucket every millisecondsPerToken. When the bucket is full,
extra tokens are lost. The rate limiter is used to check a key for conformance by calling tryConsume.

If there are tokens in a key's bucket, then one token will be consumed and the method will return true,
but if the bucket is empty, the method will return false.

# pedestal-connector-test

A comprehensive test suite for [Pedestal](https://github.com/pedestal/pedestal) connectors, designed to ensure
behavioral consistency across different connector implementations.

# Testing

```shell
clojure -X:jetty:test
clojure -X:http-kit:test
clojure -M:protojure:test -e websocket
```

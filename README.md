# Audit API

Swagger is available at http://localhost:8030/swagger-ui/index.html

## AMQP contract

- work-inbound — produced tasks (Producer → Worker)
- work-outbound — processed / discarded / dead-letter tasks (Worker → Producer/Audit)
- certified-result — certified tasks (Producer → Audit)

### ⚠️ Dead-letter convention

If a message fails processing and is dead-lettered, it is re-published to the `work-outbound` exchange with routing key:

`task.produced.<color>`

In the context of the Audit service, such messages are treated as DISCARDED

# Scenarios Reproducing

## Scenario 5

_1. Change the task String generation to a pattern {COLOR}-UUID where
COLOR is one of { RED, BLUE, GREEN } and produce a task String with
a different color each time (round robin is ok)_

_2. Deploy 3 instances of Audit, modified and configured in a way where
each service only keeps track of events of a single individual color that
it is configured to (e.g. Audit service configured to „RED” should only
register „RED” events)_

#### Reproducing

1. Clean & Build in gradle
2. Kill all audits
```bash
pkill -f audit-0.0.1-SNAPSHOT.jar
```

3. Up 3 audits with the specified colors:
```bash
java -jar build/libs/audit-0.0.1-SNAPSHOT.jar --server.port=8031 --audit.color=red   > audit-8031.log 2>&1 &
java -jar build/libs/audit-0.0.1-SNAPSHOT.jar --server.port=8032 --audit.color=green > audit-8032.log 2>&1 &
java -jar build/libs/audit-0.0.1-SNAPSHOT.jar --server.port=8033 --audit.color=blue  > audit-8033.log 2>&1 &
```


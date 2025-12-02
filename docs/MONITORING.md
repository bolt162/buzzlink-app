# Monitoring & Observability with Prometheus

## Overview

This document describes how Prometheus would be integrated with BuzzLink for comprehensive monitoring, alerting, and observability.

## Architecture

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│              │         │              │         │              │
│  Spring Boot │ metrics │  Prometheus  │  query  │   Grafana    │
│  + Actuator  ├────────►│    Server    ├────────►│  Dashboards  │
│              │         │              │         │              │
└──────────────┘         └──────┬───────┘         └──────────────┘
                                │
                                │ alerts
                                ▼
                         ┌──────────────┐
                         │              │
                         │ Alertmanager │
                         │              │
                         └──────────────┘
```

## Spring Boot Actuator Integration

### Configuration

Already included in `backend/build.gradle`:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

Already configured in `backend/src/main/resources/application.properties`:
```properties
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.metrics.export.prometheus.enabled=true
management.endpoint.health.show-details=always
```

### Available Metrics Endpoints

- **Prometheus scrape endpoint:** `http://localhost:8080/actuator/prometheus`
- **Health check:** `http://localhost:8080/actuator/health`
- **All metrics:** `http://localhost:8080/actuator/metrics`

### Sample Metrics Output

```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space"} 1.2345678E8

# HELP http_server_requests_seconds
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",status="200",uri="/api/channels"} 245
http_server_requests_seconds_sum{method="GET",status="200",uri="/api/channels"} 12.34

# HELP websocket_connections_active Active WebSocket connections
# TYPE websocket_connections_active gauge
websocket_connections_active 42
```

## Custom Metrics

### Application-Specific Metrics

Add these custom metrics to the BuzzLink backend:

```java
// In MessageService.java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

@Service
public class MessageService {
    private final Counter messagesCounter;
    private final Timer messageSaveTimer;

    public MessageService(MeterRegistry registry) {
        this.messagesCounter = Counter.builder("buzzlink.messages.sent")
            .description("Total messages sent")
            .tag("type", "all")
            .register(registry);

        this.messageSaveTimer = Timer.builder("buzzlink.messages.save.time")
            .description("Time to save message to database")
            .register(registry);
    }

    public MessageDTO createMessage(...) {
        return messageSaveTimer.recordCallable(() -> {
            // ... save logic
            messagesCounter.increment();
            return savedMessage;
        });
    }
}
```

### Custom Metrics to Implement

1. **Messages:**
   - `buzzlink_messages_sent_total` - Total messages sent
   - `buzzlink_messages_deleted_total` - Total messages deleted
   - `buzzlink_messages_save_seconds` - Message save latency

2. **WebSocket:**
   - `buzzlink_websocket_connections_active` - Active WS connections
   - `buzzlink_websocket_messages_sent` - Messages sent via WS
   - `buzzlink_websocket_errors_total` - WS errors

3. **Channels:**
   - `buzzlink_channels_total` - Total channels
   - `buzzlink_channel_subscribers` - Subscribers per channel

4. **Users:**
   - `buzzlink_users_online` - Currently online users
   - `buzzlink_users_total` - Total registered users

5. **Reactions:**
   - `buzzlink_reactions_added_total` - Reactions added
   - `buzzlink_reactions_removed_total` - Reactions removed

## Prometheus Configuration

### prometheus.yml

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'buzzlink-prod'
    env: 'production'

scrape_configs:
  # BuzzLink Backend
  - job_name: 'buzzlink-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
        labels:
          service: 'backend'

  # Multiple backend instances in production
  - job_name: 'buzzlink-backend-cluster'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'backend-1:8080'
        - 'backend-2:8080'
        - 'backend-3:8080'
        labels:
          service: 'backend'

  # PostgreSQL Exporter
  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']
        labels:
          service: 'database'

  # Node Exporter (system metrics)
  - job_name: 'node'
    static_configs:
      - targets: ['node-exporter:9100']
        labels:
          service: 'system'
```

## Grafana Dashboards

### Dashboard 1: System Overview

**Panels:**

1. **System Health (Single Stat)**
   ```promql
   up{job="buzzlink-backend"}
   ```

2. **Request Rate (Graph)**
   ```promql
   rate(http_server_requests_seconds_count[5m])
   ```

3. **Error Rate (Graph)**
   ```promql
   rate(http_server_requests_seconds_count{status=~"5.."}[5m])
   ```

4. **Response Time p95 (Graph)**
   ```promql
   histogram_quantile(0.95,
     rate(http_server_requests_seconds_bucket[5m])
   )
   ```

5. **JVM Heap Usage (Graph)**
   ```promql
   jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
   ```

### Dashboard 2: Application Metrics

**Panels:**

1. **Messages Sent Rate (Graph)**
   ```promql
   rate(buzzlink_messages_sent_total[5m])
   ```

2. **Active WebSocket Connections (Graph)**
   ```promql
   buzzlink_websocket_connections_active
   ```

3. **Online Users (Graph)**
   ```promql
   buzzlink_users_online
   ```

4. **Message Save Latency (Heatmap)**
   ```promql
   rate(buzzlink_messages_save_seconds_bucket[5m])
   ```

5. **Database Connection Pool (Graph)**
   ```promql
   hikaricp_connections_active
   hikaricp_connections_idle
   ```

### Dashboard 3: Business Metrics

**Panels:**

1. **Daily Active Users (Single Stat)**
   ```promql
   count(count_over_time(buzzlink_messages_sent_total{user=~".+"}[24h]))
   ```

2. **Messages per Channel (Bar Gauge)**
   ```promql
   sum by (channel) (buzzlink_messages_sent_total)
   ```

3. **Reaction Rate (Graph)**
   ```promql
   rate(buzzlink_reactions_added_total[5m])
   ```

## Alerting Rules

### alerts.yml

```yaml
groups:
  - name: buzzlink_alerts
    interval: 30s
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: |
          rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} requests/sec"

      # Service down
      - alert: ServiceDown
        expr: up{job="buzzlink-backend"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "BuzzLink backend is down"
          description: "Backend service {{ $labels.instance }} is unreachable"

      # High response time
      - alert: HighResponseTime
        expr: |
          histogram_quantile(0.95,
            rate(http_server_requests_seconds_bucket[5m])
          ) > 1
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "High response time"
          description: "95th percentile response time is {{ $value }}s"

      # Memory usage
      - alert: HighMemoryUsage
        expr: |
          (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High heap memory usage"
          description: "Heap memory usage is {{ $value | humanizePercentage }}"

      # Database connection pool exhaustion
      - alert: DatabasePoolExhaustion
        expr: |
          hikaricp_connections_active / hikaricp_connections_max > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Database connection pool nearly exhausted"
          description: "{{ $value | humanizePercentage }} of connections in use"

      # WebSocket connection spike
      - alert: WebSocketSpike
        expr: |
          rate(buzzlink_websocket_connections_active[5m]) > 100
        for: 5m
        labels:
          severity: info
        annotations:
          summary: "Unusual WebSocket connection spike"
          description: "WebSocket connections increased by {{ $value }}/sec"

      # Low message throughput (potential issue)
      - alert: LowMessageThroughput
        expr: |
          rate(buzzlink_messages_sent_total[15m]) < 0.1
        for: 30m
        labels:
          severity: info
        annotations:
          summary: "Low message activity"
          description: "Message rate is only {{ $value }}/sec"
```

## Alertmanager Configuration

### alertmanager.yml

```yaml
global:
  smtp_smarthost: 'smtp.gmail.com:587'
  smtp_from: 'alerts@buzzlink.com'
  smtp_auth_username: 'alerts@buzzlink.com'
  smtp_auth_password: 'password'

route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'team-email'

  routes:
    - match:
        severity: critical
      receiver: 'pagerduty'
      continue: true

    - match:
        severity: warning
      receiver: 'team-slack'

receivers:
  - name: 'team-email'
    email_configs:
      - to: 'team@buzzlink.com'
        headers:
          Subject: 'BuzzLink Alert: {{ .GroupLabels.alertname }}'

  - name: 'pagerduty'
    pagerduty_configs:
      - service_key: 'your-pagerduty-key'

  - name: 'team-slack'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/WEBHOOK/URL'
        channel: '#alerts'
        title: 'BuzzLink Alert'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
```

## Running Prometheus (Demo)

### Docker Compose

```yaml
version: '3'
services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - ./alerts.yml:/etc/prometheus/alerts.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-storage:/var/lib/grafana

volumes:
  grafana-storage:
```

## Production Considerations

### High Availability

- Run multiple Prometheus instances
- Use Thanos for long-term storage
- Configure Prometheus federation

### Data Retention

```yaml
# In prometheus.yml
storage:
  tsdb:
    retention.time: 30d
    retention.size: 50GB
```

### Security

- Enable authentication on Grafana
- Use HTTPS for all connections
- Restrict Prometheus scrape to private network
- Use service discovery instead of static configs

### Performance

- Tune scrape intervals based on needs
- Use recording rules for expensive queries
- Implement metric relabeling to reduce cardinality
- Use remote write for centralized storage

## Testing Metrics

```bash
# Check Prometheus metrics endpoint
curl http://localhost:8080/actuator/prometheus

# Check health
curl http://localhost:8080/actuator/health

# Query Prometheus
curl 'http://localhost:9090/api/v1/query?query=up'
```

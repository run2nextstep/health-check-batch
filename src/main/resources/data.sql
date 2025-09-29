-- Initial target servers for development environment
INSERT INTO target_servers (name, url, method, timeout_ms, request_body, enabled, description, environment, created_at, updated_at) VALUES
                                                                                                                                      ('JSONPlaceholder Posts', 'https://jsonplaceholder.typicode.com/posts/1', 'GET', 5000, NULL, true, 'Test API for GET requests', 'dev', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                      ('HTTPBin Status', 'https://httpbin.org/status/200', 'GET', 3000, NULL, true, 'HTTPBin status endpoint', 'dev', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                      ('HTTPBin POST Test', 'https://httpbin.org/post', 'POST', 4000, '{"test": "data", "timestamp": "2024-01-01"}', true, 'HTTPBin POST endpoint for testing', 'dev', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                      ('Local Development API', 'http://localhost:8080/actuator/health', 'GET', 2000, NULL, false, 'Local Spring Boot actuator health endpoint', 'dev', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Initial target servers for test environment
INSERT INTO target_servers (name, url, method, timeout_ms, request_body, enabled, description, environment, created_at, updated_at) VALUES
                                                                                                                                      ('Test API Server', 'http://test.internal.com/health', 'GET', 3000, NULL, false, 'Test environment API health check', 'test', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                      ('Test Database Health', 'http://test.internal.com/db/health', 'GET', 5000, NULL, false, 'Test database connectivity check', 'test', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Initial target servers for production environment
INSERT INTO target_servers (name, url, method, timeout_ms, request_body, enabled, description, environment, created_at, updated_at) VALUES
                                                                                                                                      ('Production API Gateway', 'https://api.production.com/health', 'GET', 5000, NULL, false, 'Production API gateway health check', 'prod', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                      ('Payment Service Health', 'https://payment.production.com/health', 'POST', 8000, '{"service": "payment", "check": "health"}', false, 'Production payment service health check', 'prod', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                      ('User Service Health', 'https://user.production.com/actuator/health', 'GET', 6000, NULL, false, 'Production user service health check', 'prod', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample execution logs for demonstration (uncomment if you want sample data for testing)
/*
INSERT INTO execution_logs (target_server_id, server_name, url, method, success, status_code, elapsed_time_ms, response_body, execution_time, batch_execution_id, environment) VALUES
(1, 'JSONPlaceholder Posts', 'https://jsonplaceholder.typicode.com/posts/1', 'GET', true, 200, 234, '{"id": 1, "title": "Sample Post", "body": "Sample body content"}', DATEADD('MINUTE', -5, CURRENT_TIMESTAMP), 'sample-batch-001', 'dev'),
(2, 'HTTPBin Status', 'https://httpbin.org/status/200', 'GET', true, 200, 156, '{"status": "ok", "timestamp": "2024-01-01T10:00:00Z"}', DATEADD('MINUTE', -5, CURRENT_TIMESTAMP), 'sample-batch-001', 'dev'),
(3, 'HTTPBin POST Test', 'https://httpbin.org/post', 'POST', true, 200, 445, '{"args": {}, "data": "{\"test\": \"data\"}", "json": {"test": "data"}}', DATEADD('MINUTE', -5, CURRENT_TIMESTAMP), 'sample-batch-001', 'dev'),
(1, 'JSONPlaceholder Posts', 'https://jsonplaceholder.typicode.com/posts/1', 'GET', true, 200, 298, '{"id": 1, "title": "Sample Post", "body": "Sample body content"}', DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), 'sample-batch-002', 'dev'),
(2, 'HTTPBin Status', 'https://httpbin.org/status/200', 'GET', false, 0, 5000, NULL, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), 'sample-batch-002', 'dev'),
(3, 'HTTPBin POST Test', 'https://httpbin.org/post', 'POST', true, 200, 612, '{"args": {}, "data": "{\"test\": \"data\"}", "json": {"test": "data"}}', DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), 'sample-batch-002', 'dev');

-- Add error message for failed log
UPDATE execution_logs SET error_message = 'Connection timeout after 5000ms' WHERE success = false;
*/
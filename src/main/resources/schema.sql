-- H2 Database Schema for Health Check Batch Application
-- Place this file in src/main/resources/

-- Drop tables if they exist (for clean recreation)
DROP TABLE IF EXISTS execution_logs;
DROP TABLE IF EXISTS target_servers;

-- Create target_servers table
CREATE TABLE target_servers (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              name VARCHAR(100) NOT NULL,
                              url VARCHAR(500) NOT NULL,
                              method VARCHAR(10) NOT NULL DEFAULT 'GET',
                              timeout_ms BIGINT DEFAULT 5000,
                              request_body TEXT,
                              enabled BOOLEAN DEFAULT TRUE,
                              description VARCHAR(500),
                              environment VARCHAR(20),
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create execution_logs table
CREATE TABLE execution_logs (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              target_server_id BIGINT,
                              server_name VARCHAR(100),
                              url VARCHAR(500),
                              method VARCHAR(10),
                              success BOOLEAN,
                              status_code INTEGER,
                              elapsed_time_ms BIGINT,
                              error_message TEXT,
                              response_body TEXT,
                              execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              batch_execution_id VARCHAR(255),
                              environment VARCHAR(20),
                              FOREIGN KEY (target_server_id) REFERENCES target_servers(id)
);

-- Create indexes for better performance
CREATE INDEX idx_target_servers_enabled ON target_servers(enabled);
CREATE INDEX idx_target_servers_environment ON target_servers(environment);
CREATE INDEX idx_target_servers_enabled_env ON target_servers(enabled, environment);

CREATE INDEX idx_execution_logs_execution_time ON execution_logs(execution_time);
CREATE INDEX idx_execution_logs_target_server_id ON execution_logs(target_server_id);
CREATE INDEX idx_execution_logs_success ON execution_logs(success);
CREATE INDEX idx_execution_logs_batch_id ON execution_logs(batch_execution_id);
CREATE INDEX idx_execution_logs_server_name ON execution_logs(server_name);
CREATE INDEX idx_execution_logs_environment ON execution_logs(environment);
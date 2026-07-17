-- Add replied_to_message_id column to message table
ALTER TABLE message ADD COLUMN replied_to_message_id BIGINT;
ALTER TABLE message ADD CONSTRAINT fk_message_replied_to_message_id
    FOREIGN KEY (replied_to_message_id) REFERENCES message(id);

-- Create message_read table for tracking read status
CREATE TABLE message_read (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_read_message_id FOREIGN KEY (message_id) REFERENCES message(id),
    CONSTRAINT fk_message_read_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_message_read_message_user UNIQUE (message_id, user_id)
);

CREATE INDEX idx_message_read_message_id ON message_read(message_id);
CREATE INDEX idx_message_read_user_id ON message_read(user_id);

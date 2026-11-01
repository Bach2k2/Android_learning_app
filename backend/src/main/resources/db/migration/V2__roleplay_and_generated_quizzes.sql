CREATE TABLE generated_quiz_question (
    id UUID PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    options_json TEXT NOT NULL,
    correct_index INT NOT NULL,
    explanation TEXT
);
CREATE INDEX idx_generated_quiz_question_lesson_id ON generated_quiz_question(lesson_id);

CREATE TABLE roleplay_character (
    id VARCHAR(64) PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    supported_levels_json TEXT NOT NULL
);

CREATE TABLE roleplay_scenario (
    id VARCHAR(64) PRIMARY KEY,
    character_id VARCHAR(64) NOT NULL REFERENCES roleplay_character(id),
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL
);
CREATE INDEX idx_roleplay_scenario_character_id ON roleplay_scenario(character_id);

INSERT INTO roleplay_character (id, display_name, description, supported_levels_json) VALUES
('aiko-cafe-guide', 'Aiko', 'A friendly guide helping you practice everyday Japanese in a neighborhood café.', '["beginner","elementary","intermediate"]'),
('ren-train-helper', 'Ren', 'A patient station helper who makes asking for directions feel comfortable.', '["beginner","elementary","intermediate"]');

INSERT INTO roleplay_scenario (id, character_id, title, description) VALUES
('ordering-at-the-cafe', 'aiko-cafe-guide', 'Ordering at a café', 'Choose a drink, ask about the menu, and respond to a simple follow-up.'),
('meeting-a-friend', 'aiko-cafe-guide', 'Meeting a friend', 'Greet a friend and make a simple plan for the afternoon.'),
('finding-the-platform', 'ren-train-helper', 'Finding the platform', 'Ask where a train platform is and confirm the direction.'),
('buying-a-ticket', 'ren-train-helper', 'Buying a ticket', 'Ask for a ticket and clarify a destination or departure time.');

CREATE TABLE learning_conversation (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    character_id VARCHAR(64) NOT NULL REFERENCES roleplay_character(id),
    scenario_id VARCHAR(64) NOT NULL REFERENCES roleplay_scenario(id),
    level VARCHAR(32) NOT NULL,
    target_language VARCHAR(8) NOT NULL,
    translation_language VARCHAR(8) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_conversation_target_language CHECK (target_language = 'ja'),
    CONSTRAINT ck_conversation_translation_language CHECK (translation_language IN ('en', 'vi')),
    CONSTRAINT ck_conversation_level CHECK (level IN ('beginner', 'elementary', 'intermediate'))
);
CREATE INDEX idx_learning_conversation_owner_expiry ON learning_conversation(user_id, expires_at);

CREATE TABLE conversation_message (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES learning_conversation(id) ON DELETE CASCADE,
    role VARCHAR(16) NOT NULL,
    text TEXT NOT NULL,
    translation TEXT,
    reading TEXT,
    client_message_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_conversation_message_role CHECK (role IN ('USER', 'TUTOR')),
    CONSTRAINT uk_conversation_message_client_role UNIQUE (conversation_id, client_message_id, role)
);
CREATE INDEX idx_conversation_message_history ON conversation_message(conversation_id, created_at, id);

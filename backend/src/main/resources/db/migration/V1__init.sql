CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Users & auth
CREATE TABLE IF NOT EXISTS app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_token (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON refresh_token(user_id);

-- Lessons
CREATE TABLE IF NOT EXISTS lesson (
    id BIGSERIAL PRIMARY KEY,
    lesson_number INT NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    overview TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS vocabulary (
    id UUID PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    kana VARCHAR(255) NOT NULL,
    kanji VARCHAR(255),
    meaning VARCHAR(255) NOT NULL,
    example_sentence TEXT
);
CREATE INDEX IF NOT EXISTS idx_vocabulary_lesson_id ON vocabulary(lesson_id);

CREATE TABLE IF NOT EXISTS grammar_point (
    id UUID PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    explanation TEXT NOT NULL,
    example_sentence TEXT
);
CREATE INDEX IF NOT EXISTS idx_grammar_point_lesson_id ON grammar_point(lesson_id);

CREATE TABLE IF NOT EXISTS lesson_example_sentence (
    id UUID PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    japanese TEXT NOT NULL,
    english TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_lesson_example_sentence_lesson_id ON lesson_example_sentence(lesson_id);

CREATE TABLE IF NOT EXISTS quiz_question (
    id UUID PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    options_json TEXT NOT NULL,
    correct_index INT NOT NULL,
    explanation TEXT,
    source VARCHAR(32) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_quiz_question_lesson_id ON quiz_question(lesson_id);

-- Progress
CREATE TABLE IF NOT EXISTS user_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    lesson_id BIGINT NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    last_viewed_at TIMESTAMPTZ,
    quiz_score INT,
    CONSTRAINT uk_progress_user_lesson UNIQUE (user_id, lesson_id)
);
CREATE INDEX IF NOT EXISTS idx_user_progress_user_id ON user_progress(user_id);

-- Seed: 5 MVP lessons (original content; beginner-level; inspired by common curricula)
INSERT INTO lesson (lesson_number, title, overview) VALUES
(1, 'Greetings & Introductions', 'Basic greetings, simple self-introduction, and polite yes/no.'),
(2, 'Numbers & Time', 'Counting, basic time expressions, and simple schedules.'),
(3, 'Daily Routines', 'Talking about daily activities using common verbs.'),
(4, 'Directions & Locations', 'Asking where things are and giving simple directions.'),
(5, 'Shopping Basics', 'Simple shopping phrases, prices, and polite requests.')
ON CONFLICT (lesson_number) DO NOTHING;

-- Lesson 1 vocab
INSERT INTO vocabulary (id, lesson_id, kana, kanji, meaning, example_sentence)
SELECT gen_random_uuid(), l.id, v.kana, v.kanji, v.meaning, v.example_sentence
FROM lesson l
JOIN (VALUES
 ('こんにちは', NULL, 'hello (daytime)', 'こんにちは。'),
 ('おはよう', NULL, 'good morning (casual)', 'おはよう。'),
 ('おはようございます', NULL, 'good morning (polite)', 'おはようございます。'),
 ('こんばんは', NULL, 'good evening', 'こんばんは。'),
 ('ありがとう', NULL, 'thank you (casual)', 'ありがとう。'),
 ('ありがとうございます', NULL, 'thank you (polite)', 'ありがとうございます。'),
 ('すみません', NULL, 'excuse me / sorry', 'すみません。'),
 ('はい', NULL, 'yes', 'はい、そうです。'),
 ('いいえ', NULL, 'no', 'いいえ、ちがいます。')
) AS v(kana, kanji, meaning, example_sentence) ON TRUE
WHERE l.lesson_number = 1;

INSERT INTO grammar_point (id, lesson_id, title, explanation, example_sentence)
SELECT gen_random_uuid(), l.id, g.title, g.explanation, g.example_sentence
FROM lesson l
JOIN (VALUES
 ('A は B です', 'Polite pattern meaning "A is B". Use to state identity or facts.', 'わたしはマリアです。'),
 ('A は B ですか', 'Turn the sentence into a yes/no question by adding か.', 'あなたは学生ですか。'),
 ('はい／いいえ + そうです／ちがいます', 'Simple ways to agree or disagree politely.', 'はい、そうです。')
) AS g(title, explanation, example_sentence) ON TRUE
WHERE l.lesson_number = 1;

INSERT INTO lesson_example_sentence (id, lesson_id, japanese, english)
SELECT gen_random_uuid(), l.id, e.japanese, e.english
FROM lesson l
JOIN (VALUES
 ('はじめまして。', 'Nice to meet you.'),
 ('わたしはアリです。', 'I am Ali.'),
 ('どうぞよろしく。', 'Please treat me well / Nice to meet you (set phrase).')
) AS e(japanese, english) ON TRUE
WHERE l.lesson_number = 1;

INSERT INTO quiz_question (id, lesson_id, prompt, options_json, correct_index, explanation, source)
SELECT gen_random_uuid(), l.id, q.prompt, q.options_json, q.correct_index, q.explanation, 'SEED'
FROM lesson l
JOIN (VALUES
 ('"A は B です" means…', '["A is B (polite)","A is not B","Is A B?","A and B"]', 0, 'です is a polite "to be".'),
 ('Choose the polite "thank you".', '["ありがとう","ありがとうございます","すみません","こんばんは"]', 1, 'ありがとうございます is the polite form.'),
 ('Make it a question: あなたは学生です', '["あなたは学生です。","あなたは学生ですか。","あなたは学生か。","あなた学生ですか。"]', 1, 'Add か at the end to ask politely.')
) AS q(prompt, options_json, correct_index, explanation) ON TRUE
WHERE l.lesson_number = 1;

-- Lesson 2
INSERT INTO vocabulary (id, lesson_id, kana, kanji, meaning, example_sentence)
SELECT gen_random_uuid(), l.id, v.kana, v.kanji, v.meaning, v.example_sentence
FROM lesson l
JOIN (VALUES
 ('いち', '一', 'one', 'いちは一です。'),
 ('に', '二', 'two', '二つあります。'),
 ('さん', '三', 'three', '三人です。'),
 ('よん', '四', 'four', '四時です。'),
 ('ご', '五', 'five', '五分です。'),
 ('いま', '今', 'now', 'いま、九時です。'),
 ('じ', '時', 'o''clock (counter)', '七時です。'),
 ('ふん', '分', 'minutes (counter)', '十分です。'),
 ('はん', '半', 'half', '七時半です。')
) AS v(kana, kanji, meaning, example_sentence) ON TRUE
WHERE l.lesson_number = 2;

INSERT INTO grammar_point (id, lesson_id, title, explanation, example_sentence)
SELECT gen_random_uuid(), l.id, g.title, g.explanation, g.example_sentence
FROM lesson l
JOIN (VALUES
 ('いま 〜じ 〜ふん です', 'Tell the time using じ (hours) and ふん/ぷん (minutes).', 'いま、八時十分です。'),
 ('〜から 〜まで', 'Use から (from) and まで (until) for time ranges.', '九時から五時までです。')
) AS g(title, explanation, example_sentence) ON TRUE
WHERE l.lesson_number = 2;

INSERT INTO lesson_example_sentence (id, lesson_id, japanese, english)
SELECT gen_random_uuid(), l.id, e.japanese, e.english
FROM lesson l
JOIN (VALUES
 ('いま、何時ですか。', 'What time is it now?'),
 ('いま、七時半です。', 'It''s 7:30 now.'),
 ('学校は九時からです。', 'School starts from 9:00.')
) AS e(japanese, english) ON TRUE
WHERE l.lesson_number = 2;

INSERT INTO quiz_question (id, lesson_id, prompt, options_json, correct_index, explanation, source)
SELECT gen_random_uuid(), l.id, q.prompt, q.options_json, q.correct_index, q.explanation, 'SEED'
FROM lesson l
JOIN (VALUES
 ('"七時半" is…', '["7:00","7:30","8:30","6:30"]', 1, '半 means half (past).'),
 ('Choose the best translation: 九時から五時まで', '["From 9 to 5","At 9 and 5","Before 9","After 5"]', 0, 'から = from, まで = until.'),
 ('How do you ask "What time is it now?"', '["いま、何時ですか。","いま、何分ですか。","いつですか。","何時までですか。"]', 0, 'Use いま + 何時ですか。')
) AS q(prompt, options_json, correct_index, explanation) ON TRUE
WHERE l.lesson_number = 2;

-- Lesson 3
INSERT INTO vocabulary (id, lesson_id, kana, kanji, meaning, example_sentence)
SELECT gen_random_uuid(), l.id, v.kana, v.kanji, v.meaning, v.example_sentence
FROM lesson l
JOIN (VALUES
 ('おきます', '起きます', 'to wake up', '六時におきます。'),
 ('ねます', '寝ます', 'to sleep', '十一時にねます。'),
 ('たべます', '食べます', 'to eat', 'ごはんをたべます。'),
 ('のみます', '飲みます', 'to drink', 'みずをのみます。'),
 ('いきます', '行きます', 'to go', '学校へいきます。'),
 ('かえります', '帰ります', 'to return', 'うちへかえります。'),
 ('べんきょうします', '勉強します', 'to study', '日本語をべんきょうします。')
) AS v(kana, kanji, meaning, example_sentence) ON TRUE
WHERE l.lesson_number = 3;

INSERT INTO grammar_point (id, lesson_id, title, explanation, example_sentence)
SELECT gen_random_uuid(), l.id, g.title, g.explanation, g.example_sentence
FROM lesson l
JOIN (VALUES
 ('〜に (time)', 'Use に after specific times (like 6時) to mark when something happens.', '六時におきます。'),
 ('〜を (object)', 'Use を after the thing you do an action to (direct object).', 'パンをたべます。'),
 ('〜へ (direction)', 'Use へ to indicate direction (to/toward).', '会社へいきます。')
) AS g(title, explanation, example_sentence) ON TRUE
WHERE l.lesson_number = 3;

INSERT INTO lesson_example_sentence (id, lesson_id, japanese, english)
SELECT gen_random_uuid(), l.id, e.japanese, e.english
FROM lesson l
JOIN (VALUES
 ('七時にあさごはんをたべます。', 'I eat breakfast at 7.'),
 ('九時に学校へいきます。', 'I go to school at 9.'),
 ('夜、日本語をべんきょうします。', 'At night, I study Japanese.')
) AS e(japanese, english) ON TRUE
WHERE l.lesson_number = 3;

INSERT INTO quiz_question (id, lesson_id, prompt, options_json, correct_index, explanation, source)
SELECT gen_random_uuid(), l.id, q.prompt, q.options_json, q.correct_index, q.explanation, 'SEED'
FROM lesson l
JOIN (VALUES
 ('Pick the correct particle: 六時 ___ おきます。', '["を","に","へ","で"]', 1, 'Specific time uses に.'),
 ('"みずをのみます" means…', '["I drink water","I eat water","I go to water","Water goes"]', 0, 'のみます = to drink.'),
 ('Choose the best: 学校___いきます。', '["を","へ","に","の"]', 1, 'へ marks direction.')
) AS q(prompt, options_json, correct_index, explanation) ON TRUE
WHERE l.lesson_number = 3;

-- Lesson 4
INSERT INTO vocabulary (id, lesson_id, kana, kanji, meaning, example_sentence)
SELECT gen_random_uuid(), l.id, v.kana, v.kanji, v.meaning, v.example_sentence
FROM lesson l
JOIN (VALUES
 ('ここ', NULL, 'here', 'ここです。'),
 ('そこ', NULL, 'there (near you)', 'そこです。'),
 ('あそこ', NULL, 'over there', 'あそこです。'),
 ('どこ', NULL, 'where', 'どこですか。'),
 ('みぎ', '右', 'right', 'みぎへいきます。'),
 ('ひだり', '左', 'left', 'ひだりへいきます。'),
 ('まっすぐ', NULL, 'straight', 'まっすぐいきます。'),
 ('ちかく', '近く', 'near', '駅のちかくです。')
) AS v(kana, kanji, meaning, example_sentence) ON TRUE
WHERE l.lesson_number = 4;

INSERT INTO grammar_point (id, lesson_id, title, explanation, example_sentence)
SELECT gen_random_uuid(), l.id, g.title, g.explanation, g.example_sentence
FROM lesson l
JOIN (VALUES
 ('A は どこですか', 'Ask where A is.', 'トイレはどこですか。'),
 ('〜の ちかく', 'Use の to connect nouns; ちかく means "near".', '駅のちかくです。')
) AS g(title, explanation, example_sentence) ON TRUE
WHERE l.lesson_number = 4;

INSERT INTO lesson_example_sentence (id, lesson_id, japanese, english)
SELECT gen_random_uuid(), l.id, e.japanese, e.english
FROM lesson l
JOIN (VALUES
 ('駅はどこですか。', 'Where is the station?'),
 ('まっすぐいって、みぎです。', 'Go straight, then it''s on the right.'),
 ('コンビニはここです。', 'The convenience store is here.')
) AS e(japanese, english) ON TRUE
WHERE l.lesson_number = 4;

INSERT INTO quiz_question (id, lesson_id, prompt, options_json, correct_index, explanation, source)
SELECT gen_random_uuid(), l.id, q.prompt, q.options_json, q.correct_index, q.explanation, 'SEED'
FROM lesson l
JOIN (VALUES
 ('"どこ" means…', '["here","where","right","near"]', 1, 'どこ = where.'),
 ('Pick the best: 駅の___です。 (near)', '["ちかく","みぎ","ひだり","そこ"]', 0, 'ちかく = near.'),
 ('"まっすぐ" is…', '["straight","left","over there","now"]', 0, 'まっすぐ = straight.')
) AS q(prompt, options_json, correct_index, explanation) ON TRUE
WHERE l.lesson_number = 4;

-- Lesson 5
INSERT INTO vocabulary (id, lesson_id, kana, kanji, meaning, example_sentence)
SELECT gen_random_uuid(), l.id, v.kana, v.kanji, v.meaning, v.example_sentence
FROM lesson l
JOIN (VALUES
 ('いくら', NULL, 'how much', 'これはいくらですか。'),
 ('たかい', '高い', 'expensive / high', 'このりんごはたかいです。'),
 ('やすい', '安い', 'cheap', 'これはやすいです。'),
 ('これ', NULL, 'this', 'これをください。'),
 ('それ', NULL, 'that', 'それは何ですか。'),
 ('ください', NULL, 'please give me', '水をください。'),
 ('みせ', '店', 'shop/store', 'この店は大きいです。')
) AS v(kana, kanji, meaning, example_sentence) ON TRUE
WHERE l.lesson_number = 5;

INSERT INTO grammar_point (id, lesson_id, title, explanation, example_sentence)
SELECT gen_random_uuid(), l.id, g.title, g.explanation, g.example_sentence
FROM lesson l
JOIN (VALUES
 ('これ／それ は いくらですか', 'Ask the price of something using いくら (how much).', 'これはいくらですか。'),
 ('〜を ください', 'Polite request meaning "Please give me ~".', 'これをください。')
) AS g(title, explanation, example_sentence) ON TRUE
WHERE l.lesson_number = 5;

INSERT INTO lesson_example_sentence (id, lesson_id, japanese, english)
SELECT gen_random_uuid(), l.id, e.japanese, e.english
FROM lesson l
JOIN (VALUES
 ('これはいくらですか。', 'How much is this?'),
 ('りんごをください。', 'Please give me an apple.'),
 ('それはたかいです。', 'That is expensive.')
) AS e(japanese, english) ON TRUE
WHERE l.lesson_number = 5;

INSERT INTO quiz_question (id, lesson_id, prompt, options_json, correct_index, explanation, source)
SELECT gen_random_uuid(), l.id, q.prompt, q.options_json, q.correct_index, q.explanation, 'SEED'
FROM lesson l
JOIN (VALUES
 ('Choose: "How much is this?"', '["これはいくらですか。","これはどこですか。","これはなんですか。","これはだれですか。"]', 0, 'いくら asks price.'),
 ('"〜をください" is used to…', '["ask for an item politely","say goodbye","ask the time","introduce yourself"]', 0, 'It means "Please give me ~".'),
 ('Pick the meaning of やすい', '["cheap","expensive","near","right"]', 0, 'やすい = cheap.')
) AS q(prompt, options_json, correct_index, explanation) ON TRUE
WHERE l.lesson_number = 5;

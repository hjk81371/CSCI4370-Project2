-- There are the SQL statements we used in our project. Unknowns are represented with "?"

-- Here are our INSERT statements. Each of these queries are pretty self explanatory: they put the given data into the table
INSERT INTO user (username, password, firstName, lastName) VALUES (?, ?, ?, ?);
INSERT INTO post (userId, postDate, postText) VALUES (?, ?, ?);
INSERT INTO comment (postId, userId, commentDate, commentText) VALUES (?, ?, ?, ?);
INSERT INTO hashtag (hashtag, postId) VALUES (?, ?);
INSERT INTO heart (postId, userId) VALUES (?, ?);
INSERT INTO bookmark (postId, userId) VALUES (?, ?);
INSERT INTO follow (followerUserId, followeeUserId) VALUES (?, ?);

-- The heart, bookmark, and follow tables all require DELETE statements as well:
DELETE FROM heart WHERE postId = ? AND userId = ?;
DELETE FROM bookmark WHERE postId = ? AND userId = ?;
DELETE FROM follow WHERE followerUserId = ? AND followeeUserId = ?;

-- Most of our SELECT queries were simple, with additional logic done in Java

-- Selects posts (or certain attributes from posts) made by a specific user, ordered from newest to oldest
SELECT * FROM post WHERE userId = ? ORDER BY postDate DESC;

-- Selects id, date, text, and userId of a specific post
SELECT postId, postDate, postText, userId FROM post WHERE postId = ?;

-- Selects all hearts/comments/bookmarks given to a single post
SELECT * FROM heart WHERE postId = ?;
SELECT * FROM comment WHERE postId = ?;
SELECT * FROM bookmark WHERE postId = ?;

-- Selects all comments given to a single post, ordered from newest to oldest
SELECT * FROM comment WHERE postId = ? ORDER BY commentDate DESC;

-- Select posts which contain hashtags within a certain list
-- A variable number of arguments are passed into the query using Java
SELECT DISTINCT post.postId, userId, postText, postDate
FROM post, hashtag
WHERE post.postId = hashtag.postId
AND hashTag IN (?, ?, ... ?)
ORDER BY postDate DESC;

-- Selects posts made by users who are followed by a given user, sorted from newest to oldest
SELECT postId, post.userId, postDate, postText
FROM follow, post
WHERE followeeUserId = post.userId
AND followerUserId = ?
ORDER BY postDate DESC;

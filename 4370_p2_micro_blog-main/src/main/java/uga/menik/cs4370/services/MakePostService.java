package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.sql.DataSource;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.models.FollowableUser;

@Service
public class MakePostService {

	private final DataSource dataSource;
	private final UserService userService;

	@Autowired
	public MakePostService(DataSource dataSource, UserService userService) {
		this.dataSource = dataSource;
		this.userService = userService;
	}

	public boolean makePost(User user, String text) {

		String userId = user.getUserId();

		// Get current data and time
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date currentDate = new Date();
		String formattedDate = sdf.format(currentDate);

		final String sqlString = "INSERT INTO post (userId, postDate, postText) VALUES (?, ?, ?)";

		try (Connection conn = dataSource.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, userId);
			pstmt.setString(2, formattedDate);
			pstmt.setString(3, text);

			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				System.out.println("Post inserted successfully!");

				ResultSet generatedKeys = pstmt.getGeneratedKeys();
				if (generatedKeys.next()) {

					String postId = Integer.toString(generatedKeys.getInt(1));
					System.out.println("Generated postId: " + postId);
					addHashTags(postId, text);
				} else {
					System.out.println("Failed to get generated postId key");
				}

				return true;

			} else {
				System.out.println("Failed to insert post.");
				return false;
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return false;
		}

	} // makePost

	private void addHashTags(String postId, String text) {
		List<String> hashTags = hashTagExtractor(text);

		System.out.println("HASHTAG LENGTH: " + hashTags.size());

		for (String hashtag : hashTags) {
			String sqlString = "INSERT INTO hashtag (hashtag, postId) VALUES (?, ?)";

			try (Connection conn = dataSource.getConnection();
					PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

				pstmt.setString(1, hashtag);
				pstmt.setString(2, postId);

				pstmt.executeUpdate();

			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		} // for

	} // addHashTags

	private List<String> hashTagExtractor(String input) {

		List<String> hashtags = new ArrayList<>();
		Pattern pattern = Pattern.compile("#\\w+");
		Matcher matcher = pattern.matcher(input);

		while (matcher.find()) {
			hashtags.add(matcher.group());
		}

		return hashtags;
	} // HashtagExtractor

	public List<Post> getPosts() {
		List<Post> posts = new ArrayList<>();

		final String sqlString = "select * from post order by postDate desc";

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

			try (ResultSet rs = pstmt.executeQuery()) {

				while (rs.next()) {
					String currPostId = rs.getString("postId");
					String currUserId = rs.getString("userId");
					String currPostText = rs.getString("postText");
					String dbDateTime = rs.getString("postDate");

        
        			LocalDateTime dateTime = LocalDateTime.parse(dbDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        			String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a"));

					User currUser = userService.getUserById(currUserId);

					int heartsCount = getHeartsCount(currPostId);
					int commentsCount = getCommentsCount(currPostId);
					boolean isHearted = getIsHearted(currPostId);
					boolean isBookmarked = getIsBookmarked(currPostId);

					Post post = new Post(currPostId, currPostText, formattedDate, currUser, heartsCount, commentsCount,
							isHearted, isBookmarked);
					posts.add(post);
				} // while

			}

		} catch (SQLException sqle) {
			System.err.println("SQL EXCEPTION: " + sqle.getMessage());
		} // try

		return posts;
	} // getPosts

	public List<Post> getPostsFromUID(String user) {
		List<Post> posts = new ArrayList<>();

		final String sqlString = "select * from post where userId = " + user + " order by postDate desc";

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

			try (ResultSet rs = pstmt.executeQuery()) {

				while (rs.next()) {
					String currPostId = rs.getString("postId");
					String currUserId = rs.getString("userId");
					String currPostText = rs.getString("postText");
					String currPostDate = rs.getString("postDate");

					User currUser = userService.getUserById(currUserId);

					int heartsCount = getHeartsCount(currPostId);
					int commentsCount = getCommentsCount(currPostId);
					boolean isHearted = getIsHearted(currPostId);
					boolean isBookmarked = getIsBookmarked(currPostId);

					Post post = new Post(currPostId, currPostText, currPostDate, currUser, heartsCount, commentsCount,
							isHearted, isBookmarked);
					posts.add(post);
				} // while

			}

		} catch (SQLException sqle) {
			System.err.println("SQL EXCEPTION: " + sqle.getMessage());
		} // try

		return posts;
	} // getPosts



	private int getHeartsCount(String postId) {

		final String sqlString = "select * from heart where postId = " + postId;

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			while (rs.next()) {
				count++;
			}
			return count;

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return 0;
		}
	} // getHeartsCount

	private int getCommentsCount(String postId) {

		final String sqlString = "select * from comment where postId = " + postId;

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			while (rs.next()) {
				count++;
			}
			return count;

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return 0;
		}
	} // getCommentsCount

	private boolean getIsHearted(String postId) {

		final String sqlString = "select * from heart where postId = " + postId;

		String loggedInUserId = userService.getLoggedInUser().getUserId();
		System.out.println("isHearted UserId: " + loggedInUserId);

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String currId = rs.getString("userId");
				if (currId.equals(loggedInUserId)) {
					return true;
				}
			}
			return false;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return false;
		}
	} // getIsHearted

	private boolean getIsBookmarked(String postId) {

		final String sqlString = "select * from bookmark where postId = " + postId;

		String loggedInUserId = userService.getLoggedInUser().getUserId();
		System.out.println("isBookmarked UserId: " + loggedInUserId);

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String currId = rs.getString("userId");
				if (currId.equals(loggedInUserId)) {
					return true;
				}
			}
			return false;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return false;
		}
	} // isBookmarked

	public List<Comment> getComments(String postId) {

		final String sqlString = "select * from comment where postId = " + postId + " order by commentDate desc";

		List<Comment> comments = new ArrayList<>();

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String currContent = rs.getString("commentText");
				String currDate = rs.getString("commentDate");
				String currUserId = rs.getString("userId");
				User currUser = userService.getUserById(currUserId);
				// postId, content, date, user
				Comment currComment = new Comment(postId, currContent, currDate, currUser);
				comments.add(currComment);
			}

			return comments;

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return comments;
		}
	}

	public boolean makeComment(String postId, String comment) {

		final String sqlString = "insert into comment (postId, userId, commentDate, commentText) values (?, ?, ?, ?)";

		String userId = userService.getLoggedInUser().getUserId();

		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
		Date currentDate = new Date();
		String formattedDate = sdf.format(currentDate);

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

			pstmt.setString(1, postId);
			pstmt.setString(2, userId);
			pstmt.setString(3, formattedDate);
			pstmt.setString(4, comment);

			int rowsChanged = pstmt.executeUpdate();

			if (rowsChanged > 0) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException sqle) {
			System.err.println("SQL EXCEPTION: " + sqle.getMessage());
			return false;
		} // try

	} // makeComment

	public List<ExpandedPost> getExpandedPost(String postId) {

		final String sql = "select postId, postDate, postText, userId from post where postId = " + postId;

		User currUser = userService.getLoggedInUser();

		boolean currIsHearted = getIsHearted(postId);

		boolean currIsBookmarked = getIsBookmarked(postId);

		int currHeartCount = getHeartsCount(postId);

		int currCommentCount = getCommentsCount(postId);

		List<Comment> currComments = getComments(postId);

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			try (ResultSet rs = pstmt.executeQuery()) {

				while (rs.next()) {
					String currPostDate = rs.getString("postDate");
					String currPostText = rs.getString("postText");
					String userId = rs.getString("userId");
					System.out.println("before getUser");
					User postUser = userService.getUserById(userId);
					System.out.println("after getUser");
					ExpandedPost expandedPost = new ExpandedPost(postId, currPostText, currPostDate, postUser,
							currHeartCount, currCommentCount, currIsHearted, currIsBookmarked, currComments);
					return List.of(expandedPost);
				}
				return null;
			} catch (SQLException e) {
				System.out.println("ERROR IN FIRST TRY");
				e.printStackTrace();
				return null;
			}

		} catch (SQLException sqle) {
			System.err.println("SQL EXCEPTIONL " + sqle.getMessage());
			return null;
		} 
	}

	public boolean handleHeart(String postId, boolean isHeart) {

		String userId = userService.getLoggedInUser().getUserId();

		boolean isHeart2 = getIsHearted(postId);

		System.out.println("HEART2: " + isHeart2);

		if (!isHeart2) {
			// liking the post

			final String sqlString = "insert into heart (postId, userId) values (?, ?)";


			try (Connection conn = dataSource.getConnection();
					PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

				pstmt.setString(1, postId);
				pstmt.setString(2, userId);

				int rowsChanged = pstmt.executeUpdate();

				if (rowsChanged > 0) {
					return true;
				} else {
					return false;
				}

			} catch (SQLException sqle) {
				System.err.println("SQL EXCEPTION: " + sqle.getMessage());
				return false;
			} // try

		} else {
			// unliking the post

			final String sqlString = "delete from heart where postId = ? and userId = ?";

			try (Connection conn = dataSource.getConnection();
					PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

				pstmt.setString(1, postId);
				pstmt.setString(2, userId);

				int rowsChanged = pstmt.executeUpdate();

				if (rowsChanged > 0) {
					return true;
				} else {
					return false;
				}

			} catch (SQLException sqle) {
				System.err.println("SQL EXCEPTION: " + sqle.getMessage());
				return false;
			} // try

		} // if

	} // handleHeart

	public boolean handleBookmark(String postId, boolean isBookmark) {

		boolean isBookmarked2 = getIsBookmarked(postId);

		if (!isBookmarked2) {
			// liking the post

			final String sqlString = "insert into bookmark (postId, userId) values (?, ?)";

			String userId = userService.getLoggedInUser().getUserId();

			try (Connection conn = dataSource.getConnection();
					PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

				pstmt.setString(1, postId);
				pstmt.setString(2, userId);

				int rowsChanged = pstmt.executeUpdate();

				if (rowsChanged > 0) {
					return true;
				} else {
					return false;
				}

			} catch (SQLException sqle) {
				System.err.println("SQL EXCEPTION: " + sqle.getMessage());
				return false;
			} // try

		} else {
			// unliking the post

			final String sqlString = "delete from bookmark where postId = ? and userId = ?";

			String userId = userService.getLoggedInUser().getUserId();

			try (Connection conn = dataSource.getConnection();
					PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

				pstmt.setString(1, postId);
				pstmt.setString(2, userId);

				int rowsChanged = pstmt.executeUpdate();

				if (rowsChanged > 0) {
					return true;
				} else {
					return false;
				}

			} catch (SQLException sqle) {
				System.err.println("SQL EXCEPTION: " + sqle.getMessage());
				return false;
			} // try

		} // if

	} // handleBookmark

	// Takes a search (string) and returns a list of Post objects
	public List<Post> getPostsFromHashtag(String input) {
		List<String> hashtags = hashTagExtractor(input);
		List<Post> posts = new ArrayList<>();

		String sqlString = "SELECT DISTINCT post.postId, userId, postText, postDate FROM post, hashtag WHERE post.postId = hashtag.postId AND hashTag IN (";

		// Add correct number of ?s to query
		for (int i = 0; i < hashtags.size(); i++) {
			sqlString += "?";

			if (i != hashtags.size() - 1) {
				sqlString += ", ";
			} else {
				sqlString += ") ORDER BY postDate DESC";
			}
		}

		try {
			// Connect to database and make preparedstatement
			Connection conn = dataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sqlString);

			// Prepare statement with hashtag values
			for (int i = 0; i < hashtags.size(); i++) {
				pstmt.setString(i + 1, hashtags.get(i));
			}

			ResultSet rs = pstmt.executeQuery();

			// Access posts from query and return them to website
			while (rs.next()) {
				String currPostId = rs.getString("postId");
				String currUserId = rs.getString("userId");
				String currPostText = rs.getString("postText");
				String currPostDate = rs.getString("postDate");
	
				User currUser = userService.getUserById(currUserId);
	
				int heartsCount = getHeartsCount(currPostId);
				int commentsCount = getCommentsCount(currPostId);
				boolean isHearted = getIsHearted(currPostId);
				boolean isBookmarked = getIsBookmarked(currPostId);
	
				Post post = new Post(currPostId, currPostText, currPostDate, currUser,
					heartsCount, commentsCount, isHearted, isBookmarked);
				// TODO: Figure out what this means:
				// if bookmarked add post otherwise do nothing
				posts.add(post);
			}

		} catch (SQLException sqle) {
			System.err.println("SQL EXCEPTION: " + sqle.getMessage());
		}

		return posts;
	} // getPostsFromHashtag

	public List<Post> getPostsFromBookmark() {
		List<Post> posts = new ArrayList<>();

		final String sqlString = "select * from post order by postDate desc";

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

			try (ResultSet rs = pstmt.executeQuery()) {

				while (rs.next()) {
					String currPostId = rs.getString("postId");
					String currUserId = rs.getString("userId");
					String currPostText = rs.getString("postText");
					String currPostDate = rs.getString("postDate");

					User currUser = userService.getUserById(currUserId);

					int heartsCount = getHeartsCount(currPostId);
					int commentsCount = getCommentsCount(currPostId);
					boolean isHearted = getIsHearted(currPostId);
					boolean isBookmarked = getIsBookmarked(currPostId);

					Post post = new Post(currPostId, currPostText, currPostDate, currUser, heartsCount, commentsCount,
							isHearted, isBookmarked);
				
					if(post.isBookmarked() == true) {
						posts.add(post);
					}
					

				} // while

			}

		} catch (SQLException sqle) {
			System.err.println("SQL EXCEPTION: " + sqle.getMessage());
		} // try

		return posts;
	} // getPostsFromBookmark

	// Once this function is working, go to HomeController.java and change line
	// List<Post> posts = makePostService.getPosts(); (Line 61)
	public List<Post> getFollowerPosts() {
		List<Post> posts = new ArrayList<>();
		final String sqlString = "SELECT postId, post.userId, postDate, postText FROM follow, post WHERE followeeUserId = post.userId AND followerUserId = ? ORDER BY postDate DESC;";

		try {

			Connection conn = dataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sqlString);
			pstmt.setString(1, userService.getLoggedInUser().getUserId());

			try (ResultSet rs = pstmt.executeQuery()) {

				while (rs.next()) {
					String currPostId = rs.getString("postId");
					String currUserId = rs.getString("userId");
					String currPostText = rs.getString("postText");
					String currPostDate = rs.getString("postDate");

					User currUser = userService.getUserById(currUserId);

					int heartsCount = getHeartsCount(currPostId);
					int commentsCount = getCommentsCount(currPostId);
					boolean isHearted = getIsHearted(currPostId);
					boolean isBookmarked = getIsBookmarked(currPostId);

					Post post = new Post(currPostId, currPostText, currPostDate, currUser, heartsCount, commentsCount,
							isHearted, isBookmarked);
				
					posts.add(post);
				} // while

			}

		} catch (SQLException sqle) {
			System.err.println("SQL EXCEPTION: " + sqle.getMessage());
		} // try

		return posts;
	} // getPostsFollower
}


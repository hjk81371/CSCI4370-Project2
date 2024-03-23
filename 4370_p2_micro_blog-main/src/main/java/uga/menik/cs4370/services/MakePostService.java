package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        SimpleDateFormat sdf = new SimpleDateFormat("MM dd, yyyy, hh:mm a");
        Date currentDate = new Date();
        String formattedDate = sdf.format(currentDate);

        final String sqlString = "INSERT INTO post (userId, postDate, postText) VALUES (?, ?, ?)";


        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS)) {

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


            try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
    
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

        final String sqlString = "select * from post";

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
                    boolean isHearted = getIsHearted(currPostId, currUserId);
                    boolean isBookmarked = getIsBookmarked(currPostId, currUserId);
                    
                    Post post = new Post(currPostId, currPostText, currPostDate, currUser, heartsCount, commentsCount, isHearted, isBookmarked);
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

    private boolean getIsHearted(String postId, String currUserId) {

        final String sqlString = "select * from heart where postId = " + postId;


        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String currId = rs.getString("userId");
                if (currId.equals(currUserId)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    } // getIsHearted


    private boolean getIsBookmarked(String postId, String currUserId) {

        final String sqlString = "select * from bookmark where postId = " + postId;


        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String currId = rs.getString("userId");
                if (currId.equals(currUserId)) {
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

        final String sqlString = "select * from comment where postId = " + postId;
    
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


        final String sql = "select postId, postDate, postText from post where postId = " + postId;

        User currUser = userService.getLoggedInUser();

        boolean currIsHearted = getIsHearted(postId, currUser.getUserId());

        boolean currIsBookmarked = getIsBookmarked(postId, currUser.getUserId());

        int currHeartCount = getHeartsCount(postId);

        int currCommentCount = getCommentsCount(postId);

        List<Comment> currComments = getComments(postId);

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {

                while(rs.next()) {
                    String currPostDate = rs.getString("postDate");
                    String currPostText = rs.getString("postText");
                    ExpandedPost expandedPost = new ExpandedPost(postId, currPostText, currPostDate, currUser, currHeartCount, currCommentCount, currIsHearted, currIsBookmarked, currComments);
                    return List.of(expandedPost);
                }
                return null;
            }

        } catch (SQLException sqle) {
            System.err.println("SQL EXCEPTIONL " + sqle.getMessage());
            return null;
        }
    }

    public boolean handleHeart(String postId, boolean isHeart) {

        if (isHeart) {
            // liking the post

            final String sqlString = "insert into heart (postId, userId) values (?, ?)";

            String userId = userService.getLoggedInUser().getUserId();
    
            try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
    
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

            String userId = userService.getLoggedInUser().getUserId();
    
            try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
    
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

        if (isBookmark) {
            // liking the post

            final String sqlString = "insert into bookmark (postId, userId) values (?, ?)";

            String userId = userService.getLoggedInUser().getUserId();
    
            try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
    
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
    
            try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
    
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

}

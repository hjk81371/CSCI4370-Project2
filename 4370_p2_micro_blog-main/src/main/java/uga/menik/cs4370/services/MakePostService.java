package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
        Date currentDate = new Date();
        String formattedDate = sdf.format(currentDate);

        final String sqlString = "INSERT INTO post (userId, postDate, postText) VALUES (?, ?, ?)";


        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, formattedDate);
            pstmt.setString(3, text);
    
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Post inserted successfully!");
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

}

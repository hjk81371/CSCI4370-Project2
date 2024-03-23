package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import uga.menik.cs4370.services.*;

@Service
public class ProfileService {
   
    private final DataSource dataSource;
    private final UserService userService;

    @Autowired
    public ProfileService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

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

     public List<Post> getPosts(String userId) {
        List<Post> posts = new ArrayList<>();

        final String sqlString = "select * from post where userid = " +userId;

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


}

/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.FollowableUser;
import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.utility.Utility;

/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {

    private final DataSource dataSource;
    private final UserService userService;

    @Autowired
    public PeopleService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }
    

    /**
     * This function should query and return all users that 
     * are followable. The list should not contain the user 
     * with id userIdToExclude.
     */
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {

        List<FollowableUser> followableUserList = new ArrayList<>();
        // Write an SQL query to find the users that are not the current user.
        final String sqlString = "select * from user where user.userID != " + userIdToExclude;

        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String currUserId = rs.getString("userId");
                    String currRowFirstName = rs.getString("firstName");
                    String currRowLastName = rs.getString("lastName");
                    boolean isFollowed = getIsFollowed(userIdToExclude, currUserId);
                    String lastPostedDate = getLastPostedDate(currUserId);
                   
                    FollowableUser currFollowableUser = new FollowableUser(currUserId, currRowFirstName, currRowLastName, isFollowed, lastPostedDate);//"Mar 07, 2024, 10:54 PM"); //currPostDate); 
                    followableUserList.add(currFollowableUser);
                } // while

            }

        } catch (SQLException sqle) {
            System.err.println("SQL EXCEPTION 12: " + sqle.getMessage());
        } // try

        // Use the query result to create a list of followable users.
        // See UserService.java to see how to access rows and their attributes
        // from the query result.
        // Check the following createSampleFollowableUserList function to see 
        // how to create a list of FollowableUsers.

        // Replace the following line and return the list you created.
        return followableUserList;
    }

    private String getLastPostedDate(String userId) {
        final String sqlString = "SELECT min(postDate) AS latest_post FROM post WHERE userId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {


            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    System.out.println("DATE USERID: " + userId);
                    String dbDateTime = rs.getString("latest_post");
                    if (dbDateTime == null) {
                        return "N/A";
                    } else {
                        LocalDateTime dateTime = LocalDateTime.parse(dbDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a"));
                        return formattedDate;                   
                    }
                } // while
                return "N/A";
            }
        } catch (SQLException sqle) {
            System.err.println("SQL EXCEPTION this_one: " + sqle.getMessage());
            return "N/A";
        } // try
    } // getIsFollowed

    private boolean getIsFollowed(String currUserId, String otherUserId) {
        final String sqlString = "select * from follow where followerUserId = ? and followeeUserId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

            pstmt.setString(1, currUserId);
            pstmt.setString(2, otherUserId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    return true;
                } // while
                return false;
            }
        } catch (SQLException sqle) {
            System.err.println("SQL EXCEPTION 13: " + sqle.getMessage());
            return false;
        } // try
    } // getIsFollowed

    

    public boolean handleFollow(String userId, boolean isFollow) {

        String loggedInUserId = userService.getLoggedInUser().getUserId();

        if (isFollow) {
            // following the user

            String sqlString = "insert into follow (followerUserId, followeeUserId) values (?, ?)";


            try (Connection conn = dataSource.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

                pstmt.setString(1, loggedInUserId);
                pstmt.setString(2, userId);

                int rowsChanged = pstmt.executeUpdate();

                if (rowsChanged > 0) {
                    return true;
                } else {
                    return false;
                }

            } catch (SQLException sqle) {
                System.err.println("SQL EXCEPTION 14: " + sqle.getMessage());
                return false;
            } // try

        } else {
            // unfollowing the user

            String sqlString = "delete from follow where followerUserId = ? and followeeUserId = ?";

            try (Connection conn = dataSource.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sqlString)) {

                pstmt.setString(1, loggedInUserId);
                pstmt.setString(2, userId);

                int rowsChanged = pstmt.executeUpdate();

                if (rowsChanged > 0) {
                    return true;
                } else {
                    return false;
                }

            } catch (SQLException sqle) {
                System.err.println("SQL EXCEPTION 15: " + sqle.getMessage());
                return false;
            } // try

        } // if

    } // handleFollow

}

package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.User;

@Service
public class MakePostService {
    
    private final DataSource dataSource;

    @Autowired
    public MakePostService(DataSource dataSource) {
        this.dataSource = dataSource;
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

    }

}

/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uga.menik.cs4370.services.MakePostService;
import uga.menik.cs4370.services.UserService;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.utility.Utility;


/**
 * Handles /hashtagsearch URL and possibly others.
 * At this point no other URLs.
 */
@Controller
@RequestMapping("/hashtagsearch")
public class HashtagSearchController {

    private final MakePostService makePostSrv;
    private final UserService usrSrv;
    public HashtagSearchController(MakePostService makePostSrv, UserService usrSrv) {
        this.postService = postService;
        this.usrSrv = usrSrv;
    }

    /**
     * This function handles the /hashtagsearch URL itself.
     * This URL can process a request parameter with name hashtags.
     * In the browser the URL will look something like below:
     * http://localhost:8081/hashtagsearch?hashtags=%23amazing+%23fireworks
     * Note: the value of the hashtags is URL encoded.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "hashtags") String hashtags) {
        System.out.println("User is searching: " + hashtags);

        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.

        User logdIn = userService.getLoggedInUser();
        List<String> hashList = new ArrayList<>();
        List<ExpandedPost> hashPostComb = new ArrayList<>();

        for(String hash : hashtags.split("\\s+")){
            hashList.add(hashtag);
        }
        for(String hashtag : hashList){
            if(!hastag.startsWith('#')){
                hashtag = '#' + hashtag;
            }
            List<ExpandedPost> hashPosts = makePostSrv.getHashtag(logdIn, hashtag);
            hashPostComb.addAll(hashPosts);
        }
        hashPostCom = makePostSrv.order.By
        List<Post> posts = Utility.createSamplePostsListWithoutComments();
        mv.addObject("posts", posts);

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // String errorMessage = "Some error occured!";
        // mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);
        
        return mv;
    }
    
}

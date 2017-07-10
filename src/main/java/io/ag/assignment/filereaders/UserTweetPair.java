package io.ag.assignment.filereaders;

public class UserTweetPair {

   private final String user;
   private final String tweet;

   public UserTweetPair(String user, String tweet) {
      this.user = user;
      this.tweet = tweet;
   }

   public String getUser() {
      return user;
   }

   public String getTweet() {
      return tweet;
   }
}

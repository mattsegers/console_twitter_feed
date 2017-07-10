package io.console.twitterfeed.filereaders;

import io.console.twitterfeed.framework.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.ValidationException;

import org.apache.commons.lang3.StringUtils;

public class TweetFileToTreeMapReader {

   private File tweetFile;

   // A map of users who are followed by other users, created by the UserFile Reader
   private HashMap<String, HashSet<String>> followedByMapSet = null;

   // A TreeMap of User --> User/Tweet pairs. TreeMap gives good performance and added to that the ordering is natural
   // (alphabetical in this case), which we want.
   private TreeMap<String, List<UserTweetPair>> tweetsByUser = null;

   /*
    * Builds the expected output to print to console based on the current state of the map.
    */
   public String getPrintableOutput() {
      StringBuilder sb = new StringBuilder();

      for (Map.Entry<String, List<UserTweetPair>> entry : tweetsByUser.entrySet()) {
         sb.append(entry.getKey());
         sb.append(":");
         sb.append(System.lineSeparator());
         for (UserTweetPair tweet : entry.getValue()) {
            sb.append("\t@");
            sb.append(tweet.getUser());
            sb.append(": ");
            sb.append(tweet.getTweet());
            sb.append(System.lineSeparator());
         }
      }
      return sb.toString().trim();
   }

   /*
    * Initializes the map if it has not already been initialized.
    */
   private void lazyLoadMap() {
      if (this.tweetsByUser == null) {
         this.tweetsByUser = new TreeMap<>();
      }
   }

   public TweetFileToTreeMapReader(HashMap<String, HashSet<String>> followedByMapSet) {
      this.followedByMapSet = followedByMapSet;
   }

   public TweetFileToTreeMapReader withTweetsFile(File file) {
      this.tweetFile = file;
      return this;
   }

   /*
   @formatter:off
   Does processing:
    - Loads map if necessary
    - Adds users based on input from UserFile Reader
    - Reads tweets and matches to users accordingly
    @formatter:on
    */
   public void doProcessing() throws IOException, ValidationException {
      lazyLoadMap();
      addAllUsers();
      readTweetsFileToMap();
   }

   private void addAllUsers() {
      for (Map.Entry<String, HashSet<String>> entry : followedByMapSet.entrySet()) {
         tweetsByUser.put(entry.getKey(), new ArrayList<UserTweetPair>());
      }
   }

   /*
    * Reads tweets from a file, line by line, to a TreeMap
    */
   private void readTweetsFileToMap() throws IOException, ValidationException {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tweetFile)));

      String tweet = br.readLine();
      while (tweet != null) {
         parseAndAddTweetLine(tweet);
         tweet = br.readLine();
      }

      br.close();
   }

   /*
    * Parses a single line, assumes the format is "userName> this is a tweet"
    */
   private void parseAndAddTweetLine(String line) throws ValidationException {
      // If the line is empty, ignore it and continue
      if (line.trim().isEmpty()) {
         return;
      }
      String[] splitLine = StringUtils.splitByWholeSeparator(line, "> ");
      if (splitLine.length < 2 || splitLine[1].trim().isEmpty()) {
         throw new IllegalArgumentException(
               "Expected file format is \"userName> this is a tweet...\", but found this line: \"" + splitLine[0]
                     + "\"");
      }

      String userName = splitLine[0].trim();

      if (!StringUtils.isAlphanumeric(userName)) {
         throw new IllegalArgumentException(
               "Username is expected to be letters and numbers only, name was: " + userName);
      }

      // Get the tweet, and substring it to the minimum length between its own length and the maximum allowable length
      // (140).
      String tweet = splitLine[1].substring(0, Math.min(Configuration.MAX_TWEET_LENGTH, splitLine[1].length()));
      UserTweetPair utp = new UserTweetPair(userName, tweet);

      HashSet<String> usersWhoFollow = followedByMapSet.get(userName);

      for (String userWhoFollows : usersWhoFollow) {
         if (tweetsByUser.containsKey(userWhoFollows)) {
            tweetsByUser.get(userWhoFollows).add(utp);
         } else {
            List<UserTweetPair> newArrayList = new ArrayList<>();
            newArrayList.add(utp);
            tweetsByUser.put(userWhoFollows, newArrayList);
         }
      }
   }
}

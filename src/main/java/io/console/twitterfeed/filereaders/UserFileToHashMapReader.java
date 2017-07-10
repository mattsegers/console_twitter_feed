package io.console.twitterfeed.filereaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.ValidationException;

import org.apache.commons.lang3.StringUtils;

public class UserFileToHashMapReader {

   private File userFile;
   // A Map of Username --> Users who follow this user
   private HashMap<String, HashSet<String>> followedByMapSet = null;

   public HashMap<String, HashSet<String>> getFollowedByMapSet() {
      return followedByMapSet;
   }

   private void lazyLoadMap() {
      followedByMapSet = new HashMap<>();
   }

   public UserFileToHashMapReader withUserFile(File file) {
      this.userFile = file;
      return this;
   }

   /*
   @formatter:off
   Does processing:
    - Loads map if necessary
    - Reads users into map from user file
    @formatter:on
    */
   public void doProcessing() throws IOException, ValidationException {
      lazyLoadMap();
      readUserFileToMap();
   }

   private void readUserFileToMap() throws IOException, ValidationException {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(userFile)));

      String line = br.readLine();
      while (line != null) {
         parseAndAddUserLine(line);
         line = br.readLine();
      }

      br.close();
   }

   /*
    * Parses a single user file line. Format is assumed to be "John follows Doe" where there may or may not be a space
    * after "follows"
    */
   private void parseAndAddUserLine(String line) throws ValidationException {
      // Ignore empty lines
      if (line.trim().isEmpty()) {
         return;
      }

      String[] splitLine = StringUtils.splitByWholeSeparator(line, "follows");
      if (splitLine.length < 2 || splitLine[1].trim().isEmpty()) {
         throw new IllegalArgumentException(
               "Expected file format is <userName> follows <user>,<user>,... but could not find followers for user: "
                     + splitLine[0]);
      }

      String userWhoFollows = splitLine[0].trim();

      if (!StringUtils.isAlphanumeric(userWhoFollows)) {
         throw new IllegalArgumentException(
               "Follower Username is expected to be letters and numbers only, name was: " + userWhoFollows);
      }

      List<String> followedUsers = parseFollowsLine(splitLine[1].trim());
      // User follows him/herself
      followedUsers.add(userWhoFollows);

      for (String followedUser : followedUsers) {
         addUserIfNew(followedUser);
         followedByMapSet.get(followedUser).add(userWhoFollows);
      }
   }

   /*
    * Parses the follows line, i.e. the text between "> and <" in the example: "John follows >this,is,the,follows,line<
    */
   private ArrayList<String> parseFollowsLine(String followsLine) throws ValidationException {
      ArrayList<String> follows = new ArrayList<String>();

      String[] followsUsers = StringUtils.split(followsLine, ",");

      for (String user : followsUsers) {
         user = user.trim();
         if (!StringUtils.isAlphanumeric(user)) {
            throw new IllegalArgumentException(
                  "Follower Username is expected to be letters and numbers only, name was: " + user);
         }
         follows.add(user);
      }

      return follows;
   }

   /*
    * Adds a read user, if such user has not appeared as a key in the map: user (key) --> users who follow this user.
    */
   private void addUserIfNew(String user) {
      if (!followedByMapSet.containsKey(user)) {
         HashSet<String> hashSet = new HashSet<>();
         hashSet.add(user); // Make sure the new user in the map follows self
         followedByMapSet.put(user, hashSet);
      }
   }
}

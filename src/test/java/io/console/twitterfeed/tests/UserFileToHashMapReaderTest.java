package io.console.twitterfeed.tests;

import io.console.twitterfeed.filereaders.TweetFileToTreeMapReader;
import io.console.twitterfeed.filereaders.UserFileToHashMapReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.bind.ValidationException;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UserFileToHashMapReaderTest {

   File uTestFile;
   File tTestFile;

   @BeforeMethod
   public void beforeMethod() {
      File ufile = new File("testUserFile.txt");
      File tfile = new File("testTweetFile.txt");

      if (ufile.exists()) {
         ufile.delete();
      }

      if (tfile.exists()) {
         tfile.delete();
      }

      uTestFile = ufile;
      tTestFile = tfile;
   }

   @AfterMethod
   public void afterMethod() {
      uTestFile.delete();
      tTestFile.delete();
   }

   @Test(dataProvider = "userFileProvider")
   public void basicUserFileReadTests(String fileText, String expected) throws IOException, ValidationException {
      FileWriter fileWriter = new FileWriter(uTestFile);

      fileWriter.write(fileText);
      fileWriter.flush();
      fileWriter.close();

      UserFileToHashMapReader userFileToHashMapReader = new UserFileToHashMapReader().withUserFile(uTestFile);
      userFileToHashMapReader.doProcessing();

      HashMap<String, HashSet<String>> followedByMap = userFileToHashMapReader.getFollowedByMapSet();
      HashMap<String, HashSet<String>> expectedMap = parseExpectedMap(expected);

      Assert.assertEquals(followedByMap, expectedMap);
   }

   @Test(dataProvider = "illegalInput", expectedExceptions = IllegalArgumentException.class)
   public void illegalUsersTests(String fileText, String expected) throws IOException, ValidationException {
      FileWriter fileWriter = new FileWriter(uTestFile);

      fileWriter.write(fileText);
      fileWriter.flush();
      fileWriter.close();

      UserFileToHashMapReader userFileToHashMapReader = new UserFileToHashMapReader().withUserFile(uTestFile);
      userFileToHashMapReader.doProcessing();
   }

   @Test(dataProvider = "notEqualsTests")
   public void notEqualsTests(String fileText, String expected) throws IOException, ValidationException {
      FileWriter fileWriter = new FileWriter(uTestFile);

      fileWriter.write(fileText);
      fileWriter.flush();
      fileWriter.close();

      UserFileToHashMapReader userFileToHashMapReader = new UserFileToHashMapReader().withUserFile(uTestFile);
      userFileToHashMapReader.doProcessing();

      HashMap<String, HashSet<String>> followedByMap = userFileToHashMapReader.getFollowedByMapSet();
      HashMap<String, HashSet<String>> expectedMap = parseExpectedMap(expected);

      Assert.assertNotEquals(followedByMap, expectedMap);
   }

   @Test(dataProvider = "tweetsProvider")
   public void basicTweetsReadTests(String userFileText, String tweetsFileText, String expectedOutput)
         throws IOException, ValidationException {
      FileWriter uFileWriter = new FileWriter(uTestFile);
      FileWriter tFileWriter = new FileWriter(tTestFile);

      uFileWriter.write(userFileText);
      uFileWriter.flush();
      uFileWriter.close();

      tFileWriter.write(tweetsFileText);
      tFileWriter.flush();
      tFileWriter.close();

      UserFileToHashMapReader userFileToHashMapReader = new UserFileToHashMapReader().withUserFile(uTestFile);
      userFileToHashMapReader.doProcessing();

      TweetFileToTreeMapReader tweetFileToTreeMapReader =
            new TweetFileToTreeMapReader(userFileToHashMapReader.getFollowedByMapSet()).withTweetsFile(tTestFile);
      tweetFileToTreeMapReader.doProcessing();

      Assert.assertEquals(tweetFileToTreeMapReader.getPrintableOutput(), expectedOutput);
   }

   @Test(dataProvider = "badTweetsProvider", expectedExceptions = IllegalArgumentException.class)
   public void badTweetsTest(String userFileText, String tweetsFileText, String expectedOutput)
         throws IOException, ValidationException {
      FileWriter uFileWriter = new FileWriter(uTestFile);
      FileWriter tFileWriter = new FileWriter(tTestFile);

      uFileWriter.write(userFileText);
      uFileWriter.flush();
      uFileWriter.close();

      tFileWriter.write(tweetsFileText);
      tFileWriter.flush();
      tFileWriter.close();

      UserFileToHashMapReader userFileToHashMapReader = new UserFileToHashMapReader().withUserFile(uTestFile);
      userFileToHashMapReader.doProcessing();

      TweetFileToTreeMapReader tweetFileToTreeMapReader =
            new TweetFileToTreeMapReader(userFileToHashMapReader.getFollowedByMapSet()).withTweetsFile(tTestFile);
      tweetFileToTreeMapReader.doProcessing();
   }

   @DataProvider(name = "userFileProvider")
   public Object[][] userFileProvider() {
      return new Object[][] {
            { "Ward follows Alan\n" + "Alan follows Martin\n" + "Ward follows Martin, Alan",
                  "Alan:Ward,Alan;Martin:Martin,Alan,Ward;Ward:Ward" },
            { "Ward follows Alan", "Alan:Ward,Alan;Ward:Ward" },
            { "Ward follows Alan,Amy,John,Timothy,Drew,Nkosi,Tabo,Tharulela,Rabbi",
                  "Alan:Ward,Alan;Ward:Ward;Amy:Amy,Ward;John:John,Ward;Timothy:Ward,Timothy;Drew:Drew,Ward;Nkosi:Nkosi,Ward;Tabo:Tabo,Ward;Tharulela:Tharulela,Ward;Rabbi:Rabbi,Ward" },
            { "Ward follows Alan\n" + "Alan follows Martin\n" + "Ward follows Martin, Alan\n"
                  + "Ward follows Martin, Alan\n" + "Ward follows Martin, Alan\n" + "Ward follows Martin, Alan\n"
                  + "Ward follows Martin, Alan\n" + "Ward follows Martin, Alan\n" + "Ward follows Martin, Alan\n"
                  + "Ward follows Martin, Alan\n" + "Ward follows Martin, Alan\n" + "Ward follows Martin, Alan\n"
                  + "Ward follows Martin, Alan\n" + "Ward follows Martin, Alan\n" + "Ward follows Martin, Alan\n",
                  "Alan:Ward,Alan;Martin:Martin,Alan,Ward;Ward:Ward" },
            { "1 follows 2", "2:1,2;1:1" }, { "1 follows 2\n\n\n", "2:1,2;1:1" },
            { "1 follows 2\n    \n     \n", "2:1,2;1:1" },
            { "AVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongName follows a",
                  "a:a,AVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongName;AVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongName:AVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongName" } };
   }

   @DataProvider(name = "illegalInput")
   public Object[][] illegalInput() {
      return new Object[][] { { "@ follows $", "not actually expected" }, { "< follows >", "not actually expected" },
            { "a followsb:c", "not actually expected" } };
   }

   @DataProvider(name = "notEqualsTests")
   public Object[][] notEqualsTests() {
      return new Object[][] { { "1 follows 2", "2:1,2;3:1" } };
   }

   @DataProvider(name = "tweetsProvider")
   public Object[][] tweetsProvider() {
      return new Object[][] { { "Ward follows Alan\n" + "Alan follows Martin\n" + "Ward follows Martin, Alan",
            "Alan> If you have a procedure with 10 parameters, you probably missed some.\n"
                  + "Ward> There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                  + "Alan> Random numbers should not be generated with a method chosen at random.",
            "Alan:\n" + "\t@Alan: If you have a procedure with 10 parameters, you probably missed some.\n"
                  + "\t@Alan: Random numbers should not be generated with a method chosen at random.\n" + "Martin:\n"
                  + "Ward:\n" + "\t@Alan: If you have a procedure with 10 parameters, you probably missed some.\n"
                  + "\t@Ward: There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                  + "\t@Alan: Random numbers should not be generated with a method chosen at random." },
            { "Ward follows Alan\n" + "Alan follows Martin\n" + "Ward follows Martin, Alan",
                  "Alan> If you have a procedure with 10 parameters, you probably missed some.\n\n\n\n\n\n\n"
                        + "Ward> There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                        + "Alan> Random numbers should not be generated with a method chosen at random.\n",
                  "Alan:\n" + "\t@Alan: If you have a procedure with 10 parameters, you probably missed some.\n"
                        + "\t@Alan: Random numbers should not be generated with a method chosen at random.\n"
                        + "Martin:\n" + "Ward:\n"
                        + "\t@Alan: If you have a procedure with 10 parameters, you probably missed some.\n"
                        + "\t@Ward: There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                        + "\t@Alan: Random numbers should not be generated with a method chosen at random." },
            { "Ward follows Alan\n" + "Alan follows Martin\n" + "Ward follows Martin, Alan",
                  "Alan> If you have a procedure with ###### parameters, you probably missed some.\n\n\n\n\n\n\n"
                        + "Ward> There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                        + "Alan> 4l@n has decided that he will tweet a tweet that is very, very long indeed. And this tweet, too long to be displayed and, really, just simply too long, will probably go unnoticed.\n",
                  "Alan:\n" + "\t@Alan: If you have a procedure with ###### parameters, you probably missed some.\n"
                        + "\t@Alan: 4l@n has decided that he will tweet a tweet that is very, very long indeed. And this tweet, too long to be displayed and, really, just simpl\n"
                        + "Martin:\n" + "Ward:\n"
                        + "\t@Alan: If you have a procedure with ###### parameters, you probably missed some.\n"
                        + "\t@Ward: There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                        + "\t@Alan: 4l@n has decided that he will tweet a tweet that is very, very long indeed. And this tweet, too long to be displayed and, really, just simpl" } };
   }

   @DataProvider(name = "badTweetsProvider")
   public Object[][] badTweetsProvider() {
      return new Object[][] { { "Ward follows Alan\n" + "Alan follows Martin\n" + "Ward follows Martin, Alan",
            "Alan> If you have a procedure with 10 parameters, you probably missed some.\n"
                  + "Ward> There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                  + "Alan>",
            "Alan:\n" + "\t@Alan: If you have a procedure with 10 parameters, you probably missed some.\n"
                  + "\t@Alan: Random numbers should not be generated with a method chosen at random.\n" + "Martin:\n"
                  + "Ward:\n" + "\t@Alan: If you have a procedure with 10 parameters, you probably missed some.\n"
                  + "\t@Ward: There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                  + "\t@Alan: Random numbers should not be generated with a method chosen at random." },
            { "Ward follows Alan\n" + "Alan follows Martin\n" + "Ward follows Martin, Alan",
                  "Alan> If you have a procedure with 10 \nparameters, you probably missed some.\n\n\n\n\n\n\n"
                        + "Ward> There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                        + "Alan> Random numbers should not be generated with a method chosen at random.\n",
                  "Alan:\n" + "\t@Alan: If you have a procedure with 10 parameters, you probably missed some.\n"
                        + "\t@Alan: Random numbers should not be generated with a method chosen at random.\n"
                        + "Martin:\n" + "Ward:\n"
                        + "\t@Alan: If you have a procedure with 10 parameters, you probably missed some.\n"
                        + "\t@Ward: There are only two hard things in Computer Science: cache invalidation, naming things and off-by-1 errors.\n"
                        + "\t@Alan: Random numbers should not be generated with a method chosen at random." } };
   }

   private HashMap<String, HashSet<String>> parseExpectedMap(String expected) {
      HashMap<String, HashSet<String>> map = new HashMap<>();

      String[] followedUserSplit = expected.split(";");

      for (String split : followedUserSplit) {
         String[] innerSplit = split.split(":");
         String followedUser = innerSplit[0];
         map.put(followedUser, new HashSet<String>());
         String[] followers = innerSplit[1].split(",");
         for (String follower : followers) {
            map.get(followedUser).add(follower);
         }
      }

      return map;
   }
}

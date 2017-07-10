package io.console.twitterfeed;

import io.console.twitterfeed.filereaders.TweetFileToTreeMapReader;
import io.console.twitterfeed.filereaders.UserFileToHashMapReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.bind.ValidationException;

public class TwitterFeed {

   public static void main(String[] args) throws IOException, ValidationException {
      try {
         String userFileStr = args[0];
         String tweetsFileStr = args[1];

         File userFile = new File(userFileStr);
         File tweetsFile = new File(tweetsFileStr);

         UserFileToHashMapReader userReader = new UserFileToHashMapReader().withUserFile(userFile);
         userReader.doProcessing();

         TweetFileToTreeMapReader tweetFileToDisplayableMapReader =
               new TweetFileToTreeMapReader(userReader.getFollowedByMapSet()).withTweetsFile(tweetsFile);
         tweetFileToDisplayableMapReader.doProcessing();

         String output = tweetFileToDisplayableMapReader.getPrintableOutput();
         System.out.println(output);

      } catch (Throwable t) {
         // Log throwable to logger, preferably using something like slf4j, but for now just console and a basic text
         // file.
         System.out.println("Error, see exception log for more:\n" + t.toString());

         // Append to keep history
         FileWriter fw = new FileWriter("exception.txt", true);
         PrintWriter pw = new PrintWriter(fw);
         t.printStackTrace(pw);
         fw.close();
         pw.close();
      }
   }
}

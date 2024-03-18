package util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AccountFileUtil {

    public static List<Integer> fetchAccountIdsFromFilenames(String directoryPath) {
        File directory = new File(directoryPath);
        FilenameFilter jsonFilter = (dir, name) -> name.endsWith(".json");
        File[] files = directory.listFiles(jsonFilter);

        List<Integer> accountIds = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String filename = file.getName();
                String accountIdStr = filename.substring(0, filename.indexOf('.'));
                try {
                    int accountId = Integer.parseInt(accountIdStr);
                    accountIds.add(accountId);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping file with non-integer prefix: " + filename);
                }
            }
        } else {
            throw new RuntimeException("Failed to list files in directory: " + directoryPath);
        }
        return accountIds;
    }

    public static String readJsonFromFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}


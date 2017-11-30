package by.tests.directoryScanner;

import by.tests.directoryScanner.enums.OptionsEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ScanThread implements Runnable {

    final private Map<String, String> options;
    private Logger logger;
    ResourceBundle messages;

    public ScanThread(Map<String, String> options) {
        this.options = options;
         messages = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    @Override
    public void run() {

        logger = getLogger();

        if (options.containsKey(OptionsEnum.waitInterval.name())) {
            while (!Thread.currentThread().isInterrupted()) {
                main();
                    long time = Long.valueOf(options.get(OptionsEnum.waitInterval.name()));
                    try {
                        Thread.currentThread().sleep(time);
                    } catch (InterruptedException e) {
                        break;
                    }
            }
        } else {
            main();
        }
    }

    /**
     * create logger and log file for current thread
     * @return object Logger log4j2
     */
    private Logger getLogger(){
        ThreadContext.put("scan", Thread.currentThread().getName());
        return LogManager.getLogger(Thread.currentThread().getName());
    }

    /**
     * basic methods
     */
    private void main() {
        logger.info(messages.getString("messages.log.scan.start.starting")
                + options.get(OptionsEnum.inputDir.name())
                + messages.getString("messages.log.scan.start.middle")
                + options.get(OptionsEnum.outputDir.name()));

        Path inputDir = Paths.get(options.get(OptionsEnum.inputDir.name()));

        // verification of the existence of such a path
        if (!Files.exists(inputDir)) {
            logger.warn(messages.getString("messages.log.scan.error.inputdir.exist.not"));
            return;
        }

        // if inputDir is a file
        if (!Files.isDirectory(inputDir)) {
            try {
                Files.copy(Paths.get(options.get(OptionsEnum.inputDir.name())), Paths.get(OptionsEnum.outputDir.name()), StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (IOException e) {
                logger.warn(messages.getString("messages.log.scan.error.file.copy") + e.getLocalizedMessage());
            }
        }

        // if the directory is specified
        List<Path> listOfContainedFiles;
        //creating a list of files wich dependens on the option includeSubfolders
        if (options.containsKey(OptionsEnum.includeSubfolders.name())
                && Boolean.valueOf(options.get(OptionsEnum.includeSubfolders.name()))) {
            listOfContainedFiles = getListOfContainedFilesRecurs(Paths.get(options.get(OptionsEnum.inputDir.name())))
                    .collect(Collectors.toList());
        } else {
            listOfContainedFiles = getListOfContainedFiles(Paths.get(options.get(OptionsEnum.inputDir.name())));
        }

        // filtering files by mask
        if (options.containsKey(OptionsEnum.mask.name())) {
            listOfContainedFiles = filtersByMask(listOfContainedFiles, options.get(OptionsEnum.mask.name()));
        }
        // copy files
        copyFiles(listOfContainedFiles, options.get(OptionsEnum.inputDir.name()), options.get(OptionsEnum.outputDir.name()));
        // delete files
        if (options.containsKey(OptionsEnum.autoDelete.name())
                && Boolean.valueOf(options.get(OptionsEnum.autoDelete.name()))) {
//            autoDelete(options.get(OptionsEnum.inputDir.name()));
            autoDelete(listOfContainedFiles);
        }
        logger.info(messages.getString("messages.log.scan.end"));
    }

    /**
     * method to get a list of files in the directory
     *
     * @param path - absolute path for directory
     * @return stream with file names
     */
    private List<Path> getListOfContainedFiles(Path path) {
        try {
            return Files.list(path).collect(Collectors.toList());
        } catch (IOException e) {
            logger.warn(messages.getString("messages.log.scan.error.get.content")
                        + e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    /**
     * method of creating a recursive list of files (including directories)
     *
     * @param path
     * @return stream with item (or items)
     */
    private Stream<Path> getListOfContainedFilesRecurs(Path path) {
        if (Files.isDirectory(path)) {
            try {
                return Stream.concat(Files.list(path).flatMap(this::getListOfContainedFilesRecurs), Stream.of(path));
            } catch (IOException e) {
                logger.warn(messages.getString("messages.log.scan.error.get.content")
                        + e.getLocalizedMessage());
                return Stream.of(path);
            }
        } else {
            return Stream.of(path);
        }
    }

    /**
     * the method copies the selected files from the list of one directory to another
     *
     * @param files    - the selected files list
     * @param inputDir - absolute path for input directory
     * @param outDir   - absolute path for output directory
     */

    private void copyFiles(List<Path> files, String inputDir, String outDir) {
        // If the list is created using recursion then need to delete input dir
        files.remove(Paths.get(inputDir));
        // create folders structure
        files.stream()
                .filter(file -> Files.isDirectory(file))
                .forEach(filePath -> {
                    String relativePath = Paths.get(inputDir).relativize(filePath).toString();
                    String foldersTree = outDir + File.separator + relativePath;
                    try {
                        Files.createDirectories(Paths.get(foldersTree));
                    } catch (IOException e) {
                        logger.warn(messages.getString("messages.log.scan.error.file.write") + e.getLocalizedMessage());
                    }
                });
        // copy files
        files.stream()
                .filter(file -> !Files.isDirectory(file))
                .forEach(filePath -> {
                    String relativePath = Paths.get(inputDir).relativize(filePath).toString();
                    Path destination = Paths.get(outDir + File.separator + relativePath);
                    try {
                        Files.copy(filePath, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.warn(messages.getString("messages.log.scan.error.file.write") + e.getLocalizedMessage());
                    }
                });
    }

    /**
     * filter file names list by mask
     *
     * @param list
     * @param pattern
     * @return if badly composed mask return empty list
     */
    private List<Path> filtersByMask(List<Path> list, String pattern) {
        try {
            return list.stream()
                    .filter(s -> s.getFileName()
                            .toString()
                            .matches(pattern))
                    .collect(Collectors.toList());
        } catch (PatternSyntaxException e) {
            logger.warn(messages.getString("messages.log.scan.error.badly.mask")
                    + e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    /**
     * delete only copied files
     *
     * @param list - files to delete
     */
    private void autoDelete(List<Path> list) {
        list.stream()
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * method delete all files from input dir
     *
     * @param inDir
     */
    private void autoDelete(String inDir) {
        Path rootPath = Paths.get(inDir);
        try {
            Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            Files.delete(rootPath);
        } catch (IOException e) {
            logger.warn(messages.getString("messages.log.scan.error.file.delete")
                        + e.getLocalizedMessage());
        }
    }


}


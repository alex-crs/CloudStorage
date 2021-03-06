package Main_Functional;

import entitys.WorkPanel;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


import static Main_Functional.MainWindowController.*;
import static Registration.RegistrationWindowController.DELIMETER;

public class FileOperations {

    public static void multipleElementDownload(String sourcePath, String targetPath, String element, WorkPanel sourcePanel) throws IOException {
        if (element.contains("f:")) {
            download(sourcePath, targetPath, element, sourcePanel);
        }
        if (element.contains("d:")) {
            String elementWithoutHead = element.replaceAll(".:", "");
            Path newTargetPath = Path.of(targetPath + elementWithoutHead);
            if (!newTargetPath.toFile().exists()) {
                Files.createDirectory(newTargetPath);
            } else {
                delete(new StringBuilder().append(targetPath), elementWithoutHead);
                Files.createDirectory(newTargetPath);
            }
            String newSourcePath = sourcePath + File.separator + elementWithoutHead;
            for (String files : sourcePanel.getNetworkManager().receiveFileList(newSourcePath)) {
                if (!files.isEmpty()) {
                    multipleElementDownload((newSourcePath), newTargetPath + File.separator, files, sourcePanel);
                }
            }
        }
    }

    public static void multipleElementUpload(WorkPanel sourcePanel, WorkPanel targetPanel, String element) throws IOException {
        Path source = Path.of(sourcePanel.getCurrentPath() + File.separator + element);
        Path target = Path.of(targetPanel.getCurrentPath() + File.separator + element);
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = target.resolve(source.relativize(dir));
                    sourcePanel.getNetworkManager().makeDir(targetPath.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    upload(file.toString(), target.resolve(source.relativize(file)).toString(), sourcePanel);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    public static void upload(String source, String target, WorkPanel sourcePanel) {
        try {
            File sourceObject = new File(source);
            File targetObject = new File(target);
            if (sourceObject.isFile()) {
                out.write(("/upload" + DELIMETER + "f" + DELIMETER + targetObject + DELIMETER
                        + sourceObject.length()).getBytes());
                while (true) {
                    String[] serverAnswer = sourcePanel.getNetworkManager().queryStringListener();
                    if ("/status-ok".equals(serverAnswer[0])) {
                        break;
                    }
                }
            } else {
                out.write(("/upload" + DELIMETER + "d" + DELIMETER + targetObject + DELIMETER
                        + sourceObject.length()).getBytes());
            }
            if (sourceObject.isFile() && sourceObject.length() != 0) {
                RandomAccessFile randomAccessFile = new RandomAccessFile(sourceObject, "rw");
                int read = 0;
                byte[] buffer = new byte[8 * 1024];
                while ((read = randomAccessFile.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                byteBuffer.clear();
                randomAccessFile.close();
                out.flush();
                while (true) {
                    String[] serverAnswer = sourcePanel.getNetworkManager().queryStringListener();
                    if ("/status-ok".equals(serverAnswer[0])) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void download(String source, String target, String element, WorkPanel sourcePanel) {
        String filename = element.replaceAll(".:", "");
        File file = new File(target + filename);
        long downloadFileLength = 0;
        try {
            out.write(("/download" + DELIMETER + (source + File.separator + filename)).getBytes());
            String[] serverAnswer = sourcePanel.getNetworkManager().queryStringListener();

                Files.deleteIfExists(Path.of(target + filename));
                Files.createFile(Path.of(target + filename));

            while (true) {
                if ("/download-ok".equals(serverAnswer[0])) {
                    downloadFileLength = Long.parseLong(serverAnswer[1]);
                    break;
                }
            }
            RandomAccessFile randomAccessFile = null;
            FileChannel fileChannel = null;
                out.write(" ".getBytes()); //???????????????????? ?????? ?????????????? ???????????????? ??????????????, ???????????? ??????????, ???????????? ???????? ??????????????
                out.flush();
            if (downloadFileLength > 0) {
                randomAccessFile = new RandomAccessFile(file, "rw");
                fileChannel = randomAccessFile.getChannel();
                while ((rbc.read(byteBuffer)) > 0) {
                    byteBuffer.flip();
                    fileChannel.position(file.length());
                    fileChannel.write(byteBuffer);
                    byteBuffer.compact();
                    if (file.length() == downloadFileLength) {
                        break;
                    }
                }
                fileChannel.close();
                randomAccessFile.close();
            }
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*?????????? ?????? ?????????????????????? ???? ?????????????????? ????????. ???????????????????? ???????????????? ?????????????? ???????????????????? ?????? ???????????????????? ???????????? ????????????
    ?????????? ??????????????????????.*/
    public static void copy(Path source, Path target) throws IOException {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = target.resolve(source.relativize(dir));
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    public static void delete(StringBuilder path, String fileName) throws IOException {
        Files.walkFileTree(Path.of(path + File.separator + fileName), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        updateAllFilesLists();
    }

    //?????????????? ?????????????? ?? ?????????? ???????? (?????????????? ?????? ???????????????? ?????????? ?? ?????????????????? ???? ??????????, ???? ?????????????????? ????????????)
    public static String clearEmptySymbolsAfterName(String name) {
        StringBuilder string = new StringBuilder().append(name);
        while (true) {
            if (!string.toString().endsWith(" ")) {
                break;
            }
            string.deleteCharAt(string.length() - 1);
        }
        return string.toString();
    }


}

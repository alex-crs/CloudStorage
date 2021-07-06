package server_app;

import java.io.File;

public class FileInfo {
    private final File file;
    private final long targetFileLength;
    private final Action Action;

    public File getFile() {
        return file;
    }

    public long getTargetFileLength() {
        return targetFileLength;
    }

    public Action isAction() {
        return Action;
    }

    public FileInfo(File file, long fileLength, Action action) {
        this.file = file;
        this.targetFileLength = fileLength;
        Action = action;
    }
}

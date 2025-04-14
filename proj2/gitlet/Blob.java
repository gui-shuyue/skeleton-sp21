package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String fileName;
    private byte[] content;
    private String id;

    public Blob(String fileName,  File CWD) {
        this.fileName = fileName;
        File file = join(CWD, fileName);
        this.content = readContents(file);
        this.id = sha1(fileName, content);
    }

    public String getFileName() {
        return fileName;
    }

    public String getId() {
        return id;
    }

    public byte[] getContent() {
        return content;
    }
}

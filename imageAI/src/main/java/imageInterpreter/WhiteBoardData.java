package imageInterpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class WhiteBoardData {
    private String content;
    Path imgFile;

    public WhiteBoardData(String img) throws IOException {
        System.out.println("Reading the image, converting it to Base64");
        this.imgFile = Paths.get(img);
        byte[] pngBytes = Files.readAllBytes(imgFile);
        this.content = Base64.getEncoder().encodeToString(pngBytes);
    }

    public String getContent() throws IOException {
       return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

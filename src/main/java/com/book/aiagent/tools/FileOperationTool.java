package com.book.aiagent.tools;


import cn.hutool.core.io.FileUtil;
import com.book.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "A tool for reading the content of a file. The file must exist and be located in the specified directory.")
    public String readFile(@ToolParam(
            description = "The name of the file to read."
    ) String filename) {
        String filePath = FILE_DIR + "/" + filename;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }

    }

    @Tool(description = "A tool for writing content to a file. If the file does not exist, it will be created. If it already exists, the content will be overwritten.")
    public String writeFile(
            @ToolParam(description = "Name of the file to write") String filename,
            @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + filename;
        try {
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully: " + filePath;
        } catch (Exception e) {
            return "Error writing file: " + e.getMessage();
        }
    }
}

package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 18:32
 **/

@Data
public class ChunkEntity {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private int chunkNumber;
    private int totalChunks;
    private int chunkSize;
    private int totalSize;
    private String identifier;
    private String filename;
    private String relativePath;
}

package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 18:32
 **/

@Data
public class ChunkEntity implements Serializable {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private int chunkNumber;
    private int totalChunks;
    private long chunkSize;
    private long totalSize;
    private String identifier;
    private String filename;
    private String relativePath;


    @Override
    public String toString() {
        return "ChunkEntity{" +
                "createTime=" + createTime +
                ", chunkNumber=" + chunkNumber +
                ", totalChunks=" + totalChunks +
                ", chunkSize=" + chunkSize +
                ", totalSize=" + totalSize +
                ", identifier='" + identifier + '\'' +
                ", filename='" + filename + '\'' +
                ", relativePath='" + relativePath + '\'' +
                '}';
    }
}

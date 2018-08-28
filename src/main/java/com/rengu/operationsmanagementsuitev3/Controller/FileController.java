package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ChunkEntity;
import com.rengu.operationsmanagementsuitev3.Service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 16:50
 **/

@Slf4j
@RestController
@RequestMapping(value = "/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping(value = "/chunks")
    public void get(HttpServletResponse httpServletResponse, ChunkEntity chunkEntity) {
    }

    @PostMapping(value = "/chunks")
    public void post(ChunkEntity chunkEntity, @RequestParam(value = "file") MultipartFile chunk) {
    }
}

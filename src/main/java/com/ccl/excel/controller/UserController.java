package com.ccl.excel.controller;

import com.ccl.excel.annotion.ExcelImport;
import com.ccl.excel.strategy.UserImportStrategy;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequestMapping("/user")
@Tag(name = "用户接口", description = "提供用户的增删改查接口")
public class UserController {


    @PostMapping("/import")
    @ResponseBody
    @ExcelImport(batchSize = 200, timeoutSeconds = 1, strategy = UserImportStrategy.class)
    public String importUser(@RequestParam("file") MultipartFile file) {
        log.info("Service层: importUsers 方法被调用，文件名为: " + file.getOriginalFilename());
        return "导入请求已接收，正在处理中...";
    }

}

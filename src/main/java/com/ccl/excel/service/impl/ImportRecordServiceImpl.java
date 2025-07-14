package com.ccl.excel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccl.excel.annotion.ExcelImport;
import com.ccl.excel.mapper.ImportRecordMapper;
import com.ccl.excel.mapper.UserMapper;
import com.ccl.excel.pojo.ImportRecord;
import com.ccl.excel.pojo.Product;
import com.ccl.excel.pojo.User;
import com.ccl.excel.strategy.ProductImportStrategy;
import com.ccl.excel.strategy.UserImportStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 业务服务类，包含使用@ExcelImport注解的示例方法。
 * 这些方法将被AOP切面拦截。
 */
@Slf4j
@Service
public class ImportRecordServiceImpl extends ServiceImpl<ImportRecordMapper, ImportRecord> {

    /**
     * 导入用户数据的方法。
     * 标记了@ExcelImport注解，表示此方法将触发自动Excel导入流程。
     *
     * @param file 待导入的Excel文件
     * @return 导入结果信息
     */
    @ExcelImport(batchSize = 200, timeoutSeconds = 10,
            strategy = UserImportStrategy.class, targetClass = User.class)
    public String importUsers(MultipartFile file) {
        log.info("Service层: importUsers 方法被调用，文件名为: " + file.getOriginalFilename());
        return "导入请求已接收，正在处理中...";
    }

    /**
     * 导入产品数据的方法。
     *
     * @param file 待导入的Excel文件
     * @return 导入结果信息
     */
    @ExcelImport(batchSize = 500, timeoutSeconds = 15,
            strategy = ProductImportStrategy.class, targetClass = Product.class)
    public String importProducts(MultipartFile file) {
        log.info("Service层: importProducts 方法被调用，文件名为: " + file.getOriginalFilename());
        return "导入请求已接收，正在处理中...";
    }
}
package com.ccl.excel.strategy;

import com.ccl.excel.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 用户数据导入策略实现。
 * 模拟用户数据的验证和导入逻辑。
 */
@Slf4j
@Component
public class UserImportStrategy implements BatchImportStrategy<User> {

    @Override
    public User convertRow(Map<String, String> rowData) {
        User user = new User();
        String name = rowData.get("姓名");
        String id = rowData.get("ID");
        String ageStr = rowData.get("年龄");
        String email = rowData.get("邮箱");

        StringBuilder errorBuilder = new StringBuilder();

        // 模拟数据转换和初步验证
        if (id == null || id.trim().isEmpty()) {
            errorBuilder.append("ID不能为空; ");
        }
        if (name == null || name.trim().isEmpty()) {
            errorBuilder.append("姓名不能为空; ");
        }
        Integer age = null;
        if (ageStr != null && !ageStr.trim().isEmpty()) {
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                errorBuilder.append("年龄格式不正确; ");
            }
        } else {
            errorBuilder.append("年龄不能为空; ");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            errorBuilder.append("邮箱格式不正确或为空; ");
        }

        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);

        if (errorBuilder.length() > 0) {
            user.setImportError(errorBuilder.toString().trim());
        }
        return user;
    }

    @Override
    public List<Map<String, String>> importBatch(List<User> users) {
        List<Map<String, String>> failedUsers = new ArrayList<>();
        log.info("--- UserImportStrategy: 开始导入批次用户数据 (大小: " + users.size() + ") ---");
        for (User user : users) {
            if (user.getImportError() != null && !user.getImportError().isEmpty()) {
                // 如果在转换阶段已经有错误，直接标记为失败
                failedUsers.add(t2Map(user));
                log.error("UserImportStrategy: 导入失败 (转换错误): " + user);
                continue;
            }

            // 模拟业务逻辑验证和持久化
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                user.setImportError("姓名不能为空");
                failedUsers.add(t2Map(user));
                log.error("UserImportStrategy: 导入失败 (业务验证): " + user);
            } else if (user.getAge() == null || user.getAge() < 0) {
                user.setImportError("年龄必须为正数");
                failedUsers.add(t2Map(user));
                log.error("UserImportStrategy: 导入失败 (业务验证): " + user);
            } else {
                // 模拟成功保存到数据库
                log.info("UserImportStrategy: 成功导入用户: " + user.getName());
            }
        }
        log.info("--- UserImportStrategy: 批次用户数据导入完成，失败数: " + failedUsers.size() + " ---");
        return failedUsers;
    }

    @Override
    public List<String> getErrorHeaders() {
        return Arrays.asList("ID", "姓名", "年龄", "邮箱", "失败原因");
    }

    @Override
    public Map<String, String> t2Map(User item) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ID", item.getId());
        map.put("姓名", item.getName());
        map.put("年龄", item.getAge() != null ? item.getAge().toString() : "");
        map.put("邮箱", item.getEmail());
        map.put("失败原因", item.getImportError() != null ? item.getImportError() : "");
        return map;
    }
}
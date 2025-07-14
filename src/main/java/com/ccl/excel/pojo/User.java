package com.ccl.excel.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户数据模型，用于Excel导入。
 * 包含一个用于存储导入失败原因的字段。
 */
@TableName("t_user")
public class User {
    @TableId
    private String id;
    private String name;
    private Integer age;
    private String email;
    private String importError; // 导入失败原因

    // Constructors, Getters, Setters
    public User() {
    }

    public User(String id, String name, Integer age, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImportError() {
        return importError;
    }

    public void setImportError(String importError) {
        this.importError = importError;
    }

    @Override
    public String toString() {
        return "User{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", age=" + age +
               ", email='" + email + '\'' +
               ", importError='" + importError + '\'' +
               '}';
    }

    /**
     * 将User对象转换为Map，用于错误报告生成。
     * @return 包含User数据的Map
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ID", id);
        map.put("姓名", name);
        map.put("年龄", age != null ? age.toString() : "");
        map.put("邮箱", email);
        map.put("失败原因", importError != null ? importError : "");
        return map;
    }
}
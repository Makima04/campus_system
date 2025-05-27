package com.example.campussysteam.common.log;

/**
 * 操作类型枚举
 */
public enum OperationType {
    /**
     * 查询
     */
    QUERY,
    
    /**
     * 新增
     */
    INSERT,
    
    /**
     * 修改
     */
    UPDATE,
    
    /**
     * 删除
     */
    DELETE,
    
    /**
     * 导出
     */
    EXPORT,
    
    /**
     * 导入
     */
    IMPORT,
    
    /**
     * 登录
     */
    LOGIN,
    
    /**
     * 登出
     */
    LOGOUT,
    
    /**
     * 授权
     */
    GRANT,
    
    /**
     * 其他
     */
    OTHER
} 
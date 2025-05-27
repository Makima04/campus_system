// API配置文件
// 定义所有API端点路径
import { getApiBaseUrl } from './index';
/**
 * 认证相关API
 */
export var authApi = {
    login: "".concat(getApiBaseUrl(), "/auth/login"),
    register: "".concat(getApiBaseUrl(), "/auth/register"),
    logout: "".concat(getApiBaseUrl(), "/auth/logout"),
    refreshToken: "".concat(getApiBaseUrl(), "/auth/refresh-token"),
};
/**
 * 用户相关API
 */
export var userApi = {
    profile: "".concat(getApiBaseUrl(), "/users/profile"),
    update: "".concat(getApiBaseUrl(), "/users/update"),
    changePassword: "".concat(getApiBaseUrl(), "/users/change-password"),
    list: "".concat(getApiBaseUrl(), "/users"),
    getById: function (id) { return "".concat(getApiBaseUrl(), "/users/").concat(id); },
};
/**
 * 图书相关API
 */
export var bookApi = {
    list: "".concat(getApiBaseUrl(), "/books"),
    getById: function (id) { return "".concat(getApiBaseUrl(), "/books/").concat(id); },
    create: "".concat(getApiBaseUrl(), "/books"),
    update: function (id) { return "".concat(getApiBaseUrl(), "/books/").concat(id); },
    delete: function (id) { return "".concat(getApiBaseUrl(), "/books/").concat(id); },
    borrow: function (id) { return "".concat(getApiBaseUrl(), "/books/").concat(id, "/borrow"); },
    return: function (id) { return "".concat(getApiBaseUrl(), "/books/").concat(id, "/return"); },
};
/**
 * 课程相关API
 */
export var courseApi = {
    list: "".concat(getApiBaseUrl(), "/courses"),
    getById: function (id) { return "".concat(getApiBaseUrl(), "/courses/").concat(id); },
    create: "".concat(getApiBaseUrl(), "/courses"),
    update: function (id) { return "".concat(getApiBaseUrl(), "/courses/").concat(id); },
    delete: function (id) { return "".concat(getApiBaseUrl(), "/courses/").concat(id); },
    enroll: function (id) { return "".concat(getApiBaseUrl(), "/courses/").concat(id, "/enroll"); },
    withdraw: function (id) { return "".concat(getApiBaseUrl(), "/courses/").concat(id, "/withdraw"); },
};
/**
 * 文件上传相关API
 */
export var uploadApi = {
    uploadFile: "".concat(getApiBaseUrl(), "/upload/file"),
    uploadImage: "".concat(getApiBaseUrl(), "/upload/image"),
    uploadAvatar: "".concat(getApiBaseUrl(), "/upload/avatar"),
};
/**
 * OSS相关API
 */
export var ossApi = {
    getPolicy: "".concat(getApiBaseUrl(), "/oss/policy"),
    getCallback: "".concat(getApiBaseUrl(), "/oss/callback"),
};
/**
 * 配置相关API
 */
export var configApi = {
    public: "".concat(getApiBaseUrl(), "/config/public"),
    server: "".concat(getApiBaseUrl(), "/config/server"),
};

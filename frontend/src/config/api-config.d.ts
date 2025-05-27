/**
 * 认证相关API
 */
export declare const authApi: {
    login: string;
    register: string;
    logout: string;
    refreshToken: string;
};
/**
 * 用户相关API
 */
export declare const userApi: {
    profile: string;
    update: string;
    changePassword: string;
    list: string;
    getById: (id: number) => string;
};
/**
 * 图书相关API
 */
export declare const bookApi: {
    list: string;
    getById: (id: number) => string;
    create: string;
    update: (id: number) => string;
    delete: (id: number) => string;
    borrow: (id: number) => string;
    return: (id: number) => string;
};
/**
 * 课程相关API
 */
export declare const courseApi: {
    list: string;
    getById: (id: number) => string;
    create: string;
    update: (id: number) => string;
    delete: (id: number) => string;
    enroll: (id: number) => string;
    withdraw: (id: number) => string;
};
/**
 * 文件上传相关API
 */
export declare const uploadApi: {
    uploadFile: string;
    uploadImage: string;
    uploadAvatar: string;
};
/**
 * OSS相关API
 */
export declare const ossApi: {
    getPolicy: string;
    getCallback: string;
};
/**
 * 配置相关API
 */
export declare const configApi: {
    public: string;
    server: string;
};

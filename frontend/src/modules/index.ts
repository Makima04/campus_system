/**
 * 模块集成入口
 * 集中导出所有模块，并提供模块注册函数
 */

// 导入所有模块的入口文件
import * as homeModule from './home'
import * as authModule from './auth'
import * as userModule from './user'
import * as adminModule from './admin'
import * as fileModule from './file'
import * as studentModule from './student'
import * as teacherModule from './teacher'
import * as permissionModule from './permission'
import * as commonModule from './common'
import * as welcomeModule from './welcome'
import * as dataModule from './data'

// 模块列表
const modules = {
  auth: authModule,
  user: userModule,
  admin: adminModule,
  student: studentModule,
  teacher: teacherModule,
  common: commonModule,
  data: dataModule
};

// 模块类型接口
interface ModuleWithRoutes {
  routes?: any[];
  registerModule?: (app: any) => void;
  [key: string]: any;
}

// 将所有模块的路由导出，以便在主路由中使用
export const getModuleRoutes = () => {
  return [
    ...homeModule.routes,
    ...authModule.routes,
    ...userModule.routes,
    ...adminModule.routes,
    ...fileModule.routes,
    ...studentModule.routes,
    ...permissionModule.routes,
    ...dataModule.routes
  ]
}

// 注册所有模块
export const registerAllModules = (app: any) => {
  Object.values(modules).forEach((module: ModuleWithRoutes) => {
    if (module.registerModule) {
      module.registerModule(app)
    }
  })
}

// 导出模块列表
export { modules }

// 导出各个模块
export {
  authModule,
  userModule,
  adminModule,
  studentModule,
  teacherModule,
  commonModule
};

// 导出类型
export type { User } from './user';
// export type { Notification, DashboardStats } from './admin';
// export type { Course, Assignment, Grade } from './student';

export default modules; 
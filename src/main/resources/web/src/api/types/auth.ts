// src/api/types/auth.ts

// 登录请求参数 - 匹配后端的 LoginRequest
export interface LoginRequest {
    username: string
    password: string
    userType: string  // 添加这个属性
    rememberMe?: boolean
}

// 通用响应结构
export interface ResultVO<T = any> {
    code: number
    message: string
    data: T
}

// 登录响应数据 - 匹配后端的 LoginVO
export interface LoginVO {
    token: string
    userInfo: {
        id: number
        username: string
        realName?: string  // 根据后端字段调整
        role?: string      // 根据后端字段调整
        userType?: string  // 添加这个字段
        avatar?: string
    }
}
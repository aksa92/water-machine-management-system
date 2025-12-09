// src/api/modules/auth.ts
import { api } from '../request'
import type { LoginRequest, LoginVO, ResultVO } from '../types/auth'

class AuthApi {
    /**
     * 用户登录
     */
    async login(data: LoginRequest): Promise<ResultVO<LoginVO>> {
        console.log('🚀 调用登录接口:', data)
        return api.post<ResultVO<LoginVO>>('/api/common/login', data)
    }
}

export const authApi = new AuthApi()
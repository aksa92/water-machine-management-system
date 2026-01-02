// 替换原文件内容
import type { LoginRequest, LoginResponse, LoginVO } from './types/auth'

// 真实的登录API调用
export const realLoginApi = async (data: LoginRequest): Promise<LoginResponse> => {
    const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://120.46.151.248:8081'

    console.log('🌐 调用登录接口:', `${API_BASE_URL}/api/common/login`)
    console.log('📤 请求数据:', data)

    try {
        const response = await fetch(`${API_BASE_URL}/api/common/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        })

        console.log('📥 响应状态:', response.status, response.statusText)

        if (!response.ok) {
            const errorText = await response.text()
            console.error('❌ 响应内容:', errorText)
            throw new Error(`网络请求失败: ${response.status} ${response.statusText}`)
        }

        const result: LoginResponse = await response.json()
        console.log('✅ 登录响应:', result)

        return result

    } catch (error: any) {
        console.error('❌ 登录接口调用失败:', error)
        throw new Error(`登录失败: ${error.message}`)
    }
}

// 备用模拟登录
export const mockLoginApi = async (data: LoginRequest): Promise<LoginResponse> => {
    await new Promise(resolve => setTimeout(resolve, 1000))

    if (data.username === 'admin' && data.password === '123456') {
        return {
            code: 200,
            message: '登录成功',
            data: {
                token: 'mock-jwt-token-' + Date.now(),
                userInfo: {
                    id: 1,
                    username: 'admin',
                    realName: '张管理员',
                    role: 'admin',
                    avatar: ''
                }
            }
        }
    } else {
        throw new Error('用户名或密码错误')
    }
}
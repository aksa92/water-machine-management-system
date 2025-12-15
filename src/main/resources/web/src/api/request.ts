// src/api/request.ts
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 统一的 fetch 封装
export async function request<T>(
    url: string,
    options: RequestInit = {}
): Promise<T> {
    // 处理日志数据，GET/HEAD 方法不显示 body
    const method = options.method?.toUpperCase() || 'GET';
    const logData: Record<string, any> = {
        method,
        headers: options.headers,
    };

    // 只对非 GET/HEAD 方法显示 body
    if (!['GET', 'HEAD'].includes(method)) {
        logData.body = options.body ? JSON.parse(options.body as string) : undefined;
    }

    console.log(`🌐 发送请求: ${API_BASE_URL}${url}`, logData)

    const defaultOptions: RequestInit = {
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
        },
    }

    // 确保登录请求不携带任何认证信息
    const isLoginRequest = url.includes('/login')
    if (isLoginRequest) {
        console.log('🔐 这是登录请求，不携带认证头')
        // 确保没有 Authorization header
        const headers = new Headers(defaultOptions.headers)
        headers.delete('Authorization')
        headers.delete('authorization')
        defaultOptions.headers = headers
    } else {
        // 非登录请求，从存储中获取 token
        const token = localStorage.getItem('token') || sessionStorage.getItem('token')
        if (token) {
            const headers = new Headers(defaultOptions.headers)
            headers.set('Authorization', `Bearer ${token}`)
            defaultOptions.headers = headers
        }
    }

    try {
        // 确保 GET/HEAD 请求不包含 body
        const fetchOptions = { ...defaultOptions, ...options };
        if (['GET', 'HEAD'].includes(method)) {
            delete fetchOptions.body;
        }

        const response = await fetch(`${API_BASE_URL}${url}`, fetchOptions)

        console.log('📥 响应状态:', response.status, response.statusText)

        // 尝试读取响应文本（无论成功与否）
        let responseText = ''
        try {
            responseText = await response.text()
            //console.log('📥 响应内容:', responseText)
        } catch (e) {
            console.log('📥 无法读取响应文本')
        }

        if (!response.ok) {
            let errorMessage = `HTTP ${response.status}: ${response.statusText}`
            if (responseText) {
                try {
                    const errorJson = JSON.parse(responseText)
                    errorMessage = errorJson.message || errorJson.error || errorMessage
                } catch {
                    errorMessage = `${errorMessage}\n${responseText}`
                }
            }

            console.error('❌ 请求失败:', errorMessage)
            throw new Error(errorMessage)
        }

        // 尝试解析 JSON
        if (responseText) {
            try {
                const data = JSON.parse(responseText)
                console.log('✅ 解析成功的数据:', data)
                return data
            } catch (e) {
                console.error('❌ JSON 解析失败:', e)
                throw new Error(`响应不是有效的 JSON: ${responseText}`)
            }
        } else {
            // 没有响应体的情况（如 204 No Content）
            return {} as T
        }
    } catch (error: any) {
        console.error('❌ 请求异常:', error)

        // 处理网络错误
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('网络连接失败，请检查网络设置和后端服务')
        }

        throw error
    }
}

// 封装常用 HTTP 方法
export const api = {
    get<T>(url: string) {
        return request<T>(url, { method: 'GET' })
    },

    post<T>(url: string, data?: any) {
        return request<T>(url, {
            method: 'POST',
            body: data ? JSON.stringify(data) : undefined,
        })
    },

    put<T>(url: string, data?: any) {
        return request<T>(url, {
            method: 'PUT',
            body: data ? JSON.stringify(data) : undefined,
        })
    },

    patch<T>(url: string, data?: any) {
        return request<T>(url, {
            method: 'PATCH',
            body: data ? JSON.stringify(data) : undefined,
        })
    },

    delete<T>(url: string) {
        return request<T>(url, { method: 'DELETE' })
    },

    upload<T>(url: string, formData: FormData) {
        const headers = new Headers()
        // 上传文件时不要设置 Content-Type，浏览器会自动设置
        headers.delete('Content-Type')

        return request<T>(url, {
            method: 'POST',
            headers,
            body: formData,
        })
    },
}
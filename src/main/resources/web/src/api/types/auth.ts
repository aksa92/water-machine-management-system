// 与后端 LoginRequest 对应的类型
export interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

// 与后端 LoginVO 对应的类型
export interface LoginVO {
  code: number
  message: string
  data: {
    token: string
    userInfo: {
      id: number
      username: string
      realName: string
      role: string
      avatar?: string
    }
  }
}
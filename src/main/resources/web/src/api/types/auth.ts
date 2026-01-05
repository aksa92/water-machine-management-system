// src/main/resources/web/src/api/types/auth.ts
export interface LoginRequest {
  username: string
  password: string
  userType?: string
  rememberMe?: boolean
}

export interface LoginVO {
  token: string
  userInfo: {
    id: number
    username: string
    realName: string
    role: string
    avatar: string
  }
}

// 在 '@/api/types/auth.ts' 文件中添加
export interface ResultVO<T = any> {
  code: number;
  message: string;
  data?: T;
  [key: string]: any;
}


export interface LoginResponse {
  code: number
  message: string
  data: LoginVO
}

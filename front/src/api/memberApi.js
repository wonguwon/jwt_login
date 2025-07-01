import apiClient from './axiosInstance'

// 내 정보 조회 (JWT 토큰 사용)
export const getMyInfo = async () => {
  try {
    const response = await apiClient.get('/v1/member/me')
    return response.data
  } catch (error) {
    console.error('내 정보 조회 실패:', error)
    throw error
  }
}

// 로그인
export const login = async (loginData) => {
  try {
    const response = await apiClient.post('/v1/member/login', loginData)
    return response.data
  } catch (error) {
    console.error('로그인 실패:', error)
    throw error
  }
}

// 회원가입
export const signup = async (signupData) => {
  try {
    const response = await apiClient.post('/v1/member/signup', signupData)
    return response.data
  } catch (error) {
    console.error('회원가입 실패:', error)
    throw error
  }
} 
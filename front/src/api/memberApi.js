import axios from 'axios'

const API_BASE_URL = 'http://localhost:8001/v1'

// axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 요청 인터셉터 - 토큰 자동 추가
apiClient.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 내 정보 조회 (JWT 토큰 사용)
export const getMyInfo = async () => {
  try {
    const response = await apiClient.get('/member/me')
    return response.data
  } catch (error) {
    console.error('내 정보 조회 실패:', error)
    throw error
  }
}

// 로그인
export const login = async (loginData) => {
  try {
    const response = await apiClient.post('/member/login', loginData)
    return response.data
  } catch (error) {
    console.error('로그인 실패:', error)
    throw error
  }
}

// 회원가입
export const signup = async (signupData) => {
  try {
    const response = await apiClient.post('/member/signup', signupData)
    return response.data
  } catch (error) {
    console.error('회원가입 실패:', error)
    throw error
  }
} 
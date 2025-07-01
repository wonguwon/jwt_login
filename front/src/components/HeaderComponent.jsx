import React, { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import styled from 'styled-components'
import Cookies from 'js-cookie'
import { getMyInfo } from '../api/memberApi'

const Header = styled.header`
  background-color: #1976d2;
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
`

const Container = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
`

const UserInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
`

const UserName = styled.span`
  font-weight: 500;
  font-size: 14px;
`

const Button = styled.button`
  background: transparent;
  border: 1px solid white;
  color: white;
  padding: 0.5rem 1rem;
  margin-left: 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;

  &:hover {
    background: white;
    color: #1976d2;
  }
`

const Logo = styled.h1`
  margin: 0;
  font-size: 1.5rem;
  cursor: pointer;
  transition: color 0.2s;
  
  &:hover {
    color: #e3f2fd;
  }
`

const HeaderComponent = () => {
  const [isLogin, setIsLogin] = useState(false)
  const [userName, setUserName] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    // 토큰 처리
    const token = Cookies.get("token")
    console.log(token)
    
    if (token) {
      sessionStorage.setItem("token", token)
      Cookies.remove("token")
      window.location.href = "/"
    }

    if (sessionStorage.getItem("token")) {
      setIsLogin(true)
      fetchUserInfo()
    }
  }, [])

  const fetchUserInfo = async () => {
    try {
      const memberInfo = await getMyInfo()
      setUserName(memberInfo.name)
    } catch (error) {
      console.error('사용자 정보 조회 실패:', error)
    }
  }

  const doLogout = () => {
    sessionStorage.removeItem("token")
    setUserName('')
    setIsLogin(false)
    window.location.reload()
  }

  return (
    <Header>
      <Container>
        <div>
          <Logo onClick={() => navigate('/')}>SPA</Logo>
        </div>
        <div>
          {!isLogin && (
            <>
              <Button onClick={() => navigate('/member/create')}>
                회원가입
              </Button>
              <Button onClick={() => navigate('/member/login')}>
                로그인
              </Button>
            </>
          )}
          {isLogin && (
            <UserInfo>
              <UserName>{userName}님 환영합니다!</UserName>
              <Button onClick={doLogout}>
                로그아웃
              </Button>
            </UserInfo>
          )}
        </div>
      </Container>
    </Header>
  )
}

export default HeaderComponent 
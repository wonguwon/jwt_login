import React, { useState } from 'react'
import styled from 'styled-components'
import { login } from '../api/memberApi'
import googleLoginImg from '../assets/google_login.png'
import kakaoLoginImg from '../assets/kakao_login.png'

const Container = styled.div`
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
  display: flex;
  justify-content: center;
`

const Card = styled.div`
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  width: 100%;
  max-width: 400px;
`

const Title = styled.h1`
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
  font-size: 1.5rem;
`

const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 1rem;
`

const Input = styled.input`
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  
  &:focus {
    outline: none;
    border-color: #1976d2;
  }
`

const Button = styled.button`
  background: #1976d2;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: #1565c0;
  }
`

const SocialContainer = styled.div`
  margin-top: 1rem;
  display: flex;
  gap: 1rem;
  justify-content: center;
`

const SocialImage = styled.img`
  max-height: 40px;
  width: auto;
  cursor: pointer;
  transition: opacity 0.2s;
  
  &:hover {
    opacity: 0.8;
  }
`

const MemberLogin = () => {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  
  const kakaoUrl = "https://kauth.kakao.com/oauth/authorize"
  const kakaoClientId = "카카오 clientID"
  const kakaoRedirectUrl = "http://localhost:3000/oauth/kakao/redirect"

  const memberLogin = async (e) => {
    e.preventDefault()
    const loginData = {
      email: email,
      password: password
    }
    try {
      const response = await login(loginData)
      const { token } = response
      sessionStorage.setItem("token", token)
   
      window.location.href = "/"
    } catch (error) {
      console.error("로그인 실패:", error)
      alert("로그인에 실패했습니다.")
    }
  }

  const kakaoLogin = () => {
    const auth_uri = `${kakaoUrl}?client_id=${kakaoClientId}&redirect_uri=${kakaoRedirectUrl}&response_type=code`
    window.location.href = auth_uri
  }

  const googleServerLogin = () => {
    window.location.href = "http://localhost:8001/oauth2/authorization/google"
  }

  return (
    <Container>
      <Card>
        <Title>로그인</Title>
        
        <Form onSubmit={memberLogin}>
          <Input
            type="email"
            placeholder="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          
          <Input
            type="password"
            placeholder="패스워드"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          
          <Button type="submit">
            로그인
          </Button>
        </Form>
        
        <SocialContainer>
          <SocialImage
            src={googleLoginImg}
            alt="Google Login"
            onClick={googleServerLogin}
          />
          <SocialImage
            src={kakaoLoginImg}
            alt="Kakao Login"
            onClick={kakaoLogin}
          />
        </SocialContainer>
      </Card>
    </Container>
  )
}

export default MemberLogin 
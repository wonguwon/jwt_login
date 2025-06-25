import React, { useEffect } from 'react'
import styled from 'styled-components'
import axios from 'axios'

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
`

const LoadingContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
`

const Spinner = styled.div`
  width: 60px;
  height: 60px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1976d2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
`

const Title = styled.h2`
  color: #333;
  font-size: 1.25rem;
`

const GoogleRedirect = () => {
  useEffect(() => {
    const code = new URL(window.location.href).searchParams.get("code")
    sendCodeToServer(code)
  }, [])

  const sendCodeToServer = async (code) => {
    try {
      const response = await axios.post("http://localhost:8080/member/google/doLogin", { code })
      const token = response.data.token
      sessionStorage.setItem("token", token)
      window.location.href = "/"
    } catch (error) {
      console.error("Google 로그인 실패:", error)
      alert("Google 로그인에 실패했습니다.")
    }
  }

  return (
    <Container>
      <LoadingContainer>
        <Spinner />
        <Title>구글 로그인 진행중...</Title>
      </LoadingContainer>
    </Container>
  )
}

export default GoogleRedirect 
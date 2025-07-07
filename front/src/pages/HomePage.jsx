import React, { useState, useEffect } from 'react'
import styled from 'styled-components'
import { useNavigate } from 'react-router-dom'
import { getMyInfo } from '../api/memberApi'

const Container = styled.div`
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
`

const WelcomeCard = styled.div`
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  margin-bottom: 2rem;
`

const Title = styled.h1`
  color: #333;
  margin-bottom: 1rem;
  font-size: 2rem;
`

const UserInfo = styled.div`
  background: #f8f9fa;
  border-radius: 6px;
  padding: 1.5rem;
  margin-top: 1rem;
`

const InfoRow = styled.div`
  display: flex;
  margin-bottom: 0.5rem;
  
  &:last-child {
    margin-bottom: 0;
  }
`

const Label = styled.span`
  font-weight: bold;
  color: #555;
  min-width: 100px;
`

const Value = styled.span`
  color: #333;
`

const LoadingMessage = styled.div`
  text-align: center;
  color: #666;
  font-size: 1.1rem;
`

const ErrorMessage = styled.div`
  color: #d32f2f;
  background: #ffebee;
  padding: 1rem;
  border-radius: 4px;
  margin-top: 1rem;
`

const FeatureCard = styled.div`
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  margin-top: 2rem;
  text-align: center;
`

const FeatureButton = styled.button`
  background: #1976d2;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 6px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background: #1565c0;
  }
`

const HomePage = () => {
  const [memberInfo, setMemberInfo] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    const fetchMemberInfo = async () => {
      try {
        const token = sessionStorage.getItem('token')
        if (!token) {
          setLoading(false)
          return
        }
        const data = await getMyInfo()
        setMemberInfo(data)
        if (data && data.email) {
          sessionStorage.setItem('email', data.email)
        }
      } catch (err) {
        console.error('멤버 정보 조회 실패:', err)
        setError('멤버 정보를 불러오는데 실패했습니다.')
      } finally {
        setLoading(false)
      }
    }
    fetchMemberInfo()
  }, [])

  if (loading) {
    return (
      <Container>
        <LoadingMessage>사용자 정보를 불러오는 중...</LoadingMessage>
      </Container>
    )
  }

  if (!sessionStorage.getItem('token')) {
    return (
      <Container>
        <WelcomeCard>
          <Title>환영합니다!</Title>
          <p>로그인하여 서비스를 이용해보세요.</p>
        </WelcomeCard>
      </Container>
    )
  }

  return ( 
    <Container>
      <WelcomeCard>
        <Title>환영합니다!</Title>
        {error ? (
          <ErrorMessage>{error}</ErrorMessage>
        ) : memberInfo ? (
          <UserInfo>
            <InfoRow>
              <Label>이름:</Label>
              <Value>{memberInfo.name}</Value>
            </InfoRow>
            <InfoRow>
              <Label>이메일:</Label>
              <Value>{memberInfo.email}</Value>
            </InfoRow>
            <InfoRow>
              <Label>역할:</Label>
              <Value>{memberInfo.role}</Value>
            </InfoRow>
            <InfoRow>
              <Label>가입일:</Label>
              <Value>{new Date(memberInfo.createdAt).toLocaleDateString('ko-KR')}</Value>
            </InfoRow>
          </UserInfo>
        ) : (
          <p>사용자 정보를 불러올 수 없습니다.</p>
        )}
      </WelcomeCard>
      
      {memberInfo && (
        <>
          <FeatureCard>
            <h2>서비스 이용하기</h2>
            <p>파일 업로드 및 다운로드 기능을 이용해보세요.</p>
            <FeatureButton onClick={() => navigate('/files')}>
              파일 업로드/다운로드
            </FeatureButton>
          </FeatureCard>
          <FeatureCard>
            <h2>채팅 이용하기</h2>
            <p>채팅 기능을 이용해보세요.</p>
            <FeatureButton onClick={() => navigate('/chat')}>
              채팅 이용하기
            </FeatureButton>
          </FeatureCard>
        </>
      )}
    </Container>
  )
}

export default HomePage 
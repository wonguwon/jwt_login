import React, { useState } from 'react'
import styled from 'styled-components'
import { signup } from '../api/memberApi'

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
  max-width: 600px;
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

const ErrorMessage = styled.div`
  color: #d32f2f;
  background: #ffebee;
  padding: 0.75rem;
  border-radius: 4px;
  font-size: 0.9rem;
  margin-bottom: 1rem;
`

const MemberCreate = () => {
  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [phoneNumber, setPhoneNumber] = useState("")
  const [error, setError] = useState("")

  const memberCreate = async (e) => {
    e.preventDefault()
    setError("")
    
    const registerData = {
      name: name,
      email: email,
      password: password,
      phoneNumber: phoneNumber
    }
    
    try {
      await signup(registerData)
      alert("회원가입이 완료되었습니다!")
      window.location.href = "/member/login"
    } catch (error) {
      console.error("회원가입 실패:", error)
      if (error.response?.data?.message) {
        setError(error.response.data.message)
      } else {
        setError("회원가입에 실패했습니다.")
      }
    }
  }

  return (
    <Container>
      <Card>
        <Title>회원가입</Title>
        
        {error && <ErrorMessage>{error}</ErrorMessage>}
        
        <Form onSubmit={memberCreate}>
          <Input
            type="text"
            placeholder="이름"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          
          <Input
            type="email"
            placeholder="이메일"
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
          
          <Input
            type="tel"
            placeholder="전화번호 (예: 010-1234-5678)"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            pattern="01[0-9]-[0-9]{4}-[0-9]{4}"
            required
          />
          
          <Button type="submit">
            회원가입
          </Button>
        </Form>
      </Card>
    </Container>
  )
}

export default MemberCreate 
import React, { useState, useEffect } from 'react'
import styled from 'styled-components'
import { signup, sendEmailCode, verifyEmailCode } from '../api/memberApi'
import Timer from '../components/Timer'

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
  transition: background 0.2s, opacity 0.2s;
  
  &:hover {
    background: #1565c0;
  }
  &:disabled {
    background: #bdbdbd;
    color: #eeeeee;
    cursor: not-allowed;
    opacity: 0.7;
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

const EmailRow = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
`;

const AuthInput = styled.input`
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.95rem;
  width: 120px;
`;

const SmallButton = styled(Button)`
  width: 70px;
  padding: 0.5rem;
  font-size: 0.9rem;
`;

const EmailButton = styled(Button)`
  width: 120px;
  padding: 0.5rem;
  font-size: 0.95rem;
`;

const MemberCreate = () => {
  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [phoneNumber, setPhoneNumber] = useState("")
  const [error, setError] = useState("")
  const [emailAuthStarted, setEmailAuthStarted] = useState(false)
  const [authCode, setAuthCode] = useState("")
  const [emailVerified, setEmailVerified] = useState(false)

  useEffect(() => {
    // 이메일이 변경되면 인증 UI 초기화
    setEmailAuthStarted(false)
    setAuthCode("")
    setEmailVerified(false)
  }, [email])

  const handleEmailAuth = async () => {
    try {
      await sendEmailCode(email);
      setEmailAuthStarted(true);
      alert('인증코드가 발송되었습니다. 메일을 확인하세요.');
    } catch (e) {
      alert(e.response?.data?.message || '인증코드 발송 실패');
    }
  }

  const handleAuthCodeChange = (e) => {
    setAuthCode(e.target.value)
  }

  const handleTimeout = () => {
    alert('시간이 초과되었습니다')
    setEmailAuthStarted(false)
    setAuthCode("")
  }

  const handleVerifyCode = async () => {
    try {
      await verifyEmailCode(email, authCode);
      setEmailVerified(true);
      setEmailAuthStarted(false); // 인증 완료 시 타이머 멈춤
      alert('이메일 인증이 완료되었습니다!');
    } catch (e) {
      setEmailVerified(false);
      alert(e.response?.data?.message || '인증코드가 올바르지 않습니다.');
    }
  }

  const memberCreate = async (e) => {
    e.preventDefault()
    setError("")
    // 이메일 인증이 완료되지 않았으면 회원가입 진행하지 않음
    if (!emailVerified) {
      setError("이메일 인증을 완료해주세요.")
      return
    }
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
          <EmailRow>
            <Input
              type="email"
              placeholder="이메일"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              style={{ marginBottom: 0, flex: 1 }}
              disabled={emailVerified} // 인증 완료 시 이메일 입력 비활성화
            />
            {!emailVerified && !emailAuthStarted && (
              <EmailButton
                type="button"
                onClick={handleEmailAuth}
                disabled={!email}
              >
                이메일 인증
              </EmailButton>
            )}
            {!emailVerified && emailAuthStarted && (
              <>
                <AuthInput
                  type="text"
                  placeholder="인증코드 입력"
                  value={authCode}
                  onChange={handleAuthCodeChange}
                  maxLength={8}
                />
                <SmallButton type="button" onClick={handleVerifyCode}>
                  확인
                </SmallButton>
                <Timer seconds={180} isActive={emailAuthStarted} onTimeout={handleTimeout} colorChangeSec={30} />
              </>
            )}
            {emailVerified && (
              <span style={{ color: '#388e3c', fontWeight: 'bold', marginLeft: 8 }}>
                인증 완료
              </span>
            )}
          </EmailRow>
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
          <Button type="submit" disabled={!emailVerified}>
            {emailVerified ? "회원가입" : "이메일 인증을 완료해주세요"}
          </Button>
        </Form>
      </Card>
    </Container>
  )
}

export default MemberCreate 
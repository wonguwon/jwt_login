import { Routes, Route } from 'react-router-dom'
import styled from 'styled-components'
import HeaderComponent from './components/HeaderComponent'
import MemberCreate from './components/MemberCreate'
import MemberLogin from './components/MemberLogin'
import GoogleRedirect from './components/GoogleRedirect'
import KakaoRedirect from './components/KakaoRedirect'
import HomePage from './components/HomePage'
import './App.css'

const AppContainer = styled.div`
  display: flex;
  flex-direction: column;
  min-height: 100vh;
`

const MainContent = styled.main`
  flex-grow: 1;
`

function App() {
  return (
    <AppContainer>
      <HeaderComponent />
      <MainContent>
        <Routes>
          <Route path="/member/create" element={<MemberCreate />} />
          <Route path="/member/login" element={<MemberLogin />} />
          <Route path="/oauth/google/redirect" element={<GoogleRedirect />} />
          <Route path="/oauth/kakao/redirect" element={<KakaoRedirect />} />
          <Route path="/" element={<HomePage />} />
        </Routes>
      </MainContent>
    </AppContainer>
  )
}

export default App

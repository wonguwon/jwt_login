import { Routes, Route } from 'react-router-dom'
import styled from 'styled-components'
import HeaderComponent from './components/HeaderComponent'
import MemberCreate from './components/MemberCreate'
import MemberLogin from './components/MemberLogin'
import KakaoRedirect from './components/KakaoRedirect'
import HomePage from './components/HomePage'
import FileUpload from './components/FileUpload'
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
          <Route path="/" element={<HomePage />} />
          <Route path="/member/login" element={<MemberLogin />} />
          <Route path="/member/create" element={<MemberCreate />} />
          <Route path="/oauth/kakao/callback" element={<KakaoRedirect />} />
          <Route path="/files" element={<FileUpload />} />
        </Routes>
      </MainContent>
    </AppContainer>
  )
}

export default App

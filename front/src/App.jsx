import { Routes, Route } from 'react-router-dom'
import styled from 'styled-components'
import HeaderComponent from './components/HeaderComponent'
import MemberCreate from './pages/MemberCreate'
import MemberLogin from './pages/MemberLogin'
import KakaoRedirect from './components/KakaoRedirect'
import HomePage from './pages/HomePage'
import FileUpload from './components/FileUpload'
import MemberList from './pages/MemberList'
import GroupChattingList from './pages/GroupChattingList'
import MyChatPage from './pages/MyChatPage'
import ChatMenuPage from './pages/ChatMenuPage'
import ChatPage from './pages/ChatPage'
// import StompChatPage from './pages/StompChatPage'
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
          <Route path="/member/list" element={<MemberList />} />
          <Route path="/chat/rooms" element={<GroupChattingList />} />
          <Route path="/mychats" element={<MyChatPage />} />
          <Route path="/chat" element={<ChatMenuPage />} />
          <Route path="/chatpage/:roomId" element={<ChatPage />} />
          {/* <Route path="/chatpage/:roomId" element={<StompChatPage />} /> */}
        </Routes>
      </MainContent>
    </AppContainer>
  )
}

export default App

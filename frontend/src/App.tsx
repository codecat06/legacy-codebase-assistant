import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import ReposPage from './pages/ReposPage';
import ChatPage from './pages/ChatPage';
import ImpactPage from './pages/ImpactPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<ReposPage />} />
          <Route path="chat" element={<ChatPage />} />
          <Route path="impact" element={<ImpactPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
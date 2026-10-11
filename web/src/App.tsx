import { BrowserRouter, Navigate, Route, Routes } from "react-router";
import WorkspacePage from "@/features/workspace/pages/WorkspacePage";
import ChatPage from "@/features/chat/pages/ChatPage";
import HomePage from "@/features/auth/pages/HomePage";

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<HomePage />} />
                YouTube channel home
                <Route
                    path="/workspace/:customUrl"
                    element={<WorkspacePage />}
                />
                {/* New chat */}
                <Route
                    path="/workspace/:customUrl/chat"
                    element={<ChatPage />}
                />
                {/* Existing conversation */}
                <Route
                    path="/workspace/:customUrl/chat/:conversationId"
                    element={<ChatPage />}
                />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

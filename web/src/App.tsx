import { BrowserRouter, Navigate, Route, Routes } from "react-router";
import HomePage from "@features/auth/pages/HomePage";
import WorkspacePage from "@/features/workspace/pages/WorkspacePage";

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route
                    path="/workspace/:channelId"
                    element={<WorkspacePage />}
                />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

import type { ReactNode } from "react";
import "./ChatLayout.css";

interface ChatLayoutProps {
    sidebar: ReactNode;
    sidebarOpen: boolean;
    onToggleSidebar: () => void;
    children: ReactNode;
}

console.log("CHAT LAYOUT LOADED");

export default function ChatLayout({
    sidebar,
    sidebarOpen,
    onToggleSidebar,
    children,
}: ChatLayoutProps) {
    return (
        <div className="chat-layout">
            <aside
                className={`chat-layout__sidebar ${
                    sidebarOpen ? "chat-layout__sidebar--open" : ""
                }`}
            >
                {sidebar}
            </aside>

            {sidebarOpen && (
                <div
                    className="chat-layout__overlay"
                    onClick={onToggleSidebar}
                />
            )}

            <main className="chat-layout__main">{children}</main>
        </div>
    );
}

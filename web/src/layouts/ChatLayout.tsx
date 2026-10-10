import type { ReactNode } from "react";
import "./ChatLayout.css";

interface ChatLayoutProps {
    sidebar: ReactNode;
    children: ReactNode;
}

export default function ChatLayout({ sidebar, children }: ChatLayoutProps) {
    return (
        <div className="chat-layout">
            <aside className="chat-layout__sidebar">{sidebar}</aside>

            <main className="chat-layout__main">{children}</main>
        </div>
    );
}

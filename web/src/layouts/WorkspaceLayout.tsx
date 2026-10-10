import type { ReactNode } from "react";
import "./WorkspaceLayout.css";

interface WorkspaceLayoutProps {
    sidebar: ReactNode;
    children: ReactNode;
}

export default function WorkspaceLayout({
    sidebar,
    children,
}: WorkspaceLayoutProps) {
    return (
        <div className="workspace-layout">
            <aside className="workspace-layout__sidebar">{sidebar}</aside>

            <main className="workspace-layout__main">{children}</main>
        </div>
    );
}

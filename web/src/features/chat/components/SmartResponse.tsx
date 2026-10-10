import { useMemo } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { oneLight } from "react-syntax-highlighter/dist/esm/styles/prism";
import "./SmartResponse.css"

interface SmartResponseProps {
    content: string;
}

type ResponseFormat = "json" | "html" | "markdown" | "text";

function detectFormat(content: string): ResponseFormat {
    const value = content.trim();

    if (!value) return "text";

    // JSON: only classify as JSON when parsing succeeds.
    if (value.startsWith("{") || value.startsWith("[")) {
        try {
            JSON.parse(value);
            return "json";
        } catch {
            // It may be ordinary text or another format.
        }
    }

    // HTML: detect common document/markup tags.
    if (
        /^<!doctype html/i.test(value) ||
        /^<(html|body|article|section|div|table|main|h[1-6])(?:\s|>)/i.test(
            value,
        )
    ) {
        return "html";
    }

    // Plain text with no Markdown markers.
    const hasMarkdown =
        /^#{1,6}\s|^\s*[-*+]\s|^\s*\d+\.\s|```|\*\*|^\s*>\s|\[[^\]]+\]\([^)]+\)/m.test(
            value,
        );

    return hasMarkdown ? "markdown" : "text";
}

export default function SmartResponse({ content }: SmartResponseProps) {
    const format = useMemo(() => detectFormat(content), [content]);

    if (format === "json") {
        const formatted = JSON.stringify(JSON.parse(content), null, 2);

        return (
            <pre className="smart-response-json">
                <code>{formatted}</code>
            </pre>
        );
    }

    if (format === "html") {
        // Render HTML as text until a deliberate sanitization policy is added.
        return (
            <pre className="smart-response-html">
                <code>{content}</code>
            </pre>
        );
    }

    if (format === "markdown") {
        return (
            <div className="smart-response-markdown">
                <ReactMarkdown
                    remarkPlugins={[remarkGfm]}
                    components={{
                        pre({ children }) {
                            return <>{children}</>;
                        },
                        code({ className, children }) {
                            const match = /language-(\w+)/.exec(
                                className ?? "",
                            );
                            const code = String(children).replace(/\n$/, "");

                            if (match) {
                                return (
                                    <SyntaxHighlighter
                                        language={match[1]}
                                        style={oneLight}
                                        PreTag="div"
                                        wrapLongLines
                                    >
                                        {code}
                                    </SyntaxHighlighter>
                                );
                            }

                            return (
                                <code className={className}>{children}</code>
                            );
                        },
                    }}
                >
                    {content}
                </ReactMarkdown>
            </div>
        );
    }

    return <div className="smart-response-text">{content}</div>;
}

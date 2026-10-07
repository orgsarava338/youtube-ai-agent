package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import com.youtubeagent.youtube.comment.YouTubeComment;
import com.youtubeagent.youtube.comment.YouTubeCommentService;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetVideoCommentsTool implements AgentTool {

    private final YouTubeCommentService commentService;
    private final ObjectMapper objectMapper;

    public GetVideoCommentsTool(YouTubeCommentService commentService, ObjectMapper objectMapper) {
        this.commentService = commentService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_video_comments";
    }

    @Override
    public String getDescription() {
        return """
                Returns comments from a YouTube video.

                Required argument:
                - videoId: the actual YouTube video ID.

                Optional argument:
                - maxResults: number of comments to return,
                  from 1 to 100. Default is 10.

                Each returned comment includes:
                - commentId: the comment ID
                - authorName: comment author's display name
                - authorChannelId: comment author's channel ID
                - authorProfileImageUrl: author's profile image
                - text: comment text
                - publishedAt: when the comment was published
                - updatedAt: when the comment was last updated
                - likeCount: number of likes

                Important:
                - videoId must be a real YouTube video ID.
                - Never invent or guess a video ID.
                - If the user provides a video title instead of a video ID,
                  first use list_videos to find the actual video ID.
                - This tool returns top-level comments.
                - It does not return replies to comments.

                Use this tool when the user asks about comments,
                viewer feedback, or comments on a specific video.
                """;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {

        if (arguments == null || arguments.get("videoId") == null) {
            throw new IllegalArgumentException("Missing required argument: videoId");
        }

        String videoId = arguments.get("videoId").toString().trim();

        if (videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }

        int maxResults = 10;

        if (arguments.get("maxResults") != null) {

            Object value = arguments.get("maxResults");

            if (value instanceof Number number) {
                maxResults = number.intValue();
            } else {
                maxResults = Integer.parseInt(value.toString());
            }
        }

        List<YouTubeComment> comments = commentService.getVideoComments(videoId, maxResults);

        try {
            return objectMapper.writeValueAsString(comments);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize YouTube comments", e);
        }
    }
}
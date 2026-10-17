# Our ROADMAP: Build the YouTube AI Agent step by step

We will build this on your existing YouTube AI Agent repository, using Spring Boot, React, PostgreSQL, YouTube APIs, and free AI model options where practical.

Our first goal is not to build every possible YouTube feature. It is to make this one complete workflow work reliably, from your instruction to the final scheduled video.

## 1. What the finished workflow will do
You enter this instruction in your app:

> Analyze my channel's last 30 days, suggest five videos that fit my audience, draft the best one, and schedule it for Saturday after I approve it.

The agent should understand the request, gather your channel data, analyze it, generate ideas, prepare a draft, ask for your approval, and schedule the video only after you approve it.

1. Analyze the channel
Retrieve the last 30 days of views, watch time, audience retention where available, top videos, and relevant engagement metrics.

2. Recommend five video ideas
Use actual channel performance, topics, and audience signals to rank five relevant ideas, explaining why each could be a good fit.

3. Prepare the best idea
Draft the title, description, script, tags where useful, thumbnail concept, and publishing details.

4. Ask for approval
Show you the complete draft and the proposed Saturday publishing date and time. Nothing gets published without your explicit approval.

5. Schedule and verify
Upload the approved video, set its visibility and publication time through the YouTube API, then confirm the returned video ID and scheduled status.


One important distinction: YouTube scheduling normally requires the actual video file to be uploaded and set to private with a future publishAt time. A script and title alone cannot be scheduled as a video. We will make the workflow handle that missing-video case explicitly.

## 2. The complete implementation plan

We will work in milestones. Each milestone has a clear deliverable and tests. We should not move forward merely because code compiles; we move forward when the milestone's behavior is verified.

Legend: Existing means the repository appears to contain the foundation. Verify means we need to test it. Build means new or incomplete functionality is expected.

### Milestone 0 — Baseline verification
Verify existing code
- Check the current Git branch and working-tree changes.
- Verify Java/Maven versions, database configuration, and environment variables.
- Run backend tests and build the React frontend.
- Start the app and test authentication, chat, conversation history, and existing tools.
- Record failures before making changes.
  
> Deliverable: a reliable baseline and a list of known issues.


### Milestone 1 — Connect and identify the YouTube channel
Existing foundation; verify
- Complete Google OAuth for the correct YouTube account.
- Confirm that the connected channel ID and channel name are correct.
- Verify token encryption, refresh, expiry handling, and disconnect behavior.
- Request only the OAuth scopes needed for the features we implement.

> Deliverable: the app can securely identify and access the user's channel.


### Milestone 2 — Build the 30-day analytics service
Build and verify
- Query the YouTube Analytics API for the last 30 completed days.
- Fetch video-level performance and aggregate channel metrics.
- Retrieve top-performing videos, available engagement metrics, and relevant audience information.
- Handle missing data, zero views, pagination, API quota, and authorization failures.
- Display the report in a structured format the AI can reliably interpret.
  
> Deliverable: accurate, date-bounded analytics with source metrics—not invented statistics.


### Milestone 3 — Implement the recommendation engine
Build
- Pass the analytics and top-video information to the AI.
- Identify successful themes, formats, and possible content gaps.
- Generate five distinct video ideas.
- Score each idea using relevance, historical performance, audience fit, and feasibility.
- Explain the reasoning and distinguish observed data from predictions.
  
> Deliverable: five ranked ideas with evidence and reasons.


### Milestone 4 — Generate a complete video draft
Build
- Choose the highest-ranked idea.
- Generate a title, description, script, and optional tags.
- Create a thumbnail brief and shot-by-shot plan.
- Save the draft in PostgreSQL.
- Let you edit the draft and regenerate individual sections.
  
> Deliverable: an editable, persisted video draft ready for review.



### Milestone 5 — Approval and safety controls
Build
- Show the draft, intended channel, and proposed publication date/time.
- Allow approve, reject, or edit-and-review.
- Persist the approval, including exactly which version was approved.
- Require reapproval if material content or publication details change.
- Prevent the AI from bypassing approval by directly invoking a publishing tool.
  
> Deliverable: a reliable approval gate before any publishing action.


### Milestone 6 — Upload and schedule
Build
- Confirm a video file is available.
- Upload the video using the YouTube Data API.
- Set the appropriate privacy status and scheduled publication time.
- Handle resumable uploads and network failures.
- Store the returned YouTube video ID and scheduling status.

> Deliverable: an approved video appears in YouTube Studio as scheduled.


### Milestone 7 — Persistent background task runner
Build
- Create durable tasks and task states in PostgreSQL.
- Execute approved work asynchronously.
- Retry recoverable failures and prevent duplicate uploads.
- Resume unfinished work after a restart.
- Track scheduled tasks, execution history, and errors.
  
> Deliverable: long-running tasks no longer depend on an open chat request.


### Milestone 8 — Complete the dashboard
Build and integrate
- Add analytics and recommendation screens.
- Add draft editing and approval controls.
- Show upload progress, scheduled time, and task status.
- Provide clear error messages and retry actions.

> Deliverable: a usable end-to-end workflow through the React application.


### Milestone 9 — Integration testing and deployment
Build and verify
- Test the full workflow with a real authorized account and a test video.
- Test API errors, expired credentials, rejected approvals, and server restarts.
- Deploy backend, database, and frontend.
- Configure secrets, HTTPS, OAuth redirects, logs, and health monitoring.
- Verify the application and task runner remain available under the selected hosting tier.
  
> Deliverable: a deployed workflow with documented limitations and recovery behavior.



## 3. How the data and services will fit together

We will extend the existing architecture instead of introducing a new framework unnecessarily.

- React frontend
    - Chat
    - analytics
    - ideas
    - drafts
    - approvals

- Spring Boot API
    - Authentication
    - authorization 
    - validation
    - REST endpoints

- Agent orchestration
    - AgentService
    - tool registry
    - AI model provider

- YouTube APIs
    - Analytics
    - channel data
    - uploads

- PostgreSQL
    - Drafts
    - approvals
    - tasks
    - history



- Background worker and scheduler
    - Durable execution
    - retries
    - upload and scheduling


The key design rule is that the AI decides what to request or propose, while normal backend services validate permissions, dates, approval state, and API arguments before doing anything consequential.

## 4. What data do we need from YouTube?

The analytics step should use the official APIs rather than asking the model to guess from the channel page.

| Data | Purpose |
| --- | --- |
| Video views and watch time | Identify what has performed well |
| Average view duration and retention, where available | Understand how well videos hold attention |
| Likes and comments, where available | Understand engagement |
| Video titles, descriptions, dates, and topics | Identify recurring themes |
| Audience and traffic-source reports, where available | Improve audience fit and discovery |
| Subscriber changes, where available | Assess channel growth |

We will use the YouTube Analytics API for channel performance and the YouTube Data API for channel/video details and uploads. Some reports and metrics have availability or authorization limitations. We must not assume that every audience demographic or retention metric will be accessible for every channel.

For the first version, the analysis period should be the last 30 completed calendar days, with the exact start and end dates displayed in the report. That avoids presenting a partial current day as a full day of performance.

## 5. Database additions we will probably need

Before creating tables, we will inspect the current Flyway migrations and entities so we don't duplicate existing functionality.

|   Record              |   What it stores  |
|   ---                 |   --- |
|   Analytics report    |   Date range, channel, metrics, retrieval time    |
|   Video idea          |	Proposed topic, rationale, ranking, evidence    |
|   Video draft         |	Title, description, script, thumbnail brief, version   |
|   Approval            |	User decision, approved version, timestamp  |
|   Scheduled task      |	Intended execution time, timezone, task state
|   Upload record       |	YouTube video ID, upload status, publication status|
|   Task event          |	Progress, retries, errors, and recovery information|

Every record that belongs to a user or channel must be scoped to that authenticated user. Approval and upload operations must also be safe to retry; otherwise a temporary network error could cause duplicate actions.

## 6. The Saturday scheduling rules

We need to distinguish three separate events:

1. Draft ready: the script and publishing metadata are prepared.

2. Upload scheduled: the approved video file is uploaded, and YouTube accepts the future publication time.

3. Published: YouTube actually makes the video public at the scheduled time.
If no video file exists yet, the agent must stop at the draft stage and ask you to provide a finished video or use a separately implemented video-generation workflow.

We will store the intended time with a timezone, use the YouTube API's required UTC timestamp conversion, and display the local publishing time back to you. We will also verify YouTube's returned status rather than claiming that a video is scheduled merely because an upload request was sent.

## 7. How we will test the complete workflow

The final acceptance test will be one realistic instruction, from start to finish.

- [ ] The connected account is the correct channel.
- [ ] The analytics report covers exactly the selected 30-day period.
- [ ] Five distinct recommendations are generated and grounded in the retrieved data.
- [ ] The selected idea has a complete saved draft.
- [ ] You can edit the draft and approve a specific version.
- [ ] No upload or publication occurs before approval.
- [ ] The agent asks for a video file if one is missing.
- [ ] An uploaded video is scheduled for the correct Saturday and timezone.
- [ ] A restart or retry does not create duplicate uploads.
- [ ] The dashboard shows the correct task state and YouTube video ID.
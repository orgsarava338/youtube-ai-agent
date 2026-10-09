
import { useState, type FormEvent } from "react";
import "./Home.css";

const features = [
  {
    icon: "✳",
    color: "purple",
    title: "Ask your channel",
    description:
      "Ask questions in plain English and get useful answers about your YouTube channel.",
  },
  {
    icon: "↗",
    color: "coral",
    title: "Track performance",
    description:
      "Explore views, watch time, subscribers, and trends to understand what's working.",
  },
  {
    icon: "☷",
    color: "blue",
    title: "Understand your audience",
    description:
      "Analyze comments and feedback to discover what your viewers care about.",
  },
  {
    icon: "✧",
    color: "green",
    title: "Discover content insights",
    description:
      "Find patterns, identify opportunities, and make more informed content decisions.",
  },
];

const exampleQuestions = [
  "How did my channel perform this month?",
  "What do viewers think about my videos?",
  "Which content should I focus on next?",
];

export default function Home() {
  const [email, setEmail] = useState("");
  const [signupMessage, setSignupMessage] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    // Connect this form to your waitlist API or signup service
    // before collecting real email addresses.
    setSignupMessage(
      "The waitlist is coming soon! Email signup isn't connected yet."
    );
  }

  return (
    <main className="home-page" id="top">
      <header className="home-header">
        <a className="home-brand" href="#top" aria-label="AssistTube home">
          <span className="brand-icon">
            <span>▶</span>
            <i>✦</i>
          </span>
          <span>AssistTube</span>
        </a>

        <nav className="home-nav" aria-label="Main navigation">
          <a href="#features">Features</a>
          <a href="#how-it-works">How it works</a>
          <a href="#creators">For creators</a>
        </nav>

        <a className="home-header-cta" href="#waitlist">
          Get early access <span>↗</span>
        </a>
      </header>

      <section className="home-hero">
        <div className="hero-copy">
          <div className="eyebrow">
            <span className="eyebrow-sparkle">✦</span>
            YOUR AI COPILOT FOR YOUTUBE
          </div>

          <h1>
            Your YouTube
            <br />
            channel.
            <br />
            <span>Understood.</span>
          </h1>

          <p className="hero-description">
            Turn channel data into clear answers, useful insights, and
            smarter next steps — just by asking.
          </p>

          <div className="hero-actions">
            <a className="button-primary" href="#waitlist">
              Join the waitlist <span>→</span>
            </a>
            <a className="button-secondary" href="#features">
              Explore features
            </a>
          </div>

          <div className="hero-note">
            <span className="note-check">✓</span>
            Built for creators who want to grow smarter.
          </div>
        </div>

        <div className="hero-visual" aria-label="AssistTube product preview">
          <div className="decor decor-one" />
          <div className="decor decor-two" />

          <div className="preview-window">
            <div className="preview-header">
              <div className="preview-brand">
                <span className="preview-brand-icon">▶</span>
                <span>AssistTube <small>AI</small></span>
              </div>
              <span className="preview-status">
                <i /> Product preview
              </span>
            </div>

            <div className="preview-body">
              <div className="preview-greeting">
                <span className="ai-avatar">✦</span>
                <div>
                  <strong>Your channel assistant</strong>
                  <p>What would you like to understand today?</p>
                </div>
              </div>

              <div className="chat-question">
                How has my channel performed this month?
              </div>

              <div className="chat-answer">
                <div className="answer-heading">
                  <span className="answer-sparkle">✦</span>
                  <strong>Here's what we can explore</strong>
                </div>
                <p>
                  Get a clearer picture of your channel with performance
                  summaries and trends from your available data.
                </p>
              </div>

              <div className="preview-stats">
                <div className="stat-card stat-purple">
                  <span>Views</span>
                  <strong>Analytics</strong>
                  <small>See your reach</small>
                </div>
                <div className="stat-card stat-coral">
                  <span>Watch time</span>
                  <strong>Engagement</strong>
                  <small>Explore viewing</small>
                </div>
                <div className="stat-card stat-green">
                  <span>Audience</span>
                  <strong>Feedback</strong>
                  <small>Learn from viewers</small>
                </div>
              </div>

              <div className="preview-chart">
                <div className="chart-heading">
                  <strong>Channel overview</strong>
                  <span>Illustrative preview</span>
                </div>
                <div className="chart-bars" aria-hidden="true">
                  {[35, 52, 43, 70, 56, 83, 62, 92, 70, 100, 76, 88].map(
                    (height, index) => (
                      <span
                        key={index}
                        style={{ height: `${height}%` }}
                      />
                    )
                  )}
                </div>
                <div className="chart-labels">
                  <span>Week 1</span>
                  <span>Week 2</span>
                  <span>Week 3</span>
                  <span>Week 4</span>
                </div>
              </div>

              <div className="preview-input">
                <span>Ask something about your channel...</span>
                <span className="preview-send">↑</span>
              </div>
            </div>
          </div>

          <div className="floating-note">
            <span className="floating-note-icon">✦</span>
            <span>
              <strong>Less guesswork.</strong>
              <small>More informed decisions.</small>
            </span>
          </div>
        </div>
      </section>

      <section className="trust-strip" id="creators">
        <span>MADE FOR THE WAY CREATORS WORK</span>
        <div>
          <span>Understand</span>
          <i>✦</i>
          <span>Discover</span>
          <i>✦</i>
          <span>Grow</span>
        </div>
      </section>

      <section className="features-section" id="features">
        <div className="section-heading">
          <span className="section-eyebrow">YOUR CHANNEL, MADE CLEARER</span>
          <h2>
            Everything you need to
            <br />
            <span>understand your channel.</span>
          </h2>
          <p>
            Spend less time digging through data and more time deciding
            what to do next.
          </p>
        </div>

        <div className="feature-grid">
          {features.map((feature) => (
            <article className="feature-card" key={feature.title}>
              <div className={`feature-icon ${feature.color}`}>
                {feature.icon}
              </div>
              <h3>{feature.title}</h3>
              <p>{feature.description}</p>
              <a href="#waitlist" aria-label={`Learn about ${feature.title}`}>
                <span>Explore feature</span> <span>↗</span>
              </a>
            </article>
          ))}
        </div>
      </section>

      <section className="workflow-section" id="how-it-works">
        <div className="section-heading">
          <span className="section-eyebrow">A SIMPLER WAY TO GET INSIGHTS</span>
          <h2>
            Ask a question.
            <br />
            <span>Find your next move.</span>
          </h2>
          <p>No complicated dashboards required. Start with what you want to know.</p>
        </div>

        <div className="workflow-grid">
          <article className="workflow-step">
            <span className="step-number">01</span>
            <div className="step-icon lavender">↗</div>
            <h3>Connect your channel</h3>
            <p>
              Link your YouTube channel when account integration becomes
              available.
            </p>
          </article>

          <div className="workflow-connector" aria-hidden="true">→</div>

          <article className="workflow-step">
            <span className="step-number">02</span>
            <div className="step-icon peach">✳</div>
            <h3>Ask a question</h3>
            <p>
              Ask about performance, audience feedback, and the things you
              want to understand.
            </p>
          </article>

          <div className="workflow-connector" aria-hidden="true">→</div>

          <article className="workflow-step">
            <span className="step-number">03</span>
            <div className="step-icon mint">✦</div>
            <h3>Discover insights</h3>
            <p>
              Turn available channel data into clearer answers and actionable
              ideas.
            </p>
          </article>
        </div>
      </section>

      <section className="questions-section">
        <div className="questions-copy">
          <span className="section-eyebrow">JUST ASK</span>
          <h2>
            Your questions.
            <br />
            <span>Your channel.</span>
          </h2>
          <p>
            You don't need to be an analytics expert. Start with a question
            and explore what your channel data can tell you.
          </p>
        </div>

        <div className="question-list">
          {exampleQuestions.map((question, index) => (
            <div className="question-item" key={question}>
              <span className="question-number">0{index + 1}</span>
              <span>{question}</span>
              <span className="question-arrow">↗</span>
            </div>
          ))}
          <p className="example-disclaimer">
            Example questions for the planned product experience.
          </p>
        </div>
      </section>

      <section className="waitlist-section" id="waitlist">
        <div className="waitlist-decoration decoration-left">✳</div>
        <div className="waitlist-decoration decoration-right">✦</div>

        <div className="waitlist-content">
          <span className="waitlist-eyebrow">GET IN EARLY</span>
          <h2>
            Be among the first
            <br />
            to try <span>AssistTube.</span>
          </h2>
          <p>
            We're preparing a smarter way to understand your YouTube
            channel. Join the list to hear about early access and launch
            updates.
          </p>

          <form className="waitlist-form" onSubmit={handleSubmit}>
            <label className="sr-only" htmlFor="waitlist-email">
              Your email address
            </label>
            <input
              id="waitlist-email"
              type="email"
              placeholder="Enter your email address"
              value={email}
              onChange={(event) => {
                setEmail(event.target.value);
                setSignupMessage("");
              }}
              required
            />
            <button type="submit">
              Join the waitlist <span>→</span>
            </button>
          </form>

          {signupMessage && (
            <p className="waitlist-message" role="status">
              {signupMessage}
            </p>
          )}

          <span className="waitlist-footnote">
            Early access updates only. Signup collection is not yet enabled.
          </span>
        </div>
      </section>

      <footer className="home-footer">
        <a className="home-brand footer-brand" href="#top">
          <span className="brand-icon">
            <span>▶</span>
            <i>✦</i>
          </span>
          <span>AssistTube</span>
        </a>

        <p>Your AI copilot for YouTube.</p>

        <nav className="footer-links" aria-label="Footer navigation">
          <a href="#features">Features</a>
          <a href="#how-it-works">How it works</a>
          <a href="mailto:hello@assistube.com">Contact</a>
        </nav>

        <span className="footer-copyright">
          © {new Date().getFullYear()} AssistTube
        </span>
      </footer>
    </main>
  );
}

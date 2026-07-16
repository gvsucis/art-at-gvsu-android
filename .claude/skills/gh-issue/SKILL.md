---
name: gh-issue
description: Analyze and address a GitHub issue end-to-end in the Art At GVSU Android app — read the issue, understand it, plan if non-trivial, implement, test, and lint. Use when asked to work on / fix / resolve a GitHub issue. Pass the issue number or URL as args.
---

# Address a GitHub issue

Work the GitHub issue given in the args (issue number or URL). Use the GitHub CLI (`gh`) for all
GitHub interactions.

## Steps

1. `gh issue view <issue>` to get the title, body, and discussion.
2. Understand the problem described. Note acceptance criteria and any linked PRs/issues.
3. **Plan if non-trivial** — for multi-file work, architectural decisions, or unclear
   requirements, explore the codebase and design an approach first (EnterPlanMode). Skip planning
   for typos, single-line changes, or obvious bugs.
4. Search the codebase for the relevant files.
5. Implement the change.
6. Write and run tests to verify it (`./gradlew :<module>:testDebugUnitTest --tests <FQN>` for a
   single test; `make test` for the full suite — see the repo `CLAUDE.md`).
7. Ensure it passes linting and type checking (`./gradlew :app:assembleDebug`).

## Conventions

- Branch names use the `jc/` prefix + issue number, e.g. `jc/123/fix-parser`.
- Commit/PR authoring follows the global ghostwriter rules: no self-attribution, no issue id in
  the commit title, first commit line ≤ 50 chars, empty PR body.
- Commit or push only when asked.
